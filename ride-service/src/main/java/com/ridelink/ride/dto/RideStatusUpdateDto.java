package com.ridelink.ride.dto;

import com.ridelink.ride.model.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to transition ride to a new lifecycle status")
public class RideStatusUpdateDto {

    @NotNull(message = "Status is required")
    @Schema(description = "Target status to transition to", example = "ACCEPTED")
    private RideStatus status;

    @Schema(description = "Driver ID performing the status change (required when accepting or assigning)", example = "drv_89a1b2c3d4e5f6a7b8c9d0e1")
    private String driverId;

    @Schema(description = "Reason for cancellation if transitioning to CANCELLED", example = "Driver vehicle broke down")
    private String cancellationReason;
}
