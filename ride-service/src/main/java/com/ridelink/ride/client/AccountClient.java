package com.ridelink.ride.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccountClient {

    private final WebClient accountWebClient;

    public Mono<Map> getUserProfile(String userId) {
        return accountWebClient.get()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .bodyToMono(Map.class);
    }
}
