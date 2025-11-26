package com.kesmarki.inticky.ticket.service;

import com.kesmarki.inticky.tenant.context.TenantContext;
import com.kesmarki.inticky.ticket.dto.CommentCreateRequest;
import com.kesmarki.inticky.ticket.dto.CommentResponse;
import com.kesmarki.inticky.ticket.entity.Comment;
import com.kesmarki.inticky.ticket.entity.Ticket;
import com.kesmarki.inticky.ticket.enums.CommentType;
import com.kesmarki.inticky.ticket.repository.CommentRepository;
import com.kesmarki.inticky.ticket.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for comment management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    /**
     * Get comments by ticket ID within tenant
     */
    public List<CommentResponse> getCommentsByTicketId(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching comments for ticket: {} in tenant: {}", ticketId, tenantId);
        
        return commentRepository.findByTicketIdAndTenantId(ticketId, tenantId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    /**
     * Get comments by ticket ID within tenant with pagination
     */
    public Page<CommentResponse> getCommentsByTicketId(UUID ticketId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching comments for ticket: {} in tenant: {} with pagination", ticketId, tenantId);
        
        return commentRepository.findByTicketIdAndTenantId(ticketId, tenantId, pageable)
                .map(CommentResponse::fromEntity);
    }

    /**
     * Get public comments by ticket ID within tenant (visible to customer)
     */
    public List<CommentResponse> getPublicCommentsByTicketId(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching public comments for ticket: {} in tenant: {}", ticketId, tenantId);
        
        return commentRepository.findPublicCommentsByTicketIdAndTenantId(ticketId, tenantId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    /**
     * Get comments by author within tenant
     */
    public Page<CommentResponse> getCommentsByAuthor(UUID authorId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching comments by author: {} in tenant: {}", authorId, tenantId);
        
        return commentRepository.findByAuthorIdAndTenantId(authorId, tenantId, pageable)
                .map(CommentResponse::fromEntity);
    }

    /**
     * Get pinned comments by ticket ID within tenant
     */
    public List<CommentResponse> getPinnedCommentsByTicketId(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching pinned comments for ticket: {} in tenant: {}", ticketId, tenantId);
        
        return commentRepository.findPinnedCommentsByTicketIdAndTenantId(ticketId, tenantId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    /**
     * Get time tracking comments by ticket ID within tenant
     */
    public List<CommentResponse> getTimeTrackingCommentsByTicketId(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching time tracking comments for ticket: {} in tenant: {}", ticketId, tenantId);
        
        return commentRepository.findTimeTrackingCommentsByTicketIdAndTenantId(ticketId, tenantId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    /**
     * Create new comment
     */
    @Transactional
    @CacheEvict(value = "tickets", key = "#request.ticketId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public CommentResponse createComment(CommentCreateRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Creating comment for ticket: {} in tenant: {}", request.getTicketId(), tenantId);

        // Verify ticket exists and belongs to tenant
        Ticket ticket = ticketRepository.findByIdAndTenantId(request.getTicketId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + request.getTicketId()));

        // Create comment entity
        Comment comment = Comment.builder()
                .ticket(ticket)
                .content(request.getContent())
                .type(request.getType())
                .authorId(request.getAuthorId())
                .authorName(request.getAuthorName())
                .authorEmail(request.getAuthorEmail())
                .isPinned(request.getIsPinned())
                .timeSpentMinutes(request.getTimeSpentMinutes())
                .metadata(request.getMetadata())
                .build();
        
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        log.info("Comment created successfully: {} for ticket: {} in tenant: {}", 
                comment.getId(), request.getTicketId(), tenantId);

        // TODO: Send notification if public comment
        // TODO: Publish comment created event

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Update comment
     */
    @Transactional
    @CacheEvict(value = "tickets", allEntries = true)
    public CommentResponse updateComment(UUID commentId, String content, UUID editedBy) {
        String tenantId = TenantContext.getTenantId();
        log.info("Updating comment: {} in tenant: {}", commentId, tenantId);

        Comment comment = commentRepository.findByIdAndTenantId(commentId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        comment.setContent(content);
        comment.markAsEdited(editedBy);
        
        comment = commentRepository.save(comment);

        log.info("Comment updated successfully: {} in tenant: {}", commentId, tenantId);

        // TODO: Publish comment updated event

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Pin/unpin comment
     */
    @Transactional
    @CacheEvict(value = "tickets", allEntries = true)
    public CommentResponse toggleCommentPin(UUID commentId, boolean pinned) {
        String tenantId = TenantContext.getTenantId();
        log.info("Toggling pin for comment: {} to {} in tenant: {}", commentId, pinned, tenantId);

        Comment comment = commentRepository.findByIdAndTenantId(commentId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        comment.setPinned(pinned);
        comment = commentRepository.save(comment);

        log.info("Comment pin toggled successfully: {} to {} in tenant: {}", commentId, pinned, tenantId);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Delete comment (soft delete by marking as deleted)
     */
    @Transactional
    @CacheEvict(value = "tickets", allEntries = true)
    public void deleteComment(UUID commentId) {
        String tenantId = TenantContext.getTenantId();
        log.info("Deleting comment: {} in tenant: {}", commentId, tenantId);

        Comment comment = commentRepository.findByIdAndTenantId(commentId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        // For now, actually delete the comment
        // In production, you might want to soft delete
        commentRepository.delete(comment);

        log.info("Comment deleted successfully: {} in tenant: {}", commentId, tenantId);

        // TODO: Publish comment deleted event
    }

    /**
     * Create system comment
     */
    @Transactional
    public CommentResponse createSystemComment(UUID ticketId, String content, CommentType type, UUID authorId, String authorName) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating system comment for ticket: {} in tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .content(content)
                .type(type)
                .authorId(authorId)
                .authorName(authorName)
                .build();
        
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Create status change comment
     */
    @Transactional
    public CommentResponse createStatusChangeComment(UUID ticketId, String previousStatus, String newStatus, UUID authorId, String authorName) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating status change comment for ticket: {} in tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        Comment comment = Comment.createStatusChangeComment(ticket, previousStatus, newStatus, authorId, authorName);
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Create assignment change comment
     */
    @Transactional
    public CommentResponse createAssignmentChangeComment(UUID ticketId, String previousAssignee, String newAssignee, UUID authorId, String authorName) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating assignment change comment for ticket: {} in tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        Comment comment = Comment.createAssignmentChangeComment(ticket, previousAssignee, newAssignee, authorId, authorName);
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Create priority change comment
     */
    @Transactional
    public CommentResponse createPriorityChangeComment(UUID ticketId, String previousPriority, String newPriority, UUID authorId, String authorName) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating priority change comment for ticket: {} in tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        Comment comment = Comment.createPriorityChangeComment(ticket, previousPriority, newPriority, authorId, authorName);
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Create category change comment
     */
    @Transactional
    public CommentResponse createCategoryChangeComment(UUID ticketId, String previousCategory, String newCategory, UUID authorId, String authorName) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating category change comment for ticket: {} in tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .type(CommentType.CATEGORY_CHANGE)
                .content(String.format("Category changed from %s to %s", previousCategory, newCategory))
                .authorId(authorId)
                .authorName(authorName)
                .previousCategory(previousCategory)
                .newCategory(newCategory)
                .build();
        
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Create escalation comment
     */
    @Transactional
    public CommentResponse createEscalationComment(UUID ticketId, UUID authorId, String authorName, String reason) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating escalation comment for ticket: {} in tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        Comment comment = Comment.createEscalationComment(ticket, authorId, authorName, reason);
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Create resolution comment
     */
    @Transactional
    public CommentResponse createResolutionComment(UUID ticketId, String resolution, UUID authorId, String authorName) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating resolution comment for ticket: {} in tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .type(CommentType.RESOLUTION)
                .content("Resolution: " + resolution)
                .authorId(authorId)
                .authorName(authorName)
                .build();
        
        comment.setTenantId(tenantId);
        comment = commentRepository.save(comment);

        return CommentResponse.fromEntity(comment);
    }

    /**
     * Get comment statistics for ticket
     */
    public Map<String, Object> getCommentStatistics(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching comment statistics for ticket: {} in tenant: {}", ticketId, tenantId);

        long totalComments = commentRepository.countByTicketIdAndTenantId(ticketId, tenantId);
        long publicComments = commentRepository.countPublicCommentsByTicketIdAndTenantId(ticketId, tenantId);
        long totalTimeSpent = commentRepository.getTotalTimeSpentMinutesByTicketIdAndTenantId(ticketId, tenantId);

        return Map.of(
                "totalComments", totalComments,
                "publicComments", publicComments,
                "internalComments", totalComments - publicComments,
                "totalTimeSpentMinutes", totalTimeSpent,
                "totalTimeSpentHours", totalTimeSpent / 60.0,
                "ticketId", ticketId,
                "tenantId", tenantId,
                "generatedAt", LocalDateTime.now()
        );
    }

    /**
     * Search comments by content
     */
    public Page<CommentResponse> searchComments(String keyword, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Searching comments with keyword: {} in tenant: {}", keyword, tenantId);
        
        return commentRepository.searchByContentAndTenantId(keyword, tenantId, pageable)
                .map(CommentResponse::fromEntity);
    }

    /**
     * Get recent comments within tenant
     */
    public List<CommentResponse> getRecentComments(int hours) {
        String tenantId = TenantContext.getTenantId();
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        log.debug("Fetching recent comments since {} in tenant: {}", since, tenantId);
        
        return commentRepository.findRecentCommentsByTenantId(since, tenantId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }
}
