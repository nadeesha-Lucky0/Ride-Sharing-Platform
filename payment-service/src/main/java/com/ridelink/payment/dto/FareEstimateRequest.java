package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for fare estimation calculation")
public class FareEstimateRequest {

    @Schema(description = "Optional text name or address of the pickup location", example = "Colombo Fort")
    private String pickupLocation;

    @Schema(description = "Optional text name or address of the destination location", example = "Mount Lavinia Beach")
    private String destinationLocation;

    @Valid
    @Schema(description = "Optional pickup GPS coordinates")
    private CoordinateDto pickupCoordinates;

    @Valid
    @Schema(description = "Optional destination GPS coordinates")
    private CoordinateDto destinationCoordinates;

    @Positive(message = "Distance must be greater than 0")
    @Schema(description = "Distance in kilometers (auto-calculated from coordinates if omitted)", example = "12.5")
    private Double distanceKm;

    @Positive(message = "Duration must be greater than 0")
    @Schema(description = "Estimated trip duration in minutes (auto-estimated if omitted)", example = "25")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Vehicle Category (e.g., ECONOMY, PREMIUM, BIKE, AUTO, XL)", example = "ECONOMY")
    private String vehicleCategory;

    @Positive(message = "Surge multiplier must be greater than 0")
    @Schema(description = "Surge multiplier during peak hours (defaults to 1.0)", example = "1.2")
    private Double surgeMultiplier;
}
