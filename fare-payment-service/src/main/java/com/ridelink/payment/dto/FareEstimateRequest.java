package com.ridelink.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FareEstimateRequest(

        @NotBlank(message = "Pickup location is required")
        String pickup,

        @NotBlank(message = "Destination location is required")
        String destination,

        @NotNull(message = "Distance is required")
        @DecimalMin(value = "0.1", message = "Distance must be at least 0.1 km")
        Double distanceKm,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        Integer durationMinutes

) {
}