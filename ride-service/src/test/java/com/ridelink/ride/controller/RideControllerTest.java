package com.ridelink.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ride.dto.DriverAssignDto;
import com.ridelink.ride.dto.RideCancelDto;
import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.dto.RideStatusUpdateDto;
import com.ridelink.ride.exception.GlobalExceptionHandler;
import com.ridelink.ride.exception.InvalidStateTransitionException;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.exception.UnauthorizedActionException;
import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Ride Controller MockMvc REST API Tests")
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    private Location samplePickup;
    private Location sampleDropoff;
    private RideResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        samplePickup = new Location("Colombo Fort", 6.9344, 79.8510);
        sampleDropoff = new Location("Bambalapitiya", 6.8920, 79.8550);

        sampleResponse = RideResponseDto.builder()
                .id("ride-123")
                .passengerId("passenger-1")
                .driverId("driver-456")
                .pickupLocation(samplePickup)
                .dropoffLocation(sampleDropoff)
                .status(RideStatus.REQUESTED)
                .distanceKm(4.8)
                .estimatedDurationMinutes(12)
                .estimatedFare(350.0)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/rides/request - 201 Created on valid input")
    void requestRide_validInput_returnsCreated() throws Exception {
        RideRequestDto request = RideRequestDto.builder()
                .passengerId("passenger-1")
                .pickupLocation(samplePickup)
                .dropoffLocation(sampleDropoff)
                .build();

        when(rideService.requestRide(any(RideRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/rides/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ride-123"))
                .andExpect(jsonPath("$.passengerId").value("passenger-1"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @DisplayName("POST /api/rides/request - 400 Bad Request on missing fields")
    void requestRide_invalidInput_returnsBadRequest() throws Exception {
        RideRequestDto invalidRequest = RideRequestDto.builder()
                .passengerId("") // blank
                .pickupLocation(null) // missing
                .build();

        mockMvc.perform(post("/api/rides/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.passengerId").exists())
                .andExpect(jsonPath("$.fieldErrors.pickupLocation").exists());
    }

    @Test
    @DisplayName("GET /api/rides/{id} - 200 OK when ride exists")
    void getRideById_found_returnsOk() throws Exception {
        when(rideService.getRideById("ride-123")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rides/ride-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ride-123"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @DisplayName("GET /api/rides/{id} - 404 Not Found when ride missing")
    void getRideById_notFound_returns404() throws Exception {
        when(rideService.getRideById("unknown-id")).thenThrow(new RideNotFoundException("unknown-id", true));

        mockMvc.perform(get("/api/rides/unknown-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ride with ID 'unknown-id' was not found."));
    }

    @Test
    @DisplayName("GET /api/rides/passenger/{passengerId} - 200 OK returns list")
    void getRidesByPassenger_returnsList() throws Exception {
        when(rideService.getRidesByPassenger("passenger-1")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/rides/passenger/passenger-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passengerId").value("passenger-1"));
    }

    @Test
    @DisplayName("PUT /api/rides/{id}/assign - 200 OK assigns driver")
    void assignDriver_returnsOk() throws Exception {
        sampleResponse.setStatus(RideStatus.ASSIGNED);
        sampleResponse.setDriverId("driver-999");
        when(rideService.assignDriver(eq("ride-123"), any(DriverAssignDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/rides/ride-123/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DriverAssignDto("driver-999"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.driverId").value("driver-999"));
    }

    @Test
    @DisplayName("PUT /api/rides/{id}/accept - 200 OK accepts ride")
    void acceptRide_returnsOk() throws Exception {
        sampleResponse.setStatus(RideStatus.ACCEPTED);
        when(rideService.acceptRide("ride-123", "driver-456")).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/rides/ride-123/accept")
                        .param("driverId", "driver-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PUT /api/rides/{id}/start - 200 OK starts ride")
    void startRide_returnsOk() throws Exception {
        sampleResponse.setStatus(RideStatus.IN_PROGRESS);
        when(rideService.startRide("ride-123", "driver-456")).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/rides/ride-123/start")
                        .param("driverId", "driver-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PUT /api/rides/{id}/complete - 200 OK completes ride")
    void completeRide_returnsOk() throws Exception {
        sampleResponse.setStatus(RideStatus.COMPLETED);
        sampleResponse.setActualFare(350.0);
        when(rideService.completeRide("ride-123", "driver-456")).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/rides/ride-123/complete")
                        .param("driverId", "driver-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.actualFare").value(350.0));
    }

    @Test
    @DisplayName("PUT /api/rides/{id}/cancel - 200 OK cancels ride")
    void cancelRide_authorized_returnsOk() throws Exception {
        sampleResponse.setStatus(RideStatus.CANCELLED);
        sampleResponse.setCancellationReason("Driver requested cancel");
        when(rideService.cancelRide(eq("ride-123"), any(RideCancelDto.class))).thenReturn(sampleResponse);

        RideCancelDto cancelDto = new RideCancelDto("driver-456", "Driver requested cancel");
        mockMvc.perform(put("/api/rides/ride-123/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /api/rides/{id}/cancel - 403 Forbidden on unauthorized user")
    void cancelRide_unauthorized_returnsForbidden() throws Exception {
        when(rideService.cancelRide(eq("ride-123"), any(RideCancelDto.class)))
                .thenThrow(new UnauthorizedActionException("User is not authorized to cancel"));

        RideCancelDto cancelDto = new RideCancelDto("unauthorized-user", "Cancel");
        mockMvc.perform(put("/api/rides/ride-123/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("User is not authorized to cancel"));
    }

    @Test
    @DisplayName("PUT /api/rides/{id}/status - 422 Unprocessable on invalid state transition")
    void updateStatus_invalidTransition_returnsUnprocessableEntity() throws Exception {
        when(rideService.updateStatus(eq("ride-123"), any(RideStatusUpdateDto.class)))
                .thenThrow(new InvalidStateTransitionException(RideStatus.REQUESTED, RideStatus.COMPLETED));

        RideStatusUpdateDto updateDto = new RideStatusUpdateDto(RideStatus.COMPLETED, "driver-456", null);
        mockMvc.perform(put("/api/rides/ride-123/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Invalid ride status transition from 'REQUESTED' to 'COMPLETED'."));
    }
}
