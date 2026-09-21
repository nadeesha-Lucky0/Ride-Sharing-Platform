package com.ridelink.ride.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final WebClient paymentWebClient;

    public Mono<Map> estimateFare(Double distanceKm, Integer durationMinutes) {
        return paymentWebClient.post()
                .uri("/api/fare/estimate")
                .bodyValue(Map.of(
                        "distanceKm", distanceKm,
                        "estimatedDurationMinutes", durationMinutes
                ))
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> processPayment(String rideId, String passengerId, String driverId, Double amount, String paymentMethod) {
        return paymentWebClient.post()
                .uri("/api/payments/process")
                .bodyValue(Map.of(
                        "rideId", rideId,
                        "passengerId", passengerId,
                        "driverId", driverId,
                        "amount", amount,
                        "paymentMethod", paymentMethod
                ))
                .retrieve()
                .bodyToMono(Map.class);
    }
}
