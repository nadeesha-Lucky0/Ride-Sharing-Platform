package com.ridelink.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateRequest {

    @NotNull(message = "Distance in KM is required")
    @Positive(message = "Distance must be greater than 0")
    private Double distanceKm;

    @NotNull(message = "Estimated duration is required")
    @Positive(message = "Duration must be greater than 0")
    private Integer estimatedDurationMinutes;

    private String vehicleCategory;
}
