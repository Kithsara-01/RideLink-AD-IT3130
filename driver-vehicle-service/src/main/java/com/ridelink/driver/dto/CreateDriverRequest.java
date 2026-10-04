package com.ridelink.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(
        description = """
                Request used to create a Driver Profile for the authenticated
                DRIVER account.

                The Account/User ID is obtained from the JWT and must not be
                included in this request.
                """
)
public class CreateDriverRequest {

    @Schema(
            description = """
                    Unique driving licence number.

                    It must contain 5 to 20 letters, numbers or hyphens.
                    The service converts the value to uppercase before saving it.
                    """,
            example = "DL-12345"
    )
    @NotBlank(message = "Driver license number is required")
    @Pattern(
            regexp = "^[A-Za-z0-9-]{5,20}$",
            message = "Driver license number must be 5-20 characters"
    )
    private String driverLicenseNumber;

    @Schema(
            description = """
                    Driver contact number.

                    It must contain 9 to 15 digits and may begin with +.
                    """,
            example = "+94771234567"
    )
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{9,15}$",
            message = "Phone number must be a valid international or local format (9-15 digits)"
    )
    private String phoneNumber;

    @Schema(
            description = """
                    Main area in which the driver provides rides.

                    Use the same area value when testing available-driver search.
                    """,
            example = "Colombo"
    )
    @NotBlank(message = "Service area is required")
    private String serviceArea;

    @Schema(
            description = """
                    Vehicle registered to this Driver Profile.

                    All vehicle fields are required.
                    """
    )
    @Valid
    @NotNull(message = "Vehicle details are required")
    private VehicleDto vehicle;

    @Schema(
            description = """
                    Initial simulated location of the driver.

                    These coordinates may later be updated using the location endpoint.
                    """
    )
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

    public void setDriverLicenseNumber(
            String driverLicenseNumber) {
        this.driverLicenseNumber = driverLicenseNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(
            String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(
            String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public VehicleDto getVehicle() {
        return vehicle;
    }

    public void setVehicle(
            VehicleDto vehicle) {
        this.vehicle = vehicle;
    }

    public LocationDto getInitialLocation() {
        return initialLocation;
    }

    public void setInitialLocation(
            LocationDto initialLocation) {
        this.initialLocation = initialLocation;
    }
}