package com.ridelink.ride.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DriverClient {

    private final WebClient driverWebClient;

    public Mono<List> getAvailableDrivers(Double latitude, Double longitude, double radiusKm) {
        return driverWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/drivers/available")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("radiusKm", radiusKm)
                        .build())
                .retrieve()
                .bodyToMono(List.class);
    }

    public Mono<Map> updateDriverAvailability(String driverId, String status) {
        return driverWebClient.put()
                .uri("/api/drivers/{id}/availability", driverId)
                .bodyValue(Map.of("status", status))
                .retrieve()
                .bodyToMono(Map.class);
    }
}
