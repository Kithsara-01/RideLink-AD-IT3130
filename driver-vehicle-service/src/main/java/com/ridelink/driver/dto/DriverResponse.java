package com.ridelink.driver.dto;

import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.Location;
import com.ridelink.driver.model.OperationalStatus;
import com.ridelink.driver.model.Vehicle;

import java.time.Instant;

public class DriverResponse {

    private String id;
    private String userId;
    private String driverLicenseNumber;
    private String phoneNumber;
    private OperationalStatus operationalStatus;
    private AvailabilityStatus availabilityStatus;
    private String serviceArea;
    private Vehicle vehicle;
    private Location currentLocation;
    private Double rating;
    private Integer totalRidesCompleted;
    private Instant createdAt;
    private Instant updatedAt;

    public DriverResponse() {
    }

    public static DriverResponse fromEntity(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setUserId(driver.getUserId());
        response.setDriverLicenseNumber(driver.getDriverLicenseNumber());
        response.setPhoneNumber(driver.getPhoneNumber());
        response.setOperationalStatus(driver.getOperationalStatus());
        response.setAvailabilityStatus(driver.getAvailabilityStatus());
        response.setServiceArea(driver.getServiceArea());
        response.setVehicle(driver.getVehicle());
        response.setCurrentLocation(driver.getCurrentLocation());
        response.setRating(driver.getRating());
        response.setTotalRidesCompleted(driver.getTotalRidesCompleted());
        response.setCreatedAt(driver.getCreatedAt());
        response.setUpdatedAt(driver.getUpdatedAt());
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

    public OperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(OperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalRidesCompleted() {
        return totalRidesCompleted;
    }

    public void setTotalRidesCompleted(Integer totalRidesCompleted) {
        this.totalRidesCompleted = totalRidesCompleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
