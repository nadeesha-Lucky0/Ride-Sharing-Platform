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
public class AccountClient {

    private final WebClient accountWebClient;

    public Mono<Map<String, Object>> getUserProfile(String userId) {
        return accountWebClient.get()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .map(res -> (Map<String, Object>) res)
                .doOnError(e -> log.warn("Failed to fetch user profile for {}: {}", userId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}
