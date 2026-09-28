package com.ridelink.ride.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to assign or match a driver to a ride")
public class DriverAssignDto {

    @NotBlank(message = "Driver ID is required")
    @Schema(description = "Unique driver identifier to assign to the ride", example = "drv_89a1b2c3d4e5f6a7b8c9d0e1")
    private String driverId;
}
