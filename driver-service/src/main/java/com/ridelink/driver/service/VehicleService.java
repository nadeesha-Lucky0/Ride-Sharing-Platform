package com.ridelink.driver.service;

import com.ridelink.driver.dto.VehicleRegistrationDto;
import com.ridelink.driver.dto.VehicleResponseDto;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.exception.VehicleNotFoundException;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.driver.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleResponseDto registerVehicle(VehicleRegistrationDto dto) {
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException(dto.getDriverId(), true));

        // Check if license plate is already registered
        Optional<Vehicle> existingVehicleWithPlate = vehicleRepository.findByLicensePlate(dto.getLicensePlate().toUpperCase().trim());
        if (existingVehicleWithPlate.isPresent()) {
            throw new DuplicateResourceException("Vehicle with license plate '" + dto.getLicensePlate() + "' is already registered.");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = dto.getActive() == null || dto.getActive();

        Vehicle vehicle = Vehicle.builder()
                .driverId(driver.getId())
                .make(dto.getMake().trim())
                .model(dto.getModel().trim())
                .year(dto.getYear())
                .color(dto.getColor().trim())
                .licensePlate(dto.getLicensePlate().toUpperCase().trim())
                .category(dto.getCategory())
                .seatingCapacity(dto.getSeatingCapacity())
                .active(isActive)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Registered vehicle {} ({}) for driver {}", saved.getId(), saved.getLicensePlate(), driver.getId());

        // Update activeVehicleId on driver if active or none currently set
        if (isActive || driver.getActiveVehicleId() == null) {
            driver.setActiveVehicleId(saved.getId());
            driver.setUpdatedAt(now);
            driverRepository.save(driver);
        }

        return toDto(saved);
    }

    public VehicleResponseDto updateVehicle(String vehicleId, VehicleRegistrationDto dto) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, true));

        // If license plate is changing, check uniqueness
        String updatedPlate = dto.getLicensePlate().toUpperCase().trim();
        if (!vehicle.getLicensePlate().equalsIgnoreCase(updatedPlate)) {
            Optional<Vehicle> duplicate = vehicleRepository.findByLicensePlate(updatedPlate);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(vehicleId)) {
                throw new DuplicateResourceException("License plate '" + updatedPlate + "' is already registered to another vehicle.");
            }
        }

        vehicle.setMake(dto.getMake().trim());
        vehicle.setModel(dto.getModel().trim());
        vehicle.setYear(dto.getYear());
        vehicle.setColor(dto.getColor().trim());
        vehicle.setLicensePlate(updatedPlate);
        vehicle.setCategory(dto.getCategory());
        vehicle.setSeatingCapacity(dto.getSeatingCapacity());
        if (dto.getActive() != null) {
            vehicle.setActive(dto.getActive());
        }
        vehicle.setUpdatedAt(LocalDateTime.now());

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Updated vehicle {} ({})", saved.getId(), saved.getLicensePlate());
        return toDto(saved);
    }

    public VehicleResponseDto getVehicleById(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, true));
        return toDto(vehicle);
    }

    public VehicleResponseDto getActiveVehicleByDriverId(String driverId) {
        // Validate driver exists
        if (!driverRepository.existsById(driverId)) {
            throw new DriverNotFoundException(driverId, true);
        }

        Vehicle vehicle = vehicleRepository.findFirstByDriverIdAndActiveTrue(driverId)
                .orElseThrow(() -> new VehicleNotFoundException("No active vehicle found for driver: " + driverId));
        return toDto(vehicle);
    }

    public List<VehicleResponseDto> getVehiclesByDriverId(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new DriverNotFoundException(driverId, true);
        }
        return vehicleRepository.findByDriverId(driverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void deleteVehicle(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, true));

        // If driver had this as activeVehicleId, clear it
        driverRepository.findById(vehicle.getDriverId()).ifPresent(driver -> {
            if (vehicleId.equals(driver.getActiveVehicleId())) {
                driver.setActiveVehicleId(null);
                driver.setUpdatedAt(LocalDateTime.now());
                driverRepository.save(driver);
            }
        });

        vehicleRepository.delete(vehicle);
        log.info("Deleted vehicle {}", vehicleId);
    }

    public VehicleResponseDto toDto(Vehicle vehicle) {
        if (vehicle == null) return null;
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriverId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .color(vehicle.getColor())
                .licensePlate(vehicle.getLicensePlate())
                .category(vehicle.getCategory())
                .seatingCapacity(vehicle.getSeatingCapacity())
                .active(vehicle.isActive())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
