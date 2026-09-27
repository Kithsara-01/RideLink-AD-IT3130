package com.ridelink.ride.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rideManagementServiceOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("RideLink - Ride Management Service API")
            .description("Ride request creation, driver assignment, lifecycle state management and simulated fare snapshots.")
            .version("1.0.0")
            .contact(new Contact().name("RideLink Development Team - Member 3").email("support@ridelink.local"))
            .license(new License().name("Academic Use Only").url("https://ridelink.local/license")));
    }
}
