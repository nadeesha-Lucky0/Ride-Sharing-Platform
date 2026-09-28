package com.ridelink.ride.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle states for a ride request")
public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    DRIVER_ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
