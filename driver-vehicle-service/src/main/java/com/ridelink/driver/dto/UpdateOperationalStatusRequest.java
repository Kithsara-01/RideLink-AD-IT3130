package com.ridelink.driver.dto;

import com.ridelink.driver.entity.OperationalStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(
        description = """
                Request used to change the operational status of a Driver Profile.

                For the viva, use an ADMIN JWT when demonstrating operational
                status management.
                """
)
public class UpdateOperationalStatusRequest {

    @Schema(
            description = """
                    New operational status.

                    ACTIVE:
                    The driver is operational and may become AVAILABLE.

                    SUSPENDED:
                    The driver cannot be assigned to rides. Setting this value also
                    changes availabilityStatus to OFFLINE.

                    PENDING_VERIFICATION:
                    The driver is awaiting operational verification.
                    """,
            example = "ACTIVE",
            allowableValues = {
                    "ACTIVE",
                    "SUSPENDED",
                    "PENDING_VERIFICATION"
            }
    )
    @NotNull(
            message = "Operational status is required (ACTIVE, SUSPENDED, PENDING_VERIFICATION)"
    )
    private OperationalStatus status;

    public UpdateOperationalStatusRequest() {
    }

    public UpdateOperationalStatusRequest(
            OperationalStatus status) {
        this.status = status;
    }

    public OperationalStatus getStatus() {
        return status;
    }

    public void setStatus(
            OperationalStatus status) {
        this.status = status;
    }
}