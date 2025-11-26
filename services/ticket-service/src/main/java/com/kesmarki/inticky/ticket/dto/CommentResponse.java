package com.kesmarki.inticky.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kesmarki.inticky.ticket.entity.Comment;
import com.kesmarki.inticky.ticket.enums.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for comment information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private UUID id;
    private UUID ticketId;
    private String content;
    private String shortContent;
    private CommentType type;

    private UUID authorId;
    private String authorName;
    private String authorEmail;

    private Boolean isEdited;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime editedAt;

    private UUID editedBy;
    private Boolean isPinned;

    private Integer timeSpentMinutes;
    private Double timeSpentHours;

    // Change tracking fields
    private String previousStatus;
    private String newStatus;
    private String previousAssignee;
    private String newAssignee;
    private String previousPriority;
    private String newPriority;
    private String previousCategory;
    private String newCategory;

    private String metadata;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed fields
    private Boolean isVisibleToCustomer;
    private Boolean isSystemGenerated;
    private Boolean isUserGenerated;
    private Boolean isChangeComment;
    private Boolean hasTimeTracking;
    private String changeDisplayText;

    /**
     * Convert Comment entity to CommentResponse
     */
    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .content(comment.getContent())
                .shortContent(comment.getShortContent())
                .type(comment.getType())
                .authorId(comment.getAuthorId())
                .authorName(comment.getAuthorName())
                .authorEmail(comment.getAuthorEmail())
                .isEdited(comment.getIsEdited())
                .editedAt(comment.getEditedAt())
                .editedBy(comment.getEditedBy())
                .isPinned(comment.getIsPinned())
                .timeSpentMinutes(comment.getTimeSpentMinutes())
                .timeSpentHours(comment.getTimeSpentHours())
                .previousStatus(comment.getPreviousStatus())
                .newStatus(comment.getNewStatus())
                .previousAssignee(comment.getPreviousAssignee())
                .newAssignee(comment.getNewAssignee())
                .previousPriority(comment.getPreviousPriority())
                .newPriority(comment.getNewPriority())
                .previousCategory(comment.getPreviousCategory())
                .newCategory(comment.getNewCategory())
                .metadata(comment.getMetadata())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                // Computed fields
                .isVisibleToCustomer(comment.isVisibleToCustomer())
                .isSystemGenerated(comment.isSystemGenerated())
                .isUserGenerated(comment.isUserGenerated())
                .isChangeComment(comment.isChangeComment())
                .hasTimeTracking(comment.hasTimeTracking())
                .changeDisplayText(comment.getChangeDisplayText())
                .build();
    }

    /**
     * Get comment type display information
     */
    public CommentTypeInfo getTypeInfo() {
        return CommentTypeInfo.builder()
                .displayName(type.getDisplayName())
                .description(type.getDescription())
                .icon(type.getIcon())
                .colorCode(type.getColorCode())
                .isVisibleToCustomer(type.isVisibleToCustomer())
                .isSystemGenerated(type.isSystemGenerated())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentTypeInfo {
        private String displayName;
        private String description;
        private String icon;
        private String colorCode;
        private Boolean isVisibleToCustomer;
        private Boolean isSystemGenerated;
    }
}
