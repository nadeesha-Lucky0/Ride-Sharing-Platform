package com.ridelink.ride.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.account.url:http://localhost:8081}")
    private String accountServiceUrl;

    @Value("${services.driver.url:http://localhost:8082}")
    private String driverServiceUrl;

    @Value("${services.payment.url:http://localhost:8084}")
    private String paymentServiceUrl;

    @Bean
    public WebClient accountWebClient() {
        return WebClient.builder().baseUrl(accountServiceUrl).build();
    }

    @Bean
    public WebClient driverWebClient() {
        return WebClient.builder().baseUrl(driverServiceUrl).build();
    }

    @Bean
    public WebClient paymentWebClient() {
        return WebClient.builder().baseUrl(paymentServiceUrl).build();
    }
}
