package com.kesmarki.inticky.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Gateway filter for response logging
 */
@Slf4j
@Component
public class ResponseLoggingFilter extends AbstractGatewayFilterFactory<ResponseLoggingFilter.Config> {

    public ResponseLoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                    .doOnSuccess(aVoid -> {
                        ServerHttpResponse response = exchange.getResponse();
                        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
                        String startTimeStr = exchange.getRequest().getHeaders().getFirst("X-Request-Start-Time");
                        
                        HttpStatus statusCode = HttpStatus.valueOf(response.getStatusCode().value());
                        
                        // Calculate response time
                        long responseTime = 0;
                        if (startTimeStr != null) {
                            try {
                                long startTime = Long.parseLong(startTimeStr);
                                responseTime = System.currentTimeMillis() - startTime;
                            } catch (NumberFormatException e) {
                                log.warn("Invalid start time format: {}", startTimeStr);
                            }
                        }
                        
                        // Log response
                        if (statusCode.is2xxSuccessful()) {
                            log.info("Response [{}] - {} in {}ms", 
                                    requestId, statusCode.value(), responseTime);
                        } else if (statusCode.is4xxClientError()) {
                            log.warn("Client Error Response [{}] - {} in {}ms", 
                                    requestId, statusCode.value(), responseTime);
                        } else if (statusCode.is5xxServerError()) {
                            log.error("Server Error Response [{}] - {} in {}ms", 
                                    requestId, statusCode.value(), responseTime);
                        } else {
                            log.info("Response [{}] - {} in {}ms", 
                                    requestId, statusCode.value(), responseTime);
                        }
                        
                        // Log response headers (excluding sensitive ones)
                        if (log.isDebugEnabled()) {
                            response.getHeaders().forEach((name, values) -> {
                                if (!isSensitiveHeader(name)) {
                                    log.debug("Response [{}] Header - {}: {}", requestId, name, values);
                                }
                            });
                        }
                        
                        // Add response timing header
                        if (responseTime > 0) {
                            response.getHeaders().add("X-Response-Time", responseTime + "ms");
                        }
                    })
                    .doOnError(throwable -> {
                        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
                        log.error("Response Error [{}] - {}", requestId, throwable.getMessage());
                    });
        };
    }

    /**
     * Check if header contains sensitive information
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") || 
               lowerName.contains("password") || 
               lowerName.contains("secret") ||
               lowerName.contains("token") ||
               lowerName.contains("key") ||
               lowerName.contains("credential");
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        // Configuration properties can be added here if needed
    }
}