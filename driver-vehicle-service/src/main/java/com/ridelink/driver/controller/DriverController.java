package com.ridelink.driver.controller;

import com.ridelink.driver.dto.AvailableDriverResponse;
import com.ridelink.driver.dto.CreateDriverRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.dto.UpdateAvailabilityRequest;
import com.ridelink.driver.dto.UpdateLocationRequest;
import com.ridelink.driver.dto.UpdateOperationalStatusRequest;
import com.ridelink.driver.dto.UpdateVehicleRequest;
import com.ridelink.driver.entity.VehicleType;
import com.ridelink.driver.service.DriverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(
        name = "Driver & Vehicle Management",
        description = "APIs for driver profiles, vehicles, availability and location."
)
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a Driver Profile",
            description = """
                    Role: DRIVER

                    Before using:
                    1. Register or login as DRIVER through Account Service.
                    2. Copy the accessToken.
                    3. Click Authorize and enter the token.

                    Do not enter an Account/User ID in the request.
                    The Account/User ID is obtained automatically from the JWT.

                    After success, copy the response field named `id`.
                    This is the Driver Profile ID used by the other Driver endpoints.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Driver Profile created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "An active DRIVER account is required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Driver Profile, licence or vehicle plate already exists"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Account Service is unavailable"
            )
    })
    public ResponseEntity<DriverResponse> registerDriver(
            @Valid @RequestBody CreateDriverRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        DriverResponse response =
                driverService.registerDriver(
                        request,
                        jwt.getTokenValue()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get driver by Driver Profile ID",
            description = """
                    Role: RIDER, DRIVER or ADMIN

                    Paste the Driver Profile ID into the id field.

                    Get this value from:
                    - POST /api/drivers response field: id

                    Database reference:
                    - Database: ridelink_driver_vehicle_service
                    - Collection: drivers
                    - Field: _id
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Driver Profile returned"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Role is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            )
    })
    public ResponseEntity<DriverResponse> getDriverById(
            @Parameter(
                    description = """
                            Paste the Driver Profile ID here.
                            Source: POST /api/drivers response field `id`.
                            MongoDB: ridelink_driver_vehicle_service > drivers > _id.
                            """,
                    required = true
            )
            @PathVariable String id) {

        return ResponseEntity.ok(
                driverService.getDriverById(id)
        );
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get driver by Account/User ID",
            description = """
                    Role: RIDER, DRIVER or ADMIN

                    Paste an Account/User ID into the userId field.

                    Get this value from:
                    - Account Service registration response field: id
                    - Account Service login response field: id
                    - GET /api/accounts/me response field: id

                    Database reference:
                    - Database: ridelink_account_service
                    - Collection: users
                    - Field: _id
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Driver Profile returned"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Role is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            )
    })
    public ResponseEntity<DriverResponse> getDriverByUserId(
            @Parameter(
                    description = """
                            Paste the Account/User ID here.
                            Source: Account Service register, login or /me response field `id`.
                            MongoDB: ridelink_account_service > users > _id.
                            """,
                    required = true
            )
            @PathVariable String userId) {

        return ResponseEntity.ok(
                driverService.getDriverByUserId(userId)
        );
    }

    @PutMapping("/{id}/vehicle")
    @Operation(
            summary = "Update vehicle details",
            description = """
                    Role: DRIVER or ADMIN

                    Paste the Driver Profile ID into the id field.
                    Provide the complete new vehicle details.

                    Supported vehicle types:
                    - SEDAN
                    - SUV
                    - VAN
                    - TUK
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicle updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Role is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Vehicle plate already exists"
            )
    })
    public ResponseEntity<DriverResponse> updateVehicle(
            @Parameter(
                    description = """
                            Paste the Driver Profile ID here.
                            Source: POST /api/drivers response field `id`.
                            MongoDB: ridelink_driver_vehicle_service > drivers > _id.
                            """,
                    required = true
            )
            @PathVariable String id,
            @Valid @RequestBody UpdateVehicleRequest request) {

        return ResponseEntity.ok(
                driverService.updateVehicle(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/availability")
    @Operation(
            summary = "Update driver availability",
            description = """
                    Role: DRIVER or ADMIN

                    Availability values:
                    - AVAILABLE: ready for a new ride
                    - BUSY: currently assigned to a ride
                    - OFFLINE: not accepting rides
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Role or driver status is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid availability change"
            )
    })
    public ResponseEntity<DriverResponse> updateAvailability(
            @Parameter(
                    description = """
                            Paste the Driver Profile ID here.
                            Source: POST /api/drivers response field `id`.
                            MongoDB: ridelink_driver_vehicle_service > drivers > _id.
                            """,
                    required = true
            )
            @PathVariable String id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {

        return ResponseEntity.ok(
                driverService.updateAvailability(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/location")
    @Operation(
            summary = "Update driver location",
            description = """
                    Role: DRIVER or ADMIN

                    Paste the Driver Profile ID into the id field.
                    Enter the driver's simulated location details.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Location updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid location"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Role is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            )
    })
    public ResponseEntity<DriverResponse> updateLocation(
            @Parameter(
                    description = """
                            Paste the Driver Profile ID here.
                            Source: POST /api/drivers response field `id`.
                            MongoDB: ridelink_driver_vehicle_service > drivers > _id.
                            """,
                    required = true
            )
            @PathVariable String id,
            @Valid @RequestBody UpdateLocationRequest request) {

        return ResponseEntity.ok(
                driverService.updateLocation(
                        id,
                        request
                )
        );
    }

    @GetMapping("/available")
    @Operation(
            summary = "Internal: Find available drivers",
            description = """
                    Called automatically by Ride Management Service.

                    Finds drivers who are ACTIVE and AVAILABLE.
                    Normal DRIVER or RIDER users should not execute this endpoint.

                    Demonstrate this feature through Ride Service driver assignment.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Available drivers returned"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not allowed"
            )
    })
    public ResponseEntity<List<AvailableDriverResponse>>
            findAvailableDrivers(
                    @Parameter(
                            description = "Enter the service area, for example Colombo",
                            example = "Colombo"
                    )
                    @RequestParam(required = false)
                    String serviceArea,

                    @Parameter(
                            description = "Select the required vehicle type",
                            example = "SEDAN"
                    )
                    @RequestParam(required = false)
                    VehicleType vehicleType,

                    @Parameter(
                            description = "Enter the pickup latitude",
                            example = "6.9271"
                    )
                    @RequestParam(required = false)
                    Double pickupLat,

                    @Parameter(
                            description = "Enter the pickup longitude",
                            example = "79.8612"
                    )
                    @RequestParam(required = false)
                    Double pickupLng,

                    @Parameter(
                            description = "Enter the search radius in kilometres",
                            example = "15.0"
                    )
                    @RequestParam(required = false)
                    Double radiusKm) {

        List<AvailableDriverResponse> availableDrivers =
                driverService.findAvailableDrivers(
                        serviceArea,
                        vehicleType,
                        pickupLat,
                        pickupLng,
                        radiusKm
                );

        return ResponseEntity.ok(
                availableDrivers
        );
    }

    @PatchMapping("/{id}/assign")
    @Operation(
            summary = "Internal: Assign driver",
            description = """
                    Called automatically by Ride Management Service.

                    Changes an AVAILABLE driver to BUSY.
                    Demonstrate this through Ride Service ride assignment.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Driver assigned and marked BUSY"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller or driver status is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Driver is not AVAILABLE"
            )
    })
    public ResponseEntity<DriverResponse> assignDriver(
            @Parameter(
                    description = """
                            Paste the Driver Profile ID here.
                            Source: Ride assignment selection or POST /api/drivers response field `id`.
                            MongoDB: ridelink_driver_vehicle_service > drivers > _id.
                            """,
                    required = true
            )
            @PathVariable String id) {

        return ResponseEntity.ok(
                driverService.assignDriver(id)
        );
    }

    @PatchMapping("/{id}/release")
    @Operation(
            summary = "Internal: Release driver",
            description = """
                    Called automatically by Ride Management Service.

                    completed=true:
                    - completed ride
                    - driver becomes AVAILABLE
                    - totalRidesCompleted increases

                    completed=false:
                    - cancelled ride
                    - driver becomes AVAILABLE
                    - completed count does not increase
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Driver released and marked AVAILABLE"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            )
    })
    public ResponseEntity<DriverResponse> releaseDriver(
            @Parameter(
                    description = """
                            Paste the assigned Driver Profile ID here.
                            Source: Ride response field `driverId`.
                            MongoDB: ridelink_driver_vehicle_service > drivers > _id.
                            """,
                    required = true
            )
            @PathVariable String id,

            @Parameter(
                    description = """
                            Select true for a completed ride.
                            Select false for a cancelled ride.
                            """,
                    example = "true"
            )
            @RequestParam(defaultValue = "true")
            boolean completed) {

        return ResponseEntity.ok(
                driverService.releaseDriver(
                        id,
                        completed
                )
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update operational status",
            description = """
                    Role: DRIVER or ADMIN
                    Recommended demo role: ADMIN

                    Operational status values:
                    - ACTIVE
                    - SUSPENDED
                    - PENDING_VERIFICATION

                    Setting SUSPENDED also changes availability to OFFLINE.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Operational status updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Role is not allowed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver Profile not found"
            )
    })
    public ResponseEntity<DriverResponse> updateOperationalStatus(
            @Parameter(
                    description = """
                            Paste the Driver Profile ID here.
                            Source: POST /api/drivers response field `id`.
                            MongoDB: ridelink_driver_vehicle_service > drivers > _id.
                            """,
                    required = true
            )
            @PathVariable String id,
            @Valid @RequestBody UpdateOperationalStatusRequest request) {

        return ResponseEntity.ok(
                driverService.updateOperationalStatus(
                        id,
                        request
                )
        );
    }
}