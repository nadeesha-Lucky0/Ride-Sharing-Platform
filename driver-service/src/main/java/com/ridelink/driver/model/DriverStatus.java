package com.ridelink.driver.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operating availability status of a driver")
public enum DriverStatus {
    ONLINE,
    AVAILABLE,
    OFFLINE,
    BUSY,
    SUSPENDED;

    public boolean isEligibleForRides() {
        return this == AVAILABLE || this == ONLINE;
    }
}
