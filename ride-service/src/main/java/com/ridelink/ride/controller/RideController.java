package com.ridelink.ride.controller;

import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.dto.RideStatusUpdateDto;
import com.ridelink.ride.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping("/request")
    public ResponseEntity<RideResponseDto> requestRide(@Valid @RequestBody RideRequestDto dto) {
        RideResponseDto response = rideService.requestRide(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponseDto> getRideById(@PathVariable String id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RideResponseDto> updateStatus(@PathVariable String id,
                                                        @Valid @RequestBody RideStatusUpdateDto dto) {
        return ResponseEntity.ok(rideService.updateStatus(id, dto));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<RideResponseDto>> getRidesByPassenger(@PathVariable String passengerId) {
        return ResponseEntity.ok(rideService.getRidesByPassenger(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponseDto>> getRidesByDriver(@PathVariable String driverId) {
        return ResponseEntity.ok(rideService.getRidesByDriver(driverId));
    }
}
