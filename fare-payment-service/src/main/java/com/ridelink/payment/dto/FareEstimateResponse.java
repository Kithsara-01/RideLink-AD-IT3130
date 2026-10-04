package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Calculated fare estimate for a requested ride")
public record FareEstimateResponse(

        @Schema(description = "Pickup location", example = "SLIIT Malabe")
        String pickup,

        @Schema(description = "Destination location", example = "Kaduwela")
        String destination,

        @Schema(description = "Estimated distance in kilometres", example = "8.5")
        Double distanceKm,

        @Schema(description = "Estimated duration in minutes", example = "25")
        Integer durationMinutes,

        @Schema(description = "Base fare for the first kilometre", example = "110.00")
        BigDecimal baseFare,

        @Schema(description = "Charge for distance after the first kilometre", example = "675.00")
        BigDecimal distanceCharge,

        @Schema(description = "Charge based on ride duration", example = "125.00")
        BigDecimal durationCharge,

        @Schema(description = "Total estimated fare", example = "910.00")
        BigDecimal estimatedFare,

        @Schema(description = "Fare currency", example = "LKR")
        String currency

) {
}