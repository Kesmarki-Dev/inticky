package com.kesmarki.inticky.ai.entity;

import com.kesmarki.inticky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an AI chat session
 */
@Entity
@Table(name = "ai_sessions", indexes = {
        @Index(name = "idx_ai_session_tenant_user", columnList = "tenant_id, user_id"),
        @Index(name = "idx_ai_session_tenant_active", columnList = "tenant_id, is_active"),
        @Index(name = "idx_ai_session_tenant_created", columnList = "tenant_id, created_at"),
        @Index(name = "idx_ai_session_last_activity", columnList = "last_activity_at")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiSession extends BaseEntity {

    /**
     * User who owns this session
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * User name (denormalized for performance)
     */
    @Column(name = "user_name", length = 255)
    private String userName;

    /**
     * User email (denormalized for performance)
     */
    @Column(name = "user_email", length = 320)
    private String userEmail;

    /**
     * Session title/name
     */
    @Column(name = "title", length = 500)
    private String title;

    /**
     * Session description
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Whether session is currently active
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Last activity timestamp
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    /**
     * Session expiry timestamp
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Total number of messages in this session
     */
    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    /**
     * Session context/metadata (JSON)
     */
    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    /**
     * Session configuration (JSON)
     */
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;

    /**
     * Associated ticket ID (if session is ticket-specific)
     */
    @Column(name = "ticket_id")
    private UUID ticketId;

    /**
     * Session type/category
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", length = 50)
    @Builder.Default
    private SessionType sessionType = SessionType.GENERAL;

    /**
     * Session priority
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    /**
     * Session tags (JSON array)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * Session statistics (JSON)
     */
    @Column(name = "statistics", columnDefinition = "TEXT")
    private String statistics;

    /**
     * Check if session is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if session is active and not expired
     */
    public boolean isActiveAndValid() {
        return isActive && !isExpired();
    }

    /**
     * Update last activity timestamp
     */
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Increment message count
     */
    public void incrementMessageCount() {
        this.messageCount = (this.messageCount != null ? this.messageCount : 0) + 1;
        updateActivity();
    }

    /**
     * Deactivate session
     */
    public void deactivate() {
        this.isActive = false;
        updateActivity();
    }

    /**
     * Extend session expiry
     */
    public void extendExpiry(int minutes) {
        this.expiresAt = LocalDateTime.now().plusMinutes(minutes);
        updateActivity();
    }

    /**
     * Session type enumeration
     */
    public enum SessionType {
        GENERAL,           // General AI chat
        TICKET_SPECIFIC,   // Ticket-related chat
        SUPPORT,          // Support assistance
        ANALYSIS,         // Data analysis
        WORKFLOW,         // Workflow assistance
        TRAINING,         // Training/learning
        DEBUGGING         // Debugging assistance
    }

    /**
     * Session priority enumeration
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }

    /**
     * Get session age in minutes
     */
    public long getAgeInMinutes() {
        return java.time.Duration.between(getCreatedAt(), LocalDateTime.now()).toMinutes();
    }

    /**
     * Get time since last activity in minutes
     */
    public long getInactivityMinutes() {
        if (lastActivityAt == null) {
            return getAgeInMinutes();
        }
        return java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Check if session is inactive for specified minutes
     */
    public boolean isInactiveFor(int minutes) {
        return getInactivityMinutes() >= minutes;
    }

    /**
     * Get display title
     */
    public String getDisplayTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        
        return switch (sessionType) {
            case TICKET_SPECIFIC -> ticketId != null ? "Ticket #" + ticketId : "Ticket Chat";
            case SUPPORT -> "Support Chat";
            case ANALYSIS -> "Data Analysis";
            case WORKFLOW -> "Workflow Assistant";
            case TRAINING -> "Training Session";
            case DEBUGGING -> "Debug Session";
            default -> "AI Chat";
        };
    }
}
