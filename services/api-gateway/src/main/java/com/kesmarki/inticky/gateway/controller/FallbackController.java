package com.kesmarki.inticky.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback for tenant service
     */
    @GetMapping("/tenant-service")
    public ResponseEntity<Map<String, Object>> tenantServiceFallback() {
        log.warn("Tenant service fallback triggered");
        return createFallbackResponse("Tenant Service", "tenant management");
    }

    /**
     * Fallback for user service
     */
    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        log.warn("User service fallback triggered");
        return createFallbackResponse("User Service", "user management and authentication");
    }

    /**
     * Fallback for ticket service
     */
    @GetMapping("/ticket-service")
    public ResponseEntity<Map<String, Object>> ticketServiceFallback() {
        log.warn("Ticket service fallback triggered");
        return createFallbackResponse("Ticket Service", "ticket management");
    }

    /**
     * Fallback for AI service
     */
    @GetMapping("/ai-service")
    public ResponseEntity<Map<String, Object>> aiServiceFallback() {
        log.warn("AI service fallback triggered");
        return createFallbackResponse("AI Service", "AI-powered features");
    }

    /**
     * Fallback for notification service
     */
    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        log.warn("Notification service fallback triggered");
        return createFallbackResponse("Notification Service", "notifications and alerts");
    }

    /**
     * Generic fallback for any service
     */
    @GetMapping("/{serviceName}")
    public ResponseEntity<Map<String, Object>> genericFallback(@PathVariable String serviceName) {
        log.warn("Generic fallback triggered for service: {}", serviceName);
        return createFallbackResponse(serviceName, "requested functionality");
    }

    /**
     * Create standardized fallback response
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String functionality) {
        Map<String, Object> response = Map.of(
                "error", "Service Unavailable",
                "message", String.format("%s is temporarily unavailable. Please try again later.", serviceName),
                "service", serviceName,
                "functionality", functionality,
                "timestamp", LocalDateTime.now().toString(),
                "suggestion", "Please check service status or contact support if the issue persists",
                "retryAfter", "30 seconds"
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Health check fallback
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthFallback() {
        Map<String, Object> response = Map.of(
                "status", "DEGRADED",
                "message", "Some services are experiencing issues",
                "timestamp", LocalDateTime.now().toString(),
                "gateway", Map.of(
                        "status", "UP",
                        "version", "1.0.0"
                )
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
