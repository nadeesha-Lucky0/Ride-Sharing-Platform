package com.ridelink.driver.controller;

import com.ridelink.driver.dto.VehicleRequestDto;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<Vehicle> registerVehicle(
            @Valid @RequestBody VehicleRequestDto dto) {

        Vehicle savedVehicle = vehicleService.registerVehicle(dto);

        return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<Vehicle> getVehicleByDriverId(
            @PathVariable String driverId) {

        return ResponseEntity.ok(
                vehicleService.getVehicleByDriverId(driverId)
        );
    }
}