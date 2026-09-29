package com.ridelink.driver.dto;

import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.model.Vehicle;
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
@Schema(description = "Response payload representing full driver profile, vehicle details, and location metrics")
public class DriverResponseDto {

    @Schema(description = "Driver unique identifier", example = "65f1a2b3c4d5e6f7a8b9c0d1")
    private String id;

    @Schema(description = "Associated user account ID", example = "usr_89a1b2c3d4e5f6a7b8c9d0e1")
    private String userId;

    @Schema(description = "Driver full name", example = "Sunil Perera")
    private String fullName;

    @Schema(description = "Driver telephone contact", example = "+94771234567")
    private String phoneNumber;

    @Schema(description = "Driver license number", example = "B1234567")
    private String licenseNumber;

    @Schema(description = "Current availability status", example = "AVAILABLE")
    private DriverStatus status;

    @Schema(description = "Current latitude coordinate", example = "6.9271")
    private Double currentLatitude;

    @Schema(description = "Current longitude coordinate", example = "79.8612")
    private Double currentLongitude;

    @Schema(description = "Current landmark or street location", example = "Colombo Fort Railway Station")
    private String currentAddress;

    @Schema(description = "Operating city", example = "Colombo")
    private String serviceCity;

    @Schema(description = "Operating zone", example = "Western Province")
    private String serviceArea;

    @Schema(description = "Average driver rating", example = "4.92")
    private Double rating;

    @Schema(description = "Total completed rides", example = "142")
    private Integer totalRides;

    @Schema(description = "Active vehicle details if registered")
    private Vehicle activeVehicle;

    @Schema(description = "Calculated distance from search origin in km (for proximity queries)", example = "2.35")
    private Double distanceKm;

    @Schema(description = "Timestamp of last location update")
    private LocalDateTime lastLocationUpdate;

    @Schema(description = "Profile creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last profile update timestamp")
    private LocalDateTime updatedAt;
}
