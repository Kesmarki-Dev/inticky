package com.kesmarki.inticky.tenant.service;

import com.kesmarki.inticky.common.exception.TenantNotFoundException;
import com.kesmarki.inticky.tenant.dto.TenantCreateRequest;
import com.kesmarki.inticky.tenant.dto.TenantResponse;
import com.kesmarki.inticky.tenant.dto.TenantUpdateRequest;
import com.kesmarki.inticky.tenant.entity.Tenant;
import com.kesmarki.inticky.tenant.enums.TenantStatus;
import com.kesmarki.inticky.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for tenant management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    /**
     * Get all tenants with pagination
     */
    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        log.debug("Fetching all tenants with pagination: {}", pageable);
        return tenantRepository.findAll(pageable)
                .map(TenantResponse::fromEntity);
    }

    /**
     * Get tenant by ID
     */
    @Cacheable(value = "tenants", key = "#tenantId")
    public TenantResponse getTenantById(String tenantId) {
        log.debug("Fetching tenant by ID: {}", tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> TenantNotFoundException.forId(tenantId));
        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Get tenant by domain
     */
    @Cacheable(value = "tenants", key = "#domain")
    public TenantResponse getTenantByDomain(String domain) {
        log.debug("Fetching tenant by domain: {}", domain);
        Tenant tenant = tenantRepository.findByDomainIgnoreCase(domain)
                .orElseThrow(() -> TenantNotFoundException.forDomain(domain));
        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Get active tenants
     */
    public Page<TenantResponse> getActiveTenants(Pageable pageable) {
        log.debug("Fetching active tenants with pagination: {}", pageable);
        return tenantRepository.findActiveTenants(pageable)
                .map(TenantResponse::fromEntity);
    }

    /**
     * Search tenants by keyword
     */
    public Page<TenantResponse> searchTenants(String keyword, Pageable pageable) {
        log.debug("Searching tenants with keyword: {}", keyword);
        return tenantRepository.searchByNameOrDomain(keyword, pageable)
                .map(TenantResponse::fromEntity);
    }

    /**
     * Create new tenant
     */
    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public TenantResponse createTenant(TenantCreateRequest request) {
        log.info("Creating new tenant: {}", request.getId());

        // Validate tenant ID uniqueness
        if (tenantRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("Tenant ID already exists: " + request.getId());
        }

        // Validate domain uniqueness
        if (tenantRepository.existsByDomainIgnoreCase(request.getDomain())) {
            throw new IllegalArgumentException("Domain already exists: " + request.getDomain());
        }

        // Create tenant entity
        Tenant tenant = Tenant.builder()
                .id(request.getId())
                .name(request.getName())
                .domain(request.getDomain().toLowerCase())
                .status(TenantStatus.ACTIVE)
                .plan(request.getPlan())
                .settings(request.getSettings())
                .build();

        // Set default settings if not provided
        if (tenant.getSettings() == null || tenant.getSettings().isEmpty()) {
            tenant.setSettings(getDefaultSettings(request.getPlan()));
        }

        tenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully: {}", tenant.getId());

        // TODO: Create admin user for the tenant (will be implemented in user-service)
        // TODO: Send welcome email
        // TODO: Publish tenant created event

        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Update tenant
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public TenantResponse updateTenant(String tenantId, TenantUpdateRequest request) {
        log.info("Updating tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> TenantNotFoundException.forId(tenantId));

        // Update fields if provided
        if (request.getName() != null) {
            tenant.setName(request.getName());
        }
        if (request.getStatus() != null) {
            tenant.setStatus(request.getStatus());
        }
        if (request.getPlan() != null) {
            tenant.setPlan(request.getPlan());
        }
        if (request.getSettings() != null) {
            tenant.setSettings(request.getSettings());
        }

        tenant = tenantRepository.save(tenant);
        log.info("Tenant updated successfully: {}", tenant.getId());

        // TODO: Publish tenant updated event

        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Update tenant settings
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public TenantResponse updateTenantSettings(String tenantId, Map<String, Object> settings) {
        log.info("Updating tenant settings: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> TenantNotFoundException.forId(tenantId));

        // Merge settings
        Map<String, Object> currentSettings = tenant.getSettings();
        if (currentSettings == null) {
            tenant.setSettings(settings);
        } else {
            currentSettings.putAll(settings);
            tenant.setSettings(currentSettings);
        }

        tenant = tenantRepository.save(tenant);
        log.info("Tenant settings updated successfully: {}", tenant.getId());

        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Suspend tenant
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public TenantResponse suspendTenant(String tenantId, String reason) {
        log.info("Suspending tenant: {} with reason: {}", tenantId, reason);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> TenantNotFoundException.forId(tenantId));

        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setSetting("suspensionReason", reason);
        tenant.setSetting("suspendedAt", LocalDateTime.now().toString());

        tenant = tenantRepository.save(tenant);
        log.info("Tenant suspended successfully: {}", tenant.getId());

        // TODO: Publish tenant suspended event
        // TODO: Send suspension notification

        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Activate tenant
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public TenantResponse activateTenant(String tenantId) {
        log.info("Activating tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> TenantNotFoundException.forId(tenantId));

        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setSetting("suspensionReason", null);
        tenant.setSetting("suspendedAt", null);
        tenant.setSetting("activatedAt", LocalDateTime.now().toString());

        tenant = tenantRepository.save(tenant);
        log.info("Tenant activated successfully: {}", tenant.getId());

        // TODO: Publish tenant activated event
        // TODO: Send activation notification

        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Delete tenant (soft delete by setting status to INACTIVE)
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void deleteTenant(String tenantId) {
        log.info("Deleting tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> TenantNotFoundException.forId(tenantId));

        tenant.setStatus(TenantStatus.INACTIVE);
        tenant.setSetting("deletedAt", LocalDateTime.now().toString());

        tenantRepository.save(tenant);
        log.info("Tenant deleted successfully: {}", tenant.getId());

        // TODO: Publish tenant deleted event
        // TODO: Schedule data cleanup
    }

    /**
     * Get tenant statistics
     */
    public Map<String, Object> getTenantStatistics() {
        log.debug("Fetching tenant statistics");

        long totalTenants = tenantRepository.count();
        long activeTenants = tenantRepository.countByStatus(TenantStatus.ACTIVE);
        long suspendedTenants = tenantRepository.countByStatus(TenantStatus.SUSPENDED);
        long trialTenants = tenantRepository.countByStatus(TenantStatus.TRIAL);

        return Map.of(
                "totalTenants", totalTenants,
                "activeTenants", activeTenants,
                "suspendedTenants", suspendedTenants,
                "trialTenants", trialTenants,
                "generatedAt", LocalDateTime.now()
        );
    }

    /**
     * Check if tenant exists and is active
     */
    public boolean isTenantActiveById(String tenantId) {
        return tenantRepository.findById(tenantId)
                .map(Tenant::isActive)
                .orElse(false);
    }

    /**
     * Check if tenant exists and is active by domain
     */
    public boolean isTenantActiveByDomain(String domain) {
        return tenantRepository.findByDomainIgnoreCase(domain)
                .map(Tenant::isActive)
                .orElse(false);
    }

    /**
     * Get default settings for a tenant plan
     */
    private Map<String, Object> getDefaultSettings(com.kesmarki.inticky.tenant.enums.TenantPlan plan) {
        return switch (plan) {
            case BASIC -> Map.of(
                    "maxUsers", 10,
                    "maxTickets", 100,
                    "features", List.of("basic_support", "email_notifications"),
                    "storageLimit", "1GB"
            );
            case PREMIUM -> Map.of(
                    "maxUsers", 100,
                    "maxTickets", 1000,
                    "features", List.of("basic_support", "email_notifications", "ai_chat", "reporting"),
                    "storageLimit", "10GB"
            );
            case ENTERPRISE -> Map.of(
                    "maxUsers", -1, // unlimited
                    "maxTickets", -1, // unlimited
                    "features", List.of("basic_support", "email_notifications", "ai_chat", "reporting", 
                                      "integrations", "custom_workflows", "sla_management"),
                    "storageLimit", "100GB"
            );
            case CUSTOM -> Map.of(
                    "maxUsers", 50,
                    "maxTickets", 500,
                    "features", List.of("basic_support", "email_notifications"),
                    "storageLimit", "5GB"
            );
        };
    }
}
