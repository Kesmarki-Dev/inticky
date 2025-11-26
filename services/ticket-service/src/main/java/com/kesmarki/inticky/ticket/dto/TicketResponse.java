package com.kesmarki.inticky.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kesmarki.inticky.ticket.entity.Ticket;
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
import java.util.stream.Collectors;

/**
 * Response DTO for ticket information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private UUID id;
    private String ticketNumber;
    private String title;
    private String description;
    private String shortDescription;
    private TicketStatus status;
    private Priority priority;
    private Category category;

    private UUID reporterId;
    private String reporterName;
    private String reporterEmail;

    private UUID assigneeId;
    private String assigneeName;
    private String assigneeEmail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime slaBreachDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closedAt;

    private UUID resolvedBy;
    private UUID closedBy;

    private Double estimatedHours;
    private Double actualHours;

    private List<String> tags;
    private String customFields;
    private String resolution;

    private Integer satisfactionRating;
    private String customerFeedback;

    private Integer escalationCount;
    private Integer reopenCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed fields
    private Boolean isActive;
    private Boolean isOverdue;
    private Boolean isApproachingSLABreach;
    private Boolean hasSLABreach;
    private Boolean isAssigned;
    private Boolean isUrgent;
    private Long ageInHours;
    private Long resolutionTimeInHours;
    private String displayName;

    // Related data (optional)
    private List<CommentResponse> comments;
    private List<AttachmentResponse> attachments;
    private Long commentCount;
    private Long attachmentCount;

    /**
     * Convert Ticket entity to TicketResponse
     */
    public static TicketResponse fromEntity(Ticket ticket) {
        return fromEntity(ticket, false, false);
    }

    /**
     * Convert Ticket entity to TicketResponse with optional related data
     */
    public static TicketResponse fromEntity(Ticket ticket, boolean includeComments, boolean includeAttachments) {
        TicketResponseBuilder builder = TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .shortDescription(ticket.getShortDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .reporterId(ticket.getReporterId())
                .reporterName(ticket.getReporterName())
                .reporterEmail(ticket.getReporterEmail())
                .assigneeId(ticket.getAssigneeId())
                .assigneeName(ticket.getAssigneeName())
                .assigneeEmail(ticket.getAssigneeEmail())
                .dueDate(ticket.getDueDate())
                .slaBreachDate(ticket.getSlaBreachDate())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .resolvedBy(ticket.getResolvedBy())
                .closedBy(ticket.getClosedBy())
                .estimatedHours(ticket.getEstimatedHours())
                .actualHours(ticket.getActualHours())
                .tags(parseTagsFromString(ticket.getTags()))
                .customFields(ticket.getCustomFields())
                .resolution(ticket.getResolution())
                .satisfactionRating(ticket.getSatisfactionRating())
                .customerFeedback(ticket.getCustomerFeedback())
                .escalationCount(ticket.getEscalationCount())
                .reopenCount(ticket.getReopenCount())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                // Computed fields
                .isActive(ticket.isActive())
                .isOverdue(ticket.isOverdue())
                .isApproachingSLABreach(ticket.isApproachingSLABreach())
                .hasSLABreach(ticket.hasSLABreach())
                .isAssigned(ticket.isAssigned())
                .isUrgent(ticket.isUrgent())
                .ageInHours(ticket.getAgeInHours())
                .resolutionTimeInHours(ticket.getResolutionTimeInHours())
                .displayName(ticket.getDisplayName());

        // Include related data if requested
        if (includeComments && ticket.getComments() != null) {
            builder.comments(ticket.getComments().stream()
                    .map(CommentResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        if (includeAttachments && ticket.getAttachments() != null) {
            builder.attachments(ticket.getAttachments().stream()
                    .map(AttachmentResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        // Set counts
        if (ticket.getComments() != null) {
            builder.commentCount((long) ticket.getComments().size());
        }
        
        if (ticket.getAttachments() != null) {
            builder.attachmentCount((long) ticket.getAttachments().size());
        }

        return builder.build();
    }

    /**
     * Convert Ticket entity to TicketResponse without related data (for performance)
     */
    public static TicketResponse fromEntityWithoutRelations(Ticket ticket) {
        return fromEntity(ticket, false, false);
    }

    /**
     * Parse tags from JSON string
     */
    private static List<String> parseTagsFromString(String tagsJson) {
        if (tagsJson == null || tagsJson.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            // Simple JSON array parsing - in production use proper JSON library
            String cleaned = tagsJson.replaceAll("[\\[\\]\"]", "");
            if (cleaned.trim().isEmpty()) {
                return List.of();
            }
            return List.of(cleaned.split(",\\s*"));
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Get priority display information
     */
    public PriorityInfo getPriorityInfo() {
        return PriorityInfo.builder()
                .level(priority.getLevel())
                .displayName(priority.getDisplayName())
                .colorCode(priority.getColorCode())
                .slaHours(priority.getSlaTime().toHours())
                .build();
    }

    /**
     * Get category display information
     */
    public CategoryInfo getCategoryInfo() {
        return CategoryInfo.builder()
                .displayName(category.getDisplayName())
                .description(category.getDescription())
                .colorCode(category.getColorCode())
                .build();
    }

    /**
     * Get status display information
     */
    public StatusInfo getStatusInfo() {
        return StatusInfo.builder()
                .isActive(status.isActive())
                .isInProgress(status.isInProgress())
                .isPending(status.isPending())
                .isResolved(status.isResolved())
                .isFinal(status.isFinal())
                .nextPossibleStatuses(List.of(status.getNextPossibleStatuses()))
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityInfo {
        private Integer level;
        private String displayName;
        private String colorCode;
        private Long slaHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private String displayName;
        private String description;
        private String colorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusInfo {
        private Boolean isActive;
        private Boolean isInProgress;
        private Boolean isPending;
        private Boolean isResolved;
        private Boolean isFinal;
        private List<TicketStatus> nextPossibleStatuses;
    }
}
