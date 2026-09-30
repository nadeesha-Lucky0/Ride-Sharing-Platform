package com.ridelink.driver.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categorization of the driver's vehicle")
public enum VehicleCategory {
    SEDAN,
    SUV,
    HATCHBACK,
    TUK_TUK,
    VAN,
    PREMIUM,
    MOTORBIKE
}
