package com.ridelink.driver.dto;

import com.ridelink.driver.entity.AvailabilityStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(
        description = """
                Request used to change a driver's availability.

                AVAILABLE means the driver can receive a new ride.
                BUSY means the driver is engaged in a ride.
                OFFLINE means the driver is not accepting rides.
                """
)
public class UpdateAvailabilityRequest {

    @Schema(
            description = """
                    New driver availability.

                    AVAILABLE:
                    The driver can be selected for a ride.

                    BUSY:
                    The driver is currently assigned to a ride. Ride Management
                    Service normally sets this through the internal assignment flow.

                    OFFLINE:
                    The driver is not accepting new rides.

                    A SUSPENDED driver cannot become AVAILABLE.
                    A BUSY driver cannot manually move directly to OFFLINE.
                    """,
            example = "AVAILABLE",
            allowableValues = {
                    "AVAILABLE",
                    "BUSY",
                    "OFFLINE"
            }
    )
    @NotNull(
            message = "Availability status is required (AVAILABLE, BUSY, OFFLINE)"
    )
    private AvailabilityStatus status;

    public UpdateAvailabilityRequest() {
    }

    public UpdateAvailabilityRequest(
            AvailabilityStatus status) {
        this.status = status;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public void setStatus(
            AvailabilityStatus status) {
        this.status = status;
    }
}