package com.kesmarki.inticky.user.repository;

import com.kesmarki.inticky.user.entity.User;
import com.kesmarki.inticky.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (case insensitive)
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Find users by tenant ID
     */
    List<User> findByTenantId(UUID tenantId);

    /**
     * Find users by tenant ID with pagination
     */
    Page<User> findByTenantId(UUID tenantId, Pageable pageable);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by tenant ID and status
     */
    List<User> findByTenantIdAndStatus(UUID tenantId, UserStatus status);

    /**
     * Check if email exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Check if email exists for different user
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:tenantId IS NULL OR u.tenantId = :tenantId) AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("tenantId") UUID tenantId, 
                          @Param("search") String search, 
                          Pageable pageable);

    /**
     * Find users by department
     */
    List<User> findByTenantIdAndDepartment(UUID tenantId, String department);

    /**
     * Count users by tenant and status
     */
    long countByTenantIdAndStatus(UUID tenantId, UserStatus status);

    /**
     * Count total users by tenant
     */
    long countByTenantId(UUID tenantId);
}
