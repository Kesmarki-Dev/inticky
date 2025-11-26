package com.kesmarki.inticky.notification.repository;

import com.kesmarki.inticky.common.repository.MultiTenantJpaRepository;
import com.kesmarki.inticky.notification.entity.NotificationTemplate;
import com.kesmarki.inticky.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for NotificationTemplate entity operations
 */
@Repository
public interface NotificationTemplateRepository extends MultiTenantJpaRepository<NotificationTemplate> {

    /**
     * Find template by name within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.name = :name AND t.tenantId = :tenantId")
    Optional<NotificationTemplate> findByNameAndTenantId(@Param("name") String name, @Param("tenantId") String tenantId);

    /**
     * Find templates by type within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.notificationType = :type AND t.tenantId = :tenantId ORDER BY t.name")
    List<NotificationTemplate> findByNotificationTypeAndTenantId(@Param("type") NotificationType type, @Param("tenantId") String tenantId);

    /**
     * Find active templates by type within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.notificationType = :type AND t.isActive = true AND t.tenantId = :tenantId ORDER BY t.name")
    List<NotificationTemplate> findActiveByNotificationTypeAndTenantId(@Param("type") NotificationType type, @Param("tenantId") String tenantId);

    /**
     * Find template by event type within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.eventType = :eventType AND t.tenantId = :tenantId ORDER BY t.isDefault DESC, t.name")
    List<NotificationTemplate> findByEventTypeAndTenantId(@Param("eventType") String eventType, @Param("tenantId") String tenantId);

    /**
     * Find default template for event type and notification type within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.eventType = :eventType AND t.notificationType = :notificationType AND " +
           "t.isDefault = true AND t.isActive = true AND t.tenantId = :tenantId")
    Optional<NotificationTemplate> findDefaultTemplate(@Param("eventType") String eventType, 
                                                      @Param("notificationType") NotificationType notificationType, 
                                                      @Param("tenantId") String tenantId);

    /**
     * Find template for event type, notification type and language within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.eventType = :eventType AND t.notificationType = :notificationType AND " +
           "t.language = :language AND t.isActive = true AND t.tenantId = :tenantId ORDER BY t.isDefault DESC")
    List<NotificationTemplate> findByEventTypeAndNotificationTypeAndLanguageAndTenantId(
            @Param("eventType") String eventType, 
            @Param("notificationType") NotificationType notificationType,
            @Param("language") String language,
            @Param("tenantId") String tenantId);

    /**
     * Find templates by language within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.language = :language AND t.tenantId = :tenantId ORDER BY t.notificationType, t.name")
    List<NotificationTemplate> findByLanguageAndTenantId(@Param("language") String language, @Param("tenantId") String tenantId);

    /**
     * Find active templates within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.isActive = true AND t.tenantId = :tenantId ORDER BY t.notificationType, t.name")
    Page<NotificationTemplate> findActiveTemplatesByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find default templates within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.isDefault = true AND t.tenantId = :tenantId ORDER BY t.notificationType, t.eventType")
    List<NotificationTemplate> findDefaultTemplatesByTenantId(@Param("tenantId") String tenantId);

    /**
     * Search templates by name or description within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId AND (" +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.name")
    Page<NotificationTemplate> searchTemplatesByKeywordAndTenantId(@Param("keyword") String keyword, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Count templates by type within tenant
     */
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.notificationType = :type AND t.tenantId = :tenantId")
    long countByNotificationTypeAndTenantId(@Param("type") NotificationType type, @Param("tenantId") String tenantId);

    /**
     * Count active templates within tenant
     */
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.isActive = true AND t.tenantId = :tenantId")
    long countActiveTemplatesByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get template statistics by type within tenant
     */
    @Query("SELECT t.notificationType, COUNT(t), COUNT(CASE WHEN t.isActive = true THEN 1 END) " +
           "FROM NotificationTemplate t WHERE t.tenantId = :tenantId GROUP BY t.notificationType")
    List<Object[]> getTemplateStatsByTypeAndTenantId(@Param("tenantId") String tenantId);

    /**
     * Get template statistics by language within tenant
     */
    @Query("SELECT t.language, COUNT(t) FROM NotificationTemplate t WHERE t.tenantId = :tenantId GROUP BY t.language ORDER BY COUNT(t) DESC")
    List<Object[]> getTemplateStatsByLanguageAndTenantId(@Param("tenantId") String tenantId);

    /**
     * Find templates by parent template ID within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.parentTemplateId = :parentTemplateId AND t.tenantId = :tenantId ORDER BY t.version DESC")
    List<NotificationTemplate> findByParentTemplateIdAndTenantId(@Param("parentTemplateId") String parentTemplateId, @Param("tenantId") String tenantId);

    /**
     * Find latest version of template within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.name = :name AND t.tenantId = :tenantId ORDER BY t.version DESC")
    List<NotificationTemplate> findLatestVersionByNameAndTenantId(@Param("name") String name, @Param("tenantId") String tenantId);

    /**
     * Check if template name exists within tenant
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM NotificationTemplate t WHERE t.name = :name AND t.tenantId = :tenantId")
    boolean existsByNameAndTenantId(@Param("name") String name, @Param("tenantId") String tenantId);

    /**
     * Find templates that need default status update (multiple defaults for same event/type)
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId AND t.isDefault = true AND " +
           "EXISTS (SELECT t2 FROM NotificationTemplate t2 WHERE t2.tenantId = :tenantId AND t2.isDefault = true AND " +
           "t2.eventType = t.eventType AND t2.notificationType = t.notificationType AND t2.id != t.id)")
    List<NotificationTemplate> findConflictingDefaultTemplatesByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find incomplete templates (missing required fields) within tenant
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId AND (" +
           "t.body IS NULL OR t.body = '' OR " +
           "(t.notificationType IN ('EMAIL', 'PUSH') AND (t.subject IS NULL OR t.subject = '')))")
    List<NotificationTemplate> findIncompleteTemplatesByTenantId(@Param("tenantId") String tenantId);

    /**
     * Update template default status
     */
    @Query("UPDATE NotificationTemplate t SET t.isDefault = :isDefault WHERE t.id = :templateId AND t.tenantId = :tenantId")
    int updateDefaultStatus(@Param("templateId") String templateId, @Param("isDefault") boolean isDefault, @Param("tenantId") String tenantId);

    /**
     * Update template active status
     */
    @Query("UPDATE NotificationTemplate t SET t.isActive = :isActive WHERE t.id = :templateId AND t.tenantId = :tenantId")
    int updateActiveStatus(@Param("templateId") String templateId, @Param("isActive") boolean isActive, @Param("tenantId") String tenantId);

    /**
     * Clear default status for event type and notification type within tenant
     */
    @Query("UPDATE NotificationTemplate t SET t.isDefault = false WHERE t.eventType = :eventType AND " +
           "t.notificationType = :notificationType AND t.tenantId = :tenantId")
    int clearDefaultStatusForEventAndType(@Param("eventType") String eventType, 
                                         @Param("notificationType") NotificationType notificationType, 
                                         @Param("tenantId") String tenantId);

    /**
     * Find most used templates within tenant (based on usage stats)
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId AND t.usageStats IS NOT NULL " +
           "ORDER BY CAST(JSON_EXTRACT(t.usageStats, '$.totalUsage') AS INTEGER) DESC")
    List<NotificationTemplate> findMostUsedTemplatesByTenantId(@Param("tenantId") String tenantId, Pageable pageable);
}
