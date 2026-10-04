package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareCalculationResult;
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

        public FareCalculationResult calculateFare(
                        double distanceKm,
                        int durationMinutes) {

                BigDecimal distance = BigDecimal.valueOf(distanceKm);
                BigDecimal duration = BigDecimal.valueOf(durationMinutes);

                BigDecimal additionalDistance = distance
                                .subtract(BigDecimal.ONE)
                                .max(BigDecimal.ZERO);

                BigDecimal distanceCharge = additionalDistance
                                .multiply(ADDITIONAL_KM_RATE)
                                .setScale(2, RoundingMode.HALF_UP);

                BigDecimal durationCharge = duration
                                .multiply(PER_MINUTE_RATE)
                                .setScale(2, RoundingMode.HALF_UP);

                BigDecimal totalFare = FIRST_KM_FARE
                                .add(distanceCharge)
                                .add(durationCharge)
                                .setScale(2, RoundingMode.HALF_UP);

                return new FareCalculationResult(
                                FIRST_KM_FARE,
                                distanceCharge,
                                durationCharge,
                                totalFare,
                                "LKR",
                                FIRST_KM_FARE,
                                ADDITIONAL_KM_RATE,
                                PER_MINUTE_RATE);
        }

        public FareEstimateResponse calculateEstimate(FareEstimateRequest request) {

                FareCalculationResult result = calculateFare(
                                request.distanceKm(),
                                request.durationMinutes());

                return new FareEstimateResponse(
                                request.pickup(),
                                request.destination(),
                                request.distanceKm(),
                                request.durationMinutes(),
                                result.baseFare(),
                                result.distanceCharge(),
                                result.durationCharge(),
                                result.totalFare(),
                                result.currency());
        }
}