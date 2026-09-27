package com.ridelink.payment.dto;

import java.math.BigDecimal;

public record FareCalculationResult(

        BigDecimal baseFare,
        BigDecimal distanceCharge,
        BigDecimal durationCharge,
        BigDecimal totalFare,
        String currency,

        BigDecimal firstKmFare,
        BigDecimal additionalKmRate,
        BigDecimal perMinuteRate

) {
}