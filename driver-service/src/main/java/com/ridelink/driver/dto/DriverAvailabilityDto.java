package com.ridelink.driver.dto;

import com.ridelink.driver.model.DriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to update a driver's operational availability")
public class DriverAvailabilityDto {

    @NotNull(message = "Status is required")
    @Schema(description = "New availability status (ONLINE, AVAILABLE, OFFLINE, BUSY, SUSPENDED)", example = "AVAILABLE")
    private DriverStatus status;
}
