package com.kesmarki.inticky.user.controller;

import com.kesmarki.inticky.common.dto.ApiResponse;
import com.kesmarki.inticky.user.dto.LoginRequest;
import com.kesmarki.inticky.user.dto.LoginResponse;
import com.kesmarki.inticky.user.dto.UserResponse;
import com.kesmarki.inticky.user.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("POST /api/auth/login - email: {}", request.getEmail());
        
        try {
            LoginResponse response = authenticationService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Validate JWT token
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token and return user info")
    public ResponseEntity<ApiResponse<UserResponse>> validateToken(
            @Parameter(description = "JWT token") @RequestHeader("Authorization") String authHeader) {
        
        log.debug("POST /api/auth/validate");
        
        try {
            // Extract token from "Bearer " prefix
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            UserResponse response = authenticationService.validateToken(token);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token validation failed: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if auth service is running")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Auth service is running"));
    }
}