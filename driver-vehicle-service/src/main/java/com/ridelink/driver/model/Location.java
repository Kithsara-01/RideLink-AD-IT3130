package com.ridelink.driver.model;

import java.time.Instant;

public class Location {

    private Double latitude;
    private Double longitude;
    private String addressOrPlace;
    private Instant lastUpdated;

    public Location() {
    }

    public Location(Double latitude, Double longitude, String addressOrPlace) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressOrPlace = addressOrPlace;
        this.lastUpdated = Instant.now();
    }

    public Location(Double latitude, Double longitude, String addressOrPlace, Instant lastUpdated) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressOrPlace = addressOrPlace;
        this.lastUpdated = lastUpdated;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddressOrPlace() {
        return addressOrPlace;
    }

    public void setAddressOrPlace(String addressOrPlace) {
        this.addressOrPlace = addressOrPlace;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
