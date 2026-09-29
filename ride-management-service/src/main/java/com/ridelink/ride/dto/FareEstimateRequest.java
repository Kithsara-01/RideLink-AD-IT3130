package com.ridelink.ride.dto;

public record FareEstimateRequest(
        String pickup,
        String destination,
        Double distanceKm,
        Integer durationMinutes
) {
}