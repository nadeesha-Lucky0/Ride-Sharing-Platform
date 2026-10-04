package com.ridelink.ride.dto;

import com.ridelink.ride.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for creating a new ride request")
public class RideRequestDto {

    @NotBlank(message = "Passenger ID is required")
    @Schema(description = "Unique identifier of the passenger requesting the ride", example = "pass_65f1a2b3c4d5e6f7a8b9c0d1")
    private String passengerId;

    @NotNull(message = "Pickup location is required")
    @Valid
    @Schema(description = "Origin pickup location details")
    private Location pickupLocation;

    @NotNull(message = "Dropoff location is required")
    @Valid
    @Schema(description = "Destination dropoff location details")
    private Location dropoffLocation;

    @Schema(description = "Optional specific driver ID to request directly", example = "drv_89a1b2c3d4e5f6a7b8c9d0e1")
    private String requestedDriverId;
}
