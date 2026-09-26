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
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver Management", description = "Driver profiles, vehicle info, availability, simulated GPS location, and proximity discovery")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register driver profile", description = "Registers an operational driver profile with vehicle and initial location details")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Driver registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Duplicate user ID, license number, or vehicle plate")
    })
    public ResponseEntity<DriverResponse> registerDriver(@Valid @RequestBody CreateDriverRequest request) {
        DriverResponse response = driverService.registerDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver by ID", description = "Retrieves driver operational and vehicle information by driver ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Driver retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get driver by User ID", description = "Retrieves driver profile linked to an account service User ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Driver retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found for user ID")
    })
    public ResponseEntity<DriverResponse> getDriverByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(driverService.getDriverByUserId(userId));
    }

    @PutMapping("/{id}/vehicle")
    @Operation(summary = "Update vehicle details", description = "Updates vehicle attributes such as make, model, plate, or capacity")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "404", description = "Driver not found"),
        @ApiResponse(responseCode = "409", description = "License plate already registered to another vehicle")
    })
    public ResponseEntity<DriverResponse> updateVehicle(
            @PathVariable String id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        return ResponseEntity.ok(driverService.updateVehicle(id, request));
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Update availability status", description = "Driver toggles availability between AVAILABLE, OFFLINE, and BUSY")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Availability updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "403", description = "Suspended driver cannot set availability to AVAILABLE"),
        @ApiResponse(responseCode = "404", description = "Driver not found"),
        @ApiResponse(responseCode = "409", description = "Cannot go OFFLINE while in an active ride")
    })
    public ResponseEntity<DriverResponse> updateAvailability(
            @PathVariable String id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(driverService.updateAvailability(id, request));
    }

    @PatchMapping("/{id}/location")
    @Operation(summary = "Update simulated GPS location", description = "Updates driver simulated latitude, longitude, and optional service area")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid coordinate bounds"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable String id,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(id, request));
    }

    @GetMapping("/available")
    @Operation(summary = "Find eligible available drivers", description = "Interservice query for Ride Management Service to discover nearby available drivers matching area, vehicle type, and pickup coordinates")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Available drivers retrieved successfully (empty list if none found)")
    })
    public ResponseEntity<List<AvailableDriverResponse>> findAvailableDrivers(
            @Parameter(description = "Optional service area filter") @RequestParam(required = false) String serviceArea,
            @Parameter(description = "Optional vehicle type filter (SEDAN, SUV, VAN, TUK)") @RequestParam(required = false) VehicleType vehicleType,
            @Parameter(description = "Pickup latitude for proximity calculation") @RequestParam(required = false) Double pickupLat,
            @Parameter(description = "Pickup longitude for proximity calculation") @RequestParam(required = false) Double pickupLng,
            @Parameter(description = "Search radius in km (default 15.0)") @RequestParam(required = false) Double radiusKm) {
        List<AvailableDriverResponse> availableDrivers = driverService.findAvailableDrivers(
            serviceArea, vehicleType, pickupLat, pickupLng, radiusKm
        );
        return ResponseEntity.ok(availableDrivers);
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Interservice Lock: Assign driver to ride", description = "Transitions driver from AVAILABLE to BUSY when ride request is assigned or accepted")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Driver assigned successfully and marked BUSY"),
        @ApiResponse(responseCode = "403", description = "Cannot assign suspended driver"),
        @ApiResponse(responseCode = "404", description = "Driver not found"),
        @ApiResponse(responseCode = "409", description = "Driver is already BUSY or OFFLINE")
    })
    public ResponseEntity<DriverResponse> assignDriver(@PathVariable String id) {
        return ResponseEntity.ok(driverService.assignDriver(id));
    }

    @PatchMapping("/{id}/release")
    @Operation(summary = "Interservice Release: Free driver upon ride completion", description = "Transitions driver from BUSY back to AVAILABLE and increments completed ride count")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Driver released successfully and marked AVAILABLE"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<DriverResponse> releaseDriver(@PathVariable String id) {
        return ResponseEntity.ok(driverService.releaseDriver(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Administrative: Update operational status", description = "Admin endpoint to set driver operational status to ACTIVE or SUSPENDED")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operational status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<DriverResponse> updateOperationalStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOperationalStatusRequest request) {
        return ResponseEntity.ok(driverService.updateOperationalStatus(id, request));
    }
}
