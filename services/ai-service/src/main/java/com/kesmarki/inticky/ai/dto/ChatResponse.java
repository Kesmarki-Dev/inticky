package com.kesmarki.inticky.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for AI chat operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String response;

    private UUID sessionId;

    private String sessionTitle;

    private Boolean sessionCreated;

    private Integer messageCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Long responseTimeMs;

    private String model;

    private Integer tokensUsed;

    private Boolean fromCache;

    private String requestId;
}
