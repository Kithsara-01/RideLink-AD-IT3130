package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class UpdateAccountRoleRequest {

    @Schema(
            description = "New role for the account. ADMIN can change an account between RIDER and DRIVER",
            example = "DRIVER",
            allowableValues = {"RIDER", "DRIVER"}
    )
    @NotBlank(message = "Role is required")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}