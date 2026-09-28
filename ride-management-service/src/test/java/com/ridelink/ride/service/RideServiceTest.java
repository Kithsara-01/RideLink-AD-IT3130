package com.ridelink.ride.service;

import com.ridelink.ride.client.AccountClient;
import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.dto.AccountResponse;
import com.ridelink.ride.dto.AvailableDriverResponse;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideLocationRequest;
import com.ridelink.ride.dto.UpdateRideStatusRequest;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.entity.RideLocation;
import com.ridelink.ride.entity.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private RideService rideService;

    @Test
    void activeRiderCanCreateRide() {
        CreateRideRequest request = request();

        AccountResponse account = riderAccount("account-rider-1");

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(account);

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.createRide(request, "valid-token");

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.REQUESTED);

        assertThat(response.getEstimatedFare())
                .isGreaterThan(2.50);
    }

    @Test
    void createRideUsesAccountServiceIdAsPassengerId() {
        CreateRideRequest request = request();

        AccountResponse account = riderAccount("account-rider-123");

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(account);

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.createRide(request, "valid-token");

        assertThat(response.getPassengerId())
                .isEqualTo("account-rider-123");

        verify(accountClient)
                .getCurrentAccount("valid-token");
    }

    @Test
    void driverAccountCannotCreateRide() {
        CreateRideRequest request = request();

        AccountResponse account = new AccountResponse();
        account.setId("driver-account-1");
        account.setRole("DRIVER");
        account.setActive(true);

        when(accountClient.getCurrentAccount("driver-token"))
                .thenReturn(account);

        assertThatThrownBy(() ->
                rideService.createRide(request, "driver-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only RIDER accounts can create rides");

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void inactiveAccountCannotCreateRide() {
        CreateRideRequest request = request();

        AccountResponse account = new AccountResponse();
        account.setId("account-rider-1");
        account.setRole("RIDER");
        account.setActive(false);

        when(accountClient.getCurrentAccount("inactive-token"))
                .thenReturn(account);

        assertThatThrownBy(() ->
                rideService.createRide(request, "inactive-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Inactive account cannot create a ride");

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void missingAccountIdIsRejected() {
        CreateRideRequest request = request();

        AccountResponse account = new AccountResponse();
        account.setRole("RIDER");
        account.setActive(true);
        account.setId(null);

        when(accountClient.getCurrentAccount("valid-token"))
                .thenReturn(account);

        assertThatThrownBy(() ->
                rideService.createRide(request, "valid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid account ID");

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    void assignRideSelectsDriverAndMovesRideToAssigned() {
        Ride ride = new Ride(
                "passenger-1",
                new RideLocation("Colombo", 6.9271, 79.8612),
                new RideLocation("Kandy", 7.2906, 80.6337),
                null,
                10.0
        );

        ride.setId("ride-1");

        AvailableDriverResponse driver = new AvailableDriverResponse();
        driver.setId("driver-1");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        when(driverClient.findAvailableDrivers(any(), any(), any(), any()))
                .thenReturn(List.of(driver));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        doNothing()
                .when(driverClient)
                .assignDriver("driver-1");

        var response = rideService.assignRide("ride-1");

        assertThat(response.getStatus())
                .isEqualTo(RideStatus.ASSIGNED);

        assertThat(response.getDriverId())
                .isEqualTo("driver-1");

        verify(driverClient)
                .assignDriver("driver-1");
    }

    @Test
    void invalidTransitionIsRejected() {
        Ride ride = new Ride(
                "passenger-1",
                null,
                null,
                null,
                10.0
        );

        ride.setId("ride-1");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(RideStatus.COMPLETED);

        assertThatThrownBy(() ->
                rideService.updateStatus("ride-1", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid ride status transition");
    }

    @Test
    void cancellationRequiresReason() {
        Ride ride = new Ride(
                "passenger-1",
                null,
                null,
                null,
                10.0
        );

        ride.setId("ride-1");

        when(rideRepository.findById("ride-1"))
                .thenReturn(Optional.of(ride));

        UpdateRideStatusRequest request =
                new UpdateRideStatusRequest();

        request.setStatus(RideStatus.CANCELLED);

        assertThatThrownBy(() ->
                rideService.updateStatus("ride-1", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cancellation reason is required");
    }

    private CreateRideRequest request() {
        CreateRideRequest request = new CreateRideRequest();

        request.setPickup(
                location("Colombo", 6.9271, 79.8612));

        request.setDestination(
                location("Kandy", 7.2906, 80.6337));

        return request;
    }

    private AccountResponse riderAccount(String id) {
        AccountResponse account = new AccountResponse();

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