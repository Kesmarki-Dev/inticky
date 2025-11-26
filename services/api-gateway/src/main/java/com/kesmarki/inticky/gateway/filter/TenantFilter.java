package com.kesmarki.inticky.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway filter for tenant context validation and injection
 */
@Slf4j
@Component
public class TenantFilter extends AbstractGatewayFilterFactory<TenantFilter.Config> {

    public TenantFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("Processing tenant context for path: {}", path);

            // Extract tenant ID from headers (set by AuthenticationFilter)
            String tenantId = request.getHeaders().getFirst("X-Tenant-Id");
            String userId = request.getHeaders().getFirst("X-User-Id");

            if (tenantId == null || tenantId.trim().isEmpty()) {
                log.warn("Missing tenant ID for authenticated request to path: {}", path);
                return forbiddenResponse(exchange, "Missing tenant context");
            }

            if (userId == null || userId.trim().isEmpty()) {
                log.warn("Missing user ID for authenticated request to path: {}", path);
                return forbiddenResponse(exchange, "Missing user context");
            }

            log.debug("Tenant context validated - tenant: {} user: {} for path: {}", tenantId, userId, path);

            // Add additional tenant-related headers
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Tenant-Validated", "true")
                    .header("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange);
        };
    }

    /**
     * Return forbidden response
     */
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = """
                {
                    "error": "Forbidden",
                    "message": "%s",
                    "timestamp": "%s",
                    "path": "%s"
                }
                """.formatted(
                message,
                java.time.Instant.now().toString(),
                exchange.getRequest().getURI().getPath()
        );

        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        // Configuration properties can be added here if needed
    }
}
