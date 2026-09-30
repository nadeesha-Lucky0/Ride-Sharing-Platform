package com.ridelink.driver.dto;

import com.ridelink.driver.model.VehicleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to register or update a vehicle associated with a driver")
public class VehicleRegistrationDto {

    @NotBlank(message = "Driver ID is required")
    @Schema(description = "Associated Driver Profile ID", example = "65f1a2b3c4d5e6f7a8b9c0d1")
    private String driverId;

    @NotBlank(message = "Vehicle make is required")
    @Schema(description = "Manufacturer / Make", example = "Toyota")
    private String make;

    @NotBlank(message = "Vehicle model is required")
    @Schema(description = "Vehicle Model", example = "Prius")
    private String model;

    @Min(value = 1990, message = "Year must be 1990 or newer")
    @Max(value = 2030, message = "Year cannot exceed 2030")
    @Schema(description = "Manufacture year", example = "2022")
    private int year;

    @NotBlank(message = "Vehicle color is required")
    @Schema(description = "Exterior Color", example = "Pearl White")
    private String color;

    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z0-9- ]{3,15}$", message = "License plate must be 3-15 alphanumeric characters and hyphens")
    @Schema(description = "Official License Plate Registration", example = "CAB-4589")
    private String licensePlate;

    @NotNull(message = "Vehicle category is required")
    @Schema(description = "Vehicle Category Class (SEDAN, SUV, HATCHBACK, TUK_TUK, VAN, PREMIUM, MOTORBIKE)", example = "SEDAN")
    private VehicleCategory category;

    @Min(value = 1, message = "Seating capacity must be at least 1 passenger")
    @Max(value = 60, message = "Seating capacity cannot exceed 60 passengers")
    @Schema(description = "Passenger seating capacity", example = "4")
    private int seatingCapacity;

    @Schema(description = "Whether this vehicle is actively in service", example = "true")
    private Boolean active;
}
