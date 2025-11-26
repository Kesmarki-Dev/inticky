package com.kesmarki.inticky.tenant.dto;

import com.kesmarki.inticky.tenant.enums.TenantPlan;
import com.kesmarki.inticky.tenant.enums.TenantStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating tenant information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUpdateRequest {

    @Size(min = 2, max = 500, message = "Tenant name must be between 2 and 500 characters")
    private String name;

    private TenantStatus status;

    private TenantPlan plan;

    private Map<String, Object> settings;
}
