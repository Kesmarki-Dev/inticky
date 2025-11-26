package com.kesmarki.inticky.ticket.repository;

import com.kesmarki.inticky.common.repository.MultiTenantJpaRepository;
import com.kesmarki.inticky.ticket.entity.Comment;
import com.kesmarki.inticky.ticket.enums.CommentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Comment entity operations
 */
@Repository
public interface CommentRepository extends MultiTenantJpaRepository<Comment> {

    /**
     * Find comments by ticket ID within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find comments by ticket ID within tenant with pagination
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    Page<Comment> findByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find public comments by ticket ID within tenant (visible to customer)
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.type IN ('PUBLIC', 'STATUS_CHANGE', 'RESOLUTION') AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findPublicCommentsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find comments by author within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.authorId = :authorId AND c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorIdAndTenantId(@Param("authorId") UUID authorId, @Param("tenantId") String tenantId);

    /**
     * Find comments by author within tenant with pagination
     */
    @Query("SELECT c FROM Comment c WHERE c.authorId = :authorId AND c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorIdAndTenantId(@Param("authorId") UUID authorId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find comments by type within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.type = :type AND c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    List<Comment> findByTypeAndTenantId(@Param("type") CommentType type, @Param("tenantId") String tenantId);

    /**
     * Find pinned comments by ticket ID within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.isPinned = true AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findPinnedCommentsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find comments with time tracking by ticket ID within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.timeSpentMinutes IS NOT NULL AND c.timeSpentMinutes > 0 AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findTimeTrackingCommentsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find recent comments within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt > :since AND c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByTenantId(@Param("since") LocalDateTime since, @Param("tenantId") String tenantId);

    /**
     * Find system generated comments by ticket ID within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.type IN ('SYSTEM', 'STATUS_CHANGE', 'ASSIGNMENT', 'PRIORITY_CHANGE', 'CATEGORY_CHANGE', 'ESCALATION') AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findSystemCommentsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find user generated comments by ticket ID within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.type IN ('PUBLIC', 'INTERNAL', 'RESOLUTION') AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findUserCommentsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Count comments by ticket ID within tenant
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.ticket.id = :ticketId AND c.tenantId = :tenantId")
    long countByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Count public comments by ticket ID within tenant
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.ticket.id = :ticketId AND c.type IN ('PUBLIC', 'STATUS_CHANGE', 'RESOLUTION') AND c.tenantId = :tenantId")
    long countPublicCommentsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Count comments by author within tenant
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.authorId = :authorId AND c.tenantId = :tenantId")
    long countByAuthorIdAndTenantId(@Param("authorId") UUID authorId, @Param("tenantId") String tenantId);

    /**
     * Get total time spent on ticket within tenant
     */
    @Query("SELECT COALESCE(SUM(c.timeSpentMinutes), 0) FROM Comment c WHERE c.ticket.id = :ticketId AND c.timeSpentMinutes IS NOT NULL AND c.tenantId = :tenantId")
    long getTotalTimeSpentMinutesByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Get total time spent by author within tenant
     */
    @Query("SELECT COALESCE(SUM(c.timeSpentMinutes), 0) FROM Comment c WHERE c.authorId = :authorId AND c.timeSpentMinutes IS NOT NULL AND c.tenantId = :tenantId")
    long getTotalTimeSpentMinutesByAuthorIdAndTenantId(@Param("authorId") UUID authorId, @Param("tenantId") String tenantId);

    /**
     * Find comments created between dates within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsCreatedBetweenAndTenantId(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("tenantId") String tenantId);

    /**
     * Find edited comments within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.isEdited = true AND c.tenantId = :tenantId ORDER BY c.editedAt DESC")
    List<Comment> findEditedCommentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Search comments by content within tenant
     */
    @Query("SELECT c FROM Comment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    Page<Comment> searchByContentAndTenantId(@Param("keyword") String keyword, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find comments by ticket and type within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.type = :type AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findByTicketIdAndTypeAndTenantId(@Param("ticketId") UUID ticketId, @Param("type") CommentType type, @Param("tenantId") String tenantId);

    /**
     * Find last comment by ticket ID within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.tenantId = :tenantId ORDER BY c.createdAt DESC LIMIT 1")
    Comment findLastCommentByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find comments by multiple ticket IDs within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id IN :ticketIds AND c.tenantId = :tenantId ORDER BY c.ticket.id, c.createdAt ASC")
    List<Comment> findByTicketIdsAndTenantId(@Param("ticketIds") List<UUID> ticketIds, @Param("tenantId") String tenantId);

    /**
     * Count comments by type within tenant
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.type = :type AND c.tenantId = :tenantId")
    long countByTypeAndTenantId(@Param("type") CommentType type, @Param("tenantId") String tenantId);

    /**
     * Find resolution comments within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.type = 'RESOLUTION' AND c.tenantId = :tenantId ORDER BY c.createdAt DESC")
    List<Comment> findResolutionCommentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find comments by ticket and author within tenant
     */
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId AND c.authorId = :authorId AND c.tenantId = :tenantId ORDER BY c.createdAt ASC")
    List<Comment> findByTicketIdAndAuthorIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("authorId") UUID authorId, @Param("tenantId") String tenantId);
}
