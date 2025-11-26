package com.kesmarki.inticky.ticket.repository;

import com.kesmarki.inticky.common.repository.MultiTenantJpaRepository;
import com.kesmarki.inticky.ticket.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Attachment entity operations
 */
@Repository
public interface AttachmentRepository extends MultiTenantJpaRepository<Attachment> {

    /**
     * Find attachments by ticket ID within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.ticket.id = :ticketId AND a.tenantId = :tenantId ORDER BY a.createdAt ASC")
    List<Attachment> findByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find attachments by ticket ID within tenant with pagination
     */
    @Query("SELECT a FROM Attachment a WHERE a.ticket.id = :ticketId AND a.tenantId = :tenantId ORDER BY a.createdAt ASC")
    Page<Attachment> findByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find attachments by uploader within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.uploadedBy = :uploadedBy AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findByUploadedByAndTenantId(@Param("uploadedBy") UUID uploadedBy, @Param("tenantId") String tenantId);

    /**
     * Find attachments by uploader within tenant with pagination
     */
    @Query("SELECT a FROM Attachment a WHERE a.uploadedBy = :uploadedBy AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    Page<Attachment> findByUploadedByAndTenantId(@Param("uploadedBy") UUID uploadedBy, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find attachments by content type within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.contentType = :contentType AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findByContentTypeAndTenantId(@Param("contentType") String contentType, @Param("tenantId") String tenantId);

    /**
     * Find image attachments within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.contentType LIKE 'image/%' AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findImageAttachmentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find document attachments within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE (a.contentType LIKE 'application/pdf' OR a.contentType LIKE 'application/msword' OR a.contentType LIKE 'application/vnd.openxmlformats%' OR a.contentType LIKE 'text/%') AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findDocumentAttachmentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find public attachments by ticket ID within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.ticket.id = :ticketId AND a.isPublic = true AND a.tenantId = :tenantId ORDER BY a.createdAt ASC")
    List<Attachment> findPublicAttachmentsByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Find unscanned attachments within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.isScanned = false AND a.tenantId = :tenantId ORDER BY a.createdAt ASC")
    List<Attachment> findUnscannedAttachmentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find infected attachments within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.scanResult IN ('INFECTED', 'VIRUS') AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findInfectedAttachmentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find safe attachments within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.scanResult IN ('CLEAN', 'SAFE') AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findSafeAttachmentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find attachments by file hash within tenant (for duplicate detection)
     */
    @Query("SELECT a FROM Attachment a WHERE a.fileHash = :fileHash AND a.tenantId = :tenantId")
    List<Attachment> findByFileHashAndTenantId(@Param("fileHash") String fileHash, @Param("tenantId") String tenantId);

    /**
     * Find attachments by filename pattern within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE LOWER(a.filename) LIKE LOWER(CONCAT('%', :pattern, '%')) AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findByFilenamePatternAndTenantId(@Param("pattern") String pattern, @Param("tenantId") String tenantId);

    /**
     * Find attachments by file extension within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE LOWER(a.fileExtension) = LOWER(:extension) AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findByFileExtensionAndTenantId(@Param("extension") String extension, @Param("tenantId") String tenantId);

    /**
     * Find large attachments within tenant (above size threshold)
     */
    @Query("SELECT a FROM Attachment a WHERE a.fileSize > :sizeThreshold AND a.tenantId = :tenantId ORDER BY a.fileSize DESC")
    List<Attachment> findLargeAttachmentsByTenantId(@Param("sizeThreshold") Long sizeThreshold, @Param("tenantId") String tenantId);

    /**
     * Find recent attachments within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.createdAt > :since AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findRecentAttachmentsByTenantId(@Param("since") LocalDateTime since, @Param("tenantId") String tenantId);

    /**
     * Find attachments created between dates within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.createdAt BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findAttachmentsCreatedBetweenAndTenantId(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("tenantId") String tenantId);

    /**
     * Count attachments by ticket ID within tenant
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.ticket.id = :ticketId AND a.tenantId = :tenantId")
    long countByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Count attachments by uploader within tenant
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.uploadedBy = :uploadedBy AND a.tenantId = :tenantId")
    long countByUploadedByAndTenantId(@Param("uploadedBy") UUID uploadedBy, @Param("tenantId") String tenantId);

    /**
     * Count attachments by content type within tenant
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.contentType = :contentType AND a.tenantId = :tenantId")
    long countByContentTypeAndTenantId(@Param("contentType") String contentType, @Param("tenantId") String tenantId);

    /**
     * Get total file size by ticket ID within tenant
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.ticket.id = :ticketId AND a.tenantId = :tenantId")
    long getTotalFileSizeByTicketIdAndTenantId(@Param("ticketId") UUID ticketId, @Param("tenantId") String tenantId);

    /**
     * Get total file size by uploader within tenant
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.uploadedBy = :uploadedBy AND a.tenantId = :tenantId")
    long getTotalFileSizeByUploadedByAndTenantId(@Param("uploadedBy") UUID uploadedBy, @Param("tenantId") String tenantId);

    /**
     * Get total file size within tenant
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.tenantId = :tenantId")
    long getTotalFileSizeByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find attachments with thumbnails within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.thumbnailPath IS NOT NULL AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<Attachment> findAttachmentsWithThumbnailsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find attachments by stored filename within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.storedFilename = :storedFilename AND a.tenantId = :tenantId")
    List<Attachment> findByStoredFilenameAndTenantId(@Param("storedFilename") String storedFilename, @Param("tenantId") String tenantId);

    /**
     * Find attachments by multiple ticket IDs within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.ticket.id IN :ticketIds AND a.tenantId = :tenantId ORDER BY a.ticket.id, a.createdAt ASC")
    List<Attachment> findByTicketIdsAndTenantId(@Param("ticketIds") List<UUID> ticketIds, @Param("tenantId") String tenantId);

    /**
     * Check if file hash exists within tenant (for duplicate detection)
     */
    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.fileHash = :fileHash AND a.tenantId = :tenantId")
    boolean existsByFileHashAndTenantId(@Param("fileHash") String fileHash, @Param("tenantId") String tenantId);

    /**
     * Find duplicate attachments by hash within tenant
     */
    @Query("SELECT a FROM Attachment a WHERE a.fileHash IN (SELECT a2.fileHash FROM Attachment a2 WHERE a2.tenantId = :tenantId GROUP BY a2.fileHash HAVING COUNT(a2) > 1) AND a.tenantId = :tenantId ORDER BY a.fileHash, a.createdAt")
    List<Attachment> findDuplicateAttachmentsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get attachment statistics by content type within tenant
     */
    @Query("SELECT a.contentType, COUNT(a), SUM(a.fileSize) FROM Attachment a WHERE a.tenantId = :tenantId GROUP BY a.contentType ORDER BY COUNT(a) DESC")
    List<Object[]> getAttachmentStatisticsByContentTypeAndTenantId(@Param("tenantId") String tenantId);

    /**
     * Get attachment statistics by file extension within tenant
     */
    @Query("SELECT a.fileExtension, COUNT(a), SUM(a.fileSize) FROM Attachment a WHERE a.tenantId = :tenantId GROUP BY a.fileExtension ORDER BY COUNT(a) DESC")
    List<Object[]> getAttachmentStatisticsByExtensionAndTenantId(@Param("tenantId") String tenantId);
}
