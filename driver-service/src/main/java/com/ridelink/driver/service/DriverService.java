package com.ridelink.driver.service;

import com.ridelink.driver.dto.DriverProfileDto;
import com.ridelink.driver.dto.DriverResponseDto;
import com.ridelink.driver.dto.ServiceAreaUpdateDto;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.model.VehicleCategory;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.driver.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final LocationService locationService;

    /**
     * Registers a new driver operational profile or updates an existing one.
     */
    public DriverResponseDto createOrUpdateProfile(DriverProfileDto dto) {
        log.info("Processing driver operational profile for userId: {}", dto.getUserId());

        // Validate license number uniqueness
        String normalizedLicense = dto.getLicenseNumber().toUpperCase().trim();
        Optional<Driver> existingWithLicense = driverRepository.findByLicenseNumber(normalizedLicense);
        if (existingWithLicense.isPresent() && !existingWithLicense.get().getUserId().equals(dto.getUserId())) {
            throw new DuplicateResourceException("License number '" + normalizedLicense + "' is already registered to another driver.");
        }

        LocalDateTime now = LocalDateTime.now();
        Driver driver = driverRepository.findByUserId(dto.getUserId())
                .orElse(Driver.builder()
                        .userId(dto.getUserId())
                        .createdAt(now)
                        .totalRides(0)
                        .rating(5.0)
                        .status(DriverStatus.OFFLINE)
                        .build());

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            driver.setFullName(dto.getFullName().trim());
        }
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            driver.setPhoneNumber(dto.getPhoneNumber().trim());
        }
        driver.setLicenseNumber(normalizedLicense);

        if (dto.getStatus() != null) {
            driver.setStatus(dto.getStatus());
        }
        if (dto.getServiceCity() != null) {
            driver.setServiceCity(dto.getServiceCity().trim());
        }
        if (dto.getServiceArea() != null) {
            driver.setServiceArea(dto.getServiceArea().trim());
        }
        if (dto.getCurrentLatitude() != null && dto.getCurrentLongitude() != null) {
            driver.setCurrentLatitude(dto.getCurrentLatitude());
            driver.setCurrentLongitude(dto.getCurrentLongitude());
            driver.setLastLocationUpdate(now);
        }
        if (dto.getCurrentAddress() != null) {
            driver.setCurrentAddress(dto.getCurrentAddress().trim());
        }
        driver.setUpdatedAt(now);

        Driver saved = driverRepository.save(driver);
        log.info("Saved driver profile ID: {} for user: {}", saved.getId(), saved.getUserId());
        return toDto(saved);
    }

    /**
     * Updates the operational availability status of a driver.
     */
    public DriverResponseDto updateAvailability(String driverId, DriverStatus status) {
        Driver driver = findDriverOrThrow(driverId);

        driver.setStatus(status);
        driver.setUpdatedAt(LocalDateTime.now());
        Driver saved = driverRepository.save(driver);

        log.info("Driver {} availability updated to {}", driverId, status);
        return toDto(saved);
    }

    /**
     * Toggles availability status (OFFLINE <-> AVAILABLE).
     */
    public DriverResponseDto toggleAvailability(String driverId) {
        Driver driver = findDriverOrThrow(driverId);

        DriverStatus newStatus = driver.getStatus().isEligibleForRides()
                ? DriverStatus.OFFLINE
                : DriverStatus.AVAILABLE;

        driver.setStatus(newStatus);
        driver.setUpdatedAt(LocalDateTime.now());
        Driver saved = driverRepository.save(driver);

        log.info("Toggled driver {} status to {}", driverId, newStatus);
        return toDto(saved);
    }

    /**
     * Updates driver operating service city and regional zone.
     */
    public DriverResponseDto updateServiceArea(String driverId, ServiceAreaUpdateDto dto) {
        Driver driver = findDriverOrThrow(driverId);

        driver.setServiceCity(dto.getServiceCity().trim());
        if (dto.getServiceArea() != null) {
            driver.setServiceArea(dto.getServiceArea().trim());
        }
        driver.setUpdatedAt(LocalDateTime.now());

        Driver saved = driverRepository.save(driver);
        log.info("Updated service area for driver {} to {}", driverId, dto.getServiceCity());
        return toDto(saved);
    }

    /**
     * Retrieves a driver profile by MongoDB ID with linked vehicle information.
     */
    public DriverResponseDto getDriverById(String driverId) {
        Driver driver = findDriverOrThrow(driverId);
        return toDto(driver);
    }

    /**
     * Retrieves a driver profile by associated User ID.
     */
    public DriverResponseDto getDriverByUserId(String userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found for user account: " + userId));
        return toDto(driver);
    }

    /**
     * Retrieves all drivers, optionally filtered by status and operating city.
     */
    public List<DriverResponseDto> getAllDrivers(DriverStatus status, String city) {
        List<Driver> drivers;
        if (status != null && city != null && !city.isBlank()) {
            drivers = driverRepository.findByStatus(status).stream()
                    .filter(d -> city.equalsIgnoreCase(d.getServiceCity()))
                    .collect(Collectors.toList());
        } else if (status != null) {
            drivers = driverRepository.findByStatus(status);
        } else if (city != null && !city.isBlank()) {
            drivers = driverRepository.findByServiceCityIgnoreCase(city.trim());
        } else {
            drivers = driverRepository.findAll();
        }

        return drivers.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Finds nearest eligible available drivers matching geographic proximity,
     * status (AVAILABLE or ONLINE), and optional vehicle category and seating criteria.
     */
    public List<DriverResponseDto> findNearestAvailableDrivers(
            Double originLat,
            Double originLon,
            double radiusKm,
            VehicleCategory category,
            Integer minSeats) {

        if (originLat == null || originLon == null) {
            throw new IllegalArgumentException("Latitude and Longitude query coordinates are required.");
        }

        // Get all drivers in ONLINE or AVAILABLE status
        List<Driver> availableDrivers = driverRepository.findByStatusIn(
                List.of(DriverStatus.AVAILABLE, DriverStatus.ONLINE)
        );

        List<DriverResponseDto> results = new ArrayList<>();

        for (Driver driver : availableDrivers) {
            if (driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
                continue;
            }

            double distance = locationService.calculateDistanceKm(
                    originLat, originLon,
                    driver.getCurrentLatitude(), driver.getCurrentLongitude()
            );

            if (distance > radiusKm) {
                continue;
            }

            // Retrieve active vehicle
            Vehicle activeVehicle = null;
            if (driver.getActiveVehicleId() != null) {
                activeVehicle = vehicleRepository.findById(driver.getActiveVehicleId()).orElse(null);
            }
            if (activeVehicle == null) {
                activeVehicle = vehicleRepository.findFirstByDriverIdAndActiveTrue(driver.getId()).orElse(null);
            }

            // Filter by vehicle category if specified
            if (category != null) {
                if (activeVehicle == null || activeVehicle.getCategory() != category) {
                    continue;
                }
            }

            // Filter by minimum seats if specified
            if (minSeats != null && minSeats > 0) {
                if (activeVehicle == null || activeVehicle.getSeatingCapacity() < minSeats) {
                    continue;
                }
            }

            DriverResponseDto dto = toDtoWithVehicle(driver, activeVehicle);
            dto.setDistanceKm(distance);
            results.add(dto);
        }

        // Sort by distance ascending
        results.sort(Comparator.comparingDouble(d -> d.getDistanceKm() != null ? d.getDistanceKm() : Double.MAX_VALUE));
        return results;
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private Driver findDriverOrThrow(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(driverId, true));
    }

    public DriverResponseDto toDto(Driver driver) {
        if (driver == null) return null;
        Vehicle activeVehicle = null;
        if (driver.getActiveVehicleId() != null) {
            activeVehicle = vehicleRepository.findById(driver.getActiveVehicleId()).orElse(null);
        }
        if (activeVehicle == null) {
            activeVehicle = vehicleRepository.findFirstByDriverIdAndActiveTrue(driver.getId()).orElse(null);
        }
        return toDtoWithVehicle(driver, activeVehicle);
    }

    private DriverResponseDto toDtoWithVehicle(Driver driver, Vehicle vehicle) {
        return DriverResponseDto.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .fullName(driver.getFullName())
                .phoneNumber(driver.getPhoneNumber())
                .licenseNumber(driver.getLicenseNumber())
                .status(driver.getStatus())
                .currentLatitude(driver.getCurrentLatitude())
                .currentLongitude(driver.getCurrentLongitude())
                .currentAddress(driver.getCurrentAddress())
                .serviceCity(driver.getServiceCity())
                .serviceArea(driver.getServiceArea())
                .rating(driver.getRating())
                .totalRides(driver.getTotalRides())
                .activeVehicle(vehicle)
                .lastLocationUpdate(driver.getLastLocationUpdate())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}
