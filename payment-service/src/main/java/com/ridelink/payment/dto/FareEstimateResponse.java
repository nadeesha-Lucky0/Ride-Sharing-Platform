package com.ridelink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateResponse {
    private Double baseFare;
    private Double distanceFare;
    private Double timeFare;
    private Double surgeMultiplier;
    private Double estimatedTotalFare;
    private String currency;
}

