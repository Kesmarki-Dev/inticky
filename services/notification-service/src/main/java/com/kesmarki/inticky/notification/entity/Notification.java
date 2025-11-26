package com.kesmarki.inticky.notification.entity;

import com.kesmarki.inticky.common.entity.BaseEntity;
import com.kesmarki.inticky.notification.enums.NotificationStatus;
import com.kesmarki.inticky.notification.enums.NotificationType;
import com.kesmarki.inticky.notification.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a notification instance
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_tenant_recipient", columnList = "tenant_id, recipient_id"),
        @Index(name = "idx_notification_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_notification_tenant_type", columnList = "tenant_id, notification_type"),
        @Index(name = "idx_notification_tenant_created", columnList = "tenant_id, created_at"),
        @Index(name = "idx_notification_scheduled", columnList = "scheduled_at"),
        @Index(name = "idx_notification_external_id", columnList = "external_id"),
        @Index(name = "idx_notification_event", columnList = "event_type, event_id")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Notification extends BaseEntity {

    /**
     * Recipient user ID
     */
    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    /**
     * Recipient email (denormalized for performance)
     */
    @Column(name = "recipient_email", length = 320)
    private String recipientEmail;

    /**
     * Recipient name (denormalized for performance)
     */
    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    /**
     * Notification type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType;

    /**
     * Notification priority
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    /**
     * Current delivery status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Event type that triggered this notification
     */
    @Column(name = "event_type", length = 100)
    private String eventType;

    /**
     * Event ID (e.g., ticket ID, user ID)
     */
    @Column(name = "event_id")
    private UUID eventId;

    /**
     * Template used for this notification
     */
    @Column(name = "template_id")
    private UUID templateId;

    /**
     * Template name (denormalized)
     */
    @Column(name = "template_name", length = 100)
    private String templateName;

    /**
     * Notification subject
     */
    @Column(name = "subject", length = 500)
    private String subject;

    /**
     * Notification body content
     */
    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    /**
     * HTML body content (for email)
     */
    @Column(name = "html_body", columnDefinition = "TEXT")
    private String htmlBody;

    /**
     * Destination address (email, phone, webhook URL, etc.)
     */
    @Column(name = "destination", length = 500)
    private String destination;

    /**
     * Sender information
     */
    @Column(name = "sender", length = 320)
    private String sender;

    /**
     * When notification should be sent
     */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    /**
     * When notification was actually sent
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * When notification was delivered (if confirmed)
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /**
     * When notification was opened/read
     */
    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    /**
     * When notification link was clicked
     */
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    /**
     * Number of delivery attempts
     */
    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 0;

    /**
     * Maximum number of attempts allowed
     */
    @Column(name = "max_attempts")
    @Builder.Default
    private Integer maxAttempts = 3;

    /**
     * Next retry attempt time
     */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    /**
     * External provider message ID
     */
    @Column(name = "external_id", length = 255)
    private String externalId;

    /**
     * Provider response/error message
     */
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    /**
     * Notification metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Template variables used (JSON)
     */
    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables;

    /**
     * Delivery configuration (JSON)
     */
    @Column(name = "delivery_config", columnDefinition = "TEXT")
    private String deliveryConfig;

    /**
     * Tracking information (JSON)
     */
    @Column(name = "tracking_info", columnDefinition = "TEXT")
    private String trackingInfo;

    /**
     * Error details (JSON)
     */
    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    /**
     * Notification tags (JSON array)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * Check if notification is pending
     */
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }

    /**
     * Check if notification is processing
     */
    public boolean isProcessing() {
        return status == NotificationStatus.PROCESSING;
    }

    /**
     * Check if notification was sent successfully
     */
    public boolean isSent() {
        return status == NotificationStatus.SENT || status == NotificationStatus.DELIVERED;
    }

    /**
     * Check if notification failed
     */
    public boolean isFailed() {
        return status == NotificationStatus.FAILED || 
               status == NotificationStatus.BOUNCED || 
               status == NotificationStatus.REJECTED;
    }

    /**
     * Check if notification is scheduled for future delivery
     */
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check if notification is ready to send
     */
    public boolean isReadyToSend() {
        return isPending() && (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now()));
    }

    /**
     * Check if notification can be retried
     */
    public boolean canRetry() {
        return isFailed() && attemptCount < maxAttempts;
    }

    /**
     * Check if notification has expired
     */
    public boolean isExpired() {
        // Consider notification expired if scheduled more than 24 hours ago and still pending
        if (scheduledAt != null && isPending()) {
            return scheduledAt.isBefore(LocalDateTime.now().minusHours(24));
        }
        return false;
    }

    /**
     * Mark notification as processing
     */
    public void markAsProcessing() {
        this.status = NotificationStatus.PROCESSING;
        this.attemptCount = (this.attemptCount != null ? this.attemptCount : 0) + 1;
    }

    /**
     * Mark notification as sent
     */
    public void markAsSent(String externalId, String providerResponse) {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.externalId = externalId;
        this.providerResponse = providerResponse;
    }

    /**
     * Mark notification as delivered
     */
    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Mark notification as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorDetails = errorMessage;
        
        // Schedule retry if possible
        if (canRetry()) {
            scheduleRetry();
        }
    }

    /**
     * Mark notification as bounced
     */
    public void markAsBounced(String reason) {
        this.status = NotificationStatus.BOUNCED;
        this.errorDetails = reason;
    }

    /**
     * Mark notification as opened
     */
    public void markAsOpened() {
        if (this.openedAt == null) {
            this.openedAt = LocalDateTime.now();
            if (this.status == NotificationStatus.SENT || this.status == NotificationStatus.DELIVERED) {
                this.status = NotificationStatus.OPENED;
            }
        }
    }

    /**
     * Mark notification as clicked
     */
    public void markAsClicked() {
        if (this.clickedAt == null) {
            this.clickedAt = LocalDateTime.now();
            this.status = NotificationStatus.CLICKED;
            
            // Also mark as opened if not already
            if (this.openedAt == null) {
                this.openedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * Schedule retry attempt
     */
    public void scheduleRetry() {
        if (canRetry()) {
            // Exponential backoff: 1min, 5min, 15min, 1hour, 4hours
            int delayMinutes = switch (attemptCount) {
                case 1 -> 1;
                case 2 -> 5;
                case 3 -> 15;
                case 4 -> 60;
                default -> 240;
            };
            
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
            this.status = NotificationStatus.PENDING;
        }
    }

    /**
     * Cancel notification
     */
    public void cancel() {
        this.status = NotificationStatus.CANCELLED;
    }

    /**
     * Expire notification
     */
    public void expire() {
        this.status = NotificationStatus.EXPIRED;
    }

    /**
     * Get delivery time in milliseconds (sent - created)
     */
    public Long getDeliveryTimeMs() {
        if (sentAt != null && getCreatedAt() != null) {
            return java.time.Duration.between(getCreatedAt(), sentAt).toMillis();
        }
        return null;
    }

    /**
     * Get time to open in milliseconds (opened - sent)
     */
    public Long getTimeToOpenMs() {
        if (openedAt != null && sentAt != null) {
            return java.time.Duration.between(sentAt, openedAt).toMillis();
        }
        return null;
    }

    /**
     * Get time to click in milliseconds (clicked - sent)
     */
    public Long getTimeToClickMs() {
        if (clickedAt != null && sentAt != null) {
            return java.time.Duration.between(sentAt, clickedAt).toMillis();
        }
        return null;
    }

    /**
     * Check if notification is high priority
     */
    public boolean isHighPriority() {
        return priority == Priority.HIGH || priority == Priority.URGENT || priority == Priority.CRITICAL;
    }

    /**
     * Get notification age in minutes
     */
    public long getAgeInMinutes() {
        return java.time.Duration.between(getCreatedAt(), LocalDateTime.now()).toMinutes();
    }
}
