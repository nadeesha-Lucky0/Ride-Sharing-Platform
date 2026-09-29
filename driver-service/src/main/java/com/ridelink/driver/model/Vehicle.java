package com.ridelink.driver.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vehicles")
@Schema(description = "Vehicle registry document linked to a driver")
public class Vehicle {

    @Id
    @Schema(description = "Unique vehicle record identifier", example = "65f1a2b3c4d5e6f7a8b9c0d2")
    private String id;

    @Indexed
    @Schema(description = "Driver identifier who operates this vehicle", example = "65f1a2b3c4d5e6f7a8b9c0d1")
    private String driverId;

    @Schema(description = "Vehicle manufacturer/make", example = "Toyota")
    private String make;

    @Schema(description = "Vehicle model name", example = "Prius")
    private String model;

    @Schema(description = "Manufacturing year", example = "2022")
    private int year;

    @Schema(description = "Vehicle exterior color", example = "Pearl White")
    private String color;

    @Indexed(unique = true)
    @Schema(description = "Official registration license plate number", example = "CAB-4589")
    private String licensePlate;

    @Schema(description = "Category class of the vehicle", example = "SEDAN")
    private VehicleCategory category;

    @Schema(description = "Passenger seating capacity excluding driver", example = "4")
    private int seatingCapacity;

    @Schema(description = "Whether this vehicle is active and approved for service", example = "true")
    private boolean active;

    @CreatedDate
    @Schema(description = "Registration timestamp")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
