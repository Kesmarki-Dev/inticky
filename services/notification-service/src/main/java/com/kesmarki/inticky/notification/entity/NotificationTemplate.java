package com.kesmarki.inticky.notification.entity;

import com.kesmarki.inticky.common.entity.BaseEntity;
import com.kesmarki.inticky.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a notification template
 */
@Entity
@Table(name = "notification_templates", indexes = {
        @Index(name = "idx_template_tenant_name", columnList = "tenant_id, name"),
        @Index(name = "idx_template_tenant_type", columnList = "tenant_id, notification_type"),
        @Index(name = "idx_template_tenant_active", columnList = "tenant_id, is_active"),
        @Index(name = "idx_template_tenant_event", columnList = "tenant_id, event_type")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotificationTemplate extends BaseEntity {

    /**
     * Template name (unique within tenant)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Template display name
     */
    @Column(name = "display_name", length = 200)
    private String displayName;

    /**
     * Template description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Notification type this template is for
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType;

    /**
     * Event type that triggers this template
     */
    @Column(name = "event_type", length = 100)
    private String eventType;

    /**
     * Template subject (for email/push)
     */
    @Column(name = "subject", length = 500)
    private String subject;

    /**
     * Template body content
     */
    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    /**
     * Template in HTML format (for email)
     */
    @Column(name = "html_body", columnDefinition = "TEXT")
    private String htmlBody;

    /**
     * Template language code
     */
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    /**
     * Whether template is active
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether this is the default template for the event type
     */
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Template variables (JSON)
     */
    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables;

    /**
     * Template metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Template configuration (JSON)
     */
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;

    /**
     * Template version
     */
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    /**
     * Parent template ID (for versioning)
     */
    @Column(name = "parent_template_id")
    private String parentTemplateId;

    /**
     * Template tags (JSON array)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * Usage statistics (JSON)
     */
    @Column(name = "usage_stats", columnDefinition = "TEXT")
    private String usageStats;

    /**
     * Check if template supports HTML
     */
    public boolean supportsHtml() {
        return notificationType == NotificationType.EMAIL && htmlBody != null && !htmlBody.trim().isEmpty();
    }

    /**
     * Check if template is email type
     */
    public boolean isEmailTemplate() {
        return notificationType == NotificationType.EMAIL;
    }

    /**
     * Check if template is push notification type
     */
    public boolean isPushTemplate() {
        return notificationType == NotificationType.PUSH;
    }

    /**
     * Check if template is SMS type
     */
    public boolean isSmsTemplate() {
        return notificationType == NotificationType.SMS;
    }

    /**
     * Check if template is webhook type
     */
    public boolean isWebhookTemplate() {
        return notificationType == NotificationType.WEBHOOK;
    }

    /**
     * Get template identifier for caching
     */
    public String getTemplateKey() {
        return String.format("%s:%s:%s:%s", getTenantId(), notificationType, eventType, language);
    }

    /**
     * Get display name or fallback to name
     */
    public String getDisplayNameOrName() {
        return displayName != null && !displayName.trim().isEmpty() ? displayName : name;
    }

    /**
     * Check if template has subject
     */
    public boolean hasSubject() {
        return subject != null && !subject.trim().isEmpty();
    }

    /**
     * Check if template has body
     */
    public boolean hasBody() {
        return body != null && !body.trim().isEmpty();
    }

    /**
     * Check if template is complete (has required fields)
     */
    public boolean isComplete() {
        if (!hasBody()) {
            return false;
        }
        
        // Email templates require subject
        if (isEmailTemplate() && !hasSubject()) {
            return false;
        }
        
        // Push templates require subject (title)
        if (isPushTemplate() && !hasSubject()) {
            return false;
        }
        
        return true;
    }

    /**
     * Activate template
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate template
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Set as default template
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * Remove default status
     */
    public void removeDefault() {
        this.isDefault = false;
    }

    /**
     * Increment version
     */
    public void incrementVersion() {
        this.version = (this.version != null ? this.version : 0) + 1;
    }
}
