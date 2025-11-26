package com.kesmarki.inticky.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gateway filter for request logging and tracing
 */
@Slf4j
@Component
public class RequestLoggingFilter extends AbstractGatewayFilterFactory<RequestLoggingFilter.Config> {

    public RequestLoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Generate request ID for tracing
            String requestId = UUID.randomUUID().toString();
            long startTime = System.currentTimeMillis();
            
            // Log incoming request
            log.info("Incoming Request [{}] - {} {} from {} - User-Agent: {}", 
                    requestId,
                    request.getMethod(),
                    request.getURI(),
                    getClientIp(request),
                    request.getHeaders().getFirst("User-Agent"));
            
            // Log request headers (excluding sensitive ones)
            if (log.isDebugEnabled()) {
                request.getHeaders().forEach((name, values) -> {
                    if (!isSensitiveHeader(name)) {
                        log.debug("Request [{}] Header - {}: {}", requestId, name, values);
                    }
                });
            }
            
            // Add request tracking headers
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Request-Id", requestId)
                    .header("X-Request-Start-Time", String.valueOf(startTime))
                    .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();
            
            return chain.filter(modifiedExchange)
                    .doFinally(signalType -> {
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        
                        log.info("Request [{}] completed in {}ms - Signal: {}", 
                                requestId, duration, signalType);
                        
                        // Log slow requests
                        if (duration > 5000) { // 5 seconds
                            log.warn("Slow Request [{}] - took {}ms for {} {}", 
                                    requestId, duration, request.getMethod(), request.getURI());
                        }
                    });
        };
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
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
               lowerName.contains("key");
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        private boolean logHeaders = false;
        private boolean logBody = false;
        private long slowRequestThreshold = 5000; // milliseconds

        public boolean isLogHeaders() {
            return logHeaders;
        }

        public void setLogHeaders(boolean logHeaders) {
            this.logHeaders = logHeaders;
        }

        public boolean isLogBody() {
            return logBody;
        }

        public void setLogBody(boolean logBody) {
            this.logBody = logBody;
        }

        public long getSlowRequestThreshold() {
            return slowRequestThreshold;
        }

        public void setSlowRequestThreshold(long slowRequestThreshold) {
            this.slowRequestThreshold = slowRequestThreshold;
        }
    }
}
