package com.kesmarki.inticky.security.context;

import com.kesmarki.inticky.tenant.context.TenantContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

/**
 * Security context that combines Spring Security with tenant information
 */
@Slf4j
@Data
public class TenantSecurityContext {
    
    private String tenantId;
    private String userId;
    private List<String> roles;
    private String email;
    private boolean authenticated;
    
    /**
     * Get current security context
     */
    public static TenantSecurityContext getCurrent() {
        TenantSecurityContext context = new TenantSecurityContext();
        
        // Get tenant information
        context.tenantId = TenantContext.getTenantId();
        context.userId = TenantContext.getUserId();
        
        String rolesString = TenantContext.getUserRoles();
        if (rolesString != null) {
            context.roles = Arrays.asList(rolesString.split(","));
        }
        
        // Get Spring Security authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            context.authenticated = authentication.isAuthenticated();
            if (authentication.getPrincipal() instanceof String) {
                context.email = (String) authentication.getPrincipal();
            }
        }
        
        return context;
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null) {
            return false;
        }
        
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user has all specified roles
     */
    public boolean hasAllRoles(String... roles) {
        if (this.roles == null) {
            return false;
        }
        
        for (String role : roles) {
            if (!this.roles.contains(role)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if context is valid (has tenant and user)
     */
    public boolean isValid() {
        return tenantId != null && userId != null && authenticated;
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if user is agent
     */
    public boolean isAgent() {
        return hasAnyRole("ADMIN", "AGENT");
    }
    
    /**
     * Check if user can access tenant
     */
    public boolean canAccessTenant(String targetTenantId) {
        return tenantId != null && tenantId.equals(targetTenantId);
    }
    
    @Override
    public String toString() {
        return String.format("TenantSecurityContext{tenantId='%s', userId='%s', roles=%s, authenticated=%s}",
                tenantId, userId, roles, authenticated);
    }
}
