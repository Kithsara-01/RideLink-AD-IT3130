package com.ridelink.ride.dto;

public record FinalFareRequest(
        Double actualDistanceKm,
        Integer actualDurationMinutes
) {
}