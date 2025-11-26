package com.kesmarki.inticky.ticket.entity;

import com.kesmarki.inticky.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Entity representing a file attachment on a ticket
 */
@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachment_tenant_ticket", columnList = "tenant_id, ticket_id"),
        @Index(name = "idx_attachment_tenant_uploader", columnList = "tenant_id, uploaded_by"),
        @Index(name = "idx_attachment_tenant_created", columnList = "tenant_id, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true, exclude = {"ticket"})
public class Attachment extends BaseEntity {

    /**
     * The ticket this attachment belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /**
     * Original filename
     */
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    /**
     * Stored filename (UUID-based)
     */
    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;

    /**
     * File path in storage
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * MIME type of the file
     */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /**
     * File size in bytes
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * File extension
     */
    @Column(name = "file_extension", length = 10)
    private String fileExtension;

    /**
     * ID of the user who uploaded the file
     */
    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    /**
     * Name of the uploader (denormalized for performance)
     */
    @Column(name = "uploader_name", length = 255)
    private String uploaderName;

    /**
     * Description or comment about the attachment
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Whether the file is publicly accessible
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Whether the file has been scanned for viruses
     */
    @Column(name = "is_scanned")
    @Builder.Default
    private Boolean isScanned = false;

    /**
     * Virus scan result
     */
    @Column(name = "scan_result", length = 50)
    private String scanResult;

    /**
     * File hash (for duplicate detection)
     */
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    /**
     * Thumbnail path (for images)
     */
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    /**
     * Image dimensions (for images) - JSON format: {"width": 1920, "height": 1080}
     */
    @Column(name = "image_dimensions", length = 100)
    private String imageDimensions;

    /**
     * Additional metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Check if file is an image
     */
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Check if file is a document
     */
    public boolean isDocument() {
        if (contentType == null) return false;
        return contentType.startsWith("application/pdf") ||
               contentType.startsWith("application/msword") ||
               contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
               contentType.startsWith("text/");
    }

    /**
     * Check if file is a video
     */
    public boolean isVideo() {
        return contentType != null && contentType.startsWith("video/");
    }

    /**
     * Check if file is an audio
     */
    public boolean isAudio() {
        return contentType != null && contentType.startsWith("audio/");
    }

    /**
     * Check if file is an archive
     */
    public boolean isArchive() {
        if (contentType == null) return false;
        return contentType.equals("application/zip") ||
               contentType.equals("application/x-rar-compressed") ||
               contentType.equals("application/x-7z-compressed") ||
               contentType.equals("application/gzip");
    }

    /**
     * Get file size in human readable format
     */
    public String getHumanReadableFileSize() {
        if (fileSize == null) return "Unknown";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Get file type category
     */
    public String getFileTypeCategory() {
        if (isImage()) return "Image";
        if (isDocument()) return "Document";
        if (isVideo()) return "Video";
        if (isAudio()) return "Audio";
        if (isArchive()) return "Archive";
        return "Other";
    }

    /**
     * Get file icon based on type
     */
    public String getFileIcon() {
        if (isImage()) return "🖼️";
        if (isDocument()) return "📄";
        if (isVideo()) return "🎥";
        if (isAudio()) return "🎵";
        if (isArchive()) return "📦";
        return "📎";
    }

    /**
     * Check if file is safe (scanned and clean)
     */
    public boolean isSafe() {
        return isScanned && ("CLEAN".equals(scanResult) || "SAFE".equals(scanResult));
    }

    /**
     * Check if file has virus
     */
    public boolean hasVirus() {
        return isScanned && ("INFECTED".equals(scanResult) || "VIRUS".equals(scanResult));
    }

    /**
     * Check if file scan is pending
     */
    public boolean isScanPending() {
        return !isScanned;
    }

    /**
     * Get download URL (to be implemented by service)
     */
    public String getDownloadUrl() {
        return "/api/tickets/" + ticket.getId() + "/attachments/" + getId() + "/download";
    }

    /**
     * Get thumbnail URL (for images)
     */
    public String getThumbnailUrl() {
        if (thumbnailPath != null) {
            return "/api/tickets/" + ticket.getId() + "/attachments/" + getId() + "/thumbnail";
        }
        return null;
    }

    /**
     * Mark as scanned
     */
    public void markAsScanned(String result) {
        this.isScanned = true;
        this.scanResult = result;
    }

    /**
     * Set image dimensions
     */
    public void setImageDimensions(int width, int height) {
        this.imageDimensions = String.format("{\"width\": %d, \"height\": %d}", width, height);
    }

    /**
     * Get short filename (truncated if too long)
     */
    public String getShortFilename() {
        if (filename == null || filename.length() <= 30) {
            return filename;
        }
        
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            String name = filename.substring(0, dotIndex);
            String extension = filename.substring(dotIndex);
            if (name.length() > 25) {
                return name.substring(0, 22) + "..." + extension;
            }
        } else {
            return filename.substring(0, 27) + "...";
        }
        
        return filename;
    }

    /**
     * Validate file type against allowed types
     */
    public boolean isAllowedFileType(String[] allowedTypes) {
        if (allowedTypes == null || allowedTypes.length == 0) {
            return true; // No restrictions
        }
        
        String extension = getFileExtension();
        if (extension == null) {
            return false;
        }
        
        for (String allowedType : allowedTypes) {
            if (extension.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if file size is within limit
     */
    public boolean isWithinSizeLimit(long maxSizeBytes) {
        return fileSize != null && fileSize <= maxSizeBytes;
    }
}
