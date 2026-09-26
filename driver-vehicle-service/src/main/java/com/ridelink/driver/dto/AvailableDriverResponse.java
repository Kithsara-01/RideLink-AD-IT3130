package com.ridelink.driver.dto;

import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.Location;
import com.ridelink.driver.model.Vehicle;

public class AvailableDriverResponse {

    private String id;
    private String userId;
    private String driverLicenseNumber;
    private String phoneNumber;
    private String serviceArea;
    private Vehicle vehicle;
    private Location currentLocation;
    private Double distanceKm;
    private Double rating;

    public AvailableDriverResponse() {
    }

    public static AvailableDriverResponse fromEntity(Driver driver, Double distanceKm) {
        AvailableDriverResponse response = new AvailableDriverResponse();
        response.setId(driver.getId());
        response.setUserId(driver.getUserId());
        response.setDriverLicenseNumber(driver.getDriverLicenseNumber());
        response.setPhoneNumber(driver.getPhoneNumber());
        response.setServiceArea(driver.getServiceArea());
        response.setVehicle(driver.getVehicle());
        response.setCurrentLocation(driver.getCurrentLocation());
        response.setDistanceKm(distanceKm != null ? Math.round(distanceKm * 100.0) / 100.0 : null);
        response.setRating(driver.getRating());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public void setDriverLicenseNumber(String driverLicenseNumber) {
        this.driverLicenseNumber = driverLicenseNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
