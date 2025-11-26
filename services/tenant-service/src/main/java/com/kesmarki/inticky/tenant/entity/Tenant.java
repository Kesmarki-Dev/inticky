package com.kesmarki.inticky.tenant.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kesmarki.inticky.tenant.enums.TenantPlan;
import com.kesmarki.inticky.tenant.enums.TenantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Tenant entity representing an organization in the multi-tenant system
 */
@Entity
@Table(name = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @NotBlank(message = "Tenant name is required")
    @Size(max = 500, message = "Tenant name must not exceed 500 characters")
    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @NotBlank(message = "Domain is required")
    @Size(max = 255, message = "Domain must not exceed 255 characters")
    @Column(name = "domain", nullable = false, unique = true, length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 50)
    @Builder.Default
    private TenantPlan plan = TenantPlan.BASIC;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> settings = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Helper methods for settings management
    public void setSetting(String key, Object value) {
        if (settings == null) {
            settings = new HashMap<>();
        }
        settings.put(key, value);
    }

    public Object getSetting(String key) {
        return settings != null ? settings.get(key) : null;
    }

    public <T> T getSetting(String key, Class<T> type) {
        Object value = getSetting(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public String getSettingAsString(String key, String defaultValue) {
        Object value = getSetting(key);
        return value != null ? value.toString() : defaultValue;
    }

    public Integer getSettingAsInteger(String key, Integer defaultValue) {
        Object value = getSetting(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    public Boolean getSettingAsBoolean(String key, Boolean defaultValue) {
        Object value = getSetting(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    // Business logic methods
    public boolean isActive() {
        return TenantStatus.ACTIVE.equals(status);
    }

    public boolean isSuspended() {
        return TenantStatus.SUSPENDED.equals(status);
    }

    public boolean isTrial() {
        return TenantStatus.TRIAL.equals(status);
    }

    public boolean isPremiumOrHigher() {
        return TenantPlan.PREMIUM.equals(plan) || TenantPlan.ENTERPRISE.equals(plan);
    }

    public boolean isEnterprise() {
        return TenantPlan.ENTERPRISE.equals(plan);
    }

    @Override
    public String toString() {
        return String.format("Tenant{id='%s', name='%s', domain='%s', status=%s, plan=%s}",
                id, name, domain, status, plan);
    }
}
