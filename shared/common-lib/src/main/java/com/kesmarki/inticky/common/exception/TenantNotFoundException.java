package com.kesmarki.inticky.common.exception;

/**
 * Exception thrown when a tenant is not found
 */
public class TenantNotFoundException extends RuntimeException {
    
    public TenantNotFoundException(String message) {
        super(message);
    }
    
    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static TenantNotFoundException forId(String tenantId) {
        return new TenantNotFoundException("Tenant not found with ID: " + tenantId);
    }
    
    public static TenantNotFoundException forDomain(String domain) {
        return new TenantNotFoundException("Tenant not found with domain: " + domain);
    }
}
