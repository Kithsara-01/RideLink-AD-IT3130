package com.ridelink.payment.dto;

import java.math.BigDecimal;

public record FareEstimateResponse(

        String pickup,
        String destination,
        Double distanceKm,
        Integer durationMinutes,

        BigDecimal baseFare,
        BigDecimal distanceCharge,
        BigDecimal durationCharge,
        BigDecimal estimatedFare,

        String currency

) {
}