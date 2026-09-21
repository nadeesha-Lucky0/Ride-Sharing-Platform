package com.ridelink.driver.service;

import com.ridelink.driver.dto.DriverProfileDto;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final LocationService locationService;

    public Driver createOrUpdateProfile(DriverProfileDto dto) {
        Driver driver = driverRepository.findByUserId(dto.getUserId())
                .orElse(Driver.builder()
                        .userId(dto.getUserId())
                        .createdAt(LocalDateTime.now())
                        .totalRides(0)
                        .rating(5.0)
                        .status(DriverStatus.OFFLINE)
                        .build());

        driver.setLicenseNumber(dto.getLicenseNumber());
        if (dto.getStatus() != null) {
            driver.setStatus(dto.getStatus());
        }
        if (dto.getCurrentLatitude() != null && dto.getCurrentLongitude() != null) {
            driver.setCurrentLatitude(dto.getCurrentLatitude());
            driver.setCurrentLongitude(dto.getCurrentLongitude());
        }
        driver.setUpdatedAt(LocalDateTime.now());

        return driverRepository.save(driver);
    }

    public Driver updateAvailability(String driverId, DriverStatus status) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + driverId));

        driver.setStatus(status);
        driver.setUpdatedAt(LocalDateTime.now());
        return driverRepository.save(driver);
    }

    public Driver getDriverById(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + driverId));
    }

    public Driver getDriverByUserId(String userId) {
        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found for user: " + userId));
    }

    public List<Driver> findNearestAvailableDrivers(Double lat, Double lon, double radiusKm) {
        List<Driver> availableDrivers = driverRepository.findByStatus(DriverStatus.AVAILABLE);

        return availableDrivers.stream()
                .filter(d -> d.getCurrentLatitude() != null && d.getCurrentLongitude() != null)
                .filter(d -> locationService.calculateDistanceKm(lat, lon, d.getCurrentLatitude(), d.getCurrentLongitude()) <= radiusKm)
                .sorted(Comparator.comparingDouble(d -> locationService.calculateDistanceKm(lat, lon, d.getCurrentLatitude(), d.getCurrentLongitude())))
                .collect(Collectors.toList());
    }
}
