package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for calculating the final fare upon trip completion")
public class FinalFareRequest {

    @NotBlank(message = "Ride ID is required")
    @Schema(description = "Identifier of the completed ride", example = "RIDE-98124")
    private String rideId;

    @Schema(description = "Vehicle category (ECONOMY, PREMIUM, BIKE, AUTO, XL)", example = "ECONOMY")
    private String vehicleCategory;

    @NotNull(message = "Actual distance in KM is required")
    @PositiveOrZero(message = "Actual distance must be zero or positive")
    @Schema(description = "Actual distance traveled in kilometers", example = "14.2")
    private Double actualDistanceKm;

    @NotNull(message = "Actual duration in minutes is required")
    @PositiveOrZero(message = "Actual duration must be zero or positive")
    @Schema(description = "Actual trip duration in minutes", example = "32")
    private Integer actualDurationMinutes;

    @Positive(message = "Surge multiplier must be positive")
    @Schema(description = "Surge multiplier at the time the ride was taken (defaults to 1.0)", example = "1.1")
    private Double surgeMultiplier;

    @PositiveOrZero(message = "Waiting time must be zero or positive")
    @Schema(description = "Driver waiting time before pickup in minutes", example = "5")
    private Integer waitingTimeMinutes;

    @PositiveOrZero(message = "Discount amount must be zero or positive")
    @Schema(description = "Promotional discount or coupon deduction amount", example = "20.0")
    private Double discountAmount;
}
