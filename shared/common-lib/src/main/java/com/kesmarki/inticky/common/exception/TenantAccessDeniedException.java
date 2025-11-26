package com.kesmarki.inticky.common.exception;

/**
 * Exception thrown when access to a tenant resource is denied
 */
public class TenantAccessDeniedException extends RuntimeException {
    
    public TenantAccessDeniedException(String message) {
        super(message);
    }
    
    public TenantAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static TenantAccessDeniedException forTenant(String tenantId) {
        return new TenantAccessDeniedException("Access denied for tenant: " + tenantId);
    }
    
    public static TenantAccessDeniedException forResource(String tenantId, String resourceId) {
        return new TenantAccessDeniedException(
            String.format("Access denied for tenant %s to resource %s", tenantId, resourceId)
        );
    }
}
