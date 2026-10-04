package com.ridelink.ride.dto;

import com.ridelink.ride.entity.RideStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateRideStatusRequest {

    @NotNull(message = "Target status is required")
    private RideStatus status;

    @Size(max = 300, message = "Cancellation reason cannot exceed 300 characters")
    private String cancellationReason;

    private Double actualDistanceKm;

    private Integer actualDurationMinutes;

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public void setActualDistanceKm(Double actualDistanceKm) {
        this.actualDistanceKm = actualDistanceKm;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }
}