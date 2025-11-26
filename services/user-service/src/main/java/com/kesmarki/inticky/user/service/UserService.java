package com.kesmarki.inticky.user.service;

import com.kesmarki.inticky.user.dto.UserCreateRequest;
import com.kesmarki.inticky.user.dto.UserResponse;
import com.kesmarki.inticky.user.dto.UserUpdateRequest;
import com.kesmarki.inticky.user.entity.User;
import com.kesmarki.inticky.user.entity.UserStatus;
import com.kesmarki.inticky.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for user management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    /**
     * Get users by tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByTenant(UUID tenantId, Pageable pageable) {
        log.debug("Getting users for tenant: {} with pagination: {}", tenantId, pageable);
        return userRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToUserResponse);
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UUID tenantId, String search, Pageable pageable) {
        log.debug("Searching users for tenant: {} with search: {} and pagination: {}", tenantId, search, pageable);
        return userRepository.searchUsers(tenantId, search, pageable)
                .map(this::mapToUserResponse);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(UUID id) {
        log.debug("Getting user by ID: {}", id);
        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    /**
     * Create new user
     */
    public UserResponse createUser(UserCreateRequest request) {
        log.debug("Creating new user: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create user entity
        User user = User.builder()
                .tenantId(request.getTenantId())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .jobTitle(request.getJobTitle())
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .emailVerified(request.getEmailVerified() != null ? request.getEmailVerified() : false)
                .loginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getEmail());

        return mapToUserResponse(savedUser);
    }

    /**
     * Update existing user
     */
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        log.debug("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // Check if email is being changed and if it already exists
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail().toLowerCase());
        }

        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User updated successfully: {}", savedUser.getEmail());

        return mapToUserResponse(savedUser);
    }

    /**
     * Delete user
     */
    public void deleteUser(UUID id) {
        log.debug("Deleting user: {}", id);

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    /**
     * Get user statistics for tenant
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getUserStats(UUID tenantId) {
        log.debug("Getting user statistics for tenant: {}", tenantId);

        long totalUsers = userRepository.countByTenantId(tenantId);
        long activeUsers = userRepository.countByTenantIdAndStatus(tenantId, UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByTenantIdAndStatus(tenantId, UserStatus.INACTIVE);
        long suspendedUsers = userRepository.countByTenantIdAndStatus(tenantId, UserStatus.SUSPENDED);

        return java.util.Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "inactiveUsers", inactiveUsers,
                "suspendedUsers", suspendedUsers,
                "timestamp", LocalDateTime.now()
        );
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .jobTitle(user.getJobTitle())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
