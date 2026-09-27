package com.ridelink.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FinalFareRequest(

        @NotNull(message = "Actual distance is required")
        @DecimalMin(value = "0.1", message = "Actual distance must be at least 0.1 km")
        Double actualDistanceKm,

        @NotNull(message = "Actual duration is required")
        @Min(value = 1, message = "Actual duration must be at least 1 minute")
        Integer actualDurationMinutes

) {
}