package com.ridelink.ride.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to cancel a ride")
public class RideCancelDto {

    @NotBlank(message = "Cancelling User ID is required")
    @Schema(description = "ID of passenger or driver requesting the cancellation", example = "pass_65f1a2b3c4d5e6f7a8b9c0d1")
    private String userId;

    @Schema(description = "Reason for ride cancellation", example = "Found alternate transportation")
    private String reason;
}
