package com.ridelink.driver.dto;

import com.ridelink.driver.model.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAvailabilityDto {

    @NotNull(message = "Status is required")
    private DriverStatus status;
}
