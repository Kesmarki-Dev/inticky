package com.kesmarki.inticky.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Role entity representing user roles
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name"),
    @Index(name = "idx_role_tenant", columnList = "tenant_id"),
    @Index(name = "idx_role_name_tenant", columnList = "name, tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"userRoles", "rolePermissions"})
@EqualsAndHashCode(of = "id")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId; // null for system-wide roles

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    @Builder.Default
    private Boolean isSystemRole = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // Relationships
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();

    // Helper methods
    public boolean isSystemRole() {
        return Boolean.TRUE.equals(isSystemRole);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void addPermission(Permission permission) {
        RolePermission rolePermission = RolePermission.builder()
                .role(this)
                .permission(permission)
                .assignedAt(LocalDateTime.now())
                .build();
        rolePermissions.add(rolePermission);
    }

    public void removePermission(Permission permission) {
        rolePermissions.removeIf(rolePermission -> rolePermission.getPermission().equals(permission));
    }

    public Set<Permission> getPermissions() {
        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasPermission(String permissionName) {
        return rolePermissions.stream()
                .anyMatch(rolePermission -> rolePermission.getPermission().getName().equals(permissionName));
    }
}
