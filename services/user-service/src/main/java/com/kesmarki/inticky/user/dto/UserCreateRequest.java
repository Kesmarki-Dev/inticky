package com.kesmarki.inticky.user.dto;

import com.kesmarki.inticky.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User creation request")
public class UserCreateRequest {

    @Schema(description = "Tenant ID")
    private UUID tenantId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "User password", example = "password123")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Schema(description = "Phone number", example = "+1-555-0123")
    private String phone;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Schema(description = "Department", example = "IT")
    private String department;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    @Schema(description = "Job title", example = "Software Engineer")
    private String jobTitle;

    @Schema(description = "User status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Email verified flag", example = "false")
    private Boolean emailVerified;
}
