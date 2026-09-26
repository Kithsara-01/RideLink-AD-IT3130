package com.ridelink.driver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI driverVehicleServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RideLink - Driver & Vehicle Service API")
                .description("Microservice managing driver operational profiles, vehicle details, availability states, simulated GPS tracking, and proximity driver discovery for RideLink.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("RideLink Development Team - Member 2")
                    .email("support@ridelink.local"))
                .license(new License()
                    .name("Academic Use Only")
                    .url("https://ridelink.local/license")));
    }
}
