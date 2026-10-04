package com.ridelink.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.dto.DriverAvailabilityDto;
import com.ridelink.driver.dto.DriverProfileDto;
import com.ridelink.driver.dto.DriverResponseDto;
import com.ridelink.driver.dto.LocationUpdateDto;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.exception.GlobalExceptionHandler;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.model.VehicleCategory;
import com.ridelink.driver.service.DriverService;
import com.ridelink.driver.service.LocationService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Driver Controller MockMvc Tests")
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DriverService driverService;

    @MockBean
    private LocationService locationService;

    private DriverResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = DriverResponseDto.builder()
                .id("driver-101")
                .userId("user-501")
                .fullName("Sunil Perera")
                .phoneNumber("+94771234567")
                .licenseNumber("B1234567")
                .status(DriverStatus.AVAILABLE)
                .currentLatitude(6.9344)
                .currentLongitude(79.8510)
                .serviceCity("Colombo")
                .rating(4.9)
                .totalRides(50)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/drivers - 200 OK on valid profile payload")
    void createOrUpdateProfile_valid_returnsOk() throws Exception {
        DriverProfileDto dto = DriverProfileDto.builder()
                .userId("user-501")
                .licenseNumber("B1234567")
                .fullName("Sunil Perera")
                .build();

        when(driverService.createOrUpdateProfile(any(DriverProfileDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("driver-101"))
                .andExpect(jsonPath("$.userId").value("user-501"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST /api/drivers - 400 Bad Request on missing userId or licenseNumber")
    void createOrUpdateProfile_missingFields_returnsBadRequest() throws Exception {
        DriverProfileDto invalidDto = DriverProfileDto.builder()
                .userId("") // blank
                .licenseNumber("") // blank
                .build();

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.userId").exists())
                .andExpect(jsonPath("$.fieldErrors.licenseNumber").exists());
    }

    @Test
    @DisplayName("POST /api/drivers - 409 Conflict on duplicate license plate/number")
    void createOrUpdateProfile_duplicate_returnsConflict() throws Exception {
        DriverProfileDto dto = DriverProfileDto.builder()
                .userId("user-501")
                .licenseNumber("B1234567")
                .build();

        when(driverService.createOrUpdateProfile(any(DriverProfileDto.class)))
                .thenThrow(new DuplicateResourceException("License number already registered."));

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("License number already registered."));
    }

    @Test
    @DisplayName("GET /api/drivers/{id} - 200 OK when driver exists")
    void getDriverById_found_returnsOk() throws Exception {
        when(driverService.getDriverById("driver-101")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/drivers/driver-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("driver-101"))
                .andExpect(jsonPath("$.licenseNumber").value("B1234567"));
    }

    @Test
    @DisplayName("GET /api/drivers/{id} - 404 Not Found when driver missing")
    void getDriverById_notFound_returns404() throws Exception {
        when(driverService.getDriverById("unknown")).thenThrow(new DriverNotFoundException("unknown", true));

        mockMvc.perform(get("/api/drivers/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/drivers/{id}/availability - 200 OK updates status")
    void updateAvailability_returnsOk() throws Exception {
        when(driverService.updateAvailability(eq("driver-101"), any(DriverStatus.class))).thenReturn(sampleResponse);

        DriverAvailabilityDto dto = new DriverAvailabilityDto(DriverStatus.AVAILABLE);

        mockMvc.perform(put("/api/drivers/driver-101/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("PUT /api/drivers/{id}/location - 200 OK updates GPS coordinates")
    void updateLocation_returnsOk() throws Exception {
        Driver updatedDriver = Driver.builder().id("driver-101").currentLatitude(6.9350).currentLongitude(79.8520).build();
        when(locationService.updateLocation(eq("driver-101"), eq(6.9350), eq(79.8520), any())).thenReturn(updatedDriver);

        LocationUpdateDto dto = new LocationUpdateDto(6.9350, 79.8520, "Colombo Fort");

        mockMvc.perform(put("/api/drivers/driver-101/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("driver-101"));
    }

    @Test
    @DisplayName("GET /api/drivers/available - 200 OK returns available drivers")
    void getAvailableDrivers_returnsList() throws Exception {
        sampleResponse.setDistanceKm(1.5);
        when(driverService.findNearestAvailableDrivers(eq(6.9344), eq(79.8510), eq(10.0), any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/drivers/available")
                        .param("latitude", "6.9344")
                        .param("longitude", "79.8510")
                        .param("radiusKm", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("driver-101"))
                .andExpect(jsonPath("$[0].distanceKm").value(1.5));
    }
}
