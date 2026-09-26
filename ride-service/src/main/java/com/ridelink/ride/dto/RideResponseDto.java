package com.ridelink.ride.dto;

import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponseDto {
    private String id;
    private String passengerId;
    private String driverId;
    private Location pickupLocation;
    private Location dropoffLocation;
    private RideStatus status;
    private Double estimatedFare;
    private Double actualFare;
    private Double distanceKm;
    private Integer estimatedDurationMinutes;
    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
}
