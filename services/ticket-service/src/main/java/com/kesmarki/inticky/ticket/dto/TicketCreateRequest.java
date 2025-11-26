package com.kesmarki.inticky.ticket.dto;

import com.kesmarki.inticky.ticket.enums.Category;
import com.kesmarki.inticky.ticket.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    private String description;

    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Builder.Default
    private Category category = Category.SUPPORT;

    @NotNull(message = "Reporter ID is required")
    private UUID reporterId;

    private String reporterName;

    private String reporterEmail;

    private UUID assigneeId;

    private String assigneeName;

    private String assigneeEmail;

    private LocalDateTime dueDate;

    private Double estimatedHours;

    private List<String> tags;

    private String customFields; // JSON string

    private String resolution;
}
