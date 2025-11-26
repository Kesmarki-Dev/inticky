package com.kesmarki.inticky.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login response")
public class LoginResponse {

    @Schema(description = "JWT access token")
    private String token;

    @Schema(description = "User information")
    private UserResponse user;
}
