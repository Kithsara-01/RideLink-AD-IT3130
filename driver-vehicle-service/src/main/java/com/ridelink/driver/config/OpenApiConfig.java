package com.ridelink.driver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI driverVehicleServiceOpenAPI() {

        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                )
                .info(
                        new Info()
                                .title("RideLink - Driver & Vehicle Service API")
                                .description("""
                                        Manages driver profiles, vehicles, availability and location.

                                        Demo steps:
                                        1. Login as DRIVER using Account Service.
                                        2. Click Authorize and enter the JWT.
                                        3. Create a Driver Profile.
                                        4. Copy its id for later Driver requests.

                                        Account/User ID and Driver Profile ID are different.
                                        """)
                                .version("1.0.0")
                );
    }
}