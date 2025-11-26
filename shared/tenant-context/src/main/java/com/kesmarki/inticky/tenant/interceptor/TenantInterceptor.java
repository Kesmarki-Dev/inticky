package com.kesmarki.inticky.tenant.interceptor;

import com.kesmarki.inticky.tenant.context.TenantContext;
import com.kesmarki.inticky.tenant.resolver.TenantResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that sets tenant context for each request
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {
    
    private final TenantResolver tenantResolver;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Resolve and set tenant context
            String tenantId = tenantResolver.resolveTenantId(request);
            String userId = tenantResolver.resolveUserId(request);
            String userRoles = tenantResolver.resolveUserRoles(request);
            
            TenantContext.setTenantId(tenantId);
            TenantContext.setUserId(userId);
            TenantContext.setUserRoles(userRoles);
            
            log.debug("Tenant context set: {}", TenantContext.getCurrentContext());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to set tenant context", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // Clear tenant context after request completion
        TenantContext.clear();
        log.debug("Tenant context cleared");
    }
}
