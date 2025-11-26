package com.kesmarki.inticky.tenant.resolver;

import com.kesmarki.inticky.common.exception.TenantNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Resolves tenant ID from HTTP requests
 */
@Slf4j
@Component
public class TenantResolver {
    
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Value("${inticky.jwt.secret:default-secret-key-for-development-only-change-in-production}")
    private String jwtSecret;
    
    /**
     * Resolve tenant ID from HTTP request
     * Priority: JWT token > X-Tenant-ID header
     */
    public String resolveTenantId(HttpServletRequest request) {
        // Try to get tenant ID from JWT token first
        String tenantId = resolveTenantFromJwt(request);
        if (StringUtils.hasText(tenantId)) {
            log.debug("Resolved tenant ID from JWT: {}", tenantId);
            return tenantId;
        }
        
        // Fallback to X-Tenant-ID header
        tenantId = request.getHeader(TENANT_HEADER);
        if (StringUtils.hasText(tenantId)) {
            log.debug("Resolved tenant ID from header: {}", tenantId);
            return tenantId;
        }
        
        // No tenant ID found
        log.warn("No tenant ID found in request");
        throw new TenantNotFoundException("Tenant ID not found in request");
    }
    
    /**
     * Resolve user ID from JWT token
     */
    public String resolveUserId(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return null;
            }
            
            Claims claims = parseJwtToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.debug("Could not resolve user ID from JWT: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Resolve user roles from JWT token
     */
    public String resolveUserRoles(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return null;
            }
            
            Claims claims = parseJwtToken(token);
            return claims.get("roles", String.class);
        } catch (Exception e) {
            log.debug("Could not resolve user roles from JWT: {}", e.getMessage());
            return null;
        }
    }
    
    private String resolveTenantFromJwt(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return null;
            }
            
            Claims claims = parseJwtToken(token);
            return claims.get("tenantId", String.class);
        } catch (Exception e) {
            log.debug("Could not resolve tenant ID from JWT: {}", e.getMessage());
            return null;
        }
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    private Claims parseJwtToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
