package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.model.FareRule;
import com.ridelink.payment.repository.FareRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FareCalculationService {

    private final FareRuleRepository fareRuleRepository;

    @Value("${app.fare.base-fare:50.0}")
    private double defaultBaseFare;

    @Value("${app.fare.per-km-rate:15.0}")
    private double defaultPerKmRate;

    @Value("${app.fare.per-minute-rate:2.0}")
    private double defaultPerMinuteRate;

    @Value("${app.fare.surge-multiplier:1.0}")
    private double defaultSurgeMultiplier;

    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        double baseFare = defaultBaseFare;
        double perKmRate = defaultPerKmRate;
        double perMinuteRate = defaultPerMinuteRate;

        if (request.getVehicleCategory() != null) {
            FareRule rule = fareRuleRepository.findByVehicleCategory(request.getVehicleCategory()).orElse(null);
            if (rule != null) {
                baseFare = rule.getBaseFare();
                perKmRate = rule.getPerKmRate();
                perMinuteRate = rule.getPerMinuteRate();
            }
        }

        double distanceFare = request.getDistanceKm() * perKmRate;
        double timeFare = request.getEstimatedDurationMinutes() * perMinuteRate;
        double surgeMultiplier = defaultSurgeMultiplier;

        double subtotal = baseFare + distanceFare + timeFare;
        double total = Math.round(subtotal * surgeMultiplier * 100.0) / 100.0;

        return FareEstimateResponse.builder()
                .baseFare(baseFare)
                .distanceFare(Math.round(distanceFare * 100.0) / 100.0)
                .timeFare(Math.round(timeFare * 100.0) / 100.0)
                .surgeMultiplier(surgeMultiplier)
                .estimatedTotalFare(total)
                .currency("INR")
                .build();
    }
}
