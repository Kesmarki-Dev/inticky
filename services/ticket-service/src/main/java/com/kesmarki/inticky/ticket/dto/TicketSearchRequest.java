package com.kesmarki.inticky.ticket.dto;

import com.kesmarki.inticky.ticket.enums.Category;
import com.kesmarki.inticky.ticket.enums.Priority;
import com.kesmarki.inticky.ticket.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for searching tickets with filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSearchRequest {

    private String keyword;
    private String ticketNumber;
    
    private List<TicketStatus> statuses;
    private List<Priority> priorities;
    private List<Category> categories;
    
    private UUID reporterId;
    private UUID assigneeId;
    
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime updatedAfter;
    private LocalDateTime updatedBefore;
    private LocalDateTime dueAfter;
    private LocalDateTime dueBefore;
    
    private Boolean isOverdue;
    private Boolean isUnassigned;
    private Boolean hasSLABreach;
    private Boolean isApproachingSLABreach;
    
    private List<String> tags;
    private String customFieldsQuery; // JSON query string
    
    private Integer minSatisfactionRating;
    private Integer maxSatisfactionRating;
    
    private Double minEstimatedHours;
    private Double maxEstimatedHours;
    private Double minActualHours;
    private Double maxActualHours;
    
    private Integer minEscalationCount;
    private Integer maxEscalationCount;
    private Integer minReopenCount;
    private Integer maxReopenCount;
}
