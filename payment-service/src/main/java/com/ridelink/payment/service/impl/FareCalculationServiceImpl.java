package com.ridelink.payment.service.impl;

import com.ridelink.payment.dto.CoordinateDto;
import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.exception.BadRequestException;
import com.ridelink.payment.model.FareRule;
import com.ridelink.payment.repository.FareRuleRepository;
import com.ridelink.payment.service.FareCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Enterprise implementation of FareCalculationService for RideLink.
 * 
 * ==============================================================================
 * FARE CALCULATION FORMULA / BUSINESS RULES:
 * ==============================================================================
 * 1. Distance Calculation (if GPS coordinates provided):
 *    Haversine Great-Circle Distance:
 *    a = sin²(Δlat / 2) + cos(lat1) * cos(lat2) * sin²(Δlon / 2)
 *    c = 2 * atan2(√a, √(1-a))
 *    Distance (KM) = 6371.0 * c
 *
 * 2. Subtotal Formula:
 *    DistanceFare = Distance (KM) * PerKmRate
 *    TimeFare     = Duration (Minutes) * PerMinuteRate
 *    WaitingFare  = WaitingTime (Minutes) * PerMinuteRate
 *    Subtotal     = BaseFare + DistanceFare + TimeFare + WaitingFare
 *
 * 3. Surge & Minimum Fare:
 *    SurgedAmount = Max(Subtotal * SurgeMultiplier, MinimumFare)
 *
 * 4. Taxes & Discounts:
 *    TaxAmount    = SurgedAmount * (TaxRatePercent / 100.0)
 *    FinalTotal   = Max(0.0, SurgedAmount + TaxAmount - DiscountAmount)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FareCalculationServiceImpl implements FareCalculationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_CITY_SPEED_KMH = 30.0;

    private final FareRuleRepository fareRuleRepository;

    @Value("${app.fare.base-fare:50.0}")
    private double defaultBaseFare;

    @Value("${app.fare.per-km-rate:15.0}")
    private double defaultPerKmRate;

    @Value("${app.fare.per-minute-rate:2.0}")
    private double defaultPerMinuteRate;

    @Value("${app.fare.surge-multiplier:1.0}")
    private double defaultSurgeMultiplier;

    @Override
    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        log.info("Calculating fare estimate for category: {}", request.getVehicleCategory());

        double distanceKm = resolveDistance(request);
        int durationMinutes = resolveDuration(request, distanceKm);

        FareRule rule = resolveFareRule(request.getVehicleCategory());
        double baseFare = rule.getBaseFare();
        double perKmRate = rule.getPerKmRate();
        double perMinuteRate = rule.getPerMinuteRate();
        double minimumFare = rule.getMinimumFare() != null ? rule.getMinimumFare() : baseFare;
        double taxRatePercent = rule.getTaxRatePercent() != null ? rule.getTaxRatePercent() : 5.0;

        double surgeMultiplier = (request.getSurgeMultiplier() != null && request.getSurgeMultiplier() > 0)
                ? request.getSurgeMultiplier()
                : defaultSurgeMultiplier;

        double distanceFare = round(distanceKm * perKmRate);
        double timeFare = round(durationMinutes * perMinuteRate);
        double subtotal = baseFare + distanceFare + timeFare;

        double surgedSubtotal = Math.max(subtotal * surgeMultiplier, minimumFare);
        double taxAmount = round(surgedSubtotal * (taxRatePercent / 100.0));
        double estimatedTotal = round(surgedSubtotal + taxAmount);

        String formula = String.format("Total = (BaseFare[%.2f] + (%.2f km * %.2f) + (%d mins * %.2f)) * Surge[%.2f] + Tax[%.2f%%]",
                baseFare, distanceKm, perKmRate, durationMinutes, perMinuteRate, surgeMultiplier, taxRatePercent);

        return FareEstimateResponse.builder()
                .vehicleCategory(rule.getVehicleCategory() != null ? rule.getVehicleCategory() : "STANDARD")
                .distanceKm(round(distanceKm))
                .estimatedDurationMinutes(durationMinutes)
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .timeFare(timeFare)
                .surgeMultiplier(surgeMultiplier)
                .taxAmount(taxAmount)
                .estimatedTotalFare(estimatedTotal)
                .currency("INR")
                .calculationFormula(formula)
                .build();
    }

    @Override
    public FinalFareResponse calculateFinalFare(FinalFareRequest request) {
        log.info("Calculating final fare for ride: {}", request.getRideId());

        if (request.getActualDistanceKm() < 0 || request.getActualDurationMinutes() < 0) {
            throw new BadRequestException("Distance and duration must be non-negative values.");
        }

        FareRule rule = resolveFareRule(request.getVehicleCategory());
        double baseFare = rule.getBaseFare();
        double perKmRate = rule.getPerKmRate();
        double perMinuteRate = rule.getPerMinuteRate();
        double minimumFare = rule.getMinimumFare() != null ? rule.getMinimumFare() : baseFare;
        double taxRatePercent = rule.getTaxRatePercent() != null ? rule.getTaxRatePercent() : 5.0;

        double surgeMultiplier = (request.getSurgeMultiplier() != null && request.getSurgeMultiplier() > 0)
                ? request.getSurgeMultiplier()
                : 1.0;

        int waitingMinutes = request.getWaitingTimeMinutes() != null ? Math.max(0, request.getWaitingTimeMinutes()) : 0;
        double discountAmount = request.getDiscountAmount() != null ? Math.max(0, request.getDiscountAmount()) : 0.0;

        double distanceFare = round(request.getActualDistanceKm() * perKmRate);
        double timeFare = round(request.getActualDurationMinutes() * perMinuteRate);
        double waitingFare = round(waitingMinutes * perMinuteRate);

        double subtotal = baseFare + distanceFare + timeFare + waitingFare;
        double surgedSubtotal = Math.max(subtotal * surgeMultiplier, minimumFare);
        double taxAmount = round(surgedSubtotal * (taxRatePercent / 100.0));

        double finalFare = Math.max(0.0, round(surgedSubtotal + taxAmount - discountAmount));

        return FinalFareResponse.builder()
                .rideId(request.getRideId())
                .vehicleCategory(rule.getVehicleCategory() != null ? rule.getVehicleCategory() : "STANDARD")
                .actualDistanceKm(round(request.getActualDistanceKm()))
                .actualDurationMinutes(request.getActualDurationMinutes())
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .timeFare(timeFare)
                .waitingFare(waitingFare)
                .surgeMultiplier(surgeMultiplier)
                .subtotal(round(subtotal))
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .finalFare(finalFare)
                .currency("INR")
                .build();
    }

    @Override
    public List<FareRule> getAllFareRules() {
        return fareRuleRepository.findAll();
    }

    @Override
    public FareRule resolveFareRule(String vehicleCategory) {
        if (vehicleCategory != null && !vehicleCategory.trim().isEmpty()) {
            var existing = fareRuleRepository.findByVehicleCategoryIgnoreCase(vehicleCategory.trim());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        return FareRule.builder()
                .vehicleCategory(vehicleCategory != null ? vehicleCategory.toUpperCase() : "STANDARD")
                .baseFare(defaultBaseFare)
                .perKmRate(defaultPerKmRate)
                .perMinuteRate(defaultPerMinuteRate)
                .minimumFare(defaultBaseFare)
                .taxRatePercent(5.0)
                .build();
    }

    @Override
    public double calculateHaversineDistance(CoordinateDto start, CoordinateDto end) {
        if (start == null || end == null) {
            throw new BadRequestException("Start and destination coordinates are required for GPS calculation.");
        }

        double lat1 = Math.toRadians(start.getLatitude());
        double lon1 = Math.toRadians(start.getLongitude());
        double lat2 = Math.toRadians(end.getLatitude());
        double lon2 = Math.toRadians(end.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return round(EARTH_RADIUS_KM * c);
    }

    private double resolveDistance(FareEstimateRequest request) {
        if (request.getDistanceKm() != null && request.getDistanceKm() > 0) {
            return request.getDistanceKm();
        }
        if (request.getPickupCoordinates() != null && request.getDestinationCoordinates() != null) {
            return calculateHaversineDistance(request.getPickupCoordinates(), request.getDestinationCoordinates());
        }
        throw new BadRequestException("Either distanceKm or both pickupCoordinates and destinationCoordinates must be provided.");
    }

    private int resolveDuration(FareEstimateRequest request, double distanceKm) {
        if (request.getEstimatedDurationMinutes() != null && request.getEstimatedDurationMinutes() > 0) {
            return request.getEstimatedDurationMinutes();
        }
        int estimated = (int) Math.ceil((distanceKm / AVERAGE_CITY_SPEED_KMH) * 60.0);
        return Math.max(5, estimated);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
