package com.ridelink.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.dto.VehicleRegistrationDto;
import com.ridelink.driver.dto.VehicleResponseDto;
import com.ridelink.driver.exception.GlobalExceptionHandler;
import com.ridelink.driver.exception.VehicleNotFoundException;
import com.ridelink.driver.model.VehicleCategory;
import com.ridelink.driver.service.VehicleService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Vehicle Controller MockMvc Tests")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    private VehicleResponseDto sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleVehicle = VehicleResponseDto.builder()
                .id("veh-201")
                .driverId("driver-101")
                .make("Toyota")
                .model("Prius")
                .year(2022)
                .color("Pearl White")
                .licensePlate("CAB-4589")
                .category(VehicleCategory.SEDAN)
                .seatingCapacity(4)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/vehicles - 201 Created on valid registration")
    void registerVehicle_valid_returnsCreated() throws Exception {
        VehicleRegistrationDto dto = VehicleRegistrationDto.builder()
                .driverId("driver-101")
                .make("Toyota")
                .model("Prius")
                .year(2022)
                .color("Pearl White")
                .licensePlate("CAB-4589")
                .category(VehicleCategory.SEDAN)
                .seatingCapacity(4)
                .build();

        when(vehicleService.registerVehicle(any(VehicleRegistrationDto.class))).thenReturn(sampleVehicle);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("veh-201"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.licensePlate").value("CAB-4589"));
    }

    @Test
    @DisplayName("POST /api/vehicles - 400 Bad Request on invalid year/seating")
    void registerVehicle_invalid_returnsBadRequest() throws Exception {
        VehicleRegistrationDto invalidDto = VehicleRegistrationDto.builder()
                .driverId("") // blank
                .make("")
                .year(1800) // too old
                .seatingCapacity(0) // invalid
                .build();

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - 200 OK when found")
    void getVehicleById_found_returnsOk() throws Exception {
        when(vehicleService.getVehicleById("veh-201")).thenReturn(sampleVehicle);

        mockMvc.perform(get("/api/vehicles/veh-201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("veh-201"))
                .andExpect(jsonPath("$.licensePlate").value("CAB-4589"));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - 404 Not Found when missing")
    void getVehicleById_notFound_returns404() throws Exception {
        when(vehicleService.getVehicleById("unknown")).thenThrow(new VehicleNotFoundException("unknown", true));

        mockMvc.perform(get("/api/vehicles/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/vehicles/driver/{driverId} - 200 OK returns list")
    void getVehiclesByDriverId_returnsList() throws Exception {
        when(vehicleService.getVehiclesByDriverId("driver-101")).thenReturn(List.of(sampleVehicle));

        mockMvc.perform(get("/api/vehicles/driver/driver-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("veh-201"));
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} - 204 No Content")
    void deleteVehicle_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/vehicles/veh-201"))
                .andExpect(status().isNoContent());
    }
}
