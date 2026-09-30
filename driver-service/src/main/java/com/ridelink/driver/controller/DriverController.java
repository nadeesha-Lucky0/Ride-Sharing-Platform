package com.ridelink.driver.controller;

import com.ridelink.driver.dto.DriverAvailabilityDto;
import com.ridelink.driver.dto.DriverProfileDto;
import com.ridelink.driver.dto.DriverResponseDto;
import com.ridelink.driver.dto.ErrorResponseDto;
import com.ridelink.driver.dto.LocationUpdateDto;
import com.ridelink.driver.dto.ServiceAreaUpdateDto;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.model.VehicleCategory;
import com.ridelink.driver.service.DriverService;
import com.ridelink.driver.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "Driver operational profiles, availability status, service area, and geolocation tracking")
public class DriverController {

    private final DriverService driverService;
    private final LocationService locationService;

    @PostMapping
    @Operation(summary = "1. Register or update driver profile", description = "Creates or updates an operational driver profile linked to a user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile created or updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation failure",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "License number already registered",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<DriverResponseDto> createOrUpdateProfile(@Valid @RequestBody DriverProfileDto dto) {
        return ResponseEntity.ok(driverService.createOrUpdateProfile(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "2. Get driver by ID", description = "Fetches driver operational profile and active vehicle by driver profile ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<DriverResponseDto> getDriverById(
            @Parameter(description = "Driver profile ID", example = "65f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "3. Get driver by User ID", description = "Fetches driver operational profile by associated user account ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found for user ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<DriverResponseDto> getDriverByUserId(
            @Parameter(description = "Account service User ID", example = "usr_89a1b2c3d4e5f6a7b8c9d0e1") @PathVariable String userId) {
        return ResponseEntity.ok(driverService.getDriverByUserId(userId));
    }

    @GetMapping
    @Operation(summary = "4. Get all drivers", description = "Fetches all driver profiles with optional status and city filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of driver profiles",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DriverResponseDto.class))))
    })
    public ResponseEntity<List<DriverResponseDto>> getAllDrivers(
            @Parameter(description = "Optional filter by status", example = "AVAILABLE")
            @RequestParam(required = false) DriverStatus status,
            @Parameter(description = "Optional filter by service city", example = "Colombo")
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(driverService.getAllDrivers(status, city));
    }

    @PutMapping("/{id}/availability")
    @Operation(summary = "5. Update availability status", description = "Updates driver status (ONLINE, AVAILABLE, OFFLINE, BUSY, SUSPENDED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<DriverResponseDto> updateAvailability(
            @Parameter(description = "Driver profile ID") @PathVariable String id,
            @Valid @RequestBody DriverAvailabilityDto dto) {
        return ResponseEntity.ok(driverService.updateAvailability(id, dto.getStatus()));
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "6. Toggle availability status", description = "Toggles driver status between OFFLINE and AVAILABLE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status toggled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<DriverResponseDto> toggleAvailability(
            @Parameter(description = "Driver profile ID") @PathVariable String id) {
        return ResponseEntity.ok(driverService.toggleAvailability(id));
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "7. Update simulated GPS location", description = "Updates driver's live GPS coordinates and optional landmark address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Driver.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Driver> updateLocation(
            @Parameter(description = "Driver profile ID") @PathVariable String id,
            @Valid @RequestBody LocationUpdateDto dto) {
        return ResponseEntity.ok(locationService.updateLocation(id, dto.getLatitude(), dto.getLongitude(), dto.getAddress()));
    }

    @PutMapping("/{id}/service-area")
    @Operation(summary = "8. Update operating service area", description = "Updates primary service city and operating district for the driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service area updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<DriverResponseDto> updateServiceArea(
            @Parameter(description = "Driver profile ID") @PathVariable String id,
            @Valid @RequestBody ServiceAreaUpdateDto dto) {
        return ResponseEntity.ok(driverService.updateServiceArea(id, dto));
    }

    @GetMapping("/available")
    @Operation(summary = "9. Retrieve eligible available drivers", description = "Finds nearby eligible drivers sorted by distance, with optional vehicle category and seating criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of eligible available drivers",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DriverResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "Missing required query parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<List<DriverResponseDto>> getAvailableDrivers(
            @Parameter(description = "Origin pickup latitude coordinate", example = "6.9344", required = true)
            @RequestParam Double latitude,
            @Parameter(description = "Origin pickup longitude coordinate", example = "79.8510", required = true)
            @RequestParam Double longitude,
            @Parameter(description = "Search radius in kilometers", example = "10.0")
            @RequestParam(defaultValue = "10.0") double radiusKm,
            @Parameter(description = "Optional vehicle category filter", example = "SEDAN")
            @RequestParam(required = false) VehicleCategory category,
            @Parameter(description = "Optional minimum seating capacity", example = "4")
            @RequestParam(required = false) Integer minSeats) {
        return ResponseEntity.ok(driverService.findNearestAvailableDrivers(latitude, longitude, radiusKm, category, minSeats));
    }
}
