package com.kesmarki.inticky.tenant.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for tenant context information
 */
@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLES = new ThreadLocal<>();
    
    /**
     * Set the current tenant ID
     */
    public static void setTenantId(String tenantId) {
        log.debug("Setting tenant ID: {}", tenantId);
        TENANT_ID.set(tenantId);
    }
    
    /**
     * Get the current tenant ID
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    /**
     * Set the current user ID
     */
    public static void setUserId(String userId) {
        log.debug("Setting user ID: {}", userId);
        USER_ID.set(userId);
    }
    
    /**
     * Get the current user ID
     */
    public static String getUserId() {
        return USER_ID.get();
    }
    
    /**
     * Set the current user roles
     */
    public static void setUserRoles(String roles) {
        log.debug("Setting user roles: {}", roles);
        USER_ROLES.set(roles);
    }
    
    /**
     * Get the current user roles
     */
    public static String getUserRoles() {
        return USER_ROLES.get();
    }
    
    /**
     * Clear all context information
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        TENANT_ID.remove();
        USER_ID.remove();
        USER_ROLES.remove();
    }
    
    /**
     * Check if tenant context is set
     */
    public static boolean isSet() {
        return TENANT_ID.get() != null;
    }
    
    /**
     * Get current context as string for logging
     */
    public static String getCurrentContext() {
        return String.format("TenantContext{tenantId='%s', userId='%s', roles='%s'}", 
                getTenantId(), getUserId(), getUserRoles());
    }
}
