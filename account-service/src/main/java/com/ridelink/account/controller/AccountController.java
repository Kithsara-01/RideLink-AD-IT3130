package com.ridelink.account.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UpdateAccountRoleRequest;
import com.ridelink.account.dto.UpdateAccountStatusRequest;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.entity.User;
import com.ridelink.account.service.JwtService;
import com.ridelink.account.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@Tag(
        name = "Account Management",
        description = "APIs for account registration, authentication, profile management, and administrator account management"
)
public class AccountController {

    private final UserService userService;
    private final JwtService jwtService;

    public AccountController(
            UserService userService,
            JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Operation(
            summary = "Register a new account",
            description = "Creates a new RIDER or DRIVER account."
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                createAccountResponse(
                        "Account registered successfully",
                        user));
    }

    @Operation(
            summary = "Login to an account",
            description = "Authenticates an account and returns a JWT access token."
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.authenticate(
                request.getEmail(),
                request.getPassword());

        String token = jwtService.generateToken(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Login successful");
        response.put("accessToken", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtService.getExpirationSeconds());
        response.put("id", user.getId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("telephoneNumber", user.getTelephoneNumber());
        response.put("role", user.getRole());
        response.put("active", user.isActive());

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .header("Pragma", "no-cache")
                .body(response);
    }

    @Operation(
            summary = "Get current account profile",
            description = "Returns the profile of the currently authenticated RIDER, DRIVER, or ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAccount(
            @AuthenticationPrincipal Jwt jwt) {

        User user = userService.findActiveById(jwt.getSubject());

        return ResponseEntity.ok(
                createAccountResponse(
                        "Account retrieved successfully",
                        user));
    }

    @Operation(
            summary = "Update current account profile",
            description = "Updates the profile information of the currently authenticated account.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = userService.updateProfile(
                jwt.getSubject(),
                request.getFullName(),
                request.getTelephoneNumber());

        return ResponseEntity.ok(
                createAccountResponse(
                        "Profile updated successfully",
                        user));
    }

    @Operation(
            summary = "Find an account by email",
            description = "Allows an ADMIN to retrieve an account using its email address.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getByEmail(
            @PathVariable String email) {

        User user = userService.findByEmail(email);

        return ResponseEntity.ok(
                createAccountResponse(
                        "Account retrieved successfully",
                        user));
    }

    @Operation(
            summary = "Update account status",
            description = "Allows an ADMIN to activate or deactivate a RIDER or DRIVER account.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/{accountId}/status")
    public ResponseEntity<?> updateAccountStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountStatusRequest request) {

        User user = userService.updateAccountStatus(
                jwt.getSubject(),
                accountId,
                request.getActive());

        return ResponseEntity.ok(
                createAccountResponse(
                        "Account status updated successfully",
                        user));
    }

    @Operation(
            summary = "Update account role",
            description = "Allows an ADMIN to change an account role between RIDER and DRIVER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/{accountId}/role")
    public ResponseEntity<?> updateAccountRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountRoleRequest request) {

        User user = userService.updateAccountRole(
                jwt.getSubject(),
                accountId,
                request.getRole());

        return ResponseEntity.ok(
                createAccountResponse(
                        "Account role updated successfully",
                        user));
    }

    private Map<String, Object> createAccountResponse(
            String message,
            User user) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", message);
        response.put("id", user.getId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("telephoneNumber", user.getTelephoneNumber());
        response.put("role", user.getRole());
        response.put("active", user.isActive());

        return response;
    }
}