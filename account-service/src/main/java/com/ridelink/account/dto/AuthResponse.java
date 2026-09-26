package com.ridelink.account.dto;

import com.ridelink.account.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response payload containing JWT token and basic user details")
public class AuthResponse {

    @Schema(description = "JWT Bearer access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2NWZhYmM...")
    private String token;

    @Schema(description = "Token type prefix", example = "Bearer")
    private String tokenType;

    @Schema(description = "Unique identifier of the user", example = "65fabc1234567890abcdef12")
    private String userId;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's assigned role", example = "PASSENGER")
    private Role role;
}
