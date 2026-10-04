package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Schema(
            description = "Updated full name of the logged-in user",
            example = "Nimal Perera"
    )
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Schema(
            description = "Optional telephone number. When provided, it must contain 7 to 15 digits with an optional leading +",
            example = "+94771234567"
    )
    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "Telephone number must contain 7 to 15 digits with an optional leading +"
    )
    private String telephoneNumber;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber == null
                ? null
                : telephoneNumber.trim();
    }
}