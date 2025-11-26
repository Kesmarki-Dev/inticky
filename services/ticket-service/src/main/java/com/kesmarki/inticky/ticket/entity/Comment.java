package com.kesmarki.inticky.ticket.entity;

import com.kesmarki.inticky.common.entity.BaseEntity;
import com.kesmarki.inticky.ticket.enums.CommentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a comment on a ticket
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_tenant_ticket", columnList = "tenant_id, ticket_id"),
        @Index(name = "idx_comment_tenant_author", columnList = "tenant_id, author_id"),
        @Index(name = "idx_comment_tenant_type", columnList = "tenant_id, type"),
        @Index(name = "idx_comment_tenant_created", columnList = "tenant_id, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true, exclude = {"ticket"})
public class Comment extends BaseEntity {

    /**
     * The ticket this comment belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /**
     * Content of the comment
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Type of comment (public, internal, system, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private CommentType type = CommentType.PUBLIC;

    /**
     * ID of the user who authored the comment
     */
    @Column(name = "author_id")
    private UUID authorId;

    /**
     * Name of the author (denormalized for performance)
     */
    @Column(name = "author_name", length = 255)
    private String authorName;

    /**
     * Email of the author (denormalized for performance)
     */
    @Column(name = "author_email", length = 320)
    private String authorEmail;

    /**
     * Whether this comment was edited
     */
    @Column(name = "is_edited")
    @Builder.Default
    private Boolean isEdited = false;

    /**
     * When the comment was last edited
     */
    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    /**
     * Who edited the comment
     */
    @Column(name = "edited_by")
    private UUID editedBy;

    /**
     * Whether this comment is pinned
     */
    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    /**
     * Time spent on ticket (in minutes) - for time tracking
     */
    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;

    /**
     * Previous status (for status change comments)
     */
    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    /**
     * New status (for status change comments)
     */
    @Column(name = "new_status", length = 20)
    private String newStatus;

    /**
     * Previous assignee (for assignment change comments)
     */
    @Column(name = "previous_assignee", length = 255)
    private String previousAssignee;

    /**
     * New assignee (for assignment change comments)
     */
    @Column(name = "new_assignee", length = 255)
    private String newAssignee;

    /**
     * Previous priority (for priority change comments)
     */
    @Column(name = "previous_priority", length = 20)
    private String previousPriority;

    /**
     * New priority (for priority change comments)
     */
    @Column(name = "new_priority", length = 20)
    private String newPriority;

    /**
     * Previous category (for category change comments)
     */
    @Column(name = "previous_category", length = 30)
    private String previousCategory;

    /**
     * New category (for category change comments)
     */
    @Column(name = "new_category", length = 30)
    private String newCategory;

    /**
     * Additional metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Check if comment is visible to customer
     */
    public boolean isVisibleToCustomer() {
        return type.isVisibleToCustomer();
    }

    /**
     * Check if comment is system generated
     */
    public boolean isSystemGenerated() {
        return type.isSystemGenerated();
    }

    /**
     * Check if comment is user generated
     */
    public boolean isUserGenerated() {
        return type.isUserGenerated();
    }

    /**
     * Check if comment represents a change
     */
    public boolean isChangeComment() {
        return type.isChangeComment();
    }

    /**
     * Check if comment has time tracking information
     */
    public boolean hasTimeTracking() {
        return timeSpentMinutes != null && timeSpentMinutes > 0;
    }

    /**
     * Get time spent in hours
     */
    public Double getTimeSpentHours() {
        if (timeSpentMinutes == null) {
            return null;
        }
        return timeSpentMinutes / 60.0;
    }

    /**
     * Mark comment as edited
     */
    public void markAsEdited(UUID editedBy) {
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
        this.editedBy = editedBy;
    }

    /**
     * Pin/unpin comment
     */
    public void setPinned(boolean pinned) {
        this.isPinned = pinned;
    }

    /**
     * Get short content (first 100 characters)
     */
    public String getShortContent() {
        if (content == null || content.length() <= 100) {
            return content;
        }
        return content.substring(0, 97) + "...";
    }

    /**
     * Get display text for change comments
     */
    public String getChangeDisplayText() {
        return switch (type) {
            case STATUS_CHANGE -> String.format("Status changed from %s to %s", previousStatus, newStatus);
            case ASSIGNMENT -> {
                if (previousAssignee == null && newAssignee != null) {
                    yield String.format("Assigned to %s", newAssignee);
                } else if (previousAssignee != null && newAssignee == null) {
                    yield String.format("Unassigned from %s", previousAssignee);
                } else {
                    yield String.format("Reassigned from %s to %s", previousAssignee, newAssignee);
                }
            }
            case PRIORITY_CHANGE -> String.format("Priority changed from %s to %s", previousPriority, newPriority);
            case CATEGORY_CHANGE -> String.format("Category changed from %s to %s", previousCategory, newCategory);
            case ESCALATION -> "Ticket escalated";
            default -> content;
        };
    }

    /**
     * Create status change comment
     */
    public static Comment createStatusChangeComment(Ticket ticket, String previousStatus, String newStatus, UUID authorId, String authorName) {
        return Comment.builder()
                .ticket(ticket)
                .type(CommentType.STATUS_CHANGE)
                .content(String.format("Status changed from %s to %s", previousStatus, newStatus))
                .authorId(authorId)
                .authorName(authorName)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .build();
    }

    /**
     * Create assignment change comment
     */
    public static Comment createAssignmentChangeComment(Ticket ticket, String previousAssignee, String newAssignee, UUID authorId, String authorName) {
        String content;
        if (previousAssignee == null && newAssignee != null) {
            content = String.format("Assigned to %s", newAssignee);
        } else if (previousAssignee != null && newAssignee == null) {
            content = String.format("Unassigned from %s", previousAssignee);
        } else {
            content = String.format("Reassigned from %s to %s", previousAssignee, newAssignee);
        }

        return Comment.builder()
                .ticket(ticket)
                .type(CommentType.ASSIGNMENT)
                .content(content)
                .authorId(authorId)
                .authorName(authorName)
                .previousAssignee(previousAssignee)
                .newAssignee(newAssignee)
                .build();
    }

    /**
     * Create priority change comment
     */
    public static Comment createPriorityChangeComment(Ticket ticket, String previousPriority, String newPriority, UUID authorId, String authorName) {
        return Comment.builder()
                .ticket(ticket)
                .type(CommentType.PRIORITY_CHANGE)
                .content(String.format("Priority changed from %s to %s", previousPriority, newPriority))
                .authorId(authorId)
                .authorName(authorName)
                .previousPriority(previousPriority)
                .newPriority(newPriority)
                .build();
    }

    /**
     * Create escalation comment
     */
    public static Comment createEscalationComment(Ticket ticket, UUID authorId, String authorName, String reason) {
        return Comment.builder()
                .ticket(ticket)
                .type(CommentType.ESCALATION)
                .content("Ticket escalated" + (reason != null ? ": " + reason : ""))
                .authorId(authorId)
                .authorName(authorName)
                .build();
    }
}
