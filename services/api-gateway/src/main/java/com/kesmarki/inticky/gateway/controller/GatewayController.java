package com.kesmarki.inticky.gateway.controller;

import com.kesmarki.inticky.gateway.service.ServiceHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Gateway management and monitoring controller
 */
@Slf4j
@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class GatewayController {

    private final ServiceHealthService serviceHealthService;

    /**
     * Get gateway information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getGatewayInfo() {
        Map<String, Object> info = Map.of(
                "name", "Inticky API Gateway",
                "version", "1.0.0",
                "description", "Central API Gateway for Inticky Ticketing System",
                "timestamp", LocalDateTime.now().toString(),
                "features", Map.of(
                        "authentication", "JWT-based authentication",
                        "rateLimiting", "Redis-based rate limiting",
                        "circuitBreaker", "Resilience4j circuit breaker",
                        "loadBalancing", "Spring Cloud LoadBalancer",
                        "monitoring", "Prometheus metrics and health checks",
                        "tracing", "Distributed tracing with Zipkin"
                ),
                "services", Map.of(
                        "tenant-service", "Tenant management",
                        "user-service", "User management and authentication", 
                        "ticket-service", "Ticket management and workflow",
                        "ai-service", "AI-powered features",
                        "notification-service", "Notifications and alerts"
                )
        );

        return ResponseEntity.ok(info);
    }

    /**
     * Get health status of all services
     */
    @GetMapping("/health/services")
    public Mono<ResponseEntity<Map<String, ServiceHealthService.ServiceHealth>>> getServicesHealth() {
        return serviceHealthService.getAllServiceHealth()
                .map(ResponseEntity::ok)
                .doOnNext(response -> log.debug("Services health check completed"));
    }

    /**
     * Get overall system health
     */
    @GetMapping("/health/system")
    public Mono<ResponseEntity<ServiceHealthService.SystemHealth>> getSystemHealth() {
        return serviceHealthService.getSystemHealth()
                .map(ResponseEntity::ok)
                .doOnNext(response -> log.debug("System health check completed"));
    }

    /**
     * Get gateway statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGatewayStats() {
        // TODO: Implement actual statistics collection
        Map<String, Object> stats = Map.of(
                "uptime", "N/A", // Calculate actual uptime
                "totalRequests", "N/A", // Get from metrics
                "activeConnections", "N/A", // Get from metrics
                "averageResponseTime", "N/A", // Get from metrics
                "errorRate", "N/A", // Calculate from metrics
                "rateLimitHits", "N/A", // Get from Redis
                "circuitBreakerTrips", "N/A", // Get from Resilience4j
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Get gateway configuration (non-sensitive parts)
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getGatewayConfig() {
        Map<String, Object> config = Map.of(
                "routes", Map.of(
                        "tenant-service", "/api/tenants/**",
                        "user-service-auth", "/api/auth/**",
                        "user-service-users", "/api/users/**",
                        "ticket-service", "/api/tickets/**",
                        "ai-service", "/api/ai/**",
                        "notification-service", "/api/notifications/**"
                ),
                "filters", Map.of(
                        "authentication", "JWT-based authentication filter",
                        "tenant", "Multi-tenant context filter",
                        "rateLimiting", "Redis-based rate limiting",
                        "circuitBreaker", "Resilience4j circuit breaker",
                        "logging", "Request/response logging"
                ),
                "cors", Map.of(
                        "allowedOrigins", "*",
                        "allowedMethods", "GET, POST, PUT, DELETE, PATCH, OPTIONS",
                        "allowCredentials", true
                ),
                "security", Map.of(
                        "authenticationRequired", true,
                        "excludedPaths", "/api/auth/**, /actuator/**, /swagger-ui/**, /api-docs/**"
                )
        );

        return ResponseEntity.ok(config);
    }
}
