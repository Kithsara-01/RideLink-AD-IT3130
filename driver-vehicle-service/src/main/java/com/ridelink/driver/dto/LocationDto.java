package com.ridelink.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        description = """
                Simulated driver location used during Driver Profile creation.

                The project does not connect to a real GPS device.
                """
)
public class LocationDto {

    @Schema(
            description = "Latitude between -90 and 90",
            example = "6.9271",
            minimum = "-90.0",
            maximum = "90.0"
    )
    @NotNull(message = "Latitude is required")
    @DecimalMin(
            value = "-90.0",
            message = "Latitude must be >= -90.0"
    )
    @DecimalMax(
            value = "90.0",
            message = "Latitude must be <= 90.0"
    )
    private Double latitude;

    @Schema(
            description = "Longitude between -180 and 180",
            example = "79.8612",
            minimum = "-180.0",
            maximum = "180.0"
    )
    @NotNull(message = "Longitude is required")
    @DecimalMin(
            value = "-180.0",
            message = "Longitude must be >= -180.0"
    )
    @DecimalMax(
            value = "180.0",
            message = "Longitude must be <= 180.0"
    )
    private Double longitude;

    @Schema(
            description = """
                    Optional human-readable place or address.

                    Maximum length is 200 characters.
                    """,
            example = "Colombo Fort"
    )
    @Size(
            max = 200,
            message = "Address or place description cannot exceed 200 characters"
    )
    private String addressOrPlace;

    public LocationDto() {
    }

    public LocationDto(
            Double latitude,
            Double longitude,
            String addressOrPlace) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressOrPlace = addressOrPlace;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(
            Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(
            Double longitude) {
        this.longitude = longitude;
    }

    public String getAddressOrPlace() {
        return addressOrPlace;
    }

    public void setAddressOrPlace(
            String addressOrPlace) {
        this.addressOrPlace = addressOrPlace;
    }
}