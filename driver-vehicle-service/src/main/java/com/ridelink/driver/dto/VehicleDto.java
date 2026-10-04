package com.ridelink.driver.dto;

import com.ridelink.driver.entity.VehicleType;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(
        description = "Vehicle information included when creating a Driver Profile"
)
public class VehicleDto {

    @Schema(
            description = "Vehicle manufacturer",
            example = "Toyota"
    )
    @NotBlank(message = "Vehicle make is required")
    private String make;

    @Schema(
            description = "Vehicle model",
            example = "Aqua"
    )
    @NotBlank(message = "Vehicle model is required")
    private String model;

    @Schema(
            description = "Vehicle manufacturing year, from 2000 to 2030",
            example = "2022",
            minimum = "2000",
            maximum = "2030"
    )
    @NotNull(message = "Vehicle year is required")
    @Min(
            value = 2000,
            message = "Vehicle manufacturing year must be 2000 or later"
    )
    @Max(
            value = 2030,
            message = "Vehicle manufacturing year is invalid"
    )
    private Integer year;

    @Schema(
            description = """
                    Unique vehicle licence plate.

                    Use 4 to 15 uppercase letters, numbers, spaces or hyphens.
                    """,
            example = "WP-CAB-1234"
    )
    @NotBlank(message = "License plate number is required")
    @Pattern(
            regexp = "^[A-Z0-9- ]{4,15}$",
            message = "License plate must be 4-15 alphanumeric characters/hyphens"
    )
    private String licensePlate;

    @Schema(
            description = "Vehicle colour",
            example = "White"
    )
    @NotBlank(message = "Vehicle color is required")
    private String color;

    @Schema(
            description = "Vehicle category used for ride matching",
            example = "SEDAN",
            allowableValues = {
                    "SEDAN",
                    "SUV",
                    "VAN",
                    "TUK"
            }
    )
    @NotNull(
            message = "Vehicle type is required (SEDAN, SUV, VAN, TUK)"
    )
    private VehicleType vehicleType;

    @Schema(
            description = "Passenger seating capacity, from 1 to 15",
            example = "4",
            minimum = "1",
            maximum = "15"
    )
    @NotNull(message = "Seating capacity is required")
    @Min(
            value = 1,
            message = "Capacity must be at least 1"
    )
    @Max(
            value = 15,
            message = "Capacity cannot exceed 15"
    )
    private Integer capacity;

    public VehicleDto() {
    }

    public VehicleDto(
            String make,
            String model,
            Integer year,
            String licensePlate,
            String color,
            VehicleType vehicleType,
            Integer capacity
    ) {
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

    public void setMake(
            String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(
            String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(
            Integer year) {
        this.year = year;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(
            String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(
            String color) {
        this.color = color;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(
            VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(
            Integer capacity) {
        this.capacity = capacity;
    }
}