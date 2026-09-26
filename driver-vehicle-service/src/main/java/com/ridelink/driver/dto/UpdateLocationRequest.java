package com.ridelink.driver.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateLocationRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private Double longitude;

    @Size(max = 200, message = "Address or place description cannot exceed 200 characters")
    private String addressOrPlace;

    @Size(max = 100, message = "Service area description cannot exceed 100 characters")
    private String serviceArea;

    public UpdateLocationRequest() {
    }

    public UpdateLocationRequest(Double latitude, Double longitude, String addressOrPlace, String serviceArea) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressOrPlace = addressOrPlace;
        this.serviceArea = serviceArea;
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

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }
}
