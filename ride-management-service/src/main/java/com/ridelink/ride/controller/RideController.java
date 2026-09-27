package com.ridelink.ride.controller;

import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.dto.UpdateRideStatusRequest;
import com.ridelink.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@Tag(name = "Ride Management", description = "Ride requests, driver assignment and ride lifecycle management")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @Operation(summary = "Create ride request", description = "Creates a requested ride and calculates a transparent distance-based fare estimate")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ride created"),
        @ApiResponse(responseCode = "400", description = "Invalid ride details")
    })
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride", description = "Retrieves a ride by its stable identifier")
    public ResponseEntity<RideResponse> getRide(@PathVariable String id) {
        return ResponseEntity.ok(rideService.getRide(id));
    }

    @GetMapping
    @Operation(summary = "List rides", description = "Lists rides for a passenger or driver")
    public ResponseEntity<List<RideResponse>> listRides(
        @RequestParam(required = false) String passengerId,
        @RequestParam(required = false) String driverId) {
        if (passengerId != null && !passengerId.isBlank()) {
            return ResponseEntity.ok(rideService.getPassengerRides(passengerId));
        }
        if (driverId != null && !driverId.isBlank()) {
            return ResponseEntity.ok(rideService.getDriverRides(driverId));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign nearest eligible driver", description = "Synchronously queries Driver & Vehicle Service and locks the selected driver")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Driver assigned"),
        @ApiResponse(responseCode = "409", description = "No driver available or ride is not requestable"),
        @ApiResponse(responseCode = "503", description = "Driver service unavailable")
    })
    public ResponseEntity<RideResponse> assignRide(@PathVariable String id) {
        return ResponseEntity.ok(rideService.assignRide(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ride lifecycle status", description = "Applies valid lifecycle transitions: assigned, accepted, in-progress, completed, or cancelled")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ride status updated"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public ResponseEntity<RideResponse> updateStatus(
        @PathVariable String id,
        @Valid @RequestBody UpdateRideStatusRequest request) {
        return ResponseEntity.ok(rideService.updateStatus(id, request));
    }
}
