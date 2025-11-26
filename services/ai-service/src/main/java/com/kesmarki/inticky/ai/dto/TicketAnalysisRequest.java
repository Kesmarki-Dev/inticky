package com.kesmarki.inticky.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for ticket analysis operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAnalysisRequest {

    private UUID ticketId;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    private String description;

    private String category;

    private String currentPriority;

    private String currentAssignee;

    @Builder.Default
    private Boolean includeKeywords = true;

    @Builder.Default
    private Boolean includeSentiment = true;

    @Builder.Default
    private Boolean suggestCategory = true;

    @Builder.Default
    private Boolean suggestPriority = true;

    @Builder.Default
    private Boolean suggestAssignee = true;
}
