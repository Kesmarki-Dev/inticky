package com.kesmarki.inticky.user.controller;

import com.kesmarki.inticky.common.dto.ApiResponse;
import com.kesmarki.inticky.user.dto.UserCreateRequest;
import com.kesmarki.inticky.user.dto.UserResponse;
import com.kesmarki.inticky.user.dto.UserUpdateRequest;
import com.kesmarki.inticky.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for user management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User CRUD operations and management")
public class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve paginated list of users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("GET /api/users - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        log.debug("GET /api/users/{}", userId);
        
        Optional<UserResponse> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.success(userOpt.get()));
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by keyword")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("GET /api/users/search - keyword: {}, tenantId: {}", keyword, tenantId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.searchUsers(tenantId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Create new user
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        
        log.info("POST /api/users - email: {}", request.getEmail());
        
        try {
            UserResponse user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Failed to create user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    /**
     * Update existing user
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update existing user information")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("PUT /api/users/{}", userId);
        
        try {
            UserResponse user = userService.updateUser(userId, request);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Failed to update user: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete user by ID")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        log.info("DELETE /api/users/{}", userId);
        
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("Failed to delete user: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Get user statistics for tenant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats(
            @RequestParam(required = false) UUID tenantId) {
        
        log.debug("GET /api/users/stats - tenantId: {}", tenantId);
        
        Map<String, Object> statistics = userService.getUserStats(tenantId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}