package com.ridelink.payment.service;

import com.ridelink.payment.dto.CoordinateDto;
import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.exception.BadRequestException;
import com.ridelink.payment.model.FareRule;
import com.ridelink.payment.repository.FareRuleRepository;
import com.ridelink.payment.service.impl.FareCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FareCalculationServiceTest {

    @Mock
    private FareRuleRepository fareRuleRepository;

    @InjectMocks
    private FareCalculationServiceImpl fareCalculationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fareCalculationService, "defaultBaseFare", 50.0);
        ReflectionTestUtils.setField(fareCalculationService, "defaultPerKmRate", 15.0);
        ReflectionTestUtils.setField(fareCalculationService, "defaultPerMinuteRate", 2.0);
        ReflectionTestUtils.setField(fareCalculationService, "defaultSurgeMultiplier", 1.0);
    }

    @Test
    @DisplayName("Happy Path: Estimate fare with direct distance and duration")
    void testEstimateFare_DirectDistanceAndDuration() {
        FareEstimateRequest request = FareEstimateRequest.builder()
                .distanceKm(10.0)
                .estimatedDurationMinutes(20)
                .vehicleCategory("ECONOMY")
                .surgeMultiplier(1.0)
                .build();

        FareRule economyRule = FareRule.builder()
                .vehicleCategory("ECONOMY")
                .baseFare(50.0)
                .perKmRate(15.0)
                .perMinuteRate(2.0)
                .minimumFare(60.0)
                .taxRatePercent(5.0)
                .build();

        when(fareRuleRepository.findByVehicleCategoryIgnoreCase("ECONOMY")).thenReturn(Optional.of(economyRule));

        FareEstimateResponse response = fareCalculationService.estimateFare(request);

        assertNotNull(response);
        assertEquals(50.0, response.getBaseFare());
        assertEquals(150.0, response.getDistanceFare()); // 10 * 15
        assertEquals(40.0, response.getTimeFare());     // 20 * 2
        // Subtotal = 50 + 150 + 40 = 240.0. Tax (5%) = 12.0. Total = 252.0
        assertEquals(12.0, response.getTaxAmount());
        assertEquals(252.0, response.getEstimatedTotalFare());
        assertEquals("INR", response.getCurrency());
    }

    @Test
    @DisplayName("Happy Path: Estimate fare with GPS coordinates using Haversine formula")
    void testEstimateFare_WithGPSCoordinates() {
        // Colombo Fort (6.9344, 79.8428) to Mount Lavinia (6.8400, 79.8650) ~ 10.7 km
        CoordinateDto pickup = CoordinateDto.builder().latitude(6.9344).longitude(79.8428).build();
        CoordinateDto dropoff = CoordinateDto.builder().latitude(6.8400).longitude(79.8650).build();

        FareEstimateRequest request = FareEstimateRequest.builder()
                .pickupCoordinates(pickup)
                .destinationCoordinates(dropoff)
                .vehicleCategory("ECONOMY")
                .surgeMultiplier(1.2)
                .build();

        FareRule economyRule = FareRule.builder()
                .vehicleCategory("ECONOMY")
                .baseFare(50.0)
                .perKmRate(15.0)
                .perMinuteRate(2.0)
                .minimumFare(60.0)
                .taxRatePercent(5.0)
                .build();

        when(fareRuleRepository.findByVehicleCategoryIgnoreCase("ECONOMY")).thenReturn(Optional.of(economyRule));

        FareEstimateResponse response = fareCalculationService.estimateFare(request);

        assertNotNull(response);
        assertTrue(response.getDistanceKm() > 10.0);
        assertTrue(response.getEstimatedDurationMinutes() > 0);
        assertEquals(1.2, response.getSurgeMultiplier());
        assertTrue(response.getEstimatedTotalFare() > response.getBaseFare());
    }

    @Test
    @DisplayName("Boundary / Negative: Missing both distance and coordinates throws BadRequestException")
    void testEstimateFare_MissingDistanceAndCoordinates_ThrowsException() {
        FareEstimateRequest request = FareEstimateRequest.builder()
                .vehicleCategory("ECONOMY")
                .build();

        assertThrows(BadRequestException.class, () -> fareCalculationService.estimateFare(request));
    }

    @Test
    @DisplayName("Happy Path: Calculate final fare with waiting time, surge, and promo discount")
    void testCalculateFinalFare_WithWaitingAndDiscount() {
        FinalFareRequest request = FinalFareRequest.builder()
                .rideId("RIDE-999")
                .vehicleCategory("PREMIUM")
                .actualDistanceKm(15.0)
                .actualDurationMinutes(30)
                .waitingTimeMinutes(10)
                .surgeMultiplier(1.2)
                .discountAmount(50.0)
                .build();

        FareRule premiumRule = FareRule.builder()
                .vehicleCategory("PREMIUM")
                .baseFare(100.0)
                .perKmRate(20.0)
                .perMinuteRate(3.0)
                .minimumFare(120.0)
                .taxRatePercent(5.0)
                .build();

        when(fareRuleRepository.findByVehicleCategoryIgnoreCase("PREMIUM")).thenReturn(Optional.of(premiumRule));

        FinalFareResponse response = fareCalculationService.calculateFinalFare(request);

        assertNotNull(response);
        assertEquals("RIDE-999", response.getRideId());
        assertEquals(100.0, response.getBaseFare());
        assertEquals(300.0, response.getDistanceFare()); // 15 * 20
        assertEquals(90.0, response.getTimeFare());     // 30 * 3
        assertEquals(30.0, response.getWaitingFare());  // 10 * 3
        // Subtotal = 100 + 300 + 90 + 30 = 520.0
        // Surged Subtotal = 520 * 1.2 = 624.0
        // Tax (5%) = 31.2
        // Total = 624.0 + 31.2 - 50.0 (discount) = 605.2
        assertEquals(520.0, response.getSubtotal());
        assertEquals(31.2, response.getTaxAmount());
        assertEquals(50.0, response.getDiscountAmount());
        assertEquals(605.2, response.getFinalFare());
    }

    @Test
    @DisplayName("Negative Scenario: Negative distance or duration throws BadRequestException")
    void testCalculateFinalFare_NegativeMetrics_ThrowsException() {
        FinalFareRequest request = FinalFareRequest.builder()
                .rideId("RIDE-ERR")
                .actualDistanceKm(-5.0)
                .actualDurationMinutes(20)
                .build();

        assertThrows(BadRequestException.class, () -> fareCalculationService.calculateFinalFare(request));
    }
}
