package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareCalculationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FareCalculationServiceTest {

    private final FareCalculationService fareCalculationService =
            new FareCalculationService();

    @Test
    void shouldCalculateFareUsingDocumentedPricingRule() {

        FareCalculationResult result =
                fareCalculationService.calculateFare(8.5, 25);

        assertEquals(
                new BigDecimal("110.00"),
                result.baseFare()
        );

        assertEquals(
                new BigDecimal("675.00"),
                result.distanceCharge()
        );

        assertEquals(
                new BigDecimal("125.00"),
                result.durationCharge()
        );

        assertEquals(
                new BigDecimal("910.00"),
                result.totalFare()
        );

        assertEquals(
                "LKR",
                result.currency()
        );
    }
}