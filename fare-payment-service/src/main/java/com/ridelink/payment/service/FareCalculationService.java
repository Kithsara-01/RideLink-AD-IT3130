package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculationService {

    private static final BigDecimal FIRST_KM_FARE = new BigDecimal("110.00");
    private static final BigDecimal ADDITIONAL_KM_RATE = new BigDecimal("90.00");
    private static final BigDecimal PER_MINUTE_RATE = new BigDecimal("5.00");

    public FareEstimateResponse calculateEstimate(FareEstimateRequest request) {

        BigDecimal distance = BigDecimal.valueOf(request.distanceKm());
        BigDecimal duration = BigDecimal.valueOf(request.durationMinutes());

        BigDecimal additionalDistance = distance
                .subtract(BigDecimal.ONE)
                .max(BigDecimal.ZERO);

        BigDecimal distanceCharge = additionalDistance
                .multiply(ADDITIONAL_KM_RATE);

        BigDecimal durationCharge = duration
                .multiply(PER_MINUTE_RATE);

        BigDecimal estimatedFare = FIRST_KM_FARE
                .add(distanceCharge)
                .add(durationCharge)
                .setScale(2, RoundingMode.HALF_UP);

        return new FareEstimateResponse(
                request.pickup(),
                request.destination(),
                request.distanceKm(),
                request.durationMinutes(),
                FIRST_KM_FARE,
                distanceCharge.setScale(2, RoundingMode.HALF_UP),
                durationCharge.setScale(2, RoundingMode.HALF_UP),
                estimatedFare,
                "LKR"
        );
    }
}