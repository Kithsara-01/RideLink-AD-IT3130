package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class UpdateAccountStatusRequest {

    @Schema(
            description = "Set to true to activate the account or false to deactivate it",
            example = "true"
    )
    @NotNull(message = "Active status is required")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}