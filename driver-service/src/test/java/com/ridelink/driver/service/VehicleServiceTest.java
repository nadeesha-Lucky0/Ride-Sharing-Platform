package com.ridelink.driver.service;

import com.ridelink.driver.dto.VehicleRegistrationDto;
import com.ridelink.driver.dto.VehicleResponseDto;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.exception.VehicleNotFoundException;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.model.VehicleCategory;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.driver.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Vehicle Service Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Driver sampleDriver;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleDriver = Driver.builder()
                .id("driver-101")
                .userId("user-501")
                .licenseNumber("B1234567")
                .build();

        sampleVehicle = Vehicle.builder()
                .id("veh-201")
                .driverId("driver-101")
                .make("Toyota")
                .model("Prius")
                .year(2022)
                .color("Pearl White")
                .licensePlate("CAB-4589")
                .category(VehicleCategory.SEDAN)
                .seatingCapacity(4)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should register vehicle and link to driver")
    void registerVehicle_valid_shouldCreateAndLink() {
        VehicleRegistrationDto dto = VehicleRegistrationDto.builder()
                .driverId("driver-101")
                .make("Toyota")
                .model("Prius")
                .year(2022)
                .color("Pearl White")
                .licensePlate("CAB-4589")
                .category(VehicleCategory.SEDAN)
                .seatingCapacity(4)
                .active(true)
                .build();

        when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findByLicensePlate("CAB-4589")).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> {
            Vehicle v = i.getArgument(0);
            v.setId("veh-201");
            return v;
        });

        VehicleResponseDto result = vehicleService.registerVehicle(dto);

        assertNotNull(result);
        assertEquals("veh-201", result.getId());
        assertEquals("CAB-4589", result.getLicensePlate());
        assertEquals("Toyota", result.getMake());
        verify(driverRepository).save(sampleDriver);
        assertEquals("veh-201", sampleDriver.getActiveVehicleId());
    }

    @Test
    @DisplayName("Should reject vehicle registration if driver not found")
    void registerVehicle_driverNotFound_shouldThrow() {
        VehicleRegistrationDto dto = VehicleRegistrationDto.builder()
                .driverId("unknown-driver")
                .licensePlate("CAB-4589")
                .build();

        when(driverRepository.findById("unknown-driver")).thenReturn(Optional.empty());

        assertThrows(DriverNotFoundException.class, () -> vehicleService.registerVehicle(dto));
    }

    @Test
    @DisplayName("Should reject registration with duplicate license plate")
    void registerVehicle_duplicatePlate_shouldThrow() {
        VehicleRegistrationDto dto = VehicleRegistrationDto.builder()
                .driverId("driver-101")
                .licensePlate("CAB-4589")
                .build();

        when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findByLicensePlate("CAB-4589")).thenReturn(Optional.of(sampleVehicle));

        assertThrows(DuplicateResourceException.class, () -> vehicleService.registerVehicle(dto));
    }

    @Test
    @DisplayName("Should get active vehicle by driver ID")
    void getActiveVehicleByDriverId_existing_shouldReturnDto() {
        when(driverRepository.existsById("driver-101")).thenReturn(true);
        when(vehicleRepository.findFirstByDriverIdAndActiveTrue("driver-101")).thenReturn(Optional.of(sampleVehicle));

        VehicleResponseDto result = vehicleService.getActiveVehicleByDriverId("driver-101");

        assertNotNull(result);
        assertEquals("veh-201", result.getId());
        assertEquals("Prius", result.getModel());
    }

    @Test
    @DisplayName("Should get all vehicles for a driver")
    void getVehiclesByDriverId_shouldReturnList() {
        when(driverRepository.existsById("driver-101")).thenReturn(true);
        when(vehicleRepository.findByDriverId("driver-101")).thenReturn(List.of(sampleVehicle));

        List<VehicleResponseDto> list = vehicleService.getVehiclesByDriverId("driver-101");

        assertEquals(1, list.size());
        assertEquals("CAB-4589", list.get(0).getLicensePlate());
    }

    @Test
    @DisplayName("Should delete vehicle and clear driver active vehicle reference")
    void deleteVehicle_shouldDeleteAndClearDriverRef() {
        sampleDriver.setActiveVehicleId("veh-201");
        when(vehicleRepository.findById("veh-201")).thenReturn(Optional.of(sampleVehicle));
        when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));

        vehicleService.deleteVehicle("veh-201");

        verify(vehicleRepository).delete(sampleVehicle);
        assertNull(sampleDriver.getActiveVehicleId());
        verify(driverRepository).save(sampleDriver);
    }
}
