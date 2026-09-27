package com.ridelink.payment.service;

import com.ridelink.payment.dto.CoordinateDto;
import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.model.FareRule;

import java.util.List;

/**
 * Service interface defining contract for RideLink fare calculations.
 */
public interface FareCalculationService {

    /**
     * Calculates an upfront estimated fare based on distance or pickup/dropoff GPS coordinates, duration, and vehicle category.
     */
    FareEstimateResponse estimateFare(FareEstimateRequest request);

    /**
     * Calculates the finalized fare upon ride completion using actual distance, actual duration, driver waiting time, surge, and promo discounts.
     */
    FinalFareResponse calculateFinalFare(FinalFareRequest request);

    /**
     * Retrieves all available fare rules.
     */
    List<FareRule> getAllFareRules();

    /**
     * Resolves the fare rule for a given category.
     */
    FareRule resolveFareRule(String vehicleCategory);

    /**
     * Calculates distance between two GPS coordinates using the Haversine formula.
     */
    double calculateHaversineDistance(CoordinateDto start, CoordinateDto end);
}
