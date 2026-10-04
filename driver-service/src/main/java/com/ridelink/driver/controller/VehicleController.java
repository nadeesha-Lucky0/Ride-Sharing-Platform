package com.ridelink.driver.controller;

import com.ridelink.driver.dto.ErrorResponseDto;
import com.ridelink.driver.dto.VehicleRegistrationDto;
import com.ridelink.driver.dto.VehicleResponseDto;
import com.ridelink.driver.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "Vehicle registration, vehicle specifications, and driver fleet associations")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "1. Register a new vehicle", description = "Registers vehicle specifications (make, model, plate, seating, category) and links it to a driver profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failure",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "License plate already registered",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<VehicleResponseDto> registerVehicle(@Valid @RequestBody VehicleRegistrationDto dto) {
        VehicleResponseDto saved = vehicleService.registerVehicle(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "2. Get vehicle by ID", description = "Fetches vehicle details by vehicle record ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<VehicleResponseDto> getVehicleById(
            @Parameter(description = "Vehicle record ID", example = "65f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "3. Get all vehicles for a driver", description = "Fetches all vehicles registered under a specific driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of vehicles",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<List<VehicleResponseDto>> getVehiclesByDriverId(
            @Parameter(description = "Driver profile ID", example = "65f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String driverId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByDriverId(driverId));
    }

    @GetMapping("/driver/{driverId}/active")
    @Operation(summary = "4. Get active vehicle for a driver", description = "Fetches the primary active vehicle currently deployed by the driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active vehicle found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No active vehicle found for driver",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<VehicleResponseDto> getActiveVehicleByDriverId(
            @Parameter(description = "Driver profile ID", example = "65f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String driverId) {
        return ResponseEntity.ok(vehicleService.getActiveVehicleByDriverId(driverId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "5. Update vehicle specifications", description = "Updates details, specifications, or active status of a vehicle.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failure",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "License plate conflict",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @Parameter(description = "Vehicle record ID") @PathVariable String id,
            @Valid @RequestBody VehicleRegistrationDto dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "6. Delete a vehicle", description = "Removes a vehicle record from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Vehicle record ID") @PathVariable String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
