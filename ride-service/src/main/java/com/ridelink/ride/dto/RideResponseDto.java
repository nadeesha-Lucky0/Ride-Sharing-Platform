package com.ridelink.ride.dto;

import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.RideStatus;
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
@Schema(description = "Response payload representing full ride details and status")
public class RideResponseDto {

    @Schema(description = "Unique ride identifier", example = "65f1a2b3c4d5e6f7a8b9c0d1")
    private String id;

    @Schema(description = "Passenger identifier", example = "pass_65f1a2b3c4d5e6f7a8b9c0d1")
    private String passengerId;

    @Schema(description = "Assigned driver identifier", example = "drv_89a1b2c3d4e5f6a7b8c9d0e1")
    private String driverId;

    @Schema(description = "Pickup location coordinates and address")
    private Location pickupLocation;

    @Schema(description = "Dropoff destination coordinates and address")
    private Location dropoffLocation;

    @Schema(description = "Current lifecycle status of the ride", example = "REQUESTED")
    private RideStatus status;

    @Schema(description = "Estimated ride fare in LKR/USD", example = "850.00")
    private Double estimatedFare;

    @Schema(description = "Final charged fare upon completion in LKR/USD", example = "850.00")
    private Double actualFare;

    @Schema(description = "Calculated route distance in kilometers", example = "8.45")
    private Double distanceKm;

    @Schema(description = "Estimated trip duration in minutes", example = "22")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Timestamp when ride was requested")
    private LocalDateTime requestedAt;

    @Schema(description = "Timestamp when driver accepted the ride")
    private LocalDateTime acceptedAt;

    @Schema(description = "Timestamp when ride journey started")
    private LocalDateTime startedAt;

    @Schema(description = "Timestamp when ride was successfully completed")
    private LocalDateTime completedAt;

    @Schema(description = "Timestamp when ride was cancelled")
    private LocalDateTime cancelledAt;

    @Schema(description = "Reason provided for cancellation", example = "Passenger found alternative transport")
    private String cancellationReason;
}
