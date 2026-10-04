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
        description = """
                Manages RideLink user accounts, authentication and account access.

                Main features:
                - Registration: Create a new RIDER or DRIVER account.
                - Login: Sign in with email and password and receive a JWT access token.
                - Profile: View and update the currently logged-in user's profile.
                - Role Management: ADMIN can change an existing account between supported RIDER and DRIVER roles.
                - Account Status: ADMIN can activate or deactivate an existing RIDER or DRIVER account.
                - Account Lookup: ADMIN can find an account using its email address.

                Demo flow:
                Register -> Login -> Copy accessToken -> Authorize -> Use protected APIs

                Important:
                - Registration and Login do not require a JWT.
                - /me identifies the logged-in account automatically from the JWT.
                - Role, status and email lookup operations require ADMIN access.
                - Account/User ID is different from a Driver Profile ID.
                """
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
            summary = "Register a new RIDER or DRIVER account",
            description = """
                    Creates a new account.

                    Authentication:
                    - No JWT is required.

                    Request:
                    - fullName: user's name
                    - email: must be a valid email address
                    - password: minimum 8 characters
                    - role: RIDER or DRIVER
                    - telephoneNumber: optional; when provided, it must contain 7 to 15 digits with an optional leading +

                    After registration:
                    - Save the returned id. This is the Account/User ID.
                    - Login using the registered email and password to obtain a JWT.

                    Expected success:
                    - HTTP 201 Created
                    - Response includes id, fullName, email, telephoneNumber, role and active.
                    """
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
            summary = "Login and get a JWT access token",
            description = """
                    Authenticates an existing account using email and password.

                    Authentication:
                    - No JWT is required.

                    Expected success:
                    - HTTP 200 OK
                    - Response includes accessToken, tokenType, expiresIn and account details.

                    Swagger demo:
                    1. Execute this login request.
                    2. Copy the accessToken from the response.
                    3. Click Authorize at the top of Swagger UI.
                    4. Enter the JWT in the bearerAuth authorization box.
                    5. You can then call protected endpoints allowed for that account's role.

                    The Account/User ID returned here is also the JWT subject used to identify
                    the authenticated account in protected requests.
                    """
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
            summary = "Get my account profile",
            description = """
                    Returns the profile of the currently authenticated account.

                    Required role:
                    - RIDER, DRIVER or ADMIN

                    Prerequisite:
                    - Login first and authorize Swagger using the returned JWT.

                    No Account/User ID is entered in the URL.
                    The service identifies the account from the JWT subject.

                    Expected success:
                    - HTTP 200 OK
                    - Response includes the current account's profile information.
                    """,
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
            summary = "Update my account profile",
            description = """
                    Updates the full name and telephone number of the currently authenticated account.

                    Required role:
                    - RIDER, DRIVER or ADMIN

                    Prerequisite:
                    - Login first and authorize Swagger using the returned JWT.

                    The account is identified from the JWT subject. Do not provide an Account/User ID.

                    Expected success:
                    - HTTP 200 OK
                    - Response contains the updated profile.
                    """,
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
            description = """
                    Retrieves an account using its email address.

                    Required role:
                    - ADMIN only

                    Prerequisite:
                    - Login as ADMIN and authorize Swagger using the ADMIN JWT.

                    Path value:
                    - Replace {email} with the email address of the account you want to retrieve.

                    Expected success:
                    - HTTP 200 OK
                    - Response contains the matching account details.

                    Common authorization errors:
                    - 401 if the JWT is missing or invalid.
                    - 403 if the authenticated account is not ADMIN.
                    """,
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
            summary = "Activate or deactivate an account",
            description = """
                    Changes the active status of a RIDER or DRIVER account.

                    Required role:
                    - ADMIN only

                    Prerequisite:
                    - Login as ADMIN and authorize Swagger using the ADMIN JWT.

                    ID guidance:
                    - {accountId} is an Account/User ID.
                    - Replace it with the id returned by account registration, login or account lookup.
                    - It is NOT a Driver Profile ID.

                    Request:
                    - active = true to activate the account.
                    - active = false to deactivate the account.

                    Expected success:
                    - HTTP 200 OK
                    - Response contains the updated account.

                    Common authorization errors:
                    - 401 if the JWT is missing or invalid.
                    - 403 if the authenticated account is not ADMIN.
                    """,
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
            summary = "Change an account role",
            description = """
                    Changes an account role between RIDER and DRIVER.

                    Required role:
                    - ADMIN only

                    Prerequisite:
                    - Login as ADMIN and authorize Swagger using the ADMIN JWT.

                    ID guidance:
                    - {accountId} is an Account/User ID.
                    - Replace it with the id returned by account registration, login or account lookup.
                    - It is NOT a Driver Profile ID.

                    Supported target roles:
                    - RIDER
                    - DRIVER

                    Expected success:
                    - HTTP 200 OK
                    - Response contains the account with its updated role.

                    Common authorization errors:
                    - 401 if the JWT is missing or invalid.
                    - 403 if the authenticated account is not ADMIN.
                    """,
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