package com.ridelink.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FinalFareResponse(

        String rideId,
        Double actualDistanceKm,
        Integer actualDurationMinutes,

        BigDecimal baseFare,
        BigDecimal distanceCharge,
        BigDecimal durationCharge,
        BigDecimal totalFare,

        String currency,

        BigDecimal firstKmFare,
        BigDecimal additionalKmRate,
        BigDecimal perMinuteRate,

        Instant finalizedAt

) {
}