package com.kesmarki.inticky.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kesmarki.inticky.ticket.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for attachment information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private UUID id;
    private UUID ticketId;
    private String filename;
    private String shortFilename;
    private String storedFilename;
    private String filePath;
    private String contentType;
    private Long fileSize;
    private String humanReadableFileSize;
    private String fileExtension;

    private UUID uploadedBy;
    private String uploaderName;
    private String description;

    private Boolean isPublic;
    private Boolean isScanned;
    private String scanResult;
    private String fileHash;
    private String thumbnailPath;
    private String imageDimensions;
    private String metadata;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed fields
    private Boolean isImage;
    private Boolean isDocument;
    private Boolean isVideo;
    private Boolean isAudio;
    private Boolean isArchive;
    private String fileTypeCategory;
    private String fileIcon;
    private Boolean isSafe;
    private Boolean hasVirus;
    private Boolean isScanPending;

    // URLs
    private String downloadUrl;
    private String thumbnailUrl;

    /**
     * Convert Attachment entity to AttachmentResponse
     */
    public static AttachmentResponse fromEntity(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .ticketId(attachment.getTicket().getId())
                .filename(attachment.getFilename())
                .shortFilename(attachment.getShortFilename())
                .storedFilename(attachment.getStoredFilename())
                .filePath(attachment.getFilePath())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .humanReadableFileSize(attachment.getHumanReadableFileSize())
                .fileExtension(attachment.getFileExtension())
                .uploadedBy(attachment.getUploadedBy())
                .uploaderName(attachment.getUploaderName())
                .description(attachment.getDescription())
                .isPublic(attachment.getIsPublic())
                .isScanned(attachment.getIsScanned())
                .scanResult(attachment.getScanResult())
                .fileHash(attachment.getFileHash())
                .thumbnailPath(attachment.getThumbnailPath())
                .imageDimensions(attachment.getImageDimensions())
                .metadata(attachment.getMetadata())
                .createdAt(attachment.getCreatedAt())
                .updatedAt(attachment.getUpdatedAt())
                // Computed fields
                .isImage(attachment.isImage())
                .isDocument(attachment.isDocument())
                .isVideo(attachment.isVideo())
                .isAudio(attachment.isAudio())
                .isArchive(attachment.isArchive())
                .fileTypeCategory(attachment.getFileTypeCategory())
                .fileIcon(attachment.getFileIcon())
                .isSafe(attachment.isSafe())
                .hasVirus(attachment.hasVirus())
                .isScanPending(attachment.isScanPending())
                // URLs
                .downloadUrl(attachment.getDownloadUrl())
                .thumbnailUrl(attachment.getThumbnailUrl())
                .build();
    }

    /**
     * Get file type display information
     */
    public FileTypeInfo getFileTypeInfo() {
        return FileTypeInfo.builder()
                .category(fileTypeCategory)
                .icon(fileIcon)
                .isImage(isImage)
                .isDocument(isDocument)
                .isVideo(isVideo)
                .isAudio(isAudio)
                .isArchive(isArchive)
                .build();
    }

    /**
     * Get security information
     */
    public SecurityInfo getSecurityInfo() {
        return SecurityInfo.builder()
                .isScanned(isScanned)
                .scanResult(scanResult)
                .isSafe(isSafe)
                .hasVirus(hasVirus)
                .isScanPending(isScanPending)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileTypeInfo {
        private String category;
        private String icon;
        private Boolean isImage;
        private Boolean isDocument;
        private Boolean isVideo;
        private Boolean isAudio;
        private Boolean isArchive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityInfo {
        private Boolean isScanned;
        private String scanResult;
        private Boolean isSafe;
        private Boolean hasVirus;
        private Boolean isScanPending;
    }
}
