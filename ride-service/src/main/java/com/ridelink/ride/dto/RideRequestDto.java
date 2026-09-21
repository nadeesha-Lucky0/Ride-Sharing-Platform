package com.ridelink.ride.dto;

import com.ridelink.ride.model.Location;
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
public class RideRequestDto {

    @NotBlank(message = "Passenger ID is required")
    private String passengerId;

    @NotNull(message = "Pickup location is required")
    private Location pickupLocation;

    @NotNull(message = "Dropoff location is required")
    private Location dropoffLocation;
}
