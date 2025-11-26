package com.kesmarki.inticky.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT Token Provider for authentication and authorization
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${inticky.gateway.security.jwt.secret:mySecretKey123456789012345678901234567890}") String secret,
            @Value("${inticky.gateway.security.jwt.expiration:86400000}") long validityInMilliseconds) {
        
        // Ensure the secret is at least 256 bits (32 characters) for HS256
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
        
        log.info("JWT Token Provider initialized with expiration: {} ms", validityInMilliseconds);
    }

    /**
     * Create JWT token with user information
     */
    public String createToken(String userId, String tenantId, String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Create JWT token with custom claims
     */
    public String createToken(String userId, String tenantId, String email, String role, Map<String, Object> additionalClaims) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        JwtBuilder builder = Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity);

        // Add additional claims
        if (additionalClaims != null) {
            additionalClaims.forEach(builder::claim);
        }

        return builder.signWith(secretKey, Jwts.SIG.HS256).compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get user ID from token
     */
    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Get tenant ID from token
     */
    public String getTenantIdFromToken(String token) {
        return getClaimsFromToken(token).get("tenantId", String.class);
    }

    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    /**
     * Get role from token
     */
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    /**
     * Get custom claim from token
     */
    public Object getClaimFromToken(String token, String claimName) {
        return getClaimsFromToken(token).get(claimName);
    }

    /**
     * Get all claims from token
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get token expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * Refresh token (create new token with same claims but extended expiration)
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String userId = claims.getSubject();
            String tenantId = claims.get("tenantId", String.class);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            return createToken(userId, tenantId, email, role);
        } catch (Exception e) {
            log.error("Error refreshing JWT token: {}", e.getMessage());
            throw new RuntimeException("Cannot refresh JWT token", e);
        }
    }
}