package com.ridelink.account.dto;

import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile details response")
public class UserProfileDto {

    @Schema(description = "Unique user identifier", example = "65fabc1234567890abcdef12")
    private String id;

    @Schema(description = "Registered email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Contact phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Assigned user role", example = "PASSENGER")
    private Role role;

    @Schema(description = "Current account status", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Account active flag", example = "true")
    private boolean active;

    @Schema(description = "Timestamp when account was created", example = "2026-03-26T10:15:30")
    private LocalDateTime createdAt;
}
