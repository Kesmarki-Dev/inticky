package com.kesmarki.inticky.ticket.service;

import com.kesmarki.inticky.tenant.context.TenantContext;
import com.kesmarki.inticky.ticket.dto.*;
import com.kesmarki.inticky.ticket.entity.Comment;
import com.kesmarki.inticky.ticket.entity.Ticket;
import com.kesmarki.inticky.ticket.enums.CommentType;
import com.kesmarki.inticky.ticket.enums.Priority;
import com.kesmarki.inticky.ticket.enums.TicketStatus;
import com.kesmarki.inticky.ticket.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for ticket management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentService commentService;
    private final TicketNumberService ticketNumberService;

    @Value("${inticky.ticket.default-priority:MEDIUM}")
    private Priority defaultPriority;

    @Value("${inticky.ticket.auto-assign:false}")
    private boolean autoAssign;

    /**
     * Get all tickets within tenant with pagination
     */
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching all tickets for tenant: {} with pagination: {}", tenantId, pageable);
        
        return ticketRepository.findAllByTenantId(tenantId, pageable)
                .map(TicketResponse::fromEntityWithoutRelations);
    }

    /**
     * Get ticket by ID within tenant
     */
    @Cacheable(value = "tickets", key = "#ticketId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public TicketResponse getTicketById(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching ticket by ID: {} for tenant: {}", ticketId, tenantId);
        
        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));
        
        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Get ticket by ticket number within tenant
     */
    @Cacheable(value = "tickets", key = "#ticketNumber + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public TicketResponse getTicketByNumber(String ticketNumber) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching ticket by number: {} for tenant: {}", ticketNumber, tenantId);
        
        Ticket ticket = ticketRepository.findByTicketNumberAndTenantId(ticketNumber, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with number: " + ticketNumber));
        
        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Get active tickets within tenant
     */
    public Page<TicketResponse> getActiveTickets(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching active tickets for tenant: {} with pagination: {}", tenantId, pageable);
        
        return ticketRepository.findActiveTicketsByTenantId(tenantId, pageable)
                .map(TicketResponse::fromEntityWithoutRelations);
    }

    /**
     * Get tickets assigned to user within tenant
     */
    public Page<TicketResponse> getTicketsAssignedToUser(UUID assigneeId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching tickets assigned to user: {} for tenant: {}", assigneeId, tenantId);
        
        return ticketRepository.findByAssigneeIdAndTenantId(assigneeId, tenantId, pageable)
                .map(TicketResponse::fromEntityWithoutRelations);
    }

    /**
     * Get tickets reported by user within tenant
     */
    public Page<TicketResponse> getTicketsReportedByUser(UUID reporterId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching tickets reported by user: {} for tenant: {}", reporterId, tenantId);
        
        return ticketRepository.findByReporterIdAndTenantId(reporterId, tenantId, pageable)
                .map(TicketResponse::fromEntityWithoutRelations);
    }

    /**
     * Get unassigned tickets within tenant
     */
    public Page<TicketResponse> getUnassignedTickets(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching unassigned tickets for tenant: {}", tenantId);
        
        return ticketRepository.findUnassignedTicketsByTenantId(tenantId, pageable)
                .map(TicketResponse::fromEntityWithoutRelations);
    }

    /**
     * Search tickets by keyword within tenant
     */
    public Page<TicketResponse> searchTickets(String keyword, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Searching tickets with keyword: {} for tenant: {}", keyword, tenantId);
        
        return ticketRepository.searchByKeywordAndTenantId(keyword, tenantId, pageable)
                .map(TicketResponse::fromEntityWithoutRelations);
    }

    /**
     * Advanced search tickets with filters
     */
    public Page<TicketResponse> searchTicketsAdvanced(TicketSearchRequest searchRequest, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Advanced search tickets for tenant: {} with filters: {}", tenantId, searchRequest);
        
        // TODO: Implement advanced search with Specification pattern
        // For now, use simple keyword search
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            return searchTickets(searchRequest.getKeyword(), pageable);
        }
        
        return getAllTickets(pageable);
    }

    /**
     * Create new ticket within tenant
     */
    @Transactional
    @CacheEvict(value = "tickets", allEntries = true)
    public TicketResponse createTicket(TicketCreateRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Creating new ticket: {} for tenant: {}", request.getTitle(), tenantId);

        // Generate ticket number
        String ticketNumber = ticketNumberService.generateTicketNumber(tenantId);

        // Create ticket entity
        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : defaultPriority)
                .category(request.getCategory())
                .reporterId(request.getReporterId())
                .reporterName(request.getReporterName())
                .reporterEmail(request.getReporterEmail())
                .assigneeId(request.getAssigneeId())
                .assigneeName(request.getAssigneeName())
                .assigneeEmail(request.getAssigneeEmail())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .tags(convertTagsToString(request.getTags()))
                .customFields(request.getCustomFields())
                .resolution(request.getResolution())
                .build();
        
        ticket.setTenantId(tenantId);
        
        // Calculate SLA breach date
        ticket.calculateSLABreachDate();
        
        ticket = ticketRepository.save(ticket);

        // Create initial system comment
        commentService.createSystemComment(
                ticket.getId(),
                "Ticket created",
                CommentType.SYSTEM,
                request.getReporterId(),
                request.getReporterName()
        );

        log.info("Ticket created successfully: {} ({}) for tenant: {}", 
                ticket.getId(), ticket.getTicketNumber(), tenantId);

        // TODO: Send notification to assignee if assigned
        // TODO: Publish ticket created event
        // TODO: Auto-assign if enabled

        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Update ticket within tenant
     */
    @Transactional
    @CacheEvict(value = "tickets", key = "#ticketId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public TicketResponse updateTicket(UUID ticketId, TicketUpdateRequest request, UUID updatedBy, String updatedByName) {
        String tenantId = TenantContext.getTenantId();
        log.info("Updating ticket: {} for tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        // Track changes for audit
        boolean hasChanges = false;

        // Update basic fields
        if (request.getTitle() != null && !request.getTitle().equals(ticket.getTitle())) {
            ticket.setTitle(request.getTitle());
            hasChanges = true;
        }
        
        if (request.getDescription() != null && !request.getDescription().equals(ticket.getDescription())) {
            ticket.setDescription(request.getDescription());
            hasChanges = true;
        }

        // Update status with tracking
        if (request.getStatus() != null && request.getStatus() != ticket.getStatus()) {
            TicketStatus oldStatus = ticket.getStatus();
            ticket.updateStatus(request.getStatus(), updatedBy);
            
            // Create status change comment
            commentService.createStatusChangeComment(
                    ticketId,
                    oldStatus.toString(),
                    request.getStatus().toString(),
                    updatedBy,
                    updatedByName
            );
            hasChanges = true;
        }

        // Update priority with tracking
        if (request.getPriority() != null && request.getPriority() != ticket.getPriority()) {
            Priority oldPriority = ticket.getPriority();
            ticket.setPriority(request.getPriority());
            
            // Recalculate SLA breach date
            ticket.calculateSLABreachDate();
            
            // Create priority change comment
            commentService.createPriorityChangeComment(
                    ticketId,
                    oldPriority.toString(),
                    request.getPriority().toString(),
                    updatedBy,
                    updatedByName
            );
            hasChanges = true;
        }

        // Update category with tracking
        if (request.getCategory() != null && request.getCategory() != ticket.getCategory()) {
            var oldCategory = ticket.getCategory();
            ticket.setCategory(request.getCategory());
            
            // Create category change comment
            commentService.createCategoryChangeComment(
                    ticketId,
                    oldCategory.toString(),
                    request.getCategory().toString(),
                    updatedBy,
                    updatedByName
            );
            hasChanges = true;
        }

        // Update assignment with tracking
        if (isAssignmentChanged(ticket, request)) {
            String oldAssignee = ticket.getAssigneeName();
            String newAssignee = request.getAssigneeName();
            
            ticket.assignTo(request.getAssigneeId(), request.getAssigneeName(), request.getAssigneeEmail());
            
            // Create assignment change comment
            commentService.createAssignmentChangeComment(
                    ticketId,
                    oldAssignee,
                    newAssignee,
                    updatedBy,
                    updatedByName
            );
            hasChanges = true;
        }

        // Update other fields
        if (request.getDueDate() != null) {
            ticket.setDueDate(request.getDueDate());
            hasChanges = true;
        }
        
        if (request.getEstimatedHours() != null) {
            ticket.setEstimatedHours(request.getEstimatedHours());
            hasChanges = true;
        }
        
        if (request.getActualHours() != null) {
            ticket.setActualHours(request.getActualHours());
            hasChanges = true;
        }
        
        if (request.getTags() != null) {
            ticket.setTags(convertTagsToString(request.getTags()));
            hasChanges = true;
        }
        
        if (request.getCustomFields() != null) {
            ticket.setCustomFields(request.getCustomFields());
            hasChanges = true;
        }
        
        if (request.getResolution() != null) {
            ticket.setResolution(request.getResolution());
            hasChanges = true;
        }
        
        if (request.getSatisfactionRating() != null) {
            ticket.setSatisfactionRating(request.getSatisfactionRating());
            hasChanges = true;
        }
        
        if (request.getCustomerFeedback() != null) {
            ticket.setCustomerFeedback(request.getCustomerFeedback());
            hasChanges = true;
        }

        if (hasChanges) {
            ticket = ticketRepository.save(ticket);
            log.info("Ticket updated successfully: {} for tenant: {}", ticket.getId(), tenantId);
            
            // TODO: Send notifications
            // TODO: Publish ticket updated event
        } else {
            log.debug("No changes detected for ticket: {} in tenant: {}", ticketId, tenantId);
        }

        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Assign ticket to user
     */
    @Transactional
    @CacheEvict(value = "tickets", key = "#ticketId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public TicketResponse assignTicket(UUID ticketId, UUID assigneeId, String assigneeName, String assigneeEmail, UUID assignedBy, String assignedByName) {
        String tenantId = TenantContext.getTenantId();
        log.info("Assigning ticket: {} to user: {} for tenant: {}", ticketId, assigneeId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        String oldAssignee = ticket.getAssigneeName();
        ticket.assignTo(assigneeId, assigneeName, assigneeEmail);
        
        ticket = ticketRepository.save(ticket);

        // Create assignment comment
        commentService.createAssignmentChangeComment(
                ticketId,
                oldAssignee,
                assigneeName,
                assignedBy,
                assignedByName
        );

        log.info("Ticket assigned successfully: {} to {} for tenant: {}", ticketId, assigneeName, tenantId);

        // TODO: Send notification to assignee
        // TODO: Publish ticket assigned event

        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Unassign ticket
     */
    @Transactional
    @CacheEvict(value = "tickets", key = "#ticketId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public TicketResponse unassignTicket(UUID ticketId, UUID unassignedBy, String unassignedByName) {
        String tenantId = TenantContext.getTenantId();
        log.info("Unassigning ticket: {} for tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        String oldAssignee = ticket.getAssigneeName();
        ticket.unassign();
        
        ticket = ticketRepository.save(ticket);

        // Create unassignment comment
        commentService.createAssignmentChangeComment(
                ticketId,
                oldAssignee,
                null,
                unassignedBy,
                unassignedByName
        );

        log.info("Ticket unassigned successfully: {} for tenant: {}", ticketId, tenantId);

        // TODO: Publish ticket unassigned event

        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Escalate ticket
     */
    @Transactional
    @CacheEvict(value = "tickets", key = "#ticketId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public TicketResponse escalateTicket(UUID ticketId, String reason, UUID escalatedBy, String escalatedByName) {
        String tenantId = TenantContext.getTenantId();
        log.info("Escalating ticket: {} for tenant: {} with reason: {}", ticketId, tenantId, reason);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        ticket.escalate();
        ticket = ticketRepository.save(ticket);

        // Create escalation comment
        commentService.createEscalationComment(ticketId, escalatedBy, escalatedByName, reason);

        log.info("Ticket escalated successfully: {} for tenant: {}", ticketId, tenantId);

        // TODO: Send escalation notifications
        // TODO: Publish ticket escalated event

        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Close ticket
     */
    @Transactional
    @CacheEvict(value = "tickets", key = "#ticketId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public TicketResponse closeTicket(UUID ticketId, String resolution, UUID closedBy, String closedByName) {
        String tenantId = TenantContext.getTenantId();
        log.info("Closing ticket: {} for tenant: {}", ticketId, tenantId);

        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.updateStatus(TicketStatus.CLOSED, closedBy);
        
        if (resolution != null && !resolution.trim().isEmpty()) {
            ticket.setResolution(resolution);
        }
        
        ticket = ticketRepository.save(ticket);

        // Create status change comment
        commentService.createStatusChangeComment(
                ticketId,
                oldStatus.toString(),
                TicketStatus.CLOSED.toString(),
                closedBy,
                closedByName
        );

        // Create resolution comment if provided
        if (resolution != null && !resolution.trim().isEmpty()) {
            commentService.createResolutionComment(ticketId, resolution, closedBy, closedByName);
        }

        log.info("Ticket closed successfully: {} for tenant: {}", ticketId, tenantId);

        // TODO: Send closure notification
        // TODO: Publish ticket closed event

        return TicketResponse.fromEntity(ticket, true, true);
    }

    /**
     * Get ticket statistics within tenant
     */
    public Map<String, Object> getTicketStatistics() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching ticket statistics for tenant: {}", tenantId);

        long totalTickets = ticketRepository.countByTenantId(tenantId);
        long activeTickets = ticketRepository.countActiveTicketsByTenantId(tenantId);
        long unassignedTickets = ticketRepository.countUnassignedTicketsByTenantId(tenantId);
        long overdueTickets = ticketRepository.countOverdueTicketsByTenantId(LocalDateTime.now(), tenantId);
        
        long newTickets = ticketRepository.countByStatusAndTenantId(TicketStatus.NEW, tenantId);
        long openTickets = ticketRepository.countByStatusAndTenantId(TicketStatus.OPEN, tenantId);
        long inProgressTickets = ticketRepository.countByStatusAndTenantId(TicketStatus.IN_PROGRESS, tenantId);
        long pendingTickets = ticketRepository.countByStatusAndTenantId(TicketStatus.PENDING, tenantId);
        long resolvedTickets = ticketRepository.countByStatusAndTenantId(TicketStatus.RESOLVED, tenantId);
        long closedTickets = ticketRepository.countByStatusAndTenantId(TicketStatus.CLOSED, tenantId);
        
        long criticalTickets = ticketRepository.countByPriorityAndTenantId(Priority.CRITICAL, tenantId);
        long highTickets = ticketRepository.countByPriorityAndTenantId(Priority.HIGH, tenantId);
        long mediumTickets = ticketRepository.countByPriorityAndTenantId(Priority.MEDIUM, tenantId);
        long lowTickets = ticketRepository.countByPriorityAndTenantId(Priority.LOW, tenantId);

        Double avgSatisfactionRating = ticketRepository.getAverageSatisfactionRatingByTenantId(tenantId);
        Double avgResolutionTimeHours = ticketRepository.getAverageResolutionTimeInHoursByTenantId(tenantId);

        return Map.of(
                "totalTickets", totalTickets,
                "activeTickets", activeTickets,
                "unassignedTickets", unassignedTickets,
                "overdueTickets", overdueTickets,
                "statusBreakdown", Map.of(
                        "new", newTickets,
                        "open", openTickets,
                        "inProgress", inProgressTickets,
                        "pending", pendingTickets,
                        "resolved", resolvedTickets,
                        "closed", closedTickets
                ),
                "priorityBreakdown", Map.of(
                        "critical", criticalTickets,
                        "high", highTickets,
                        "medium", mediumTickets,
                        "low", lowTickets
                ),
                "averageSatisfactionRating", avgSatisfactionRating,
                "averageResolutionTimeHours", avgResolutionTimeHours,
                "tenantId", tenantId,
                "generatedAt", LocalDateTime.now()
        );
    }

    /**
     * Get overdue tickets within tenant
     */
    public List<TicketResponse> getOverdueTickets() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching overdue tickets for tenant: {}", tenantId);
        
        return ticketRepository.findOverdueTicketsByTenantId(LocalDateTime.now(), tenantId)
                .stream()
                .map(TicketResponse::fromEntityWithoutRelations)
                .toList();
    }

    /**
     * Get tickets approaching SLA breach within tenant
     */
    public List<TicketResponse> getTicketsApproachingSLABreach() {
        String tenantId = TenantContext.getTenantId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(2); // 2 hours warning
        
        log.debug("Fetching tickets approaching SLA breach for tenant: {}", tenantId);
        
        return ticketRepository.findTicketsApproachingSLABreach(now, threshold, tenantId)
                .stream()
                .map(TicketResponse::fromEntityWithoutRelations)
                .toList();
    }

    /**
     * Get tickets with SLA breach within tenant
     */
    public List<TicketResponse> getTicketsWithSLABreach() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching tickets with SLA breach for tenant: {}", tenantId);
        
        return ticketRepository.findTicketsWithSLABreach(LocalDateTime.now(), tenantId)
                .stream()
                .map(TicketResponse::fromEntityWithoutRelations)
                .toList();
    }

    /**
     * Check if assignment has changed
     */
    private boolean isAssignmentChanged(Ticket ticket, TicketUpdateRequest request) {
        UUID currentAssigneeId = ticket.getAssigneeId();
        UUID newAssigneeId = request.getAssigneeId();
        
        if (currentAssigneeId == null && newAssigneeId == null) {
            return false;
        }
        
        if (currentAssigneeId == null || newAssigneeId == null) {
            return true;
        }
        
        return !currentAssigneeId.equals(newAssigneeId);
    }

    /**
     * Convert tags list to JSON string
     */
    private String convertTagsToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        
        // Simple JSON array format - in production use proper JSON library
        return "[\"" + String.join("\", \"", tags) + "\"]";
    }
}
