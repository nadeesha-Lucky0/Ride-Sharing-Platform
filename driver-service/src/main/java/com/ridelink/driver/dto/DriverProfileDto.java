package com.ridelink.driver.dto;

import com.ridelink.driver.model.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileDto {

    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private DriverStatus status;

    private Double currentLatitude;

    private Double currentLongitude;

    private Double rating;

    private Integer totalRides;
}
