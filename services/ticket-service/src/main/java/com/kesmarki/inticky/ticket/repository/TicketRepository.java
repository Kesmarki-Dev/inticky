package com.kesmarki.inticky.ticket.repository;

import com.kesmarki.inticky.common.repository.MultiTenantJpaRepository;
import com.kesmarki.inticky.ticket.entity.Ticket;
import com.kesmarki.inticky.ticket.enums.Category;
import com.kesmarki.inticky.ticket.enums.Priority;
import com.kesmarki.inticky.ticket.enums.TicketStatus;
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
 * Repository for Ticket entity operations
 */
@Repository
public interface TicketRepository extends MultiTenantJpaRepository<Ticket> {

    /**
     * Find ticket by ticket number within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.ticketNumber = :ticketNumber AND t.tenantId = :tenantId")
    Optional<Ticket> findByTicketNumberAndTenantId(@Param("ticketNumber") String ticketNumber, @Param("tenantId") String tenantId);

    /**
     * Find tickets by status within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = :status AND t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    List<Ticket> findByStatusAndTenantId(@Param("status") TicketStatus status, @Param("tenantId") String tenantId);

    /**
     * Find tickets by status within tenant with pagination
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = :status AND t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    Page<Ticket> findByStatusAndTenantId(@Param("status") TicketStatus status, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find tickets by priority within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.priority = :priority AND t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    List<Ticket> findByPriorityAndTenantId(@Param("priority") Priority priority, @Param("tenantId") String tenantId);

    /**
     * Find tickets by category within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.category = :category AND t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    List<Ticket> findByCategoryAndTenantId(@Param("category") Category category, @Param("tenantId") String tenantId);

    /**
     * Find tickets assigned to user within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.assigneeId = :assigneeId AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    List<Ticket> findByAssigneeIdAndTenantId(@Param("assigneeId") UUID assigneeId, @Param("tenantId") String tenantId);

    /**
     * Find tickets assigned to user within tenant with pagination
     */
    @Query("SELECT t FROM Ticket t WHERE t.assigneeId = :assigneeId AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    Page<Ticket> findByAssigneeIdAndTenantId(@Param("assigneeId") UUID assigneeId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find tickets reported by user within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.reporterId = :reporterId AND t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    List<Ticket> findByReporterIdAndTenantId(@Param("reporterId") UUID reporterId, @Param("tenantId") String tenantId);

    /**
     * Find tickets reported by user within tenant with pagination
     */
    @Query("SELECT t FROM Ticket t WHERE t.reporterId = :reporterId AND t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    Page<Ticket> findByReporterIdAndTenantId(@Param("reporterId") UUID reporterId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find unassigned tickets within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.assigneeId IS NULL AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    List<Ticket> findUnassignedTicketsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find unassigned tickets within tenant with pagination
     */
    @Query("SELECT t FROM Ticket t WHERE t.assigneeId IS NULL AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    Page<Ticket> findUnassignedTicketsByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find active tickets within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('CLOSED', 'CANCELLED') AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    List<Ticket> findActiveTicketsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find active tickets within tenant with pagination
     */
    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('CLOSED', 'CANCELLED') AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    Page<Ticket> findActiveTicketsByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find overdue tickets within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.dueDate < :now AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED') AND t.tenantId = :tenantId ORDER BY t.dueDate ASC")
    List<Ticket> findOverdueTicketsByTenantId(@Param("now") LocalDateTime now, @Param("tenantId") String tenantId);

    /**
     * Find tickets approaching SLA breach within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.slaBreachDate BETWEEN :now AND :threshold AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED') AND t.tenantId = :tenantId ORDER BY t.slaBreachDate ASC")
    List<Ticket> findTicketsApproachingSLABreach(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold, @Param("tenantId") String tenantId);

    /**
     * Find tickets with SLA breach within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.slaBreachDate < :now AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED') AND t.tenantId = :tenantId ORDER BY t.slaBreachDate ASC")
    List<Ticket> findTicketsWithSLABreach(@Param("now") LocalDateTime now, @Param("tenantId") String tenantId);

    /**
     * Search tickets by keyword within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.tenantId = :tenantId AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<Ticket> searchByKeywordAndTenantId(@Param("keyword") String keyword, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find tickets created between dates within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.tenantId = :tenantId ORDER BY t.createdAt DESC")
    List<Ticket> findTicketsCreatedBetweenAndTenantId(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("tenantId") String tenantId);

    /**
     * Find tickets by multiple statuses within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.status IN :statuses AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    Page<Ticket> findByStatusInAndTenantId(@Param("statuses") List<TicketStatus> statuses, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find tickets by multiple priorities within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.priority IN :priorities AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    Page<Ticket> findByPriorityInAndTenantId(@Param("priorities") List<Priority> priorities, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Count tickets by status within tenant
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status AND t.tenantId = :tenantId")
    long countByStatusAndTenantId(@Param("status") TicketStatus status, @Param("tenantId") String tenantId);

    /**
     * Count tickets by priority within tenant
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.priority = :priority AND t.tenantId = :tenantId")
    long countByPriorityAndTenantId(@Param("priority") Priority priority, @Param("tenantId") String tenantId);

    /**
     * Count active tickets within tenant
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN ('CLOSED', 'CANCELLED') AND t.tenantId = :tenantId")
    long countActiveTicketsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Count unassigned tickets within tenant
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assigneeId IS NULL AND t.tenantId = :tenantId")
    long countUnassignedTicketsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Count overdue tickets within tenant
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.dueDate < :now AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED') AND t.tenantId = :tenantId")
    long countOverdueTicketsByTenantId(@Param("now") LocalDateTime now, @Param("tenantId") String tenantId);

    /**
     * Get tickets assigned to user by status within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.assigneeId = :assigneeId AND t.status = :status AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    List<Ticket> findByAssigneeIdAndStatusAndTenantId(@Param("assigneeId") UUID assigneeId, @Param("status") TicketStatus status, @Param("tenantId") String tenantId);

    /**
     * Find tickets with high priority that are unassigned within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.priority IN ('CRITICAL', 'HIGH') AND t.assigneeId IS NULL AND t.status NOT IN ('CLOSED', 'CANCELLED') AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    List<Ticket> findHighPriorityUnassignedTicketsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find recently updated tickets within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.updatedAt > :since AND t.tenantId = :tenantId ORDER BY t.updatedAt DESC")
    List<Ticket> findRecentlyUpdatedTicketsByTenantId(@Param("since") LocalDateTime since, @Param("tenantId") String tenantId);

    /**
     * Find tickets by assignee and status within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.assigneeId = :assigneeId AND t.status IN :statuses AND t.tenantId = :tenantId ORDER BY t.priority ASC, t.createdAt ASC")
    Page<Ticket> findByAssigneeIdAndStatusInAndTenantId(@Param("assigneeId") UUID assigneeId, @Param("statuses") List<TicketStatus> statuses, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Get next ticket number for tenant
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(t.ticketNumber, LOCATE('-', t.ticketNumber) + 1) AS int)), 0) + 1 " +
           "FROM Ticket t WHERE t.tenantId = :tenantId AND t.ticketNumber LIKE CONCAT(:prefix, '-%')")
    Integer getNextTicketNumber(@Param("tenantId") String tenantId, @Param("prefix") String prefix);

    /**
     * Check if ticket number exists within tenant
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.ticketNumber = :ticketNumber AND t.tenantId = :tenantId")
    boolean existsByTicketNumberAndTenantId(@Param("ticketNumber") String ticketNumber, @Param("tenantId") String tenantId);

    /**
     * Find tickets with satisfaction rating within tenant
     */
    @Query("SELECT t FROM Ticket t WHERE t.satisfactionRating IS NOT NULL AND t.tenantId = :tenantId ORDER BY t.closedAt DESC")
    List<Ticket> findTicketsWithSatisfactionRatingByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get average satisfaction rating within tenant
     */
    @Query("SELECT AVG(t.satisfactionRating) FROM Ticket t WHERE t.satisfactionRating IS NOT NULL AND t.tenantId = :tenantId")
    Double getAverageSatisfactionRatingByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get average resolution time in hours within tenant
     */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (t.resolvedAt - t.createdAt)) / 3600) FROM Ticket t WHERE t.resolvedAt IS NOT NULL AND t.tenantId = :tenantId")
    Double getAverageResolutionTimeInHoursByTenantId(@Param("tenantId") String tenantId);
}
