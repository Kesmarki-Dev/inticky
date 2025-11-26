package com.kesmarki.inticky.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Main application class for API Gateway
 */
@SpringBootApplication(scanBasePackages = {
        "com.kesmarki.inticky.gateway",
        "com.kesmarki.inticky.common",
        "com.kesmarki.inticky.tenant",
        "com.kesmarki.inticky.security"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Configure routes programmatically
     * This will be enhanced with configuration-based routing
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Tenant Service Routes
                .route("tenant-service", r -> r
                        .path("/api/tenants/**")
                        .uri("http://tenant-service:8081"))
                
                // User Service Routes
                .route("user-service-auth", r -> r
                        .path("/api/auth/**")
                        .uri("http://user-service:8082"))
                .route("user-service-users", r -> r
                        .path("/api/users/**")
                        .uri("http://user-service:8082"))
                
                // Ticket Service Routes
                .route("ticket-service", r -> r
                        .path("/api/tickets/**")
                        .uri("http://ticket-service:8083"))
                
                // AI Service Routes (when implemented)
                .route("ai-service", r -> r
                        .path("/api/ai/**")
                        .uri("http://ai-service:8084"))
                
                // Notification Service Routes (when implemented)
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://notification-service:8085"))
                
                .build();
    }
}
