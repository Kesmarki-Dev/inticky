package com.kesmarki.inticky.tenant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "tenant-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }
}
