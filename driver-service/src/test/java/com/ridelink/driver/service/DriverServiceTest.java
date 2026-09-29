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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Driver Service Unit Tests")
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Spy
    private LocationService locationService = new LocationService(null);

    @InjectMocks
    private DriverService driverService;

    private Driver sampleDriver;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleDriver = Driver.builder()
                .id("driver-101")
                .userId("user-501")
                .fullName("Sunil Perera")
                .phoneNumber("+94771234567")
                .licenseNumber("B1234567")
                .status(DriverStatus.OFFLINE)
                .currentLatitude(6.9344)
                .currentLongitude(79.8510)
                .serviceCity("Colombo")
                .serviceArea("Western Province")
                .rating(4.9)
                .totalRides(50)
                .activeVehicleId("veh-201")
                .createdAt(LocalDateTime.now())
                .build();

        sampleVehicle = Vehicle.builder()
                .id("veh-201")
                .driverId("driver-101")
                .make("Toyota")
                .model("Prius")
                .year(2021)
                .color("White")
                .licensePlate("CAB-1234")
                .category(VehicleCategory.SEDAN)
                .seatingCapacity(4)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Profile Management Tests")
    class ProfileTests {

        @Test
        @DisplayName("Should create new driver profile when not exists")
        void createOrUpdateProfile_newDriver_shouldCreate() {
            DriverProfileDto dto = DriverProfileDto.builder()
                    .userId("user-501")
                    .fullName("Sunil Perera")
                    .phoneNumber("+94771234567")
                    .licenseNumber("B1234567")
                    .serviceCity("Colombo")
                    .build();

            when(driverRepository.findByLicenseNumber("B1234567")).thenReturn(Optional.empty());
            when(driverRepository.findByUserId("user-501")).thenReturn(Optional.empty());
            when(driverRepository.save(any(Driver.class))).thenAnswer(i -> {
                Driver d = i.getArgument(0);
                d.setId("driver-101");
                return d;
            });

            DriverResponseDto response = driverService.createOrUpdateProfile(dto);

            assertNotNull(response);
            assertEquals("driver-101", response.getId());
            assertEquals("user-501", response.getUserId());
            assertEquals("B1234567", response.getLicenseNumber());
            assertEquals(DriverStatus.OFFLINE, response.getStatus());
        }

        @Test
        @DisplayName("Should reject profile registration with duplicate license number")
        void createOrUpdateProfile_duplicateLicense_shouldThrowDuplicateException() {
            Driver existingOtherDriver = Driver.builder().id("other-id").userId("other-user").build();

            DriverProfileDto dto = DriverProfileDto.builder()
                    .userId("user-501")
                    .licenseNumber("B1234567")
                    .build();

            when(driverRepository.findByLicenseNumber("B1234567")).thenReturn(Optional.of(existingOtherDriver));

            assertThrows(DuplicateResourceException.class,
                    () -> driverService.createOrUpdateProfile(dto));
        }

        @Test
        @DisplayName("Should retrieve driver by ID with linked active vehicle")
        void getDriverById_existing_shouldReturnDtoWithVehicle() {
            when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));
            when(vehicleRepository.findById("veh-201")).thenReturn(Optional.of(sampleVehicle));

            DriverResponseDto response = driverService.getDriverById("driver-101");

            assertNotNull(response);
            assertEquals("driver-101", response.getId());
            assertNotNull(response.getActiveVehicle());
            assertEquals("Toyota", response.getActiveVehicle().getMake());
        }

        @Test
        @DisplayName("Should throw DriverNotFoundException for non-existent driver")
        void getDriverById_notFound_shouldThrowException() {
            when(driverRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThrows(DriverNotFoundException.class,
                    () -> driverService.getDriverById("unknown"));
        }
    }

    @Nested
    @DisplayName("Availability & Service Area Tests")
    class AvailabilityTests {

        @Test
        @DisplayName("Should update driver availability status")
        void updateAvailability_shouldChangeStatus() {
            when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));
            when(driverRepository.save(any(Driver.class))).thenAnswer(i -> i.getArgument(0));

            DriverResponseDto response = driverService.updateAvailability("driver-101", DriverStatus.AVAILABLE);

            assertEquals(DriverStatus.AVAILABLE, response.getStatus());
        }

        @Test
        @DisplayName("Should toggle availability status from OFFLINE to AVAILABLE")
        void toggleAvailability_fromOffline_shouldBecomeAvailable() {
            sampleDriver.setStatus(DriverStatus.OFFLINE);
            when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));
            when(driverRepository.save(any(Driver.class))).thenAnswer(i -> i.getArgument(0));

            DriverResponseDto response = driverService.toggleAvailability("driver-101");

            assertEquals(DriverStatus.AVAILABLE, response.getStatus());
        }

        @Test
        @DisplayName("Should toggle availability status from AVAILABLE to OFFLINE")
        void toggleAvailability_fromAvailable_shouldBecomeOffline() {
            sampleDriver.setStatus(DriverStatus.AVAILABLE);
            when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));
            when(driverRepository.save(any(Driver.class))).thenAnswer(i -> i.getArgument(0));

            DriverResponseDto response = driverService.toggleAvailability("driver-101");

            assertEquals(DriverStatus.OFFLINE, response.getStatus());
        }

        @Test
        @DisplayName("Should update driver operating service area")
        void updateServiceArea_shouldUpdateCityAndArea() {
            when(driverRepository.findById("driver-101")).thenReturn(Optional.of(sampleDriver));
            when(driverRepository.save(any(Driver.class))).thenAnswer(i -> i.getArgument(0));

            ServiceAreaUpdateDto dto = new ServiceAreaUpdateDto("Kandy", "Central Province");
            DriverResponseDto response = driverService.updateServiceArea("driver-101", dto);

            assertEquals("Kandy", response.getServiceCity());
            assertEquals("Central Province", response.getServiceArea());
        }
    }

    @Nested
    @DisplayName("Eligible Available Drivers Query Tests")
    class ProximityTests {

        @Test
        @DisplayName("Should find available drivers within radius and sort by distance")
        void findNearestAvailableDrivers_withinRadius_shouldReturnSorted() {
            Driver driverNear = Driver.builder()
                    .id("driver-near")
                    .status(DriverStatus.AVAILABLE)
                    .currentLatitude(6.9350)
                    .currentLongitude(79.8520)
                    .activeVehicleId("veh-near")
                    .build();

            Driver driverFar = Driver.builder()
                    .id("driver-far")
                    .status(DriverStatus.ONLINE)
                    .currentLatitude(6.9700)
                    .currentLongitude(79.8900)
                    .activeVehicleId("veh-far")
                    .build();

            when(driverRepository.findByStatusIn(anyList())).thenReturn(List.of(driverFar, driverNear));
            when(vehicleRepository.findById("veh-near")).thenReturn(Optional.of(sampleVehicle));
            when(vehicleRepository.findById("veh-far")).thenReturn(Optional.of(sampleVehicle));

            List<DriverResponseDto> results = driverService.findNearestAvailableDrivers(
                    6.9344, 79.8510, 10.0, null, null
            );

            assertFalse(results.isEmpty());
            assertEquals(2, results.size());
            assertEquals("driver-near", results.get(0).getId());
            assertTrue(results.get(0).getDistanceKm() < results.get(1).getDistanceKm());
        }

        @Test
        @DisplayName("Should filter out drivers exceeding search radius")
        void findNearestAvailableDrivers_exceedingRadius_shouldExclude() {
            Driver driverTooFar = Driver.builder()
                    .id("driver-kandy")
                    .status(DriverStatus.AVAILABLE)
                    .currentLatitude(7.2906) // ~115km away
                    .currentLongitude(80.6337)
                    .build();

            when(driverRepository.findByStatusIn(anyList())).thenReturn(List.of(driverTooFar));

            List<DriverResponseDto> results = driverService.findNearestAvailableDrivers(
                    6.9344, 79.8510, 10.0, null, null
            );

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should filter available drivers by vehicle category")
        void findNearestAvailableDrivers_filterByCategory_shouldMatch() {
            Driver driverSedan = Driver.builder()
                    .id("driver-sedan")
                    .status(DriverStatus.AVAILABLE)
                    .currentLatitude(6.9350)
                    .currentLongitude(79.8520)
                    .activeVehicleId("veh-sedan")
                    .build();

            Driver driverTukTuk = Driver.builder()
                    .id("driver-tuktuk")
                    .status(DriverStatus.AVAILABLE)
                    .currentLatitude(6.9350)
                    .currentLongitude(79.8520)
                    .activeVehicleId("veh-tuktuk")
                    .build();

            Vehicle sedan = Vehicle.builder().id("veh-sedan").category(VehicleCategory.SEDAN).seatingCapacity(4).build();
            Vehicle tuktuk = Vehicle.builder().id("veh-tuktuk").category(VehicleCategory.TUK_TUK).seatingCapacity(3).build();

            when(driverRepository.findByStatusIn(anyList())).thenReturn(List.of(driverSedan, driverTukTuk));
            when(vehicleRepository.findById("veh-sedan")).thenReturn(Optional.of(sedan));
            when(vehicleRepository.findById("veh-tuktuk")).thenReturn(Optional.of(tuktuk));

            List<DriverResponseDto> results = driverService.findNearestAvailableDrivers(
                    6.9344, 79.8510, 10.0, VehicleCategory.TUK_TUK, null
            );

            assertEquals(1, results.size());
            assertEquals("driver-tuktuk", results.get(0).getId());
        }
    }
}
