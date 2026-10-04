package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request used to calculate and store the final fare for a completed ride")
public record FinalFareRequest(

        @Schema(
                description = "Actual distance travelled by the completed ride in kilometres",
                example = "8.5",
                minimum = "0.1"
        )
        @NotNull(message = "Actual distance is required")
        @DecimalMin(value = "0.1", message = "Actual distance must be at least 0.1 km")
        Double actualDistanceKm,

        @Schema(
                description = "Actual duration of the completed ride in minutes",
                example = "25",
                minimum = "1"
        )
        @NotNull(message = "Actual duration is required")
        @Min(value = 1, message = "Actual duration must be at least 1 minute")
        Integer actualDurationMinutes

) {
}