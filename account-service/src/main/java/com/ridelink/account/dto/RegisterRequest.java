package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @Schema(
            description = "Full name of the new user",
            example = "Nimal Perera"
    )
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Schema(
            description = "Valid email address used to register and login",
            example = "nimal.perera@example.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @Schema(
            description = "Password with at least 8 characters",
            example = "RideLink123"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    @Schema(
            description = "Account role for registration",
            example = "RIDER",
            allowableValues = {"RIDER", "DRIVER"}
    )
    @NotBlank(message = "Role is required")
    private String role;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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