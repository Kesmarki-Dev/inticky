package com.kesmarki.inticky.ai.repository;

import com.kesmarki.inticky.ai.entity.AiSession;
import com.kesmarki.inticky.common.repository.MultiTenantJpaRepository;
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
 * Repository for AiSession entity operations
 */
@Repository
public interface AiSessionRepository extends MultiTenantJpaRepository<AiSession> {

    /**
     * Find active sessions by user within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.userId = :userId AND s.isActive = true AND s.tenantId = :tenantId ORDER BY s.lastActivityAt DESC")
    List<AiSession> findActiveSessionsByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") String tenantId);

    /**
     * Find active sessions by user within tenant with pagination
     */
    @Query("SELECT s FROM AiSession s WHERE s.userId = :userId AND s.isActive = true AND s.tenantId = :tenantId ORDER BY s.lastActivityAt DESC")
    Page<AiSession> findActiveSessionsByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find all sessions by user within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.userId = :userId AND s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    Page<AiSession> findSessionsByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find sessions by ticket ID within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.ticketId = :ticketId AND s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    List<AiSession> findSessionsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find sessions by type within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.sessionType = :sessionType AND s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    Page<AiSession> findSessionsByTypeAndTenantId(@Param("sessionType") AiSession.SessionType sessionType, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find expired sessions within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.expiresAt < :now AND s.isActive = true AND s.tenantId = :tenantId")
    List<AiSession> findExpiredSessionsByTenantId(@Param("now") LocalDateTime now, @Param("tenantId") String tenantId);

    /**
     * Find inactive sessions within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.lastActivityAt < :threshold AND s.isActive = true AND s.tenantId = :tenantId")
    List<AiSession> findInactiveSessionsByTenantId(@Param("threshold") LocalDateTime threshold, @Param("tenantId") String tenantId);

    /**
     * Count active sessions by user within tenant
     */
    @Query("SELECT COUNT(s) FROM AiSession s WHERE s.userId = :userId AND s.isActive = true AND s.tenantId = :tenantId")
    long countActiveSessionsByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") String tenantId);

    /**
     * Count total sessions by user within tenant
     */
    @Query("SELECT COUNT(s) FROM AiSession s WHERE s.userId = :userId AND s.tenantId = :tenantId")
    long countSessionsByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") String tenantId);

    /**
     * Count active sessions within tenant
     */
    @Query("SELECT COUNT(s) FROM AiSession s WHERE s.isActive = true AND s.tenantId = :tenantId")
    long countActiveSessionsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find recent sessions within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.createdAt > :since AND s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    List<AiSession> findRecentSessionsByTenantId(@Param("since") LocalDateTime since, @Param("tenantId") String tenantId);

    /**
     * Find sessions with high message count within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.messageCount >= :minMessages AND s.tenantId = :tenantId ORDER BY s.messageCount DESC")
    List<AiSession> findHighActivitySessionsByTenantId(@Param("minMessages") Integer minMessages, @Param("tenantId") String tenantId);

    /**
     * Find sessions by priority within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.priority = :priority AND s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    List<AiSession> findSessionsByPriorityAndTenantId(@Param("priority") AiSession.Priority priority, @Param("tenantId") String tenantId);

    /**
     * Search sessions by title or description within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.tenantId = :tenantId AND (" +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.lastActivityAt DESC")
    Page<AiSession> searchSessionsByKeywordAndTenantId(@Param("keyword") String keyword, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find sessions created between dates within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.createdAt BETWEEN :startDate AND :endDate AND s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    List<AiSession> findSessionsCreatedBetweenAndTenantId(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("tenantId") String tenantId);

    /**
     * Get session statistics by type within tenant
     */
    @Query("SELECT s.sessionType, COUNT(s), AVG(s.messageCount) FROM AiSession s WHERE s.tenantId = :tenantId GROUP BY s.sessionType")
    List<Object[]> getSessionStatisticsByTypeAndTenantId(@Param("tenantId") String tenantId);

    /**
     * Get session statistics by user within tenant
     */
    @Query("SELECT s.userId, s.userName, COUNT(s), SUM(s.messageCount) FROM AiSession s WHERE s.tenantId = :tenantId GROUP BY s.userId, s.userName ORDER BY COUNT(s) DESC")
    List<Object[]> getSessionStatisticsByUserAndTenantId(@Param("tenantId") String tenantId);

    /**
     * Find most active sessions within tenant
     */
    @Query("SELECT s FROM AiSession s WHERE s.tenantId = :tenantId ORDER BY s.messageCount DESC, s.lastActivityAt DESC")
    Page<AiSession> findMostActiveSessionsByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find sessions that need cleanup (inactive and old)
     */
    @Query("SELECT s FROM AiSession s WHERE s.tenantId = :tenantId AND (" +
           "(s.isActive = false AND s.updatedAt < :oldThreshold) OR " +
           "(s.isActive = true AND s.lastActivityAt < :inactiveThreshold))")
    List<AiSession> findSessionsForCleanup(@Param("tenantId") String tenantId, 
                                          @Param("oldThreshold") LocalDateTime oldThreshold,
                                          @Param("inactiveThreshold") LocalDateTime inactiveThreshold);

    /**
     * Update session activity timestamp
     */
    @Query("UPDATE AiSession s SET s.lastActivityAt = :timestamp WHERE s.id = :sessionId AND s.tenantId = :tenantId")
    void updateSessionActivity(@Param("sessionId") UUID sessionId, @Param("tenantId") String tenantId, @Param("timestamp") LocalDateTime timestamp);

    /**
     * Deactivate expired sessions
     */
    @Query("UPDATE AiSession s SET s.isActive = false WHERE s.expiresAt < :now AND s.isActive = true AND s.tenantId = :tenantId")
    int deactivateExpiredSessions(@Param("now") LocalDateTime now, @Param("tenantId") String tenantId);

    /**
     * Increment message count for session
     */
    @Query("UPDATE AiSession s SET s.messageCount = s.messageCount + 1, s.lastActivityAt = :timestamp WHERE s.id = :sessionId AND s.tenantId = :tenantId")
    void incrementMessageCount(@Param("sessionId") UUID sessionId, @Param("tenantId") String tenantId, @Param("timestamp") LocalDateTime timestamp);
}
