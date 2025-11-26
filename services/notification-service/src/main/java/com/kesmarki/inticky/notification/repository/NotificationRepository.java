package com.kesmarki.inticky.notification.repository;

import com.kesmarki.inticky.common.repository.MultiTenantJpaRepository;
import com.kesmarki.inticky.notification.entity.Notification;
import com.kesmarki.inticky.notification.enums.NotificationStatus;
import com.kesmarki.inticky.notification.enums.NotificationType;
import com.kesmarki.inticky.notification.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Notification entity operations
 */
@Repository
public interface NotificationRepository extends MultiTenantJpaRepository<Notification> {

    /**
     * Find notifications by recipient within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.tenantId = :tenantId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndTenantId(@Param("recipientId") UUID recipientId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find notifications by status within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.tenantId = :tenantId ORDER BY n.createdAt ASC")
    List<Notification> findByStatusAndTenantId(@Param("status") NotificationStatus status, @Param("tenantId") String tenantId);

    /**
     * Find notifications by type within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.notificationType = :type AND n.tenantId = :tenantId ORDER BY n.createdAt DESC")
    Page<Notification> findByNotificationTypeAndTenantId(@Param("type") NotificationType type, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find notifications by priority within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.priority = :priority AND n.tenantId = :tenantId ORDER BY n.createdAt DESC")
    List<Notification> findByPriorityAndTenantId(@Param("priority") Priority priority, @Param("tenantId") String tenantId);

    /**
     * Find notifications by event within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.eventType = :eventType AND n.eventId = :eventId AND n.tenantId = :tenantId ORDER BY n.createdAt DESC")
    List<Notification> findByEventTypeAndEventIdAndTenantId(@Param("eventType") String eventType, @Param("eventId") UUID eventId, @Param("tenantId") String tenantId);

    /**
     * Find notifications ready to send
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.tenantId = :tenantId AND " +
           "(n.scheduledAt IS NULL OR n.scheduledAt <= :now) ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findReadyToSend(@Param("status") NotificationStatus status, @Param("tenantId") String tenantId, @Param("now") LocalDateTime now);

    /**
     * Find notifications ready for retry
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.tenantId = :tenantId AND " +
           "n.nextRetryAt IS NOT NULL AND n.nextRetryAt <= :now AND n.attemptCount < n.maxAttempts " +
           "ORDER BY n.priority DESC, n.nextRetryAt ASC")
    List<Notification> findReadyForRetry(@Param("status") NotificationStatus status, @Param("tenantId") String tenantId, @Param("now") LocalDateTime now);

    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.tenantId = :tenantId AND " +
           "n.scheduledAt IS NOT NULL AND n.scheduledAt < :expiredBefore")
    List<Notification> findExpiredNotifications(@Param("status") NotificationStatus status, @Param("tenantId") String tenantId, @Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * Find notifications by external ID
     */
    @Query("SELECT n FROM Notification n WHERE n.externalId = :externalId AND n.tenantId = :tenantId")
    Optional<Notification> findByExternalIdAndTenantId(@Param("externalId") String externalId, @Param("tenantId") String tenantId);

    /**
     * Find recent notifications within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt > :since AND n.tenantId = :tenantId ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("since") LocalDateTime since, @Param("tenantId") String tenantId);

    /**
     * Count notifications by status within tenant
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.tenantId = :tenantId")
    long countByStatusAndTenantId(@Param("status") NotificationStatus status, @Param("tenantId") String tenantId);

    /**
     * Count notifications by type within tenant
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.notificationType = :type AND n.tenantId = :tenantId")
    long countByNotificationTypeAndTenantId(@Param("type") NotificationType type, @Param("tenantId") String tenantId);

    /**
     * Count notifications by recipient within tenant
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.tenantId = :tenantId")
    long countByRecipientIdAndTenantId(@Param("recipientId") UUID recipientId, @Param("tenantId") String tenantId);

    /**
     * Get notification statistics by status within tenant
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.tenantId = :tenantId GROUP BY n.status")
    List<Object[]> getNotificationStatsByStatusAndTenantId(@Param("tenantId") String tenantId);

    /**
     * Get notification statistics by type within tenant
     */
    @Query("SELECT n.notificationType, COUNT(n), AVG(CASE WHEN n.sentAt IS NOT NULL AND n.createdAt IS NOT NULL THEN EXTRACT(EPOCH FROM (n.sentAt - n.createdAt)) ELSE NULL END) " +
           "FROM Notification n WHERE n.tenantId = :tenantId GROUP BY n.notificationType")
    List<Object[]> getNotificationStatsByTypeAndTenantId(@Param("tenantId") String tenantId);

    /**
     * Get delivery rate statistics within tenant
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN n.status IN ('SENT', 'DELIVERED', 'OPENED', 'CLICKED') THEN 1 END) as delivered, " +
           "COUNT(CASE WHEN n.status IN ('FAILED', 'BOUNCED', 'REJECTED') THEN 1 END) as failed, " +
           "COUNT(n) as total " +
           "FROM Notification n WHERE n.tenantId = :tenantId AND n.createdAt >= :since")
    List<Object[]> getDeliveryRateStats(@Param("tenantId") String tenantId, @Param("since") LocalDateTime since);

    /**
     * Get engagement statistics within tenant
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN n.openedAt IS NOT NULL THEN 1 END) as opened, " +
           "COUNT(CASE WHEN n.clickedAt IS NOT NULL THEN 1 END) as clicked, " +
           "COUNT(CASE WHEN n.status IN ('SENT', 'DELIVERED', 'OPENED', 'CLICKED') THEN 1 END) as delivered " +
           "FROM Notification n WHERE n.tenantId = :tenantId AND n.createdAt >= :since")
    List<Object[]> getEngagementStats(@Param("tenantId") String tenantId, @Param("since") LocalDateTime since);

    /**
     * Find notifications for cleanup (old and completed)
     */
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND " +
           "n.createdAt < :cleanupBefore AND " +
           "n.status IN ('SENT', 'DELIVERED', 'OPENED', 'CLICKED', 'FAILED', 'BOUNCED', 'REJECTED', 'CANCELLED', 'EXPIRED')")
    List<Notification> findNotificationsForCleanup(@Param("tenantId") String tenantId, @Param("cleanupBefore") LocalDateTime cleanupBefore);

    /**
     * Find high priority pending notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND " +
           "n.status = 'PENDING' AND n.priority IN ('HIGH', 'URGENT', 'CRITICAL') AND " +
           "(n.scheduledAt IS NULL OR n.scheduledAt <= :now) " +
           "ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findHighPriorityPendingNotifications(@Param("tenantId") String tenantId, @Param("now") LocalDateTime now);

    /**
     * Find notifications by template within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.templateId = :templateId AND n.tenantId = :tenantId ORDER BY n.createdAt DESC")
    Page<Notification> findByTemplateIdAndTenantId(@Param("templateId") UUID templateId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find notifications created between dates within tenant
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate AND n.tenantId = :tenantId ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("tenantId") String tenantId);

    /**
     * Update notification status by external ID
     */
    @Query("UPDATE Notification n SET n.status = :status, n.deliveredAt = :deliveredAt WHERE n.externalId = :externalId AND n.tenantId = :tenantId")
    int updateStatusByExternalId(@Param("externalId") String externalId, @Param("status") NotificationStatus status, @Param("deliveredAt") LocalDateTime deliveredAt, @Param("tenantId") String tenantId);

    /**
     * Mark notifications as expired
     */
    @Query("UPDATE Notification n SET n.status = 'EXPIRED' WHERE n.status = 'PENDING' AND n.tenantId = :tenantId AND " +
           "n.scheduledAt IS NOT NULL AND n.scheduledAt < :expiredBefore")
    int markExpiredNotifications(@Param("tenantId") String tenantId, @Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * Get average delivery time by type within tenant
     */
    @Query("SELECT n.notificationType, AVG(EXTRACT(EPOCH FROM (n.sentAt - n.createdAt))) " +
           "FROM Notification n WHERE n.tenantId = :tenantId AND n.sentAt IS NOT NULL AND n.createdAt >= :since " +
           "GROUP BY n.notificationType")
    List<Object[]> getAverageDeliveryTimeByType(@Param("tenantId") String tenantId, @Param("since") LocalDateTime since);
}
