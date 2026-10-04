package com.ridelink.driver.dto;

import com.ridelink.driver.model.DriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to register or update a driver operational profile")
public class DriverProfileDto {

    private String id;

    @NotBlank(message = "User ID is required")
    @Schema(description = "Account service User identifier", example = "usr_89a1b2c3d4e5f6a7b8c9d0e1")
    private String userId;

    @Schema(description = "Driver full name", example = "Sunil Perera")
    private String fullName;

    @Schema(description = "Driver phone number", example = "+94771234567")
    private String phoneNumber;

    @NotBlank(message = "License number is required")
    @Schema(description = "Government driver license number", example = "B1234567")
    private String licenseNumber;

    @Schema(description = "Initial or updated availability status", example = "AVAILABLE")
    private DriverStatus status;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    @Schema(description = "Current GPS latitude coordinate", example = "6.9271")
    private Double currentLatitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    @Schema(description = "Current GPS longitude coordinate", example = "79.8612")
    private Double currentLongitude;

    @Schema(description = "Current street address or landmark", example = "Colombo Fort Railway Station")
    private String currentAddress;

    @Schema(description = "Primary operating city", example = "Colombo")
    private String serviceCity;

    @Schema(description = "Operating district or province", example = "Western Province")
    private String serviceArea;
}
