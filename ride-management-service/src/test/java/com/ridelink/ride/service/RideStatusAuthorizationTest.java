package com.ridelink.ride.service;

import com.ridelink.ride.client.AccountClient;
import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.FareClient;
import com.ridelink.ride.dto.DriverProfileResponse;
import com.ridelink.ride.dto.UpdateRideStatusRequest;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.entity.RideLocation;
import com.ridelink.ride.entity.RideStatus;
import com.ridelink.ride.entity.VehicleType;
import com.ridelink.ride.exception.ApiException;
import com.ridelink.ride.repository.RideRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideStatusAuthorizationTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverClient driverClient;

    @Mock
    private AccountClient accountClient;

    @Mock
    private FareClient fareClient;

    @InjectMocks
    private RideService rideService;

    @Test
    void assignedDriverCanAcceptOwnRide() {

        Ride ride = ride(
                RideStatus.ASSIGNED,
                "rider-1",
                "driver-profile-1");

        UpdateRideStatusRequest request =
                request(RideStatus.ACCEPTED);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.getDriverByUserId(
                "driver-account-1",
                "driver-token"))
                .thenReturn(
                        new DriverProfileResponse(
                                "driver-profile-1",
                                "driver-account-1"));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        var response =
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "driver-account-1",
                        "DRIVER",
                        "driver-token");

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.ACCEPTED);

        verify(rideRepository)
                .save(ride);
    }

    @Test
    void differentDriverCannotAcceptRide() {

        Ride ride = ride(
                RideStatus.ASSIGNED,
                "rider-1",
                "driver-profile-1");

        UpdateRideStatusRequest request =
                request(RideStatus.ACCEPTED);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.getDriverByUserId(
                "driver-account-2",
                "driver-token-2"))
                .thenReturn(
                        new DriverProfileResponse(
                                "driver-profile-2",
                                "driver-account-2"));

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "driver-account-2",
                        "DRIVER",
                        "driver-token-2"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatus())
                                .isEqualTo(
                                        HttpStatus.FORBIDDEN));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void owningRiderCanCancelOwnRide() {

        Ride ride = ride(
                RideStatus.REQUESTED,
                "rider-1",
                null);

        UpdateRideStatusRequest request =
                request(RideStatus.CANCELLED);

        request.setCancellationReason(
                "Passenger changed plans");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        var response =
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "rider-1",
                        "RIDER",
                        "rider-token");

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.CANCELLED);

        verify(rideRepository)
                .save(ride);
    }

    @Test
    void differentRiderCannotCancelRide() {

        Ride ride = ride(
                RideStatus.REQUESTED,
                "rider-1",
                null);

        UpdateRideStatusRequest request =
                request(RideStatus.CANCELLED);

        request.setCancellationReason(
                "Not this rider's ride");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "rider-2",
                        "RIDER",
                        "rider-token-2"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatus())
                                .isEqualTo(
                                        HttpStatus.FORBIDDEN));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void riderCannotAcceptRide() {

        Ride ride = ride(
                RideStatus.ASSIGNED,
                "rider-1",
                "driver-profile-1");

        UpdateRideStatusRequest request =
                request(RideStatus.ACCEPTED);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "rider-1",
                        "RIDER",
                        "rider-token"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatus())
                                .isEqualTo(
                                        HttpStatus.FORBIDDEN));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void assignedDriverInvalidTransitionReturnsConflict() {

        Ride ride = ride(
                RideStatus.ASSIGNED,
                "rider-1",
                "driver-profile-1");

        UpdateRideStatusRequest request =
                request(RideStatus.IN_PROGRESS);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.getDriverByUserId(
                "driver-account-1",
                "driver-token"))
                .thenReturn(
                        new DriverProfileResponse(
                                "driver-profile-1",
                                "driver-account-1"));

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "driver-account-1",
                        "DRIVER",
                        "driver-token"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatus())
                                .isEqualTo(
                                        HttpStatus.CONFLICT));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void unauthorizedDriverCompletionHasNoSideEffects() {

        Ride ride = ride(
                RideStatus.IN_PROGRESS,
                "rider-1",
                "driver-profile-1");

        UpdateRideStatusRequest request =
                request(RideStatus.COMPLETED);

        request.setActualDistanceKm(12.5);
        request.setActualDurationMinutes(35);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.getDriverByUserId(
                "driver-account-2",
                "driver-token-2"))
                .thenReturn(
                        new DriverProfileResponse(
                                "driver-profile-2",
                                "driver-account-2"));

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "driver-account-2",
                        "DRIVER",
                        "driver-token-2"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatus())
                                .isEqualTo(
                                        HttpStatus.FORBIDDEN));

        verify(fareClient, never())
                .finalizeFare(any(), any());

        verify(driverClient, never())
                .releaseDriver(any(), any(Boolean.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void adminCanPerformValidLifecycleTransition() {

        Ride ride = ride(
                RideStatus.ASSIGNED,
                "rider-1",
                "driver-profile-1");

        UpdateRideStatusRequest request =
                request(RideStatus.ACCEPTED);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        var response =
                rideService.updateStatus(
                        "ride-1",
                        request,
                        "admin-account",
                        "ADMIN",
                        "admin-token");

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.ACCEPTED);

        verify(driverClient, never())
                .getDriverByUserId(any(), any());
    }

    private UpdateRideStatusRequest request(
            RideStatus status) {

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(status);

        return request;
    }

    private Ride ride(
            RideStatus status,
            String passengerId,
            String driverId) {

        RideLocation pickup =
                new RideLocation(
                        "Colombo Fort",
                        6.9271,
                        79.8612);

        RideLocation destination =
                new RideLocation(
                        "Bambalapitiya",
                        6.8936,
                        79.8567);

        Ride ride =
                new Ride(
                        passengerId,
                        pickup,
                        destination,
                        VehicleType.SEDAN,
                        500.0);

        ride.setStatus(status);
        ride.setDriverId(driverId);
        ride.setRequestedAt(Instant.now());
        ride.setUpdatedAt(Instant.now());

        return ride;
    }
}