package com.ridelink.ride.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final WebClient paymentWebClient;

    public Mono<Map<String, Object>> estimateFare(Double distanceKm, Integer durationMinutes) {
        return paymentWebClient.post()
                .uri("/api/fare/estimate")
                .bodyValue(Map.of(
                        "distanceKm", distanceKm != null ? distanceKm : 5.0,
                        "estimatedDurationMinutes", durationMinutes != null ? durationMinutes : 15
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .map(res -> (Map<String, Object>) res)
                .doOnError(e -> log.warn("Failed to estimate fare from payment-service: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Map<String, Object>> processPayment(String rideId, String passengerId, String driverId, Double amount, String paymentMethod) {
        return paymentWebClient.post()
                .uri("/api/payments/process")
                .bodyValue(Map.of(
                        "rideId", rideId != null ? rideId : "",
                        "passengerId", passengerId != null ? passengerId : "",
                        "driverId", driverId != null ? driverId : "",
                        "amount", amount != null ? amount : 0.0,
                        "paymentMethod", paymentMethod != null ? paymentMethod : "CARD"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .map(res -> (Map<String, Object>) res)
                .doOnError(e -> log.warn("Failed to process payment for ride {}: {}", rideId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}
