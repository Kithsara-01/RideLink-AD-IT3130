package com.ridelink.ride.dto;

import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.entity.RideLocation;
import com.ridelink.ride.entity.RideStatus;
import com.ridelink.ride.entity.VehicleType;

import java.time.Instant;

public class RideResponse {
    private String id;
    private String passengerId;
    private String driverId;
    private RideLocation pickup;
    private RideLocation destination;
    private VehicleType vehicleType;
    private RideStatus status;
    private Double estimatedFare;
    private Double finalFare;
    private String cancellationReason;
    private Instant requestedAt;
    private Instant updatedAt;
    private Instant acceptedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    public static RideResponse fromEntity(Ride ride) {
        RideResponse response = new RideResponse();
        response.id = ride.getId();
        response.passengerId = ride.getPassengerId();
        response.driverId = ride.getDriverId();
        response.pickup = ride.getPickup();
        response.destination = ride.getDestination();
        response.vehicleType = ride.getVehicleType();
        response.status = ride.getStatus();
        response.estimatedFare = ride.getEstimatedFare();
        response.finalFare = ride.getFinalFare();
        response.cancellationReason = ride.getCancellationReason();
        response.requestedAt = ride.getRequestedAt();
        response.updatedAt = ride.getUpdatedAt();
        response.acceptedAt = ride.getAcceptedAt();
        response.startedAt = ride.getStartedAt();
        response.completedAt = ride.getCompletedAt();
        response.cancelledAt = ride.getCancelledAt();
        return response;
    }

    public String getId() { return id; }
    public String getPassengerId() { return passengerId; }
    public String getDriverId() { return driverId; }
    public RideLocation getPickup() { return pickup; }
    public RideLocation getDestination() { return destination; }
    public VehicleType getVehicleType() { return vehicleType; }
    public RideStatus getStatus() { return status; }
    public Double getEstimatedFare() { return estimatedFare; }
    public Double getFinalFare() { return finalFare; }
    public String getCancellationReason() { return cancellationReason; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
}
