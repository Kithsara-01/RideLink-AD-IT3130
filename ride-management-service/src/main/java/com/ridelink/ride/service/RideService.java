package com.ridelink.ride.service;

import com.ridelink.ride.client.AccountClient;
import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.FareClient;
import com.ridelink.ride.dto.AccountResponse;
import com.ridelink.ride.dto.AvailableDriverResponse;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.FareEstimateRequest;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.dto.FinalFareRequest;
import com.ridelink.ride.dto.FinalFareResponse;
import com.ridelink.ride.dto.RideLocationRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.dto.UpdateRideStatusRequest;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.entity.RideLocation;
import com.ridelink.ride.entity.RideStatus;
import com.ridelink.ride.exception.ApiException;
import com.ridelink.ride.repository.RideRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class RideService {

    private static final double ESTIMATED_AVERAGE_SPEED_KMH = 30.0;

    private final RideRepository rideRepository;
    private final DriverClient driverClient;
    private final AccountClient accountClient;
    private final FareClient fareClient;

    public RideService(
            RideRepository rideRepository,
            DriverClient driverClient,
            AccountClient accountClient,
            FareClient fareClient) {

        this.rideRepository = rideRepository;
        this.driverClient = driverClient;
        this.accountClient = accountClient;
        this.fareClient = fareClient;
    }

    public RideResponse createRide(
            CreateRideRequest request,
            String bearerToken) {

        AccountResponse account =
                accountClient.getCurrentAccount(bearerToken);

        if (!account.isActive()) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Inactive account cannot create a ride");
        }

        if (!"RIDER".equals(account.getRole())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Only RIDER accounts can create rides");
        }

        if (account.getId() == null
                || account.getId().isBlank()) {

            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Account Service returned an invalid account ID");
        }

        RideLocation pickup =
                toLocation(request.getPickup());

        RideLocation destination =
                toLocation(request.getDestination());

        double distanceKm =
                roundDistance(
                        distanceInKm(
                                pickup,
                                destination));

        int durationMinutes =
                estimateDurationMinutes(
                        distanceKm);

        FareEstimateRequest fareRequest =
                new FareEstimateRequest(
                        pickup.getPlace(),
                        destination.getPlace(),
                        distanceKm,
                        durationMinutes);

        FareEstimateResponse fareResponse =
                fareClient.estimateFare(
                        fareRequest);

        Ride ride = new Ride(
                account.getId().trim(),
                pickup,
                destination,
                request.getVehicleType(),
                fareResponse
                        .estimatedFare()
                        .doubleValue());

        return RideResponse.fromEntity(
                rideRepository.save(ride));
    }

    public RideResponse getRide(String id) {

        return RideResponse.fromEntity(
                findRide(id));
    }

    public List<RideResponse> getPassengerRides(
            String passengerId) {

        return rideRepository
                .findByPassengerIdOrderByRequestedAtDesc(
                        passengerId.trim())
                .stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    public List<RideResponse> getDriverRides(
            String driverId) {

        return rideRepository
                .findByDriverIdOrderByRequestedAtDesc(
                        driverId.trim())
                .stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    public RideResponse assignRide(String id) {

        Ride ride = findRide(id);

        requireStatus(
                ride,
                RideStatus.REQUESTED);

        List<AvailableDriverResponse> drivers =
                driverClient.findAvailableDrivers(
                        null,
                        ride.getVehicleType(),
                        ride.getPickup().getLatitude(),
                        ride.getPickup().getLongitude()
                );

        if (drivers == null || drivers.isEmpty()) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "No available driver matches this ride request");
        }

        String driverId =
                drivers.get(0).getId();

        if (driverId == null
                || driverId.isBlank()) {

            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Driver service returned an invalid driver ID");
        }

        driverClient.assignDriver(
                driverId);

        ride.setDriverId(
                driverId);

        ride.setStatus(
                RideStatus.ASSIGNED);

        touch(ride);

        return RideResponse.fromEntity(
                rideRepository.save(ride));
    }

    public RideResponse updateStatus(
            String id,
            UpdateRideStatusRequest request) {

        Ride ride =
                findRide(id);

        RideStatus current =
                ride.getStatus();

        RideStatus target =
                request.getStatus();

        if (!allowedTransitions(current)
                .contains(target)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid ride status transition from "
                            + current
                            + " to "
                            + target);
        }

        if (target == RideStatus.CANCELLED) {

            if (request.getCancellationReason() == null
                    || request.getCancellationReason()
                            .isBlank()) {

                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Cancellation reason is required");
            }

            ride.setCancellationReason(
                    request.getCancellationReason()
                            .trim());

            ride.setCancelledAt(
                    Instant.now());

            if (ride.getDriverId() != null) {

                driverClient.releaseDriver(
                        ride.getDriverId(),
                        false);
            }

        } else if (target == RideStatus.ACCEPTED) {

            ride.setAcceptedAt(
                    Instant.now());

        } else if (target == RideStatus.IN_PROGRESS) {

            ride.setStartedAt(
                    Instant.now());

        } else if (target == RideStatus.COMPLETED) {

            validateCompletionDetails(
                    request);

            FinalFareRequest finalFareRequest =
                    new FinalFareRequest(
                            request.getActualDistanceKm(),
                            request.getActualDurationMinutes());

            FinalFareResponse finalFareResponse =
                    fareClient.finalizeFare(
                            ride.getId(),
                            finalFareRequest);

            ride.setFinalFare(
                    finalFareResponse
                            .totalFare()
                            .doubleValue());

            ride.setCompletedAt(
                    Instant.now());

            if (ride.getDriverId() != null) {

                driverClient.releaseDriver(
                        ride.getDriverId(),
                        true);
            }
        }

        ride.setStatus(
                target);

        touch(ride);

        return RideResponse.fromEntity(
                rideRepository.save(ride));
    }

    private void validateCompletionDetails(
            UpdateRideStatusRequest request) {

        if (request.getActualDistanceKm() == null) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Actual distance is required when completing a ride");
        }

        if (request.getActualDistanceKm() < 0.1) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Actual distance must be at least 0.1 km");
        }

        if (request.getActualDurationMinutes() == null) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Actual duration is required when completing a ride");
        }

        if (request.getActualDurationMinutes() < 1) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Actual duration must be at least 1 minute");
        }
    }

    private Ride findRide(
            String id) {

        return rideRepository
                .findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Ride not found with ID: "
                                        + id));
    }

    private void requireStatus(
            Ride ride,
            RideStatus expected) {

        if (ride.getStatus()
                != expected) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Ride must be in "
                            + expected
                            + " status, but is "
                            + ride.getStatus());
        }
    }

    private Set<RideStatus> allowedTransitions(
            RideStatus status) {

        return switch (status) {

            case REQUESTED ->
                    EnumSet.of(
                            RideStatus.CANCELLED);

            case ASSIGNED ->
                    EnumSet.of(
                            RideStatus.ACCEPTED,
                            RideStatus.CANCELLED);

            case ACCEPTED ->
                    EnumSet.of(
                            RideStatus.IN_PROGRESS,
                            RideStatus.CANCELLED);

            case IN_PROGRESS ->
                    EnumSet.of(
                            RideStatus.COMPLETED,
                            RideStatus.CANCELLED);

            case COMPLETED, CANCELLED ->
                    EnumSet.noneOf(
                            RideStatus.class);
        };
    }

    private RideLocation toLocation(
            RideLocationRequest request) {

        return new RideLocation(
                request.getPlace().trim(),
                request.getLatitude(),
                request.getLongitude());
    }

    private void touch(
            Ride ride) {

        ride.setUpdatedAt(
                Instant.now());
    }

    private double distanceInKm(
            RideLocation from,
            RideLocation to) {

        double latDistance =
                Math.toRadians(
                        to.getLatitude()
                                - from.getLatitude());

        double lonDistance =
                Math.toRadians(
                        to.getLongitude()
                                - from.getLongitude());

        double a =
                Math.sin(latDistance / 2)
                        * Math.sin(latDistance / 2)
                        + Math.cos(
                        Math.toRadians(
                                from.getLatitude()))
                        * Math.cos(
                        Math.toRadians(
                                to.getLatitude()))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);

        return 6371.0
                * 2
                * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a));
    }

    private double roundDistance(
            double distanceKm) {

        return Math.round(
                distanceKm * 100.0)
                / 100.0;
    }

    private int estimateDurationMinutes(
            double distanceKm) {

        double hours =
                distanceKm
                        / ESTIMATED_AVERAGE_SPEED_KMH;

        return Math.max(
                1,
                (int) Math.ceil(
                        hours * 60.0));
    }
}