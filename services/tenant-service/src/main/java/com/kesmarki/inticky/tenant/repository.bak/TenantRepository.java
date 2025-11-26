package com.kesmarki.inticky.tenant.repository;

import com.kesmarki.inticky.tenant.entity.Tenant;
import com.kesmarki.inticky.tenant.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Tenant entity operations
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    /**
     * Find tenant by domain
     */
    Optional<Tenant> findByDomain(String domain);

    /**
     * Find tenant by domain (case insensitive)
     */
    @Query("SELECT t FROM Tenant t WHERE LOWER(t.domain) = LOWER(:domain)")
    Optional<Tenant> findByDomainIgnoreCase(@Param("domain") String domain);

    /**
     * Find tenants by status
     */
    List<Tenant> findByStatus(TenantStatus status);

    /**
     * Find tenants by status with pagination
     */
    Page<Tenant> findByStatus(TenantStatus status, Pageable pageable);

    /**
     * Check if domain exists
     */
    boolean existsByDomain(String domain);

    /**
     * Check if domain exists (case insensitive)
     */
    @Query("SELECT COUNT(t) > 0 FROM Tenant t WHERE LOWER(t.domain) = LOWER(:domain)")
    boolean existsByDomainIgnoreCase(@Param("domain") String domain);

    /**
     * Find tenants by name containing (case insensitive)
     */
    @Query("SELECT t FROM Tenant t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Tenant> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find active tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE'")
    List<Tenant> findActiveTenants();

    /**
     * Find active tenants with pagination
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE'")
    Page<Tenant> findActiveTenants(Pageable pageable);

    /**
     * Count tenants by status
     */
    long countByStatus(TenantStatus status);

    /**
     * Find tenants created after specific date
     */
    List<Tenant> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find tenants created between dates
     */
    List<Tenant> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Search tenants by name or domain
     */
    @Query("SELECT t FROM Tenant t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.domain) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Tenant> searchByNameOrDomain(@Param("keyword") String keyword);

    /**
     * Search tenants by name or domain with pagination
     */
    @Query("SELECT t FROM Tenant t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.domain) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Tenant> searchByNameOrDomain(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find tenants with specific setting
     */
    @Query("SELECT t FROM Tenant t WHERE JSON_EXTRACT(t.settings, :settingPath) IS NOT NULL")
    List<Tenant> findTenantsWithSetting(@Param("settingPath") String settingPath);

    /**
     * Find tenants with specific setting value
     */
    @Query("SELECT t FROM Tenant t WHERE JSON_EXTRACT(t.settings, :settingPath) = :settingValue")
    List<Tenant> findTenantsWithSettingValue(@Param("settingPath") String settingPath, 
                                           @Param("settingValue") String settingValue);
}
