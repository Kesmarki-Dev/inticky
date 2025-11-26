package com.kesmarki.inticky.tenant.dto;

import com.kesmarki.inticky.tenant.enums.TenantPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new tenant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCreateRequest {

    @NotBlank(message = "Tenant ID is required")
    @Size(min = 2, max = 50, message = "Tenant ID must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Tenant ID can only contain lowercase letters, numbers, and hyphens")
    private String id;

    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 500, message = "Tenant name must be between 2 and 500 characters")
    private String name;

    @NotBlank(message = "Domain is required")
    @Size(min = 3, max = 255, message = "Domain must be between 3 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid domain format")
    private String domain;

    @Builder.Default
    private TenantPlan plan = TenantPlan.BASIC;

    private Map<String, Object> settings;

    // Admin user information for initial setup
    @NotBlank(message = "Admin email is required")
    @Size(max = 320, message = "Email must not exceed 320 characters")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format")
    private String adminEmail;

    @NotBlank(message = "Admin first name is required")
    @Size(max = 255, message = "First name must not exceed 255 characters")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    @Size(max = 255, message = "Last name must not exceed 255 characters")
    private String adminLastName;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String adminPassword;
}
