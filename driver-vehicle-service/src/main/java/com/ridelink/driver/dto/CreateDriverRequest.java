package com.ridelink.driver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateDriverRequest {

    @NotBlank(message = "Driver license number is required")
    @Pattern(
            regexp = "^[A-Za-z0-9-]{5,20}$",
            message = "Driver license number must be 5-20 characters"
    )
    private String driverLicenseNumber;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{9,15}$",
            message = "Phone number must be a valid international or local format (9-15 digits)"
    )
    private String phoneNumber;

    @NotBlank(message = "Service area is required")
    private String serviceArea;

    @Valid
    @NotNull(message = "Vehicle details are required")
    private VehicleDto vehicle;

    @Valid
    @NotNull(message = "Initial simulated location is required")
    private LocationDto initialLocation;

    public CreateDriverRequest() {
    }

    public CreateDriverRequest(
            String driverLicenseNumber,
            String phoneNumber,
            String serviceArea,
            VehicleDto vehicle,
            LocationDto initialLocation
    ) {
        this.driverLicenseNumber = driverLicenseNumber;
        this.phoneNumber = phoneNumber;
        this.serviceArea = serviceArea;
        this.vehicle = vehicle;
        this.initialLocation = initialLocation;
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

    public VehicleDto getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleDto vehicle) {
        this.vehicle = vehicle;
    }

    public LocationDto getInitialLocation() {
        return initialLocation;
    }

    public void setInitialLocation(LocationDto initialLocation) {
        this.initialLocation = initialLocation;
    }
}