package com.ridelink.ride.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverClient {

    private final WebClient driverWebClient;

    public Mono<List<Map<String, Object>>> getAvailableDrivers(Double latitude, Double longitude, double radiusKm) {
        return driverWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/drivers/available")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("radiusKm", radiusKm)
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(3))
                .map(res -> (List<Map<String, Object>>) (List<?>) res)
                .doOnError(e -> log.warn("Failed to fetch available drivers near ({}, {}): {}", latitude, longitude, e.getMessage()))
                .onErrorResume(e -> Mono.just(Collections.emptyList()));
    }

    public Mono<Map<String, Object>> updateDriverAvailability(String driverId, String status) {
        if (driverId == null || driverId.isBlank()) {
            return Mono.empty();
        }
        return driverWebClient.put()
                .uri("/api/drivers/{id}/availability", driverId)
                .bodyValue(Map.of("status", status))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .map(res -> (Map<String, Object>) res)
                .doOnError(e -> log.warn("Failed to update driver availability for {}: {}", driverId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}
