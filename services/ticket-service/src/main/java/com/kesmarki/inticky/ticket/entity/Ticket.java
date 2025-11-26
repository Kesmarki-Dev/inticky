package com.kesmarki.inticky.ticket.entity;

import com.kesmarki.inticky.common.entity.BaseEntity;
import com.kesmarki.inticky.ticket.enums.Category;
import com.kesmarki.inticky.ticket.enums.Priority;
import com.kesmarki.inticky.ticket.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a support ticket
 */
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_ticket_tenant_priority", columnList = "tenant_id, priority"),
        @Index(name = "idx_ticket_tenant_category", columnList = "tenant_id, category"),
        @Index(name = "idx_ticket_tenant_assignee", columnList = "tenant_id, assignee_id"),
        @Index(name = "idx_ticket_tenant_reporter", columnList = "tenant_id, reporter_id"),
        @Index(name = "idx_ticket_tenant_created", columnList = "tenant_id, created_at"),
        @Index(name = "idx_ticket_tenant_due_date", columnList = "tenant_id, due_date"),
        @Index(name = "idx_ticket_number", columnList = "ticket_number", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Ticket extends BaseEntity {

    /**
     * Unique ticket number for display (e.g., TICK-001)
     */
    @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
    private String ticketNumber;

    /**
     * Ticket title/subject
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * Detailed description of the issue
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Current status of the ticket
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.NEW;

    /**
     * Priority level of the ticket
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Category of the ticket
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @Builder.Default
    private Category category = Category.SUPPORT;

    /**
     * ID of the user who reported the ticket
     */
    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    /**
     * Name of the reporter (denormalized for performance)
     */
    @Column(name = "reporter_name", length = 255)
    private String reporterName;

    /**
     * Email of the reporter (denormalized for performance)
     */
    @Column(name = "reporter_email", length = 320)
    private String reporterEmail;

    /**
     * ID of the user assigned to handle the ticket
     */
    @Column(name = "assignee_id")
    private UUID assigneeId;

    /**
     * Name of the assignee (denormalized for performance)
     */
    @Column(name = "assignee_name", length = 255)
    private String assigneeName;

    /**
     * Email of the assignee (denormalized for performance)
     */
    @Column(name = "assignee_email", length = 320)
    private String assigneeEmail;

    /**
     * Due date for ticket resolution
     */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /**
     * SLA breach date (calculated based on priority)
     */
    @Column(name = "sla_breach_date")
    private LocalDateTime slaBreachDate;

    /**
     * Date when ticket was resolved
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Date when ticket was closed
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * ID of the user who resolved the ticket
     */
    @Column(name = "resolved_by")
    private UUID resolvedBy;

    /**
     * ID of the user who closed the ticket
     */
    @Column(name = "closed_by")
    private UUID closedBy;

    /**
     * Estimated hours to resolve the ticket
     */
    @Column(name = "estimated_hours")
    private Double estimatedHours;

    /**
     * Actual hours spent on the ticket
     */
    @Column(name = "actual_hours")
    private Double actualHours;

    /**
     * Tags associated with the ticket (JSON array)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * Custom fields (JSON object)
     */
    @Column(name = "custom_fields", columnDefinition = "TEXT")
    private String customFields;

    /**
     * Resolution details
     */
    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    /**
     * Customer satisfaction rating (1-5)
     */
    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;

    /**
     * Customer feedback
     */
    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    /**
     * Number of times ticket was escalated
     */
    @Column(name = "escalation_count")
    @Builder.Default
    private Integer escalationCount = 0;

    /**
     * Number of times ticket was reopened
     */
    @Column(name = "reopen_count")
    @Builder.Default
    private Integer reopenCount = 0;

    /**
     * Comments associated with this ticket
     */
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    /**
     * Attachments associated with this ticket
     */
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Check if ticket is active (not closed or cancelled)
     */
    public boolean isActive() {
        return status.isActive();
    }

    /**
     * Check if ticket is overdue
     */
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && isActive();
    }

    /**
     * Check if ticket is approaching SLA breach
     */
    public boolean isApproachingSLABreach() {
        return slaBreachDate != null && 
               LocalDateTime.now().isAfter(slaBreachDate.minusHours(2)) && 
               isActive();
    }

    /**
     * Check if ticket has breached SLA
     */
    public boolean hasSLABreach() {
        return slaBreachDate != null && LocalDateTime.now().isAfter(slaBreachDate) && isActive();
    }

    /**
     * Check if ticket is assigned
     */
    public boolean isAssigned() {
        return assigneeId != null;
    }

    /**
     * Check if ticket is unassigned
     */
    public boolean isUnassigned() {
        return assigneeId == null;
    }

    /**
     * Check if ticket is urgent (critical or high priority)
     */
    public boolean isUrgent() {
        return priority.isUrgent();
    }

    /**
     * Get age of ticket in hours
     */
    public long getAgeInHours() {
        return java.time.Duration.between(getCreatedAt(), LocalDateTime.now()).toHours();
    }

    /**
     * Get time to resolution in hours (if resolved)
     */
    public Long getResolutionTimeInHours() {
        if (resolvedAt != null) {
            return java.time.Duration.between(getCreatedAt(), resolvedAt).toHours();
        }
        return null;
    }

    /**
     * Add comment to ticket
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setTicket(this);
    }

    /**
     * Add attachment to ticket
     */
    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setTicket(this);
    }

    /**
     * Update status and set timestamps
     */
    public void updateStatus(TicketStatus newStatus, UUID updatedBy) {
        TicketStatus oldStatus = this.status;
        this.status = newStatus;

        // Set timestamps based on status
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case RESOLVED -> {
                this.resolvedAt = now;
                this.resolvedBy = updatedBy;
            }
            case CLOSED -> {
                this.closedAt = now;
                this.closedBy = updatedBy;
                if (this.resolvedAt == null) {
                    this.resolvedAt = now;
                    this.resolvedBy = updatedBy;
                }
            }
            case OPEN -> {
                // If reopening from closed/cancelled
                if (oldStatus == TicketStatus.CLOSED || oldStatus == TicketStatus.CANCELLED) {
                    this.reopenCount++;
                    this.resolvedAt = null;
                    this.closedAt = null;
                    this.resolvedBy = null;
                    this.closedBy = null;
                }
            }
        }
    }

    /**
     * Assign ticket to user
     */
    public void assignTo(UUID assigneeId, String assigneeName, String assigneeEmail) {
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.assigneeEmail = assigneeEmail;
    }

    /**
     * Unassign ticket
     */
    public void unassign() {
        this.assigneeId = null;
        this.assigneeName = null;
        this.assigneeEmail = null;
    }

    /**
     * Escalate ticket
     */
    public void escalate() {
        this.escalationCount++;
        // Increase priority if not already at highest
        if (this.priority != Priority.CRITICAL) {
            this.priority = Priority.values()[this.priority.ordinal() - 1];
        }
    }

    /**
     * Calculate and set SLA breach date based on priority
     */
    public void calculateSLABreachDate() {
        if (this.priority != null && this.getCreatedAt() != null) {
            this.slaBreachDate = this.getCreatedAt().plus(this.priority.getSlaTime());
        }
    }

    /**
     * Get display name for ticket (ticket number + title)
     */
    public String getDisplayName() {
        return ticketNumber + " - " + title;
    }

    /**
     * Get short description (first 100 characters)
     */
    public String getShortDescription() {
        if (description == null || description.length() <= 100) {
            return description;
        }
        return description.substring(0, 97) + "...";
    }
}
