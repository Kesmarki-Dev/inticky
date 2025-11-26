package com.kesmarki.inticky.gateway.filter;

import com.kesmarki.inticky.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

/**
 * Gateway filter for JWT authentication
 */
@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Paths that don't require authentication
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/actuator/health",
            "/swagger-ui",
            "/api-docs",
            "/fallback"
    );


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("Processing authentication for path: {}", path);

            // Skip authentication for excluded paths
            if (isExcludedPath(path)) {
                log.debug("Skipping authentication for excluded path: {}", path);
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return unauthorizedResponse(exchange);
            }

            String token = authHeader.substring(7);

            try {
                // Validate JWT token
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("Invalid JWT token for path: {}", path);
                    return unauthorizedResponse(exchange);
                }

                // Extract user information from token
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                String tenantId = jwtTokenProvider.getTenantIdFromToken(token);
                String userEmail = jwtTokenProvider.getEmailFromToken(token);

                log.debug("Authenticated user: {} tenant: {} for path: {}", userId, tenantId, path);

                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-Tenant-Id", tenantId)
                        .header("X-User-Email", userEmail)
                        .header("X-Authenticated", "true")
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                log.error("Authentication error for path: {} - {}", path, e.getMessage());
                return unauthorizedResponse(exchange);
            }
        };
    }

    /**
     * Check if path is excluded from authentication
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Return unauthorized response
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = """
                {
                    "error": "Unauthorized",
                    "message": "Authentication required",
                    "timestamp": "%s",
                    "path": "%s"
                }
                """.formatted(
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
