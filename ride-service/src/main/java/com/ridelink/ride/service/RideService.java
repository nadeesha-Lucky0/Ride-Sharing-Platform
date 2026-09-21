package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.PaymentClient;
import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.dto.RideStatusUpdateDto;
import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final DriverClient driverClient;
    private final PaymentClient paymentClient;

    public RideResponseDto requestRide(RideRequestDto dto) {
        double distanceKm = calculateDistance(dto.getPickupLocation(), dto.getDropoffLocation());
        int durationMinutes = (int) Math.max(5, Math.round(distanceKm * 2.5));

        Double estimatedFare = null;
        try {
            Map fareResponse = paymentClient.estimateFare(distanceKm, durationMinutes).block();
            if (fareResponse != null && fareResponse.containsKey("estimatedTotalFare")) {
                estimatedFare = Double.valueOf(fareResponse.get("estimatedTotalFare").toString());
            }
        } catch (Exception ignored) {
            estimatedFare = 50.0 + (distanceKm * 15.0);
        }

        Ride ride = Ride.builder()
                .passengerId(dto.getPassengerId())
                .pickupLocation(dto.getPickupLocation())
                .dropoffLocation(dto.getDropoffLocation())
                .status(RideStatus.REQUESTED)
                .distanceKm(distanceKm)
                .estimatedDurationMinutes(durationMinutes)
                .estimatedFare(estimatedFare)
                .requestedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Ride savedRide = rideRepository.save(ride);
        return toDto(savedRide);
    }

    public RideResponseDto updateStatus(String rideId, RideStatusUpdateDto dto) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + rideId));

        ride.setStatus(dto.getStatus());
        ride.setUpdatedAt(LocalDateTime.now());

        if (dto.getStatus() == RideStatus.ACCEPTED) {
            ride.setDriverId(dto.getDriverId());
            ride.setAcceptedAt(LocalDateTime.now());
            try {
                driverClient.updateDriverAvailability(dto.getDriverId(), "BUSY").subscribe();
            } catch (Exception ignored) {}
        } else if (dto.getStatus() == RideStatus.IN_PROGRESS) {
            ride.setStartedAt(LocalDateTime.now());
        } else if (dto.getStatus() == RideStatus.COMPLETED) {
            ride.setCompletedAt(LocalDateTime.now());
            ride.setActualFare(ride.getEstimatedFare());
            try {
                if (ride.getDriverId() != null) {
                    driverClient.updateDriverAvailability(ride.getDriverId(), "AVAILABLE").subscribe();
                    paymentClient.processPayment(ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getActualFare(), "CARD").subscribe();
                }
            } catch (Exception ignored) {}
        } else if (dto.getStatus() == RideStatus.CANCELLED) {
            ride.setCancelledAt(LocalDateTime.now());
            ride.setCancellationReason(dto.getCancellationReason());
            try {
                if (ride.getDriverId() != null) {
                    driverClient.updateDriverAvailability(ride.getDriverId(), "AVAILABLE").subscribe();
                }
            } catch (Exception ignored) {}
        }

        return toDto(rideRepository.save(ride));
    }

    public RideResponseDto getRideById(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + rideId));
        return toDto(ride);
    }

    public List<RideResponseDto> getRidesByPassenger(String passengerId) {
        return rideRepository.findByPassengerId(passengerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<RideResponseDto> getRidesByDriver(String driverId) {
        return rideRepository.findByDriverId(driverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private double calculateDistance(Location start, Location end) {
        if (start.getLatitude() == null || start.getLongitude() == null ||
            end.getLatitude() == null || end.getLongitude() == null) {
            return 5.0; // fallback default distance
        }
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(end.getLatitude() - start.getLatitude());
        double dLon = Math.toRadians(end.getLongitude() - start.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(start.getLatitude())) * Math.cos(Math.toRadians(end.getLatitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(EARTH_RADIUS_KM * c * 100.0) / 100.0;
    }

    private RideResponseDto toDto(Ride ride) {
        return RideResponseDto.builder()
                .id(ride.getId())
                .passengerId(ride.getPassengerId())
                .driverId(ride.getDriverId())
                .pickupLocation(ride.getPickupLocation())
                .dropoffLocation(ride.getDropoffLocation())
                .status(ride.getStatus())
                .estimatedFare(ride.getEstimatedFare())
                .actualFare(ride.getActualFare())
                .distanceKm(ride.getDistanceKm())
                .estimatedDurationMinutes(ride.getEstimatedDurationMinutes())
                .requestedAt(ride.getRequestedAt())
                .acceptedAt(ride.getAcceptedAt())
                .completedAt(ride.getCompletedAt())
                .build();
    }
}
