package com.ridelink.ride.controller;

import com.ridelink.ride.dto.DriverAssignDto;
import com.ridelink.ride.dto.ErrorResponseDto;
import com.ridelink.ride.dto.RideCancelDto;
import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.dto.RideStatusUpdateDto;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.service.RideService;
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
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Management", description = "Ride booking, matching orchestrator, lifecycle state machine, and ride histories")
public class RideController {

    private final RideService rideService;

    @PostMapping("/request")
    @Operation(summary = "1. Request a new ride", description = "Creates a new ride request, calculates estimated fare & duration, and matches with an eligible available driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ride requested successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or missing required fields",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> requestRide(@Valid @RequestBody RideRequestDto dto) {
        RideResponseDto response = rideService.requestRide(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "2. Get ride by ID", description = "Fetches the current details, tracking status, and pricing of a specific ride.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride details retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> getRideById(
            @Parameter(description = "Ride ID", example = "65f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @GetMapping
    @Operation(summary = "3. Get all rides", description = "Fetches all rides across the system, optionally filtered by lifecycle status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of rides",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RideResponseDto.class))))
    })
    public ResponseEntity<List<RideResponseDto>> getAllRides(
            @Parameter(description = "Optional filter by ride status", example = "REQUESTED")
            @RequestParam(required = false) RideStatus status) {
        return ResponseEntity.ok(rideService.getAllRides(status));
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "4. Get passenger ride history", description = "Returns all rides requested by a specific passenger, ordered by request date descending.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of passenger rides",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RideResponseDto.class))))
    })
    public ResponseEntity<List<RideResponseDto>> getRidesByPassenger(
            @Parameter(description = "Passenger User ID", example = "pass_65f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String passengerId) {
        return ResponseEntity.ok(rideService.getRidesByPassenger(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "5. Get driver ride history", description = "Returns all rides assigned to or driven by a specific driver, ordered by request date descending.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of driver rides",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RideResponseDto.class))))
    })
    public ResponseEntity<List<RideResponseDto>> getRidesByDriver(
            @Parameter(description = "Driver User ID", example = "drv_89a1b2c3d4e5f6a7b8c9d0e1") @PathVariable String driverId) {
        return ResponseEntity.ok(rideService.getRidesByDriver(driverId));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "6. Assign driver to ride", description = "Assigns an available driver to an unassigned ride request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver assigned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state transition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> assignDriver(
            @Parameter(description = "Ride ID") @PathVariable String id,
            @Valid @RequestBody DriverAssignDto dto) {
        return ResponseEntity.ok(rideService.assignDriver(id, dto));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "7. Driver accepts ride", description = "Transition ride status to ACCEPTED by the assigned driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride accepted",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state transition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> acceptRide(
            @Parameter(description = "Ride ID") @PathVariable String id,
            @Parameter(description = "Driver ID accepting the ride", example = "drv_89a1b2c3d4e5f6a7b8c9d0e1")
            @RequestParam String driverId) {
        return ResponseEntity.ok(rideService.acceptRide(id, driverId));
    }

    @PutMapping("/{id}/start")
    @Operation(summary = "8. Start trip (In Progress)", description = "Transition ride status to IN_PROGRESS when the driver picks up the passenger.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip started",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state transition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> startRide(
            @Parameter(description = "Ride ID") @PathVariable String id,
            @Parameter(description = "Driver ID starting the trip") @RequestParam(required = false) String driverId) {
        return ResponseEntity.ok(rideService.startRide(id, driverId));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "9. Complete trip", description = "Transition ride status to COMPLETED upon reaching destination and triggers automatic payment processing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip completed and payment processed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state transition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> completeRide(
            @Parameter(description = "Ride ID") @PathVariable String id,
            @Parameter(description = "Driver ID completing the trip") @RequestParam(required = false) String driverId) {
        return ResponseEntity.ok(rideService.completeRide(id, driverId));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "10. Cancel ride", description = "Cancels an active ride with reason validation and resets driver availability.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride cancelled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized cancellation attempt",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state transition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> cancelRide(
            @Parameter(description = "Ride ID") @PathVariable String id,
            @Valid @RequestBody RideCancelDto dto) {
        return ResponseEntity.ok(rideService.cancelRide(id, dto));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "11. Generic status update", description = "Transitions the ride to any valid state enforcing state machine rules.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "422", description = "Invalid status transition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RideResponseDto> updateStatus(
            @Parameter(description = "Ride ID") @PathVariable String id,
            @Valid @RequestBody RideStatusUpdateDto dto) {
        return ResponseEntity.ok(rideService.updateStatus(id, dto));
    }
}
