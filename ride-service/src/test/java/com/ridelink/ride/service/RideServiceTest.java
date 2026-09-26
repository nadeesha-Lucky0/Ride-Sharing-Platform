package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.PaymentClient;
import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.model.Location;
import com.ridelink.ride.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverClient driverClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private RideService rideService;

    @Test
    void requestRide_shouldSelectNearestAvailableDriver() {
        RideRequestDto request = RideRequestDto.builder()
                .passengerId("passenger-1")
                .pickupLocation(new Location("Pickup", 12.0, 77.0))
                .dropoffLocation(new Location("Dropoff", 12.3, 77.2))
                .build();

        when(paymentClient.estimateFare(anyDouble(), anyInt())).thenReturn(Mono.just(Map.of("estimatedTotalFare", 160.0)));
        when(driverClient.getAvailableDrivers(anyDouble(), anyDouble(), anyDouble())).thenReturn(Mono.just(List.of(Map.of("id", "driver-123"))));
        when(rideRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponseDto response = rideService.requestRide(request);

        assertEquals("driver-123", response.getDriverId());
        verify(driverClient).getAvailableDrivers(12.0, 77.0, 10.0);
    }
}
