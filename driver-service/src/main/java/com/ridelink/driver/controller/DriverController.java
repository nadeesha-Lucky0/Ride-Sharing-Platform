package com.ridelink.driver.controller;

import com.ridelink.driver.dto.DriverAvailabilityDto;
import com.ridelink.driver.dto.DriverProfileDto;
import com.ridelink.driver.dto.LocationUpdateDto;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.service.DriverService;
import com.ridelink.driver.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<Driver> createOrUpdateProfile(@Valid @RequestBody DriverProfileDto dto) {
        return ResponseEntity.ok(driverService.createOrUpdateProfile(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable String id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Driver> getDriverByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(driverService.getDriverByUserId(userId));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<Driver> updateAvailability(@PathVariable String id,
                                                    @Valid @RequestBody DriverAvailabilityDto dto) {
        return ResponseEntity.ok(driverService.updateAvailability(id, dto.getStatus()));
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<Driver> updateLocation(@PathVariable String id,
                                                @Valid @RequestBody LocationUpdateDto dto) {
        return ResponseEntity.ok(locationService.updateLocation(id, dto.getLatitude(), dto.getLongitude()));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        return ResponseEntity.ok(driverService.findNearestAvailableDrivers(latitude, longitude, radiusKm));
    }
}
