package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.PaymentClient;
import com.ridelink.ride.dto.DriverAssignDto;
import com.ridelink.ride.dto.RideCancelDto;
import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.dto.RideStatusUpdateDto;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.exception.UnauthorizedActionException;
import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.statemachine.RideStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final DriverClient driverClient;
    private final PaymentClient paymentClient;
    private final RideStateMachine rideStateMachine;

    /**
     * Creates a new ride request, calculates route metrics and estimated fare,
     * and attempts automated driver selection.
     */
    public RideResponseDto requestRide(RideRequestDto dto) {
        log.info("Creating ride request for passengerId: {}", dto.getPassengerId());

        double distanceKm = calculateDistance(dto.getPickupLocation(), dto.getDropoffLocation());
        int durationMinutes = calculateEstimatedDurationMinutes(distanceKm);

        // Fetch fare estimation from payment-service or compute fallback
        Double estimatedFare = fetchOrCalculateEstimatedFare(distanceKm, durationMinutes);

        // Driver selection logic
        String matchedDriverId = null;
        RideStatus initialStatus = RideStatus.REQUESTED;

        if (dto.getRequestedDriverId() != null && !dto.getRequestedDriverId().isBlank()) {
            matchedDriverId = dto.getRequestedDriverId();
            initialStatus = RideStatus.ASSIGNED;
        } else {
            matchedDriverId = findEligibleAvailableDriver(dto.getPickupLocation());
            if (matchedDriverId != null) {
                initialStatus = RideStatus.ASSIGNED;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Ride ride = Ride.builder()
                .passengerId(dto.getPassengerId())
                .driverId(matchedDriverId)
                .pickupLocation(dto.getPickupLocation())
                .dropoffLocation(dto.getDropoffLocation())
                .status(initialStatus)
                .distanceKm(distanceKm)
                .estimatedDurationMinutes(durationMinutes)
                .estimatedFare(estimatedFare)
                .requestedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride created with ID: {} and status: {}", savedRide.getId(), savedRide.getStatus());

        if (matchedDriverId != null) {
            try {
                driverClient.updateDriverAvailability(matchedDriverId, "BUSY").subscribe();
            } catch (Exception e) {
                log.warn("Failed to notify driver service of assignment: {}", e.getMessage());
            }
        }

        return toDto(savedRide);
    }

    /**
     * Assigns a driver to a requested ride.
     */
    public RideResponseDto assignDriver(String rideId, DriverAssignDto dto) {
        Ride ride = findRideOrThrow(rideId);

        rideStateMachine.validateRideStateForTransition(ride, RideStatus.ASSIGNED, dto.getDriverId());

        ride.setDriverId(dto.getDriverId());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setUpdatedAt(LocalDateTime.now());

        try {
            driverClient.updateDriverAvailability(dto.getDriverId(), "BUSY").subscribe();
        } catch (Exception e) {
            log.warn("Failed to update driver status: {}", e.getMessage());
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} assigned to driver {}", rideId, dto.getDriverId());
        return toDto(saved);
    }

    /**
     * Driver accepts an assigned or requested ride.
     */
    public RideResponseDto acceptRide(String rideId, String driverId) {
        Ride ride = findRideOrThrow(rideId);

        rideStateMachine.validateRideStateForTransition(ride, RideStatus.ACCEPTED, driverId);

        if (ride.getDriverId() != null && !ride.getDriverId().equals(driverId)) {
            throw new UnauthorizedActionException("Ride is already assigned to a different driver.");
        }

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());

        try {
            driverClient.updateDriverAvailability(driverId, "BUSY").subscribe();
        } catch (Exception e) {
            log.warn("Failed to update driver status to BUSY: {}", e.getMessage());
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} accepted by driver {}", rideId, driverId);
        return toDto(saved);
    }

    /**
     * Starts the ride trip (IN_PROGRESS).
     */
    public RideResponseDto startRide(String rideId, String driverId) {
        Ride ride = findRideOrThrow(rideId);

        validateDriverOwnership(ride, driverId);
        rideStateMachine.validateRideStateForTransition(ride, RideStatus.IN_PROGRESS, driverId);

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} is now IN_PROGRESS", rideId);
        return toDto(saved);
    }

    /**
     * Completes the ride trip (COMPLETED) and initiates payment.
     */
    public RideResponseDto completeRide(String rideId, String driverId) {
        Ride ride = findRideOrThrow(rideId);

        validateDriverOwnership(ride, driverId);
        rideStateMachine.validateRideStateForTransition(ride, RideStatus.COMPLETED, driverId);

        LocalDateTime now = LocalDateTime.now();
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(now);
        ride.setActualFare(ride.getEstimatedFare() != null ? ride.getEstimatedFare() : 100.0);
        ride.setUpdatedAt(now);

        // Process payment and free up driver
        try {
            if (ride.getDriverId() != null) {
                driverClient.updateDriverAvailability(ride.getDriverId(), "AVAILABLE").block(java.time.Duration.ofSeconds(3));
                driverClient.updateDriverAvailability(ride.getDriverId(), "AVAILABLE").subscribe();
                paymentClient.processPayment(
                        ride.getId(),
                        ride.getPassengerId(),
                        ride.getDriverId(),
                        ride.getActualFare(),
                        "CARD"
                ).block(java.time.Duration.ofSeconds(3));
                ).subscribe();
            }
        } catch (Exception e) {
            log.warn("Failed during ride completion inter-service integration: {}", e.getMessage());
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} COMPLETED successfully", rideId);
        return toDto(saved);
    }

    /**
     * Cancels a ride with authorized user validation.
     */
    public RideResponseDto cancelRide(String rideId, RideCancelDto dto) {
        Ride ride = findRideOrThrow(rideId);

        // Validate user authorization to cancel
        boolean isPassenger = ride.getPassengerId() != null && ride.getPassengerId().equals(dto.getUserId());
        boolean isDriver = ride.getDriverId() != null && ride.getDriverId().equals(dto.getUserId());
        if (!isPassenger && !isDriver && !"admin".equalsIgnoreCase(dto.getUserId())) {
            throw new UnauthorizedActionException("User '" + dto.getUserId() + "' is not authorized to cancel ride " + rideId);
        }

        rideStateMachine.validateRideStateForTransition(ride, RideStatus.CANCELLED, ride.getDriverId());

        LocalDateTime now = LocalDateTime.now();
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(now);
        ride.setCancellationReason(dto.getReason() != null ? dto.getReason() : "Cancelled by user " + dto.getUserId());
        ride.setUpdatedAt(now);

        // Free up assigned driver if one was active
        if (ride.getDriverId() != null) {
            try {
                driverClient.updateDriverAvailability(ride.getDriverId(), "AVAILABLE").subscribe();
            } catch (Exception e) {
                log.warn("Failed to reset driver availability after cancellation: {}", e.getMessage());
            }
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} CANCELLED by {}", rideId, dto.getUserId());
        return toDto(saved);
    }

    /**
     * Generic status update endpoint following the Ride Lifecycle State Machine.
     */
    public RideResponseDto updateStatus(String rideId, RideStatusUpdateDto dto) {
        Ride ride = findRideOrThrow(rideId);
        RideStatus targetStatus = dto.getStatus();

        rideStateMachine.validateRideStateForTransition(ride, targetStatus, dto.getDriverId());

        LocalDateTime now = LocalDateTime.now();
        ride.setStatus(targetStatus);
        ride.setUpdatedAt(now);

        switch (targetStatus) {
            case ASSIGNED -> {
                ride.setDriverId(dto.getDriverId());
                notifyDriverBusy(dto.getDriverId());
            }
            case ACCEPTED -> {
                if (dto.getDriverId() != null) {
                    ride.setDriverId(dto.getDriverId());
                }
                ride.setAcceptedAt(now);
                notifyDriverBusy(ride.getDriverId());
            }
            case IN_PROGRESS -> ride.setStartedAt(now);
            case COMPLETED -> {
                ride.setCompletedAt(now);
                if (ride.getActualFare() == null) {
                    ride.setActualFare(ride.getEstimatedFare() != null ? ride.getEstimatedFare() : 100.0);
                }
                notifyDriverAvailable(ride.getDriverId());
                triggerPayment(ride);
            }
            case CANCELLED -> {
                ride.setCancelledAt(now);
                ride.setCancellationReason(dto.getCancellationReason() != null ? dto.getCancellationReason() : "Ride cancelled");
                notifyDriverAvailable(ride.getDriverId());
            }
            default -> {}
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} transitioned to {}", rideId, targetStatus);
        return toDto(saved);
    }

    /**
     * Retrieves ride by ID.
     */
    public RideResponseDto getRideById(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        return toDto(ride);
    }

    /**
     * Retrieves all rides for a specific passenger.
     */
    public List<RideResponseDto> getRidesByPassenger(String passengerId) {
        return rideRepository.findByPassengerIdOrderByRequestedAtDesc(passengerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all rides for a specific driver.
     */
    public List<RideResponseDto> getRidesByDriver(String driverId) {
        return rideRepository.findByDriverIdOrderByRequestedAtDesc(driverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all rides optionally filtered by status.
     */
    public List<RideResponseDto> getAllRides(RideStatus status) {
        List<Ride> rides = (status != null)
                ? rideRepository.findByStatus(status)
                : rideRepository.findAll();
        return rides.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Helper & Private Domain Methods
    // =========================================================================

    private Ride findRideOrThrow(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId, true));
    }

    private void validateDriverOwnership(Ride ride, String driverId) {
        if (driverId != null && !driverId.isBlank() && ride.getDriverId() != null) {
            if (!ride.getDriverId().equals(driverId)) {
                throw new UnauthorizedActionException("Driver '" + driverId + "' is not authorized for this ride.");
            }
        }
    }

    private void notifyDriverBusy(String driverId) {
        if (driverId != null && !driverId.isBlank()) {
            try {
                driverClient.updateDriverAvailability(driverId, "BUSY").subscribe();
            } catch (Exception e) {
                log.warn("Failed to notify driver busy: {}", e.getMessage());
            }
        }
    }

    private void notifyDriverAvailable(String driverId) {
        if (driverId != null && !driverId.isBlank()) {
            try {
                driverClient.updateDriverAvailability(driverId, "AVAILABLE").subscribe();
            } catch (Exception e) {
                log.warn("Failed to notify driver available: {}", e.getMessage());
            }
        }
    }

    private void triggerPayment(Ride ride) {
        try {
            if (ride.getDriverId() != null) {
                paymentClient.processPayment(
                        ride.getId(),
                        ride.getPassengerId(),
                        ride.getDriverId(),
                        ride.getActualFare(),
                        "CARD"
                ).subscribe();
            }
        } catch (Exception e) {
            log.warn("Failed to process payment on ride completion: {}", e.getMessage());
        }
    }

    private Double fetchOrCalculateEstimatedFare(double distanceKm, int durationMinutes) {
        try {
            Map<String, Object> fareResponse = paymentClient.estimateFare(distanceKm, durationMinutes).block();
            if (fareResponse != null && fareResponse.containsKey("estimatedTotalFare")) {
                return Double.valueOf(fareResponse.get("estimatedTotalFare").toString());
            }
        } catch (Exception e) {
            log.debug("Payment service fare estimation unavailable, falling back to local formula: {}", e.getMessage());
        }
        // Base fare: 100.00 + 40.00/km + 5.00/min
        double computed = 100.0 + (distanceKm * 40.0) + (durationMinutes * 5.0);
        return Math.round(computed * 100.0) / 100.0;
    }

    private String findEligibleAvailableDriver(Location pickupLocation) {
        if (pickupLocation == null || pickupLocation.getLatitude() == null || pickupLocation.getLongitude() == null) {
            return null;
        }
        try {
            List<Map<String, Object>> availableDrivers = driverClient.getAvailableDrivers(
                    pickupLocation.getLatitude(),
                    pickupLocation.getLongitude(),
                    10.0
            ).block();

            if (availableDrivers != null && !availableDrivers.isEmpty()) {
                for (Map<String, Object> driver : availableDrivers) {
                    if (driver != null && driver.containsKey("id") && driver.get("id") != null) {
                        return driver.get("id").toString();
                    }
                    if (driver != null && driver.containsKey("driverId") && driver.get("driverId") != null) {
                        return driver.get("driverId").toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Driver selection lookup encountered error: {}", e.getMessage());
        }
        return null;
    }

    public double calculateDistance(Location start, Location end) {
        if (start == null || end == null ||
                start.getLatitude() == null || start.getLongitude() == null ||
                end.getLatitude() == null || end.getLongitude() == null) {
            return 5.0; // Standard fallback distance
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

    public int calculateEstimatedDurationMinutes(double distanceKm) {
        return (int) Math.max(5, Math.round(distanceKm * 2.5));
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
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .cancelledAt(ride.getCancelledAt())
                .cancellationReason(ride.getCancellationReason())
                .build();
    }
}
