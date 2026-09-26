package com.ridelink.driver.dto;

import com.ridelink.driver.entity.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UpdateVehicleRequest {

    @NotBlank(message = "Vehicle make is required")
    private String make;

    @NotBlank(message = "Vehicle model is required")
    private String model;

    @NotNull(message = "Vehicle year is required")
    @Min(value = 2000, message = "Vehicle manufacturing year must be 2000 or later")
    @Max(value = 2030, message = "Vehicle manufacturing year is invalid")
    private Integer year;

    @NotBlank(message = "License plate number is required")
    @Pattern(regexp = "^[A-Z0-9- ]{4,15}$", message = "License plate must be 4-15 alphanumeric characters/hyphens")
    private String licensePlate;

    @NotBlank(message = "Vehicle color is required")
    private String color;

    @NotNull(message = "Vehicle type is required (SEDAN, SUV, VAN, TUK)")
    private VehicleType vehicleType;

    @NotNull(message = "Seating capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 15, message = "Capacity cannot exceed 15")
    private Integer capacity;

    public UpdateVehicleRequest() {
    }

    public UpdateVehicleRequest(String make, String model, Integer year, String licensePlate, String color, VehicleType vehicleType, Integer capacity) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.color = color;
        this.vehicleType = vehicleType;
        this.capacity = capacity;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
