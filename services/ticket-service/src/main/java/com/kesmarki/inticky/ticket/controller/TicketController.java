package com.kesmarki.inticky.ticket.controller;

import com.kesmarki.inticky.common.dto.ApiResponse;
import com.kesmarki.inticky.tenant.annotation.TenantAware;
import com.kesmarki.inticky.ticket.dto.*;
import com.kesmarki.inticky.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for ticket management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@TenantAware
@Tag(name = "Ticket Management", description = "Ticket CRUD operations and workflow management")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Get all tickets with pagination
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get all tickets", description = "Retrieve all tickets within tenant with pagination")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAllTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("GET /api/tickets - pageable: {}", pageable);
        
        Page<TicketResponse> tickets = ticketService.getAllTickets(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Tickets retrieved successfully"));
    }

    /**
     * Get ticket by ID
     */
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get ticket by ID", description = "Retrieve ticket information by ID")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @Parameter(description = "Ticket ID") @PathVariable UUID ticketId) {
        
        log.debug("GET /api/tickets/{}", ticketId);
        
        TicketResponse ticket = ticketService.getTicketById(ticketId);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket retrieved successfully"));
    }

    /**
     * Get ticket by ticket number
     */
    @GetMapping("/number/{ticketNumber}")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get ticket by number", description = "Retrieve ticket information by ticket number")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByNumber(
            @Parameter(description = "Ticket number") @PathVariable String ticketNumber) {
        
        log.debug("GET /api/tickets/number/{}", ticketNumber);
        
        TicketResponse ticket = ticketService.getTicketByNumber(ticketNumber);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket retrieved successfully"));
    }

    /**
     * Get active tickets
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get active tickets", description = "Retrieve all active tickets within tenant")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getActiveTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("GET /api/tickets/active - pageable: {}", pageable);
        
        Page<TicketResponse> tickets = ticketService.getActiveTickets(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Active tickets retrieved successfully"));
    }

    /**
     * Get tickets assigned to current user
     */
    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get my assigned tickets", description = "Retrieve tickets assigned to current user")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getMyAssignedTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID currentUserId = getCurrentUserId();
        log.debug("GET /api/tickets/assigned-to-me - user: {} pageable: {}", currentUserId, pageable);
        
        Page<TicketResponse> tickets = ticketService.getTicketsAssignedToUser(currentUserId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Assigned tickets retrieved successfully"));
    }

    /**
     * Get tickets assigned to specific user
     */
    @GetMapping("/assigned-to/{assigneeId}")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get tickets assigned to user", description = "Retrieve tickets assigned to specific user")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getTicketsAssignedToUser(
            @Parameter(description = "Assignee user ID") @PathVariable UUID assigneeId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("GET /api/tickets/assigned-to/{} - pageable: {}", assigneeId, pageable);
        
        Page<TicketResponse> tickets = ticketService.getTicketsAssignedToUser(assigneeId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Assigned tickets retrieved successfully"));
    }

    /**
     * Get tickets reported by current user
     */
    @GetMapping("/reported-by-me")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get my reported tickets", description = "Retrieve tickets reported by current user")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getMyReportedTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID currentUserId = getCurrentUserId();
        log.debug("GET /api/tickets/reported-by-me - user: {} pageable: {}", currentUserId, pageable);
        
        Page<TicketResponse> tickets = ticketService.getTicketsReportedByUser(currentUserId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Reported tickets retrieved successfully"));
    }

    /**
     * Get tickets reported by specific user
     */
    @GetMapping("/reported-by/{reporterId}")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get tickets reported by user", description = "Retrieve tickets reported by specific user")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getTicketsReportedByUser(
            @Parameter(description = "Reporter user ID") @PathVariable UUID reporterId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("GET /api/tickets/reported-by/{} - pageable: {}", reporterId, pageable);
        
        Page<TicketResponse> tickets = ticketService.getTicketsReportedByUser(reporterId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Reported tickets retrieved successfully"));
    }

    /**
     * Get unassigned tickets
     */
    @GetMapping("/unassigned")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get unassigned tickets", description = "Retrieve all unassigned tickets")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getUnassignedTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("GET /api/tickets/unassigned - pageable: {}", pageable);
        
        Page<TicketResponse> tickets = ticketService.getUnassignedTickets(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Unassigned tickets retrieved successfully"));
    }

    /**
     * Search tickets by keyword
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Search tickets", description = "Search tickets by keyword")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> searchTickets(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("GET /api/tickets/search?keyword={} - pageable: {}", keyword, pageable);
        
        Page<TicketResponse> tickets = ticketService.searchTickets(keyword, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Ticket search completed successfully"));
    }

    /**
     * Advanced search tickets with filters
     */
    @PostMapping("/search/advanced")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Advanced search tickets", description = "Search tickets with advanced filters")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> searchTicketsAdvanced(
            @Valid @RequestBody TicketSearchRequest searchRequest,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("POST /api/tickets/search/advanced - request: {} pageable: {}", searchRequest, pageable);
        
        Page<TicketResponse> tickets = ticketService.searchTicketsAdvanced(searchRequest, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Advanced ticket search completed successfully"));
    }

    /**
     * Create new ticket
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ticket:create')")
    @Operation(summary = "Create ticket", description = "Create a new ticket")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody TicketCreateRequest request) {
        
        log.info("POST /api/tickets - title: {}", request.getTitle());
        
        TicketResponse ticket = ticketService.createTicket(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ticket, "Ticket created successfully"));
    }

    /**
     * Update ticket
     */
    @PutMapping("/{ticketId}")
    @PreAuthorize("hasAuthority('ticket:update')")
    @Operation(summary = "Update ticket", description = "Update ticket information")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID ticketId,
            @Valid @RequestBody TicketUpdateRequest request) {
        
        UUID currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        
        log.info("PUT /api/tickets/{} - request: {} by user: {}", ticketId, request, currentUserId);
        
        TicketResponse ticket = ticketService.updateTicket(ticketId, request, currentUserId, currentUserName);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket updated successfully"));
    }

    /**
     * Assign ticket to user
     */
    @PutMapping("/{ticketId}/assign")
    @PreAuthorize("hasAuthority('ticket:assign')")
    @Operation(summary = "Assign ticket", description = "Assign ticket to a user")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID ticketId,
            @Parameter(description = "Assignee user ID") @RequestParam UUID assigneeId,
            @Parameter(description = "Assignee name") @RequestParam String assigneeName,
            @Parameter(description = "Assignee email") @RequestParam(required = false) String assigneeEmail) {
        
        UUID currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        
        log.info("PUT /api/tickets/{}/assign - assignee: {} by user: {}", ticketId, assigneeId, currentUserId);
        
        TicketResponse ticket = ticketService.assignTicket(ticketId, assigneeId, assigneeName, assigneeEmail, currentUserId, currentUserName);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket assigned successfully"));
    }

    /**
     * Assign ticket to current user
     */
    @PutMapping("/{ticketId}/assign-to-me")
    @PreAuthorize("hasAuthority('ticket:assign')")
    @Operation(summary = "Assign ticket to me", description = "Assign ticket to current user")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicketToMe(
            @Parameter(description = "Ticket ID") @PathVariable UUID ticketId) {
        
        UUID currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        String currentUserEmail = getCurrentUserEmail();
        
        log.info("PUT /api/tickets/{}/assign-to-me - user: {}", ticketId, currentUserId);
        
        TicketResponse ticket = ticketService.assignTicket(ticketId, currentUserId, currentUserName, currentUserEmail, currentUserId, currentUserName);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket assigned to you successfully"));
    }

    /**
     * Unassign ticket
     */
    @PutMapping("/{ticketId}/unassign")
    @PreAuthorize("hasAuthority('ticket:assign')")
    @Operation(summary = "Unassign ticket", description = "Remove assignment from ticket")
    public ResponseEntity<ApiResponse<TicketResponse>> unassignTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID ticketId) {
        
        UUID currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        
        log.info("PUT /api/tickets/{}/unassign - by user: {}", ticketId, currentUserId);
        
        TicketResponse ticket = ticketService.unassignTicket(ticketId, currentUserId, currentUserName);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket unassigned successfully"));
    }

    /**
     * Escalate ticket
     */
    @PutMapping("/{ticketId}/escalate")
    @PreAuthorize("hasAuthority('ticket:escalate')")
    @Operation(summary = "Escalate ticket", description = "Escalate ticket priority and notify supervisors")
    public ResponseEntity<ApiResponse<TicketResponse>> escalateTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID ticketId,
            @Parameter(description = "Escalation reason") @RequestParam(required = false) String reason) {
        
        UUID currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        
        log.info("PUT /api/tickets/{}/escalate - reason: {} by user: {}", ticketId, reason, currentUserId);
        
        TicketResponse ticket = ticketService.escalateTicket(ticketId, reason, currentUserId, currentUserName);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket escalated successfully"));
    }

    /**
     * Close ticket
     */
    @PutMapping("/{ticketId}/close")
    @PreAuthorize("hasAuthority('ticket:close')")
    @Operation(summary = "Close ticket", description = "Close ticket with resolution")
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID ticketId,
            @Parameter(description = "Resolution details") @RequestParam(required = false) String resolution) {
        
        UUID currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        
        log.info("PUT /api/tickets/{}/close - resolution: {} by user: {}", ticketId, resolution, currentUserId);
        
        TicketResponse ticket = ticketService.closeTicket(ticketId, resolution, currentUserId, currentUserName);
        
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket closed successfully"));
    }

    /**
     * Get ticket statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get ticket statistics", description = "Get ticket statistics within tenant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketStatistics() {
        
        log.debug("GET /api/tickets/statistics");
        
        Map<String, Object> statistics = ticketService.getTicketStatistics();
        
        return ResponseEntity.ok(ApiResponse.success(statistics, "Ticket statistics retrieved successfully"));
    }

    /**
     * Get overdue tickets
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get overdue tickets", description = "Get all overdue tickets")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getOverdueTickets() {
        
        log.debug("GET /api/tickets/overdue");
        
        List<TicketResponse> tickets = ticketService.getOverdueTickets();
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "Overdue tickets retrieved successfully"));
    }

    /**
     * Get tickets approaching SLA breach
     */
    @GetMapping("/sla-warning")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get SLA warning tickets", description = "Get tickets approaching SLA breach")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsApproachingSLABreach() {
        
        log.debug("GET /api/tickets/sla-warning");
        
        List<TicketResponse> tickets = ticketService.getTicketsApproachingSLABreach();
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "SLA warning tickets retrieved successfully"));
    }

    /**
     * Get tickets with SLA breach
     */
    @GetMapping("/sla-breach")
    @PreAuthorize("hasAuthority('ticket:read')")
    @Operation(summary = "Get SLA breach tickets", description = "Get tickets with SLA breach")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsWithSLABreach() {
        
        log.debug("GET /api/tickets/sla-breach");
        
        List<TicketResponse> tickets = ticketService.getTicketsWithSLABreach();
        
        return ResponseEntity.ok(ApiResponse.success(tickets, "SLA breach tickets retrieved successfully"));
    }

    /**
     * Get current user ID from security context
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return UUID.fromString((String) authentication.getPrincipal());
        }
        throw new IllegalStateException("No authenticated user found");
    }

    /**
     * Get current user name from security context
     */
    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "Unknown User";
    }

    /**
     * Get current user email from security context
     */
    private String getCurrentUserEmail() {
        // TODO: Extract email from JWT token or user details
        return null;
    }
}
