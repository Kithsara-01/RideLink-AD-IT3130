package com.ridelink.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI farePaymentServiceOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("RideLink Fare & Payment Service API")
                        .version("1.0")
                        .description("""
                                REST API documentation for the RideLink Fare & Payment Service.

                                Main capabilities:
                                - Fare estimation
                                - Final fare calculation and storage
                                - Simulated payment recording and status retrieval
                                - Failed payment retry
                                - Receipt generation and retrieval

                                Fare calculation rule:
                                - First 1 km: LKR 110.00
                                - Each additional km: LKR 90.00
                                - Duration charge: LKR 5.00 per minute
                                - Final values are rounded to 2 decimal places using HALF_UP.

                                Payment processing is simulated for academic purposes.
                                This service does not process real money and does not collect
                                card numbers, CVV values, bank credentials, or other real
                                payment information.
                                """));
    }
}