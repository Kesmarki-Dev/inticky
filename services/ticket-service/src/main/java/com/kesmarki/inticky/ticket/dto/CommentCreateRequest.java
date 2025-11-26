package com.kesmarki.inticky.ticket.dto;

import com.kesmarki.inticky.ticket.enums.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new comment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotNull(message = "Ticket ID is required")
    private UUID ticketId;

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    @Builder.Default
    private CommentType type = CommentType.PUBLIC;

    @NotNull(message = "Author ID is required")
    private UUID authorId;

    private String authorName;

    private String authorEmail;

    @Builder.Default
    private Boolean isPinned = false;

    private Integer timeSpentMinutes;

    private String metadata;
}
