package com.ridelink.ride.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rides")
public class Ride {

    @Id
    private String id;

    @Indexed
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

    public Ride() {
    }

    public Ride(String passengerId, RideLocation pickup, RideLocation destination, VehicleType vehicleType,
                Double estimatedFare) {
        this.passengerId = passengerId;
        this.pickup = pickup;
        this.destination = destination;
        this.vehicleType = vehicleType;
        this.status = RideStatus.REQUESTED;
        this.estimatedFare = estimatedFare;
        this.requestedAt = Instant.now();
        this.updatedAt = this.requestedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public RideLocation getPickup() { return pickup; }
    public void setPickup(RideLocation pickup) { this.pickup = pickup; }
    public RideLocation getDestination() { return destination; }
    public void setDestination(RideLocation destination) { this.destination = destination; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }
    public Double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(Double estimatedFare) { this.estimatedFare = estimatedFare; }
    public Double getFinalFare() { return finalFare; }
    public void setFinalFare(Double finalFare) { this.finalFare = finalFare; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}
