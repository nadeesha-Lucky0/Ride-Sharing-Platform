package com.ridelink.driver.service;

import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final DriverRepository driverRepository;

    public Driver updateLocation(String driverId, Double latitude, Double longitude, String address) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(driverId, true));

        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        if (address != null && !address.isBlank()) {
            driver.setCurrentAddress(address);
        }
        driver.setLastLocationUpdate(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());

        log.info("Updated location for driver {}: ({}, {}) - {}", driverId, latitude, longitude, address);
        return driverRepository.save(driver);
    }

    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(EARTH_RADIUS_KM * c * 100.0) / 100.0;
    }
}
