package com.kesmarki.inticky.user.controller;

import com.kesmarki.inticky.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock Ticket Controller for dashboard functionality
 * This will be replaced by the actual Ticket Service
 */
@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets (Mock)", description = "Mock ticket operations for dashboard")
public class TicketController {

    /**
     * Get ticket statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get ticket statistics", description = "Get ticket statistics for dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketStats() {
        log.debug("GET /api/tickets/stats");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTickets", 150L);
        stats.put("openTickets", 45L);
        stats.put("inProgressTickets", 32L);
        stats.put("closedTickets", 73L);
        stats.put("highPriorityTickets", 12L);
        stats.put("averageResolutionTime", "2.5 hours");
        stats.put("customerSatisfaction", 4.2);
        stats.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get tickets with pagination
     */
    @GetMapping
    @Operation(summary = "Get tickets", description = "Get paginated list of tickets")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search) {
        
        log.debug("GET /api/tickets - page: {}, size: {}, status: {}, priority: {}, search: {}", 
                page, size, status, priority, search);
        
        // Mock ticket data
        List<Map<String, Object>> tickets = Arrays.asList(
            createMockTicket("TICKET-001", "Login issue", "HIGH", "OPEN", "John Doe", "Unable to login to the system"),
            createMockTicket("TICKET-002", "Password reset", "MEDIUM", "IN_PROGRESS", "Jane Smith", "Need to reset password"),
            createMockTicket("TICKET-003", "Feature request", "LOW", "CLOSED", "Bob Johnson", "Request for new dashboard feature"),
            createMockTicket("TICKET-004", "Bug report", "HIGH", "OPEN", "Alice Brown", "Application crashes on startup"),
            createMockTicket("TICKET-005", "Performance issue", "MEDIUM", "IN_PROGRESS", "Charlie Wilson", "Slow page loading times")
        );
        
        // Simple filtering
        if (status != null) {
            tickets = tickets.stream()
                    .filter(ticket -> status.equalsIgnoreCase((String) ticket.get("status")))
                    .toList();
        }
        
        if (priority != null) {
            tickets = tickets.stream()
                    .filter(ticket -> priority.equalsIgnoreCase((String) ticket.get("priority")))
                    .toList();
        }
        
        if (search != null && !search.trim().isEmpty()) {
            tickets = tickets.stream()
                    .filter(ticket -> 
                        ((String) ticket.get("title")).toLowerCase().contains(search.toLowerCase()) ||
                        ((String) ticket.get("description")).toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }
        
        // Pagination
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tickets.size());
        
        List<Map<String, Object>> pageContent = tickets.subList(start, end);
        Page<Map<String, Object>> ticketPage = new PageImpl<>(pageContent, pageable, tickets.size());
        
        return ResponseEntity.ok(ApiResponse.success(ticketPage));
    }

    /**
     * Get ticket by ID
     */
    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket by ID", description = "Get ticket details by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketById(@PathVariable String ticketId) {
        log.debug("GET /api/tickets/{}", ticketId);
        
        Map<String, Object> ticket = createMockTicket(
            ticketId, 
            "Sample Ticket", 
            "MEDIUM", 
            "OPEN", 
            "Demo User", 
            "This is a sample ticket for demonstration purposes"
        );
        
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    /**
     * Create mock ticket data
     */
    private Map<String, Object> createMockTicket(String id, String title, String priority, 
                                                String status, String assignee, String description) {
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("id", id);
        ticket.put("title", title);
        ticket.put("description", description);
        ticket.put("priority", priority);
        ticket.put("status", status);
        ticket.put("assignee", assignee);
        ticket.put("reporter", "Demo Reporter");
        ticket.put("category", "TECHNICAL");
        ticket.put("createdAt", LocalDateTime.now().minusDays(new Random().nextInt(30)));
        ticket.put("updatedAt", LocalDateTime.now().minusHours(new Random().nextInt(24)));
        ticket.put("dueDate", LocalDateTime.now().plusDays(new Random().nextInt(7) + 1));
        
        return ticket;
    }
}
