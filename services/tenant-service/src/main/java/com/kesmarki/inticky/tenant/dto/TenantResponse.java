package com.kesmarki.inticky.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kesmarki.inticky.tenant.entity.Tenant;
import com.kesmarki.inticky.tenant.enums.TenantPlan;
import com.kesmarki.inticky.tenant.enums.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for tenant information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private String id;
    private String name;
    private String domain;
    private TenantStatus status;
    private TenantPlan plan;
    private Map<String, Object> settings;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Statistics (optional, can be null)
    private Long userCount;
    private Long ticketCount;
    private Long activeTicketCount;

    /**
     * Convert Tenant entity to TenantResponse
     */
    public static TenantResponse fromEntity(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .status(tenant.getStatus())
                .plan(tenant.getPlan())
                .settings(tenant.getSettings())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    /**
     * Convert Tenant entity to TenantResponse with statistics
     */
    public static TenantResponse fromEntityWithStats(Tenant tenant, Long userCount, 
                                                   Long ticketCount, Long activeTicketCount) {
        TenantResponse response = fromEntity(tenant);
        response.setUserCount(userCount);
        response.setTicketCount(ticketCount);
        response.setActiveTicketCount(activeTicketCount);
        return response;
    }
}
