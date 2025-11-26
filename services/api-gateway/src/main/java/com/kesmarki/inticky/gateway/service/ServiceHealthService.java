package com.kesmarki.inticky.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for monitoring downstream service health
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHealthService {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${inticky.gateway.services.tenant-service.url:http://tenant-service:8081}")
    private String tenantServiceUrl;
    
    @Value("${inticky.gateway.services.user-service.url:http://user-service:8082}")
    private String userServiceUrl;
    
    @Value("${inticky.gateway.services.ticket-service.url:http://ticket-service:8083}")
    private String ticketServiceUrl;
    
    @Value("${inticky.gateway.services.ai-service.url:http://ai-service:8084}")
    private String aiServiceUrl;
    
    @Value("${inticky.gateway.services.notification-service.url:http://notification-service:8085}")
    private String notificationServiceUrl;

    // Cache for service health status
    private final Map<String, ServiceHealth> healthCache = new ConcurrentHashMap<>();
    
    /**
     * Get health status of all services
     */
    public Mono<Map<String, ServiceHealth>> getAllServiceHealth() {
        Map<String, String> services = Map.of(
                "tenant-service", tenantServiceUrl,
                "user-service", userServiceUrl,
                "ticket-service", ticketServiceUrl,
                "ai-service", aiServiceUrl,
                "notification-service", notificationServiceUrl
        );
        
        Map<String, Mono<ServiceHealth>> healthChecks = new HashMap<>();
        
        services.forEach((serviceName, serviceUrl) -> {
            healthChecks.put(serviceName, checkServiceHealth(serviceName, serviceUrl));
        });
        
        return Mono.zip(
                healthChecks.values(),
                results -> {
                    Map<String, ServiceHealth> healthMap = new HashMap<>();
                    int index = 0;
                    for (String serviceName : healthChecks.keySet()) {
                        healthMap.put(serviceName, (ServiceHealth) results[index++]);
                    }
                    return healthMap;
                }
        );
    }
    
    /**
     * Check health of specific service
     */
    public Mono<ServiceHealth> checkServiceHealth(String serviceName, String serviceUrl) {
        return webClientBuilder.build()
                .get()
                .uri(serviceUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    ServiceHealth health = ServiceHealth.builder()
                            .serviceName(serviceName)
                            .status(ServiceHealth.Status.UP)
                            .url(serviceUrl)
                            .responseTime(System.currentTimeMillis())
                            .details(response)
                            .build();
                    
                    // Cache the result
                    healthCache.put(serviceName, health);
                    
                    log.debug("Service {} is UP", serviceName);
                    return health;
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(ServiceHealth.builder()
                        .serviceName(serviceName)
                        .status(ServiceHealth.Status.DOWN)
                        .url(serviceUrl)
                        .responseTime(System.currentTimeMillis())
                        .error("Service unavailable or timeout")
                        .build())
                .doOnError(error -> {
                    log.warn("Service {} health check failed: {}", serviceName, error.getMessage());
                    
                    ServiceHealth downHealth = ServiceHealth.builder()
                            .serviceName(serviceName)
                            .status(ServiceHealth.Status.DOWN)
                            .url(serviceUrl)
                            .responseTime(System.currentTimeMillis())
                            .error(error.getMessage())
                            .build();
                    
                    healthCache.put(serviceName, downHealth);
                });
    }
    
    /**
     * Get cached health status
     */
    public ServiceHealth getCachedHealth(String serviceName) {
        return healthCache.get(serviceName);
    }
    
    /**
     * Check if service is healthy
     */
    public boolean isServiceHealthy(String serviceName) {
        ServiceHealth health = healthCache.get(serviceName);
        return health != null && health.getStatus() == ServiceHealth.Status.UP;
    }
    
    /**
     * Get overall system health
     */
    public Mono<SystemHealth> getSystemHealth() {
        return getAllServiceHealth()
                .map(serviceHealthMap -> {
                    long upCount = serviceHealthMap.values().stream()
                            .mapToLong(health -> health.getStatus() == ServiceHealth.Status.UP ? 1 : 0)
                            .sum();
                    
                    long totalCount = serviceHealthMap.size();
                    
                    SystemHealth.Status overallStatus;
                    if (upCount == totalCount) {
                        overallStatus = SystemHealth.Status.UP;
                    } else if (upCount > 0) {
                        overallStatus = SystemHealth.Status.DEGRADED;
                    } else {
                        overallStatus = SystemHealth.Status.DOWN;
                    }
                    
                    return SystemHealth.builder()
                            .status(overallStatus)
                            .totalServices((int) totalCount)
                            .healthyServices((int) upCount)
                            .unhealthyServices((int) (totalCount - upCount))
                            .services(serviceHealthMap)
                            .timestamp(System.currentTimeMillis())
                            .build();
                });
    }
    
    /**
     * Service health data class
     */
    public static class ServiceHealth {
        private String serviceName;
        private Status status;
        private String url;
        private long responseTime;
        private Map<String, Object> details;
        private String error;
        
        public enum Status {
            UP, DOWN, UNKNOWN
        }
        
        public static ServiceHealthBuilder builder() {
            return new ServiceHealthBuilder();
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public Status getStatus() { return status; }
        public String getUrl() { return url; }
        public long getResponseTime() { return responseTime; }
        public Map<String, Object> getDetails() { return details; }
        public String getError() { return error; }
        
        public static class ServiceHealthBuilder {
            private String serviceName;
            private Status status;
            private String url;
            private long responseTime;
            private Map<String, Object> details;
            private String error;
            
            public ServiceHealthBuilder serviceName(String serviceName) {
                this.serviceName = serviceName;
                return this;
            }
            
            public ServiceHealthBuilder status(Status status) {
                this.status = status;
                return this;
            }
            
            public ServiceHealthBuilder url(String url) {
                this.url = url;
                return this;
            }
            
            public ServiceHealthBuilder responseTime(long responseTime) {
                this.responseTime = responseTime;
                return this;
            }
            
            public ServiceHealthBuilder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }
            
            public ServiceHealthBuilder error(String error) {
                this.error = error;
                return this;
            }
            
            public ServiceHealth build() {
                ServiceHealth health = new ServiceHealth();
                health.serviceName = this.serviceName;
                health.status = this.status;
                health.url = this.url;
                health.responseTime = this.responseTime;
                health.details = this.details;
                health.error = this.error;
                return health;
            }
        }
    }
    
    /**
     * System health data class
     */
    public static class SystemHealth {
        private Status status;
        private int totalServices;
        private int healthyServices;
        private int unhealthyServices;
        private Map<String, ServiceHealth> services;
        private long timestamp;
        
        public enum Status {
            UP, DEGRADED, DOWN
        }
        
        public static SystemHealthBuilder builder() {
            return new SystemHealthBuilder();
        }
        
        // Getters
        public Status getStatus() { return status; }
        public int getTotalServices() { return totalServices; }
        public int getHealthyServices() { return healthyServices; }
        public int getUnhealthyServices() { return unhealthyServices; }
        public Map<String, ServiceHealth> getServices() { return services; }
        public long getTimestamp() { return timestamp; }
        
        public static class SystemHealthBuilder {
            private Status status;
            private int totalServices;
            private int healthyServices;
            private int unhealthyServices;
            private Map<String, ServiceHealth> services;
            private long timestamp;
            
            public SystemHealthBuilder status(Status status) {
                this.status = status;
                return this;
            }
            
            public SystemHealthBuilder totalServices(int totalServices) {
                this.totalServices = totalServices;
                return this;
            }
            
            public SystemHealthBuilder healthyServices(int healthyServices) {
                this.healthyServices = healthyServices;
                return this;
            }
            
            public SystemHealthBuilder unhealthyServices(int unhealthyServices) {
                this.unhealthyServices = unhealthyServices;
                return this;
            }
            
            public SystemHealthBuilder services(Map<String, ServiceHealth> services) {
                this.services = services;
                return this;
            }
            
            public SystemHealthBuilder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public SystemHealth build() {
                SystemHealth health = new SystemHealth();
                health.status = this.status;
                health.totalServices = this.totalServices;
                health.healthyServices = this.healthyServices;
                health.unhealthyServices = this.unhealthyServices;
                health.services = this.services;
                health.timestamp = this.timestamp;
                return health;
            }
        }
    }
}
