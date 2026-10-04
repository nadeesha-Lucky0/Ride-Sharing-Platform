package com.ridelink.driver.dto;

import com.ridelink.driver.model.VehicleCategory;
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
@Schema(description = "Response payload representing vehicle details")
public class VehicleResponseDto {

    @Schema(description = "Vehicle ID", example = "65f1a2b3c4d5e6f7a8b9c0d2")
    private String id;

    @Schema(description = "Driver identifier who operates this vehicle", example = "65f1a2b3c4d5e6f7a8b9c0d1")
    private String driverId;

    @Schema(description = "Manufacturer / Make", example = "Toyota")
    private String make;

    @Schema(description = "Vehicle Model", example = "Prius")
    private String model;

    @Schema(description = "Manufacture year", example = "2022")
    private int year;

    @Schema(description = "Exterior Color", example = "Pearl White")
    private String color;

    @Schema(description = "License Plate", example = "CAB-4589")
    private String licensePlate;

    @Schema(description = "Category Class", example = "SEDAN")
    private VehicleCategory category;

    @Schema(description = "Passenger seating capacity", example = "4")
    private int seatingCapacity;

    @Schema(description = "Whether vehicle is actively deployed", example = "true")
    private boolean active;

    @Schema(description = "Registration timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
