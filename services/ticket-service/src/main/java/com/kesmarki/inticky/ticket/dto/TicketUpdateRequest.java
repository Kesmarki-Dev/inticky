package com.kesmarki.inticky.ticket.dto;

import com.kesmarki.inticky.ticket.enums.Category;
import com.kesmarki.inticky.ticket.enums.Priority;
import com.kesmarki.inticky.ticket.enums.TicketStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for updating ticket information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdateRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    private String description;

    private TicketStatus status;

    private Priority priority;

    private Category category;

    private UUID assigneeId;

    private String assigneeName;

    private String assigneeEmail;

    private LocalDateTime dueDate;

    private Double estimatedHours;

    private Double actualHours;

    private List<String> tags;

    private String customFields; // JSON string

    private String resolution;

    private Integer satisfactionRating;

    private String customerFeedback;
}
