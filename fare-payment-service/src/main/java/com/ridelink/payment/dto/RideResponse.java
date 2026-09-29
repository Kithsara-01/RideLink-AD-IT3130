package com.ridelink.payment.dto;

public record RideResponse(
        String id,
        String passengerId,
        String status
) {
}