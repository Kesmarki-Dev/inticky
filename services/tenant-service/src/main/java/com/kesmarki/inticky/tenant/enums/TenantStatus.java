package com.kesmarki.inticky.tenant.enums;

/**
 * Tenant status enumeration
 */
public enum TenantStatus {
    /**
     * Tenant is active and fully operational
     */
    ACTIVE,
    
    /**
     * Tenant is suspended (temporary restriction)
     */
    SUSPENDED,
    
    /**
     * Tenant is in trial period
     */
    TRIAL,
    
    /**
     * Tenant is inactive (soft delete)
     */
    INACTIVE,
    
    /**
     * Tenant is pending activation
     */
    PENDING
}
