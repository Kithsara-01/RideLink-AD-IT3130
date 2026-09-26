package com.ridelink.driver.dto;

import com.ridelink.driver.entity.OperationalStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOperationalStatusRequest {

    @NotNull(message = "Operational status is required (ACTIVE, SUSPENDED, PENDING_VERIFICATION)")
    private OperationalStatus status;

    public UpdateOperationalStatusRequest() {
    }

    public UpdateOperationalStatusRequest(OperationalStatus status) {
        this.status = status;
    }

    public OperationalStatus getStatus() {
        return status;
    }

    public void setStatus(OperationalStatus status) {
        this.status = status;
    }
}
