package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.PaymentClient;
import com.ridelink.ride.dto.DriverAssignDto;
import com.ridelink.ride.dto.RideCancelDto;
import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.dto.RideStatusUpdateDto;
import com.ridelink.ride.exception.InvalidStateTransitionException;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.exception.UnauthorizedActionException;
import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.statemachine.RideStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ride Service Business Logic Tests")
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverClient driverClient;

    @Mock
    private PaymentClient paymentClient;

    @Spy
    private RideStateMachine rideStateMachine = new RideStateMachine();

    @InjectMocks
    private RideService rideService;

    private Location pickup;
    private Location dropoff;
    private Ride sampleRide;

    @BeforeEach
    void setUp() {
        pickup = new Location("Colombo Fort", 6.9344, 79.8510);
        dropoff = new Location("Bambalapitiya", 6.8920, 79.8550);

        sampleRide = Ride.builder()
                .id("ride-123")
                .passengerId("passenger-1")
                .driverId("driver-456")
                .pickupLocation(pickup)
                .dropoffLocation(dropoff)
                .status(RideStatus.REQUESTED)
                .estimatedFare(350.0)
                .distanceKm(4.8)
                .estimatedDurationMinutes(12)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Ride Request Creation Tests")
    class RequestRideTests {

        @Test
        @DisplayName("Should successfully create a ride and match nearest available driver")
        void requestRide_withAvailableDriver_shouldCreateAssignedRide() {
            RideRequestDto request = RideRequestDto.builder()
                    .passengerId("passenger-1")
                    .pickupLocation(pickup)
                    .dropoffLocation(dropoff)
                    .build();

            when(paymentClient.estimateFare(anyDouble(), anyInt()))
                    .thenReturn(Mono.just(Map.of("estimatedTotalFare", 420.0)));
            when(driverClient.getAvailableDrivers(eq(6.9344), eq(79.8510), eq(10.0)))
                    .thenReturn(Mono.just(List.of(Map.of("id", "driver-456"))));
            when(driverClient.updateDriverAvailability(eq("driver-456"), eq("BUSY")))
                    .thenReturn(Mono.just(Map.of("status", "BUSY")));
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> {
                Ride r = i.getArgument(0);
                r.setId("generated-id");
                return r;
            });

            RideResponseDto response = rideService.requestRide(request);

            assertNotNull(response);
            assertEquals("generated-id", response.getId());
            assertEquals("passenger-1", response.getPassengerId());
            assertEquals("driver-456", response.getDriverId());
            assertEquals(RideStatus.ASSIGNED, response.getStatus());
            assertEquals(420.0, response.getEstimatedFare());
            assertTrue(response.getDistanceKm() > 0);
            verify(driverClient).getAvailableDrivers(6.9344, 79.8510, 10.0);
        }

        @Test
        @DisplayName("Should create ride in REQUESTED status when no drivers are available")
        void requestRide_withoutAvailableDriver_shouldCreateRequestedRide() {
            RideRequestDto request = RideRequestDto.builder()
                    .passengerId("passenger-1")
                    .pickupLocation(pickup)
                    .dropoffLocation(dropoff)
                    .build();

            when(paymentClient.estimateFare(anyDouble(), anyInt())).thenReturn(Mono.empty());
            when(driverClient.getAvailableDrivers(anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(Mono.just(List.of()));
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

            RideResponseDto response = rideService.requestRide(request);

            assertNotNull(response);
            assertNull(response.getDriverId());
            assertEquals(RideStatus.REQUESTED, response.getStatus());
            assertNotNull(response.getEstimatedFare()); // fallback formula applied
        }

        @Test
        @DisplayName("Should assign specific driver if requestedDriverId is provided")
        void requestRide_withExplicitDriverId_shouldAssignDirectly() {
            RideRequestDto request = RideRequestDto.builder()
                    .passengerId("passenger-1")
                    .pickupLocation(pickup)
                    .dropoffLocation(dropoff)
                    .requestedDriverId("driver-special")
                    .build();

            when(paymentClient.estimateFare(anyDouble(), anyInt())).thenReturn(Mono.empty());
            when(driverClient.updateDriverAvailability("driver-special", "BUSY")).thenReturn(Mono.empty());
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

            RideResponseDto response = rideService.requestRide(request);

            assertEquals("driver-special", response.getDriverId());
            assertEquals(RideStatus.ASSIGNED, response.getStatus());
            verify(driverClient, never()).getAvailableDrivers(anyDouble(), anyDouble(), anyDouble());
        }
    }

    @Nested
    @DisplayName("Lifecycle State Transitions Tests")
    class LifecycleTransitionTests {

        @Test
        @DisplayName("Should successfully assign driver to a requested ride")
        void assignDriver_shouldTransitionToAssigned() {
            sampleRide.setStatus(RideStatus.REQUESTED);
            sampleRide.setDriverId(null);
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));
            when(driverClient.updateDriverAvailability("driver-789", "BUSY")).thenReturn(Mono.empty());
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

            RideResponseDto response = rideService.assignDriver("ride-123", new DriverAssignDto("driver-789"));

            assertEquals(RideStatus.ASSIGNED, response.getStatus());
            assertEquals("driver-789", response.getDriverId());
        }

        @Test
        @DisplayName("Should allow driver to accept ride")
        void acceptRide_shouldTransitionToAccepted() {
            sampleRide.setStatus(RideStatus.ASSIGNED);
            sampleRide.setDriverId("driver-456");
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));
            when(driverClient.updateDriverAvailability("driver-456", "BUSY")).thenReturn(Mono.empty());
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

            RideResponseDto response = rideService.acceptRide("ride-123", "driver-456");

            assertEquals(RideStatus.ACCEPTED, response.getStatus());
            assertNotNull(response.getAcceptedAt());
        }

        @Test
        @DisplayName("Should reject accept if a different driver is already assigned")
        void acceptRide_byDifferentDriver_shouldThrowUnauthorized() {
            sampleRide.setStatus(RideStatus.ASSIGNED);
            sampleRide.setDriverId("driver-456");
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));

            assertThrows(UnauthorizedActionException.class,
                    () -> rideService.acceptRide("ride-123", "driver-999"));
        }

        @Test
        @DisplayName("Should start trip and transition to IN_PROGRESS")
        void startRide_shouldTransitionToInProgress() {
            sampleRide.setStatus(RideStatus.ACCEPTED);
            sampleRide.setDriverId("driver-456");
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

            RideResponseDto response = rideService.startRide("ride-123", "driver-456");

            assertEquals(RideStatus.IN_PROGRESS, response.getStatus());
            assertNotNull(response.getStartedAt());
        }

        @Test
        @DisplayName("Should complete ride, set actualFare, trigger payment and free driver")
        void completeRide_shouldTransitionToCompletedAndProcessPayment() {
            sampleRide.setStatus(RideStatus.IN_PROGRESS);
            sampleRide.setDriverId("driver-456");
            sampleRide.setEstimatedFare(500.0);
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));
            when(driverClient.updateDriverAvailability("driver-456", "AVAILABLE")).thenReturn(Mono.empty());
            when(paymentClient.processPayment("ride-123", "passenger-1", "driver-456", 500.0, "CARD")).thenReturn(Mono.empty());
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

            RideResponseDto response = rideService.completeRide("ride-123", "driver-456");

            assertEquals(RideStatus.COMPLETED, response.getStatus());
            assertEquals(500.0, response.getActualFare());
            assertNotNull(response.getCompletedAt());
        }

        @Test
        @DisplayName("Should cancel active ride by authorized passenger")
        void cancelRide_byPassenger_shouldCancelAndFreeDriver() {
            sampleRide.setStatus(RideStatus.ASSIGNED);
            sampleRide.setDriverId("driver-456");
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));
            when(driverClient.updateDriverAvailability("driver-456", "AVAILABLE")).thenReturn(Mono.empty());
            when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

            RideCancelDto cancelDto = new RideCancelDto("passenger-1", "Changed plans");
            RideResponseDto response = rideService.cancelRide("ride-123", cancelDto);

            assertEquals(RideStatus.CANCELLED, response.getStatus());
            assertEquals("Changed plans", response.getCancellationReason());
            assertNotNull(response.getCancelledAt());
        }

        @Test
        @DisplayName("Should reject cancel ride by unauthorized user")
        void cancelRide_byUnauthorizedUser_shouldThrowUnauthorized() {
            sampleRide.setStatus(RideStatus.ASSIGNED);
            sampleRide.setPassengerId("passenger-1");
            sampleRide.setDriverId("driver-456");
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));

            RideCancelDto cancelDto = new RideCancelDto("stranger-999", "I want to cancel");
            assertThrows(UnauthorizedActionException.class,
                    () -> rideService.cancelRide("ride-123", cancelDto));
        }

        @Test
        @DisplayName("Should reject invalid state transition e.g. REQUESTED to COMPLETED")
        void updateStatus_invalidTransition_shouldThrowInvalidStateTransitionException() {
            sampleRide.setStatus(RideStatus.REQUESTED);
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));

            RideStatusUpdateDto dto = new RideStatusUpdateDto(RideStatus.COMPLETED, "driver-456", null);
            assertThrows(InvalidStateTransitionException.class,
                    () -> rideService.updateStatus("ride-123", dto));
        }

        @Test
        @DisplayName("Should reject state transition for non-existent ride")
        void updateStatus_nonExistentRide_shouldThrowRideNotFoundException() {
            when(rideRepository.findById("non-existent")).thenReturn(Optional.empty());

            RideStatusUpdateDto dto = new RideStatusUpdateDto(RideStatus.ACCEPTED, "driver-456", null);
            assertThrows(RideNotFoundException.class,
                    () -> rideService.updateStatus("non-existent", dto));
        }
    }

    @Nested
    @DisplayName("Query and History Retrieval Tests")
    class QueryTests {

        @Test
        @DisplayName("Should retrieve ride details by ride ID")
        void getRideById_existing_shouldReturnDto() {
            when(rideRepository.findById("ride-123")).thenReturn(Optional.of(sampleRide));

            RideResponseDto result = rideService.getRideById("ride-123");

            assertEquals("ride-123", result.getId());
            assertEquals("passenger-1", result.getPassengerId());
        }

        @Test
        @DisplayName("Should retrieve rides for a specific passenger")
        void getRidesByPassenger_shouldReturnList() {
            when(rideRepository.findByPassengerIdOrderByRequestedAtDesc("passenger-1"))
                    .thenReturn(List.of(sampleRide));

            List<RideResponseDto> result = rideService.getRidesByPassenger("passenger-1");

            assertEquals(1, result.size());
            assertEquals("ride-123", result.get(0).getId());
        }

        @Test
        @DisplayName("Should retrieve rides for a specific driver")
        void getRidesByDriver_shouldReturnList() {
            when(rideRepository.findByDriverIdOrderByRequestedAtDesc("driver-456"))
                    .thenReturn(List.of(sampleRide));

            List<RideResponseDto> result = rideService.getRidesByDriver("driver-456");

            assertEquals(1, result.size());
            assertEquals("driver-456", result.get(0).getDriverId());
        }

        @Test
        @DisplayName("Should retrieve all rides or filter by status")
        void getAllRides_withStatusFilter_shouldFilter() {
            when(rideRepository.findByStatus(RideStatus.REQUESTED)).thenReturn(List.of(sampleRide));

            List<RideResponseDto> result = rideService.getAllRides(RideStatus.REQUESTED);

            assertEquals(1, result.size());
            verify(rideRepository).findByStatus(RideStatus.REQUESTED);
        }
    }
}
