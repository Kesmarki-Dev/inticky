package com.kesmarki.inticky.common.repository;

import com.kesmarki.inticky.common.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base repository interface for multi-tenant entities.
 * Automatically filters all queries by tenant ID.
 */
@NoRepositoryBean
public interface MultiTenantJpaRepository<T extends BaseEntity> extends JpaRepository<T, UUID> {

    /**
     * Find entity by ID within tenant context
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.tenantId = :tenantId")
    Optional<T> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);

    /**
     * Find all entities within tenant context
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.tenantId = :tenantId")
    List<T> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find all entities within tenant context with pagination
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.tenantId = :tenantId")
    Page<T> findAllByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Count entities within tenant context
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") String tenantId);

    /**
     * Delete entity by ID within tenant context
     */
    @Query("DELETE FROM #{#entityName} e WHERE e.id = :id AND e.tenantId = :tenantId")
    void deleteByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);

    /**
     * Check if entity exists by ID within tenant context
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.tenantId = :tenantId")
    boolean existsByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);
}
