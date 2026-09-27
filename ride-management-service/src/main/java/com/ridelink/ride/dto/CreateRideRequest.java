package com.ridelink.ride.dto;

import com.ridelink.ride.entity.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateRideRequest {

    @NotBlank(message = "Passenger ID is required")
    private String passengerId;

    @NotNull(message = "Pickup location is required")
    @Valid
    private RideLocationRequest pickup;

    @NotNull(message = "Destination is required")
    @Valid
    private RideLocationRequest destination;

    private VehicleType vehicleType;

    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
    public RideLocationRequest getPickup() { return pickup; }
    public void setPickup(RideLocationRequest pickup) { this.pickup = pickup; }
    public RideLocationRequest getDestination() { return destination; }
    public void setDestination(RideLocationRequest destination) { this.destination = destination; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
}
