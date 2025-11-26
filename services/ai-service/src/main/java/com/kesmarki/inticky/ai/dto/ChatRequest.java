package com.kesmarki.inticky.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for AI chat operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message must not exceed 4000 characters")
    private String message;

    private UUID sessionId;

    private String context;

    private UUID ticketId;

    @Builder.Default
    private Boolean createSession = false;

    private String sessionTitle;

    private String sessionDescription;
}
