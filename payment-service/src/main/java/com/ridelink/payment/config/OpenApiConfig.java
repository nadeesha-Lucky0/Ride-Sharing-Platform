package com.ridelink.payment.config;

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

    @Value("${server.port:8084}")
    private String serverPort;

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink Fare & Payment Service API")
                        .description("Enterprise-grade microservice responsible for real-time dynamic fare estimation, final ride fare calculation, simulated multi-method payment processing, payment lifecycle management (status & refunds), and digital receipt generation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RideLink Engineering Team")
                                .email("dev@ridelink.com")
                                .url("https://ridelink.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }
}
