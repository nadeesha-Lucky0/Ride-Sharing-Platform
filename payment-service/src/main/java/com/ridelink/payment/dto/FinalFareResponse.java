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
@Schema(description = "Response payload containing the finalized fare calculation for completed trip")
public class FinalFareResponse {

    @Schema(description = "Ride identifier", example = "RIDE-98124")
    private String rideId;

    @Schema(description = "Applied vehicle category", example = "ECONOMY")
    private String vehicleCategory;

    @Schema(description = "Actual distance traveled in KM", example = "14.2")
    private Double actualDistanceKm;

    @Schema(description = "Actual trip duration in minutes", example = "32")
    private Integer actualDurationMinutes;

    @Schema(description = "Base fare charged", example = "50.0")
    private Double baseFare;

    @Schema(description = "Distance fare calculated", example = "213.0")
    private Double distanceFare;

    @Schema(description = "Time elapsed fare calculated", example = "64.0")
    private Double timeFare;

    @Schema(description = "Waiting time fare", example = "10.0")
    private Double waitingFare;

    @Schema(description = "Surge pricing multiplier applied", example = "1.1")
    private Double surgeMultiplier;

    @Schema(description = "Subtotal before taxes and discounts", example = "370.7")
    private Double subtotal;

    @Schema(description = "Tax applied (e.g. 5%)", example = "18.54")
    private Double taxAmount;

    @Schema(description = "Promotional discount deducted", example = "20.0")
    private Double discountAmount;

    @Schema(description = "Final total payable fare", example = "369.24")
    private Double finalFare;

    @Schema(description = "Currency code", example = "INR")
    private String currency;
}
