package com.kesmarki.inticky.user.service;

import com.kesmarki.inticky.security.jwt.JwtTokenProvider;
import com.kesmarki.inticky.user.dto.LoginRequest;
import com.kesmarki.inticky.user.dto.LoginResponse;
import com.kesmarki.inticky.user.dto.UserResponse;
import com.kesmarki.inticky.user.entity.User;
import com.kesmarki.inticky.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for user authentication operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate user and return JWT token
     */
    public LoginResponse login(LoginRequest request) {
        log.debug("Attempting login for user: {}", request.getEmail());
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }
        
        User user = userOpt.get();
        
        // Check if user is active
        if (!user.isActive()) {
            log.warn("User is not active: {}", request.getEmail());
            throw new RuntimeException("User account is not active");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getEmail());
            // Increment login attempts
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            if (user.getLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                log.warn("User account locked due to too many failed attempts: {}", request.getEmail());
            }
            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }
        
        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("User account is locked: {}", request.getEmail());
            throw new RuntimeException("Account is temporarily locked");
        }
        
        // Reset login attempts on successful login
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate JWT token
        String token = jwtTokenProvider.createToken(
                user.getId().toString(), 
                user.getTenantId().toString(), 
                user.getEmail(), 
                "USER"
        );
        
        log.info("User logged in successfully: {}", request.getEmail());
        
        // Create user response
        UserResponse userResponse = UserResponse.builder()
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
        
        return LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    /**
     * Validate JWT token and return user info
     */
    public UserResponse validateToken(String token) {
        log.debug("Validating token");
        
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        Optional<User> userOpt = userRepository.findById(java.util.UUID.fromString(userId));
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        if (!user.isActive()) {
            throw new RuntimeException("User account is not active");
        }
        
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
