package com.kesmarki.inticky.tenant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods or classes as tenant-aware.
 * Can be used for AOP-based tenant filtering or validation.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantAware {
    
    /**
     * Whether to enforce tenant validation
     */
    boolean enforce() default true;
    
    /**
     * Custom tenant validation message
     */
    String message() default "Tenant context is required";
}
