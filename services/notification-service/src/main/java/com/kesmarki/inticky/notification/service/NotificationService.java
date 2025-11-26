package com.kesmarki.inticky.notification.service;

import com.kesmarki.inticky.notification.entity.Notification;
import com.kesmarki.inticky.notification.entity.NotificationTemplate;
import com.kesmarki.inticky.notification.enums.NotificationStatus;
import com.kesmarki.inticky.notification.enums.NotificationType;
import com.kesmarki.inticky.notification.enums.Priority;
import com.kesmarki.inticky.notification.repository.NotificationRepository;
import com.kesmarki.inticky.tenant.context.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for notification management and delivery coordination
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateService templateService;
    private final EmailNotificationService emailService;
    private final PushNotificationService pushService;
    private final SmsNotificationService smsService;
    private final WebhookNotificationService webhookService;

    @Value("${inticky.notification.delivery.max-attempts:5}")
    private int maxDeliveryAttempts;

    @Value("${inticky.notification.delivery.cleanup-after-days:30}")
    private int cleanupAfterDays;

    /**
     * Get notification by ID within tenant
     */
    @Cacheable(value = "notifications", key = "#notificationId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public Notification getNotificationById(UUID notificationId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching notification: {} in tenant: {}", notificationId, tenantId);
        
        return notificationRepository.findByIdAndTenantId(notificationId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with ID: " + notificationId));
    }

    /**
     * Get notifications for recipient within tenant
     */
    public Page<Notification> getRecipientNotifications(UUID recipientId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching notifications for recipient: {} in tenant: {}", recipientId, tenantId);
        
        return notificationRepository.findByRecipientIdAndTenantId(recipientId, tenantId, pageable);
    }

    /**
     * Create and send notification
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public Notification createNotification(NotificationRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Creating notification for recipient: {} in tenant: {}", request.getRecipientId(), tenantId);

        // Get or create template
        NotificationTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateService.getTemplateById(request.getTemplateId());
        } else if (request.getTemplateName() != null) {
            template = templateService.getTemplateByName(request.getTemplateName());
        } else if (request.getEventType() != null) {
            template = templateService.getDefaultTemplate(request.getEventType(), request.getNotificationType());
        }

        // Build notification
        Notification.NotificationBuilder builder = Notification.builder()
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .recipientName(request.getRecipientName())
                .notificationType(request.getNotificationType())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL)
                .eventType(request.getEventType())
                .eventId(request.getEventId())
                .destination(request.getDestination())
                .sender(request.getSender())
                .scheduledAt(request.getScheduledAt())
                .maxAttempts(request.getMaxAttempts() != null ? request.getMaxAttempts() : maxDeliveryAttempts)
                .metadata(request.getMetadata())
                .templateVariables(request.getTemplateVariables())
                .deliveryConfig(request.getDeliveryConfig())
                .tags(request.getTags());

        // Apply template if available
        if (template != null) {
            builder.templateId(UUID.fromString(template.getId()))
                    .templateName(template.getName())
                    .subject(processTemplate(template.getSubject(), request.getTemplateVariables()))
                    .body(processTemplate(template.getBody(), request.getTemplateVariables()));
            
            if (template.getHtmlBody() != null) {
                builder.htmlBody(processTemplate(template.getHtmlBody(), request.getTemplateVariables()));
            }
        } else {
            // Use provided content
            builder.subject(request.getSubject())
                    .body(request.getBody())
                    .htmlBody(request.getHtmlBody());
        }

        Notification notification = builder.build();
        notification.setTenantId(tenantId);

        // Save notification
        notification = notificationRepository.save(notification);

        log.info("Notification created: {} for recipient: {} in tenant: {}", notification.getId(), request.getRecipientId(), tenantId);

        // Send immediately if not scheduled
        if (notification.isReadyToSend()) {
            sendNotificationAsync(notification.getId());
        }

        return notification;
    }

    /**
     * Send notification asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<Void> sendNotificationAsync(UUID notificationId) {
        try {
            sendNotification(notificationId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async notification sending failed for {}: {}", notificationId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send notification synchronously
     */
    @Transactional
    @CacheEvict(value = "notifications", key = "#notificationId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public void sendNotification(UUID notificationId) {
        Notification notification = getNotificationById(notificationId);
        String tenantId = TenantContext.getTenantId();
        
        log.info("Sending notification: {} type: {} to: {} in tenant: {}", 
                notificationId, notification.getNotificationType(), notification.getDestination(), tenantId);

        if (!notification.isReadyToSend()) {
            log.warn("Notification {} is not ready to send. Status: {}, Scheduled: {}", 
                    notificationId, notification.getStatus(), notification.getScheduledAt());
            return;
        }

        // Mark as processing
        notification.markAsProcessing();
        notificationRepository.save(notification);

        try {
            // Send via appropriate channel
            switch (notification.getNotificationType()) {
                case EMAIL -> emailService.sendEmail(notification);
                case PUSH -> pushService.sendPushNotification(notification);
                case SMS -> smsService.sendSms(notification);
                case WEBHOOK -> webhookService.sendWebhook(notification);
                default -> throw new IllegalArgumentException("Unsupported notification type: " + notification.getNotificationType());
            }

            log.info("Notification sent successfully: {} in tenant: {}", notificationId, tenantId);

        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", notificationId, e.getMessage(), e);
            
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
            
            throw e;
        }
    }

    /**
     * Process scheduled notifications
     */
    @Scheduled(fixedRateString = "#{${inticky.notification.events.batch-timeout-seconds:30} * 1000}")
    @Transactional
    public void processScheduledNotifications() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            // This is a system-level scheduled task, process all tenants
            // In production, this would need to iterate through all tenants
            log.debug("Skipping scheduled notification processing - no tenant context");
            return;
        }

        log.debug("Processing scheduled notifications for tenant: {}", tenantId);

        LocalDateTime now = LocalDateTime.now();
        
        // Process ready notifications
        List<Notification> readyNotifications = notificationRepository.findReadyToSend(
                NotificationStatus.PENDING, tenantId, now);
        
        for (Notification notification : readyNotifications) {
            try {
                sendNotificationAsync(notification.getId());
            } catch (Exception e) {
                log.error("Failed to schedule notification {}: {}", notification.getId(), e.getMessage(), e);
            }
        }

        // Process retry notifications
        List<Notification> retryNotifications = notificationRepository.findReadyForRetry(
                NotificationStatus.PENDING, tenantId, now);
        
        for (Notification notification : retryNotifications) {
            try {
                sendNotificationAsync(notification.getId());
            } catch (Exception e) {
                log.error("Failed to retry notification {}: {}", notification.getId(), e.getMessage(), e);
            }
        }

        log.debug("Processed {} ready and {} retry notifications for tenant: {}", 
                readyNotifications.size(), retryNotifications.size(), tenantId);
    }

    /**
     * Mark notification as delivered (webhook callback)
     */
    @Transactional
    @CacheEvict(value = "notifications", key = "#externalId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public void markAsDelivered(String externalId, NotificationStatus status) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Marking notification as delivered: {} status: {} in tenant: {}", externalId, status, tenantId);

        notificationRepository.findByExternalIdAndTenantId(externalId, tenantId)
                .ifPresent(notification -> {
                    switch (status) {
                        case DELIVERED -> notification.markAsDelivered();
                        case OPENED -> notification.markAsOpened();
                        case CLICKED -> notification.markAsClicked();
                        case BOUNCED -> notification.markAsBounced("Email bounced");
                        case FAILED -> notification.markAsFailed("Delivery failed");
                    }
                    notificationRepository.save(notification);
                    
                    log.info("Notification {} marked as {} in tenant: {}", externalId, status, tenantId);
                });
    }

    /**
     * Cancel notification
     */
    @Transactional
    @CacheEvict(value = "notifications", key = "#notificationId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public void cancelNotification(UUID notificationId) {
        String tenantId = TenantContext.getTenantId();
        log.info("Cancelling notification: {} in tenant: {}", notificationId, tenantId);

        Notification notification = getNotificationById(notificationId);
        
        if (notification.isPending() || notification.isProcessing()) {
            notification.cancel();
            notificationRepository.save(notification);
            
            log.info("Notification cancelled: {} in tenant: {}", notificationId, tenantId);
        } else {
            log.warn("Cannot cancel notification {} - current status: {}", notificationId, notification.getStatus());
        }
    }

    /**
     * Get notification statistics for tenant
     */
    @Cacheable(value = "notification-stats", key = "T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public Map<String, Object> getNotificationStatistics() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching notification statistics for tenant: {}", tenantId);

        LocalDateTime since = LocalDateTime.now().minusDays(30);

        // Get basic counts
        long totalNotifications = notificationRepository.countByTenantId(tenantId);
        long pendingNotifications = notificationRepository.countByStatusAndTenantId(NotificationStatus.PENDING, tenantId);
        long sentNotifications = notificationRepository.countByStatusAndTenantId(NotificationStatus.SENT, tenantId);
        long failedNotifications = notificationRepository.countByStatusAndTenantId(NotificationStatus.FAILED, tenantId);

        // Get statistics by status
        List<Object[]> statusStats = notificationRepository.getNotificationStatsByStatusAndTenantId(tenantId);
        
        // Get statistics by type
        List<Object[]> typeStats = notificationRepository.getNotificationStatsByTypeAndTenantId(tenantId);
        
        // Get delivery rate
        List<Object[]> deliveryStats = notificationRepository.getDeliveryRateStats(tenantId, since);
        
        // Get engagement stats
        List<Object[]> engagementStats = notificationRepository.getEngagementStats(tenantId, since);

        return Map.of(
                "totalNotifications", totalNotifications,
                "pendingNotifications", pendingNotifications,
                "sentNotifications", sentNotifications,
                "failedNotifications", failedNotifications,
                "statusStatistics", statusStats,
                "typeStatistics", typeStats,
                "deliveryStatistics", deliveryStats,
                "engagementStatistics", engagementStats,
                "tenantId", tenantId,
                "generatedAt", LocalDateTime.now()
        );
    }

    /**
     * Cleanup old notifications
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupOldNotifications() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            // System-level cleanup would need to iterate through all tenants
            log.debug("Skipping notification cleanup - no tenant context");
            return;
        }

        log.info("Starting notification cleanup for tenant: {}", tenantId);

        LocalDateTime cleanupBefore = LocalDateTime.now().minusDays(cleanupAfterDays);
        
        // Mark expired notifications
        LocalDateTime expiredBefore = LocalDateTime.now().minusHours(24);
        int expiredCount = notificationRepository.markExpiredNotifications(tenantId, expiredBefore);
        
        // Find notifications for cleanup
        List<Notification> notificationsToCleanup = notificationRepository.findNotificationsForCleanup(tenantId, cleanupBefore);
        
        // Delete old notifications
        int deletedCount = 0;
        for (Notification notification : notificationsToCleanup) {
            notificationRepository.delete(notification);
            deletedCount++;
        }

        log.info("Notification cleanup completed for tenant {}: {} expired, {} deleted", 
                tenantId, expiredCount, deletedCount);
    }

    /**
     * Process template variables
     */
    private String processTemplate(String template, String templateVariables) {
        if (template == null || templateVariables == null) {
            return template;
        }

        // TODO: Implement proper template processing with variables
        // This is a simple placeholder implementation
        return template;
    }

    /**
     * Notification request DTO
     */
    public static class NotificationRequest {
        private UUID recipientId;
        private String recipientEmail;
        private String recipientName;
        private NotificationType notificationType;
        private Priority priority;
        private String eventType;
        private UUID eventId;
        private UUID templateId;
        private String templateName;
        private String subject;
        private String body;
        private String htmlBody;
        private String destination;
        private String sender;
        private LocalDateTime scheduledAt;
        private Integer maxAttempts;
        private String metadata;
        private String templateVariables;
        private String deliveryConfig;
        private String tags;

        // Getters and setters
        public UUID getRecipientId() { return recipientId; }
        public void setRecipientId(UUID recipientId) { this.recipientId = recipientId; }
        
        public String getRecipientEmail() { return recipientEmail; }
        public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
        
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        
        public NotificationType getNotificationType() { return notificationType; }
        public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
        
        public Priority getPriority() { return priority; }
        public void setPriority(Priority priority) { this.priority = priority; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public UUID getEventId() { return eventId; }
        public void setEventId(UUID eventId) { this.eventId = eventId; }
        
        public UUID getTemplateId() { return templateId; }
        public void setTemplateId(UUID templateId) { this.templateId = templateId; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        
        public String getHtmlBody() { return htmlBody; }
        public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
        
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
        
        public Integer getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
        
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
        
        public String getTemplateVariables() { return templateVariables; }
        public void setTemplateVariables(String templateVariables) { this.templateVariables = templateVariables; }
        
        public String getDeliveryConfig() { return deliveryConfig; }
        public void setDeliveryConfig(String deliveryConfig) { this.deliveryConfig = deliveryConfig; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
}
