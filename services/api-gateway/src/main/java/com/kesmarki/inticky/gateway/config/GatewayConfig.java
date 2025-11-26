package com.kesmarki.inticky.gateway.config;

import com.kesmarki.inticky.gateway.filter.AuthenticationFilter;
import com.kesmarki.inticky.gateway.filter.TenantFilter;
import com.kesmarki.inticky.gateway.filter.RequestLoggingFilter;
import com.kesmarki.inticky.gateway.filter.ResponseLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Mono;

/**
 * Gateway configuration for filters, rate limiting, and other components
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    /**
     * Key resolver for rate limiting based on user ID
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }
            
            // Fallback to IP address if no user ID
            String clientIp = getClientIp(exchange);
            return Mono.just(clientIp);
        };
    }

    /**
     * Key resolver for rate limiting based on tenant ID
     */
    @Bean
    public KeyResolver tenantKeyResolver() {
        return exchange -> {
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
            if (tenantId != null) {
                return Mono.just("tenant:" + tenantId);
            }
            
            // Fallback to IP address if no tenant ID
            String clientIp = getClientIp(exchange);
            return Mono.just("ip:" + clientIp);
        };
    }

    /**
     * Key resolver for rate limiting based on IP address
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(getClientIp(exchange));
    }

    /**
     * Redis rate limiter for authentication endpoints
     */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(20, 40, 1); // 20 requests per second, burst of 40
    }

    /**
     * Redis rate limiter for general API endpoints
     */
    @Bean
    public RedisRateLimiter apiRateLimiter() {
        return new RedisRateLimiter(10, 20, 1); // 10 requests per second, burst of 20
    }

    /**
     * Redis rate limiter for AI endpoints (more restrictive)
     */
    @Bean
    public RedisRateLimiter aiRateLimiter() {
        return new RedisRateLimiter(5, 10, 1); // 5 requests per second, burst of 10
    }

    /**
     * Reactive Redis template for custom rate limiting logic
     * Commented out temporarily to fix startup issues
     */
    /*
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(org.springframework.data.redis.connection.ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .value(StringRedisSerializer.UTF_8)
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(StringRedisSerializer.UTF_8)
                .build();
        
        return new ReactiveRedisTemplate<String, String>(connectionFactory, serializationContext);
    }
    */

    /**
     * Extract client IP address from request
     */
    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return exchange.getRequest().getRemoteAddress() != null ? 
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}
