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
@Document(collection = "drivers")
@Schema(description = "Driver operational profile document")
public class Driver {

    @Id
    @Schema(description = "Unique driver profile ID", example = "65f1a2b3c4d5e6f7a8b9c0d1")
    private String id;

    @Indexed(unique = true)
    @Schema(description = "Associated user account identifier", example = "usr_89a1b2c3d4e5f6a7b8c9d0e1")
    private String userId;

    @Schema(description = "Driver full name", example = "Sunil Perera")
    private String fullName;

    @Schema(description = "Driver contact telephone number", example = "+94771234567")
    private String phoneNumber;

    @Indexed(unique = true)
    @Schema(description = "Driver legal license number", example = "B1234567")
    private String licenseNumber;

    @Schema(description = "Current operational availability status", example = "AVAILABLE")
    private DriverStatus status;

    @Schema(description = "Current simulated GPS latitude coordinate", example = "6.9271")
    private Double currentLatitude;

    @Schema(description = "Current simulated GPS longitude coordinate", example = "79.8612")
    private Double currentLongitude;

    @Schema(description = "Current place name or address location", example = "Colombo Fort Railway Station")
    private String currentAddress;

    @Schema(description = "Primary city or metropolitan operating area", example = "Colombo")
    private String serviceCity;

    @Schema(description = "Operating zone or district", example = "Western Province")
    private String serviceArea;

    @Schema(description = "Driver aggregate rating (1.0 - 5.0)", example = "4.92")
    private Double rating;

    @Schema(description = "Total completed ride count", example = "142")
    private Integer totalRides;

    @Schema(description = "ID of the vehicle currently actively driven", example = "veh_99a1b2c3d4e5f6a7b8c9d0e1")
    private String activeVehicleId;

    @Schema(description = "Timestamp of the most recent GPS location update")
    private LocalDateTime lastLocationUpdate;

    @CreatedDate
    @Schema(description = "Account registration timestamp")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Schema(description = "Last profile modification timestamp")
    private LocalDateTime updatedAt;
}
