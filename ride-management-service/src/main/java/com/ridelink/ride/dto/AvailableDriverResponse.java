package com.ridelink.ride.dto;

public class AvailableDriverResponse {
    private String id;
    private Double distanceKm;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
