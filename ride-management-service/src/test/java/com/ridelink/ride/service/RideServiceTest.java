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
import com.ridelink.ride.dto.UpdateRideStatusRequest;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.entity.RideLocation;
import com.ridelink.ride.entity.RideStatus;
import com.ridelink.ride.exception.ApiException;
import com.ridelink.ride.repository.RideRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

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
    void activeRiderCanCreateRideUsingFareServiceEstimate() {

        CreateRideRequest request = request();

        AccountResponse account =
                riderAccount("account-rider-1");

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(account);

        when(fareClient.estimateFare(
                any(FareEstimateRequest.class)))
                .thenReturn(
                        estimateResponse(
                                new BigDecimal("2450.00")));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var response =
                rideService.createRide(
                        request,
                        "valid-token");

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.REQUESTED);

        assertThat(response.getEstimatedFare())
                .isEqualTo(2450.00);

        verify(fareClient)
                .estimateFare(
                        any(FareEstimateRequest.class));
    }

    @Test
    void createRideSendsRouteMetricsToFareService() {

        CreateRideRequest request = request();

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(
                        riderAccount(
                                "account-rider-1"));

        when(fareClient.estimateFare(
                any(FareEstimateRequest.class)))
                .thenReturn(
                        estimateResponse(
                                new BigDecimal("2450.00")));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        rideService.createRide(
                request,
                "valid-token");

        ArgumentCaptor<FareEstimateRequest> captor =
                ArgumentCaptor.forClass(
                        FareEstimateRequest.class);

        verify(fareClient)
                .estimateFare(
                        captor.capture());

        FareEstimateRequest fareRequest =
                captor.getValue();

        assertThat(fareRequest.pickup())
                .isEqualTo("Colombo");

        assertThat(fareRequest.destination())
                .isEqualTo("Kandy");

        assertThat(fareRequest.distanceKm())
                .isGreaterThan(0.0);

        assertThat(fareRequest.durationMinutes())
                .isGreaterThan(0);
    }

    @Test
    void createRideUsesAccountServiceIdAsPassengerId() {

        CreateRideRequest request = request();

        AccountResponse account =
                riderAccount("account-rider-123");

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(account);

        when(fareClient.estimateFare(
                any(FareEstimateRequest.class)))
                .thenReturn(
                        estimateResponse(
                                new BigDecimal("2450.00")));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var response =
                rideService.createRide(
                        request,
                        "valid-token");

        assertThat(response.getPassengerId())
                .isEqualTo("account-rider-123");

        verify(accountClient)
                .getCurrentAccount("valid-token");
    }

    @Test
    void fareServiceUnavailableDuringCreationDoesNotSaveRide() {

        CreateRideRequest request = request();

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(
                        riderAccount(
                                "account-rider-1"));

        when(fareClient.estimateFare(
                any(FareEstimateRequest.class)))
                .thenThrow(
                        new ApiException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Fare Service is unavailable"));

        assertThatThrownBy(() ->
                rideService.createRide(
                        request,
                        "valid-token"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(
                                    HttpStatus.SERVICE_UNAVAILABLE);
                });

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void driverAccountCannotCreateRide() {

        CreateRideRequest request = request();

        AccountResponse account =
                new AccountResponse();

        account.setId("driver-account-1");
        account.setRole("DRIVER");
        account.setActive(true);

        when(accountClient.getCurrentAccount("driver-token"))
                .thenReturn(account);

        assertThatThrownBy(() ->
                rideService.createRide(
                        request,
                        "driver-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Only RIDER accounts can create rides");

        verify(fareClient, never())
                .estimateFare(
                        any(FareEstimateRequest.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void inactiveAccountCannotCreateRide() {

        CreateRideRequest request = request();

        AccountResponse account =
                new AccountResponse();

        account.setId("account-rider-1");
        account.setRole("RIDER");
        account.setActive(false);

        when(accountClient.getCurrentAccount("inactive-token"))
                .thenReturn(account);

        assertThatThrownBy(() ->
                rideService.createRide(
                        request,
                        "inactive-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Inactive account cannot create a ride");

        verify(fareClient, never())
                .estimateFare(
                        any(FareEstimateRequest.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void missingAccountIdIsRejected() {

        CreateRideRequest request = request();

        AccountResponse account =
                new AccountResponse();

        account.setRole("RIDER");
        account.setActive(true);
        account.setId(null);

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(account);

        assertThatThrownBy(() ->
                rideService.createRide(
                        request,
                        "valid-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "invalid account ID");

        verify(fareClient, never())
                .estimateFare(
                        any(FareEstimateRequest.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void assignRideSelectsFirstAvailableDriverAndMovesRideToAssigned() {

        Ride ride = requestedRide();

        AvailableDriverResponse nearestDriver =
                new AvailableDriverResponse();

        nearestDriver.setId("driver-nearest");
        nearestDriver.setDistanceKm(1.0);

        AvailableDriverResponse fartherDriver =
                new AvailableDriverResponse();

        fartherDriver.setId("driver-farther");
        fartherDriver.setDistanceKm(5.0);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.findAvailableDrivers(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(
                        List.of(
                                nearestDriver,
                                fartherDriver));

        doNothing()
                .when(driverClient)
                .assignDriver("driver-nearest");

        when(rideRepository.save(ride))
                .thenReturn(ride);

        var response =
                rideService.assignRide("ride-1");

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.ASSIGNED);

        assertThat(response.getDriverId())
                .isEqualTo("driver-nearest");

        verify(driverClient)
                .assignDriver("driver-nearest");

        verify(driverClient, never())
                .assignDriver("driver-farther");
    }

    @Test
    void assignRideReturnsConflictWhenNoDriverAvailable() {

        Ride ride = requestedRide();

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.findAvailableDrivers(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                rideService.assignRide("ride-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.CONFLICT);
                })
                .hasMessageContaining(
                        "No available driver");

        assertThat(ride.getStatus())
                .isEqualTo(RideStatus.REQUESTED);

        assertThat(ride.getDriverId())
                .isNull();

        verify(driverClient, never())
                .assignDriver(any());

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void assignRideRejectsInvalidDriverId() {

        Ride ride = requestedRide();

        AvailableDriverResponse invalidDriver =
                new AvailableDriverResponse();

        invalidDriver.setId(" ");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.findAvailableDrivers(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(
                        List.of(invalidDriver));

        assertThatThrownBy(() ->
                rideService.assignRide("ride-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(
                                    HttpStatus.SERVICE_UNAVAILABLE);
                })
                .hasMessageContaining(
                        "invalid driver ID");

        assertThat(ride.getStatus())
                .isEqualTo(RideStatus.REQUESTED);

        verify(driverClient, never())
                .assignDriver(any());

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void driverServiceUnavailableDoesNotAssignRide() {

        Ride ride = requestedRide();

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.findAvailableDrivers(
                any(),
                any(),
                any(),
                any()))
                .thenThrow(
                        new ApiException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Driver service is unavailable"));

        assertThatThrownBy(() ->
                rideService.assignRide("ride-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(
                                    HttpStatus.SERVICE_UNAVAILABLE);
                });

        assertThat(ride.getStatus())
                .isEqualTo(RideStatus.REQUESTED);

        assertThat(ride.getDriverId())
                .isNull();

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void completedRideUsesFareServiceFinalFareAndReleasesDriver() {

        Ride ride = requestedRide();

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setDriverId("driver-1");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(fareClient.finalizeFare(
                any(),
                any(FinalFareRequest.class)))
                .thenReturn(
                        finalFareResponse(
                                new BigDecimal("1875.50")));

        doNothing()
                .when(driverClient)
                .releaseDriver(
                        "driver-1",
                        true);

        when(rideRepository.save(ride))
                .thenReturn(ride);

        UpdateRideStatusRequest request =
                completionRequest();

        var response =
                rideService.updateStatus(
                        "ride-1",
                        request);

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.COMPLETED);

        assertThat(response.getFinalFare())
                .isEqualTo(1875.50);

        verify(fareClient)
                .finalizeFare(
                        "ride-1",
                        new FinalFareRequest(
                                12.5,
                                28));

        verify(driverClient)
                .releaseDriver(
                        "driver-1",
                        true);
    }

    @Test
    void completionSendsActualDistanceAndDurationToFareService() {

        Ride ride = requestedRide();

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setDriverId("driver-1");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(fareClient.finalizeFare(
                any(),
                any(FinalFareRequest.class)))
                .thenReturn(
                        finalFareResponse(
                                new BigDecimal("1875.50")));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        UpdateRideStatusRequest request =
                completionRequest();

        rideService.updateStatus(
                "ride-1",
                request);

        ArgumentCaptor<FinalFareRequest> captor =
                ArgumentCaptor.forClass(
                        FinalFareRequest.class);

        verify(fareClient)
                .finalizeFare(
                        org.mockito.ArgumentMatchers.eq("ride-1"),
                        captor.capture());

        assertThat(captor.getValue().actualDistanceKm())
                .isEqualTo(12.5);

        assertThat(captor.getValue().actualDurationMinutes())
                .isEqualTo(28);
    }

    @Test
    void completionRequiresActualDistance() {

        Ride ride = requestedRide();

        ride.setStatus(RideStatus.IN_PROGRESS);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(
                RideStatus.COMPLETED);

        request.setActualDurationMinutes(
                20);

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .hasMessageContaining(
                        "Actual distance is required");

        verify(fareClient, never())
                .finalizeFare(
                        any(),
                        any(FinalFareRequest.class));

        verify(driverClient, never())
                .releaseDriver(
                        any(),
                        any(Boolean.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void completionRequiresActualDuration() {

        Ride ride = requestedRide();

        ride.setStatus(RideStatus.IN_PROGRESS);

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(
                RideStatus.COMPLETED);

        request.setActualDistanceKm(
                5.0);

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .hasMessageContaining(
                        "Actual duration is required");

        verify(fareClient, never())
                .finalizeFare(
                        any(),
                        any(FinalFareRequest.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void fareServiceFailureLeavesRideInProgressAndDoesNotReleaseDriver() {

        Ride ride = requestedRide();

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setDriverId("driver-1");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(fareClient.finalizeFare(
                any(),
                any(FinalFareRequest.class)))
                .thenThrow(
                        new ApiException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Fare Service is unavailable"));

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        completionRequest()))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(
                                    HttpStatus.SERVICE_UNAVAILABLE);
                });

        assertThat(ride.getStatus())
                .isEqualTo(RideStatus.IN_PROGRESS);

        assertThat(ride.getFinalFare())
                .isNull();

        verify(driverClient, never())
                .releaseDriver(
                        "driver-1",
                        true);

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void cancelledRideDoesNotCallFareService() {

        Ride ride = requestedRide();

        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverId("driver-1");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        doNothing()
                .when(driverClient)
                .releaseDriver(
                        "driver-1",
                        false);

        when(rideRepository.save(ride))
                .thenReturn(ride);

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(
                RideStatus.CANCELLED);

        request.setCancellationReason(
                "Passenger changed plans");

        var response =
                rideService.updateStatus(
                        "ride-1",
                        request);

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.CANCELLED);

        verify(fareClient, never())
                .finalizeFare(
                        any(),
                        any(FinalFareRequest.class));

        verify(driverClient)
                .releaseDriver(
                        "driver-1",
                        false);
    }

    @Test
    void invalidTransitionDoesNotCallFareService() {

        Ride ride = requestedRide();

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        UpdateRideStatusRequest request =
                completionRequest();

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.CONFLICT);
                })
                .hasMessageContaining(
                        "Invalid ride status transition");

        verify(fareClient, never())
                .finalizeFare(
                        any(),
                        any(FinalFareRequest.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void cancellationRequiresReason() {

        Ride ride = requestedRide();

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(
                RideStatus.CANCELLED);

        assertThatThrownBy(() ->
                rideService.updateStatus(
                        "ride-1",
                        request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .hasMessageContaining(
                        "Cancellation reason is required");

        verify(fareClient, never())
                .finalizeFare(
                        any(),
                        any(FinalFareRequest.class));

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    private Ride requestedRide() {

        Ride ride = new Ride(
                "passenger-1",
                new RideLocation(
                        "Colombo",
                        6.9271,
                        79.8612),
                new RideLocation(
                        "Kandy",
                        7.2906,
                        80.6337),
                null,
                10.0);

        ride.setId("ride-1");

        return ride;
    }

    private CreateRideRequest request() {

        CreateRideRequest request =
                new CreateRideRequest();

        request.setPickup(
                location(
                        "Colombo",
                        6.9271,
                        79.8612));

        request.setDestination(
                location(
                        "Kandy",
                        7.2906,
                        80.6337));

        return request;
    }

    private UpdateRideStatusRequest completionRequest() {

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(
                RideStatus.COMPLETED);

        request.setActualDistanceKm(
                12.5);

        request.setActualDurationMinutes(
                28);

        return request;
    }

    private FareEstimateResponse estimateResponse(
            BigDecimal estimatedFare) {

        return new FareEstimateResponse(
                "Colombo",
                "Kandy",
                10.0,
                20,
                new BigDecimal("110.00"),
                new BigDecimal("810.00"),
                new BigDecimal("100.00"),
                estimatedFare,
                "LKR");
    }

    private FinalFareResponse finalFareResponse(
            BigDecimal totalFare) {

        return new FinalFareResponse(
                "ride-1",
                12.5,
                28,
                new BigDecimal("110.00"),
                new BigDecimal("1035.00"),
                new BigDecimal("140.00"),
                totalFare,
                "LKR",
                new BigDecimal("110.00"),
                new BigDecimal("90.00"),
                new BigDecimal("5.00"),
                Instant.now());
    }

    private AccountResponse riderAccount(
            String id) {

        AccountResponse account =
                new AccountResponse();

        account.setId(id);
        account.setRole("RIDER");
        account.setActive(true);

        return account;
    }

    private RideLocationRequest location(
            String place,
            double latitude,
            double longitude) {

        RideLocationRequest location =
                new RideLocationRequest();

        location.setPlace(place);
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }
}