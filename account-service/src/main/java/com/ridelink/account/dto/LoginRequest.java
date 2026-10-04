package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @Schema(
            description = "Email address of the registered RideLink account",
            example = "nimal.perera@example.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @Schema(
            description = "Password of the registered account",
            example = "RideLink123"
    )
    @NotBlank(message = "Password is required")
    private String password;

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
}