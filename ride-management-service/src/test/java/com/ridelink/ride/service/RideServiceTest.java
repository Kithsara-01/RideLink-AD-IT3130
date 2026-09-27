package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverClient;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverClient driverClient;

    @InjectMocks
    private RideService rideService;

    @Test
    void createRideCalculatesDistanceBasedFareAndStartsRequested() {
        CreateRideRequest request = request();
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.createRide(request);

        assertThat(response.getStatus()).isEqualTo(RideStatus.REQUESTED);
        assertThat(response.getEstimatedFare()).isGreaterThan(2.50);
        assertThat(response.getPassengerId()).isEqualTo("passenger-1");
    }

    @Test
    void assignRideSelectsDriverAndMovesRideToAssigned() {
        Ride ride = new Ride("passenger-1",
            new RideLocation("Colombo", 6.9271, 79.8612),
            new RideLocation("Kandy", 7.2906, 80.6337), null, 10.0);
        ride.setId("ride-1");
        AvailableDriverResponse driver = new AvailableDriverResponse();
        driver.setId("driver-1");
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.findAvailableDrivers(any(), any(), any(), any())).thenReturn(List.of(driver));
        when(rideRepository.save(ride)).thenReturn(ride);
        doNothing().when(driverClient).assignDriver("driver-1");

        var response = rideService.assignRide("ride-1");

        assertThat(response.getStatus()).isEqualTo(RideStatus.ASSIGNED);
        assertThat(response.getDriverId()).isEqualTo("driver-1");
        verify(driverClient).assignDriver("driver-1");
    }

    @Test
    void invalidTransitionIsRejected() {
        Ride ride = new Ride("passenger-1", null, null, null, 10.0);
        ride.setId("ride-1");
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        UpdateRideStatusRequest request = new UpdateRideStatusRequest();
        request.setStatus(RideStatus.COMPLETED);

        assertThatThrownBy(() -> rideService.updateStatus("ride-1", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid ride status transition");
    }

    @Test
    void cancellationRequiresReason() {
        Ride ride = new Ride("passenger-1", null, null, null, 10.0);
        ride.setId("ride-1");
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        UpdateRideStatusRequest request = new UpdateRideStatusRequest();
        request.setStatus(RideStatus.CANCELLED);

        assertThatThrownBy(() -> rideService.updateStatus("ride-1", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cancellation reason is required");
    }

    private CreateRideRequest request() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPassengerId(" passenger-1 ");
        request.setPickup(location("Colombo", 6.9271, 79.8612));
        request.setDestination(location("Kandy", 7.2906, 80.6337));
        return request;
    }

    private RideLocationRequest location(String place, double latitude, double longitude) {
        RideLocationRequest location = new RideLocationRequest();
        location.setPlace(place);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}
