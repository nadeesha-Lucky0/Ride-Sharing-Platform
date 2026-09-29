package com.ridelink.driver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI driverServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Driver & Vehicle Service API")
                        .description("Microservice for managing driver operational profiles, vehicle fleet registries, availability status toggles, geolocation tracking, and nearby eligible driver queries.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RideLink Driver Engineering Team")
                                .email("drivers-dev@ridelink.com")
                                .url("https://ridelink.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server"),
                        new Server().url("http://driver-service:8082").description("Docker Compose Service Network")
                ));
    }
}
