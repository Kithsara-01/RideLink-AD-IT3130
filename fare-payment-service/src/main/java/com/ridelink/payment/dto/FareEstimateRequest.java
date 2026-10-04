package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request used to calculate a fare estimate")
public record FareEstimateRequest(

        @Schema(
                description = "Pickup location for the ride",
                example = "SLIIT Malabe"
        )
        @NotBlank(message = "Pickup location is required")
        String pickup,

        @Schema(
                description = "Destination location for the ride",
                example = "Kaduwela"
        )
        @NotBlank(message = "Destination location is required")
        String destination,

        @Schema(
                description = "Estimated ride distance in kilometres",
                example = "8.5",
                minimum = "0.1"
        )
        @NotNull(message = "Distance is required")
        @DecimalMin(value = "0.1", message = "Distance must be at least 0.1 km")
        Double distanceKm,

        @Schema(
                description = "Estimated ride duration in minutes",
                example = "25",
                minimum = "1"
        )
        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        Integer durationMinutes

) {
}