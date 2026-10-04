package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Permanently stored final fare for a completed ride")
public record FinalFareResponse(

        @Schema(description = "Unique ride identifier", example = "RIDE001")
        String rideId,

        @Schema(description = "Actual distance travelled in kilometres", example = "8.5")
        Double actualDistanceKm,

        @Schema(description = "Actual ride duration in minutes", example = "25")
        Integer actualDurationMinutes,

        @Schema(description = "Base fare for the first kilometre", example = "110.00")
        BigDecimal baseFare,

        @Schema(description = "Charge for distance after the first kilometre", example = "675.00")
        BigDecimal distanceCharge,

        @Schema(description = "Charge based on actual ride duration", example = "125.00")
        BigDecimal durationCharge,

        @Schema(description = "Final amount payable for the ride", example = "910.00")
        BigDecimal totalFare,

        @Schema(description = "Fare currency", example = "LKR")
        String currency,

        @Schema(description = "Fare charged for the first kilometre", example = "110.00")
        BigDecimal firstKmFare,

        @Schema(description = "Rate for each kilometre after the first kilometre", example = "90.00")
        BigDecimal additionalKmRate,

        @Schema(description = "Rate charged per minute", example = "5.00")
        BigDecimal perMinuteRate,

        @Schema(
                description = "Time when the final fare was calculated and stored",
                example = "2026-09-28T01:30:00Z"
        )
        Instant finalizedAt

) {
}