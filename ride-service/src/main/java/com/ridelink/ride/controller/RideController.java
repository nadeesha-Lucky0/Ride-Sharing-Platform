package com.ridelink.ride.controller;

import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.dto.RideStatusUpdateDto;
import com.ridelink.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Ride Management", description = "Ride request lifecycle, driver matching, and status updates")
public class RideController {

    private final RideService rideService;

    @PostMapping("/request")
    @Operation(summary = "Request a new ride", description = "Creates a ride request and matches it to the nearest available driver when possible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ride requested successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RideResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failure",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<RideResponseDto> requestRide(@Valid @RequestBody RideRequestDto dto) {
        RideResponseDto response = rideService.requestRide(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride by ID", description = "Fetches the current ride details and status.")
    public ResponseEntity<RideResponseDto> getRideById(@PathVariable String id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ride status", description = "Moves the ride through its lifecycle states such as accepted, in progress, completed, or cancelled.")
    public ResponseEntity<RideResponseDto> updateStatus(@PathVariable String id,
                                                        @Valid @RequestBody RideStatusUpdateDto dto) {
        return ResponseEntity.ok(rideService.updateStatus(id, dto));
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Get passenger rides", description = "Returns all rides for a specific passenger.")
    public ResponseEntity<List<RideResponseDto>> getRidesByPassenger(@PathVariable String passengerId) {
        return ResponseEntity.ok(rideService.getRidesByPassenger(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get driver rides", description = "Returns all rides assigned to a specific driver.")
    public ResponseEntity<List<RideResponseDto>> getRidesByDriver(@PathVariable String driverId) {
        return ResponseEntity.ok(rideService.getRidesByDriver(driverId));
    }
}
