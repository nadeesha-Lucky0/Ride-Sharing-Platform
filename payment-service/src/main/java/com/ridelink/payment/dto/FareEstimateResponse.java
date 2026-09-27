package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload containing the calculated fare breakdown")
public class FareEstimateResponse {

    @Schema(description = "Applied vehicle category", example = "ECONOMY")
    private String vehicleCategory;

    @Schema(description = "Calculated or provided distance in KM", example = "12.5")
    private Double distanceKm;

    @Schema(description = "Calculated or provided estimated duration in minutes", example = "25")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Base fare charged for the ride", example = "50.0")
    private Double baseFare;

    @Schema(description = "Distance-based fare (distance * perKmRate)", example = "187.5")
    private Double distanceFare;

    @Schema(description = "Time-based fare (duration * perMinuteRate)", example = "50.0")
    private Double timeFare;

    @Schema(description = "Applied surge pricing multiplier", example = "1.0")
    private Double surgeMultiplier;

    @Schema(description = "Calculated tax amount", example = "14.38")
    private Double taxAmount;

    @Schema(description = "Final estimated total fare", example = "301.88")
    private Double estimatedTotalFare;

    @Schema(description = "Currency denomination", example = "INR")
    private String currency;

    @Schema(description = "Formula summary applied during estimation", example = "Total = (BaseFare + (Distance * Rate) + (Duration * TimeRate)) * SurgeMultiplier + Tax")
    private String calculationFormula;
}
