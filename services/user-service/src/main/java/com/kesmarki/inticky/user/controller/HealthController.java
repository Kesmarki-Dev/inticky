package com.kesmarki.inticky.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "user-service",
                "timestamp", LocalDateTime.now().toString(),
                "message", "🚀 User Service is running!"
        );
    }

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "message", "Welcome to Inticky User Service!",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "endpoints", Map.of(
                        "health", "/health",
                        "actuator", "/actuator/health"
                )
        );
    }
}
