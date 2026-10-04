package com.ridelink.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.exception.GlobalExceptionHandler;
import com.ridelink.payment.service.FareCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FareControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private FareCalculationService fareCalculationService;

    @InjectMocks
    private FareController fareController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(fareController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/fare/estimate - Should return 200 and fare estimate")
    void testEstimateFare_Success() throws Exception {
        FareEstimateRequest request = FareEstimateRequest.builder()
                .distanceKm(12.0)
                .estimatedDurationMinutes(25)
                .vehicleCategory("ECONOMY")
                .surgeMultiplier(1.0)
                .build();

        FareEstimateResponse response = FareEstimateResponse.builder()
                .vehicleCategory("ECONOMY")
                .distanceKm(12.0)
                .estimatedDurationMinutes(25)
                .baseFare(50.0)
                .distanceFare(180.0)
                .timeFare(50.0)
                .surgeMultiplier(1.0)
                .taxAmount(14.0)
                .estimatedTotalFare(294.0)
                .currency("INR")
                .build();

        when(fareCalculationService.estimateFare(any(FareEstimateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/fare/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedTotalFare").value(294.0))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.baseFare").value(50.0));
    }

    @Test
    @DisplayName("POST /api/fare/estimate - Validation Failure on negative distance")
    void testEstimateFare_ValidationFailure() throws Exception {
        FareEstimateRequest request = FareEstimateRequest.builder()
                .distanceKm(-10.0)
                .build();

        mockMvc.perform(post("/api/fare/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/fare/calculate-final - Should return 200 and final fare calculation")
    void testCalculateFinalFare_Success() throws Exception {
        FinalFareRequest request = FinalFareRequest.builder()
                .rideId("RIDE-12345")
                .actualDistanceKm(8.5)
                .actualDurationMinutes(18)
                .vehicleCategory("ECONOMY")
                .build();

        FinalFareResponse response = FinalFareResponse.builder()
                .rideId("RIDE-12345")
                .vehicleCategory("ECONOMY")
                .actualDistanceKm(8.5)
                .actualDurationMinutes(18)
                .baseFare(50.0)
                .finalFare(220.0)
                .currency("INR")
                .build();

        when(fareCalculationService.calculateFinalFare(any(FinalFareRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/fare/calculate-final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value("RIDE-12345"))
                .andExpect(jsonPath("$.finalFare").value(220.0));
    }
}
