package com.ridelink.driver.service;

import com.ridelink.driver.dto.VehicleRequestDto;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.driver.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public Vehicle registerVehicle(VehicleRequestDto dto) {

        // 1. Check that the driver exists
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Driver not found with id: " + dto.getDriverId()
                        ));

        // 2. Check whether the driver already has a vehicle
        if (vehicleRepository.findByDriverId(dto.getDriverId()).isPresent()) {
            throw new IllegalStateException(
                    "Driver already has a registered vehicle"
            );
        }

        // 3. Check whether the license plate is already registered
        if (vehicleRepository.findByLicensePlate(dto.getLicensePlate()).isPresent()) {
            throw new IllegalStateException(
                    "License plate is already registered"
            );
        }

        // 4. Convert DTO → Vehicle entity
        Vehicle vehicle = Vehicle.builder()
                .driverId(driver.getId())
                .make(dto.getMake())
                .model(dto.getModel())
                .year(dto.getYear())
                .color(dto.getColor())
                .licensePlate(dto.getLicensePlate())
                .seatingCapacity(dto.getSeatingCapacity())
                .build();

        // 5. Save vehicle
        return vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicleByDriverId(String driverId) {
        return vehicleRepository.findByDriverId(driverId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not found for driver: " + driverId
                        ));
    }
}