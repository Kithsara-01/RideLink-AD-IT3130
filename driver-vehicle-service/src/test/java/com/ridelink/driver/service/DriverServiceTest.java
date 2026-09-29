package com.ridelink.driver.service;

import com.ridelink.driver.client.AccountClient;
import com.ridelink.driver.dto.AccountResponse;
import com.ridelink.driver.dto.AvailableDriverResponse;
import com.ridelink.driver.dto.CreateDriverRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.dto.LocationDto;
import com.ridelink.driver.dto.UpdateAvailabilityRequest;
import com.ridelink.driver.dto.UpdateLocationRequest;
import com.ridelink.driver.dto.VehicleDto;
import com.ridelink.driver.exception.ApiException;
import com.ridelink.driver.entity.AvailabilityStatus;
import com.ridelink.driver.entity.Driver;
import com.ridelink.driver.entity.Location;
import com.ridelink.driver.entity.OperationalStatus;
import com.ridelink.driver.entity.Vehicle;
import com.ridelink.driver.entity.VehicleType;
import com.ridelink.driver.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    private static final String TEST_TOKEN = "test-driver-token";

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private DriverService driverService;

    private Driver sampleDriver;
    private CreateDriverRequest createRequest;
    private AccountResponse driverAccount;

    @BeforeEach
    void setUp() {

        Vehicle vehicle = new Vehicle(
                "Toyota",
                "Prius",
                2021,
                "WP-CAB-1234",
                "White",
                VehicleType.SEDAN,
                4
        );

        Location location = new Location(
                6.9271,
                79.8612,
                "Colombo Fort"
        );

        sampleDriver = new Driver(
                "user-101",
                "DL-12345",
                "+94771234567",
                "Colombo",
                vehicle,
                location
        );

        sampleDriver.setId("driver-001");
        sampleDriver.setOperationalStatus(OperationalStatus.ACTIVE);
        sampleDriver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);

        VehicleDto vehicleDto = new VehicleDto(
                "Toyota",
                "Prius",
                2021,
                "WP-CAB-1234",
                "White",
                VehicleType.SEDAN,
                4
        );

        LocationDto locationDto = new LocationDto(
                6.9271,
                79.8612,
                "Colombo Fort"
        );

        createRequest = new CreateDriverRequest(
                "DL-12345",
                "+94771234567",
                "Colombo",
                vehicleDto,
                locationDto
        );

        driverAccount = new AccountResponse();
        driverAccount.setId("user-101");
        driverAccount.setRole("DRIVER");
        driverAccount.setActive(true);
    }

    @Test
    @DisplayName("Active DRIVER account should successfully register a driver profile")
    void testRegisterDriver_Success() {

        when(accountClient.getCurrentAccount(TEST_TOKEN))
                .thenReturn(driverAccount);

        when(driverRepository.existsByUserId("user-101"))
                .thenReturn(false);

        when(driverRepository.existsByDriverLicenseNumber("DL-12345"))
                .thenReturn(false);

        when(driverRepository.existsByVehicleLicensePlate("WP-CAB-1234"))
                .thenReturn(false);

        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation -> {
                    Driver driver = invocation.getArgument(0);
                    driver.setId("generated-id-1");
                    return driver;
                });

        DriverResponse response =
                driverService.registerDriver(createRequest, TEST_TOKEN);

        assertNotNull(response);
        assertEquals("generated-id-1", response.getId());
        assertEquals("user-101", response.getUserId());
        assertEquals("DL-12345", response.getDriverLicenseNumber());
        assertEquals(
                OperationalStatus.ACTIVE,
                response.getOperationalStatus()
        );
        assertEquals(
                AvailabilityStatus.OFFLINE,
                response.getAvailabilityStatus()
        );

        verify(accountClient, times(1))
                .getCurrentAccount(TEST_TOKEN);

        verify(driverRepository, times(1))
                .save(any(Driver.class));
    }

    @Test
    @DisplayName("Saved driver userId should come from Account Service")
    void testRegisterDriver_UsesAccountServiceUserId() {

        driverAccount.setId("account-driver-555");

        when(accountClient.getCurrentAccount(TEST_TOKEN))
                .thenReturn(driverAccount);

        when(driverRepository.existsByUserId("account-driver-555"))
                .thenReturn(false);

        when(driverRepository.existsByDriverLicenseNumber("DL-12345"))
                .thenReturn(false);

        when(driverRepository.existsByVehicleLicensePlate("WP-CAB-1234"))
                .thenReturn(false);

        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation -> {
                    Driver driver = invocation.getArgument(0);
                    driver.setId("driver-555");
                    return driver;
                });

        driverService.registerDriver(createRequest, TEST_TOKEN);

        ArgumentCaptor<Driver> driverCaptor =
                ArgumentCaptor.forClass(Driver.class);

        verify(driverRepository).save(driverCaptor.capture());

        Driver savedDriver = driverCaptor.getValue();

        assertEquals(
                "account-driver-555",
                savedDriver.getUserId()
        );
    }

    @Test
    @DisplayName("RIDER account should not create a driver profile")
    void testRegisterDriver_RiderAccountForbidden() {

        AccountResponse riderAccount = new AccountResponse();
        riderAccount.setId("rider-101");
        riderAccount.setRole("RIDER");
        riderAccount.setActive(true);

        when(accountClient.getCurrentAccount(TEST_TOKEN))
                .thenReturn(riderAccount);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.registerDriver(
                        createRequest,
                        TEST_TOKEN
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("Only DRIVER accounts")
        );

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    @DisplayName("Inactive account should not create a driver profile")
    void testRegisterDriver_InactiveAccountForbidden() {

        AccountResponse inactiveAccount = new AccountResponse();
        inactiveAccount.setId("user-101");
        inactiveAccount.setRole("DRIVER");
        inactiveAccount.setActive(false);

        when(accountClient.getCurrentAccount(TEST_TOKEN))
                .thenReturn(inactiveAccount);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.registerDriver(
                        createRequest,
                        TEST_TOKEN
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("Inactive account")
        );

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    @DisplayName("Missing Account Service user ID should be rejected")
    void testRegisterDriver_MissingAccountId() {

        AccountResponse invalidAccount = new AccountResponse();
        invalidAccount.setId(" ");
        invalidAccount.setRole("DRIVER");
        invalidAccount.setActive(true);

        when(accountClient.getCurrentAccount(TEST_TOKEN))
                .thenReturn(invalidAccount);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.registerDriver(
                        createRequest,
                        TEST_TOKEN
                )
        );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("invalid account ID")
        );

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    @DisplayName("Duplicate driver profile for authenticated account should be rejected")
    void testRegisterDriver_DuplicateUserId() {

        when(accountClient.getCurrentAccount(TEST_TOKEN))
                .thenReturn(driverAccount);

        when(driverRepository.existsByUserId("user-101"))
                .thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.registerDriver(
                        createRequest,
                        TEST_TOKEN
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("Driver profile already exists")
        );

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    @DisplayName("Should reject registration when license plate is duplicate")
    void testRegisterDriver_DuplicatePlate() {

        when(accountClient.getCurrentAccount(TEST_TOKEN))
                .thenReturn(driverAccount);

        when(driverRepository.existsByUserId("user-101"))
                .thenReturn(false);

        when(driverRepository.existsByDriverLicenseNumber("DL-12345"))
                .thenReturn(false);

        when(driverRepository.existsByVehicleLicensePlate("WP-CAB-1234"))
                .thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.registerDriver(
                        createRequest,
                        TEST_TOKEN
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("Vehicle license plate")
        );

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    @DisplayName("Should retrieve driver by ID")
    void testGetDriverById_Success() {

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        DriverResponse response =
                driverService.getDriverById("driver-001");

        assertNotNull(response);
        assertEquals("driver-001", response.getId());
    }

    @Test
    @DisplayName("Should throw 404 when driver not found by ID")
    void testGetDriverById_NotFound() {

        when(driverRepository.findById("unknown-id"))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.getDriverById("unknown-id")
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatus()
        );
    }

    @Test
    @DisplayName("Should update driver availability to AVAILABLE")
    void testUpdateAvailability_Success() {

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UpdateAvailabilityRequest request =
                new UpdateAvailabilityRequest(
                        AvailabilityStatus.AVAILABLE
                );

        DriverResponse response =
                driverService.updateAvailability(
                        "driver-001",
                        request
                );

        assertEquals(
                AvailabilityStatus.AVAILABLE,
                response.getAvailabilityStatus()
        );
    }

    @Test
    @DisplayName("Should prevent suspended driver from becoming AVAILABLE")
    void testUpdateAvailability_SuspendedForbidden() {

        sampleDriver.setOperationalStatus(
                OperationalStatus.SUSPENDED
        );

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        UpdateAvailabilityRequest request =
                new UpdateAvailabilityRequest(
                        AvailabilityStatus.AVAILABLE
                );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.updateAvailability(
                        "driver-001",
                        request
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatus()
        );
    }

    @Test
    @DisplayName("Should prevent BUSY driver from going OFFLINE while in a trip")
    void testUpdateAvailability_BusyCannotGoOffline() {

        sampleDriver.setAvailabilityStatus(
                AvailabilityStatus.BUSY
        );

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        UpdateAvailabilityRequest request =
                new UpdateAvailabilityRequest(
                        AvailabilityStatus.OFFLINE
                );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.updateAvailability(
                        "driver-001",
                        request
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatus()
        );
    }

    @Test
    @DisplayName("Should assign an available driver and mark as BUSY")
    void testAssignDriver_Success() {

        sampleDriver.setAvailabilityStatus(
                AvailabilityStatus.AVAILABLE
        );

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        DriverResponse response =
                driverService.assignDriver("driver-001");

        assertEquals(
                AvailabilityStatus.BUSY,
                response.getAvailabilityStatus()
        );
    }

    @Test
    @DisplayName("Should reject assignment if driver is not AVAILABLE")
    void testAssignDriver_NotAvailable() {

        sampleDriver.setAvailabilityStatus(
                AvailabilityStatus.OFFLINE
        );

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> driverService.assignDriver("driver-001")
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatus()
        );
    }

    @Test
    @DisplayName("Should release driver back to AVAILABLE and increment rides count")
    void testReleaseDriver_Success() {

        sampleDriver.setAvailabilityStatus(
                AvailabilityStatus.BUSY
        );

        sampleDriver.setTotalRidesCompleted(5);

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        DriverResponse response =
                driverService.releaseDriver("driver-001");

        assertEquals(
                AvailabilityStatus.AVAILABLE,
                response.getAvailabilityStatus()
        );

        assertEquals(
                6,
                response.getTotalRidesCompleted()
        );
    }

    @Test
    @DisplayName("Should update simulated GPS location")
    void testUpdateLocation_Success() {

        when(driverRepository.findById("driver-001"))
                .thenReturn(Optional.of(sampleDriver));

        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UpdateLocationRequest request =
                new UpdateLocationRequest(
                        6.9350,
                        79.8500,
                        "Pettah",
                        "Colombo-North"
                );

        DriverResponse response =
                driverService.updateLocation(
                        "driver-001",
                        request
                );

        assertEquals(
                6.9350,
                response.getCurrentLocation().getLatitude()
        );

        assertEquals(
                79.8500,
                response.getCurrentLocation().getLongitude()
        );

        assertEquals(
                "Pettah",
                response.getCurrentLocation()
                        .getAddressOrPlace()
        );

        assertEquals(
                "Colombo-North",
                response.getServiceArea()
        );
    }

    @Test
    @DisplayName("Should find available drivers within proximity and sort by distance")
    void testFindAvailableDrivers_WithProximity() {

        sampleDriver.setAvailabilityStatus(
                AvailabilityStatus.AVAILABLE
        );

        Driver farDriver = new Driver(
                "user-102",
                "DL-99999",
                "+94779999999",
                "Colombo",
                new Vehicle(
                        "Honda",
                        "Fit",
                        2020,
                        "WP-CAX-9999",
                        "Black",
                        VehicleType.SEDAN,
                        4
                ),
                new Location(
                        7.2906,
                        80.6337,
                        "Kandy City"
                )
        );

        farDriver.setId("driver-002");
        farDriver.setOperationalStatus(
                OperationalStatus.ACTIVE
        );
        farDriver.setAvailabilityStatus(
                AvailabilityStatus.AVAILABLE
        );

        when(
                driverRepository
                        .findByOperationalStatusAndAvailabilityStatus(
                                OperationalStatus.ACTIVE,
                                AvailabilityStatus.AVAILABLE
                        )
        ).thenReturn(
                List.of(sampleDriver, farDriver)
        );

        List<AvailableDriverResponse> results =
                driverService.findAvailableDrivers(
                        null,
                        VehicleType.SEDAN,
                        6.9270,
                        79.8610,
                        15.0
                );

        assertEquals(1, results.size());

        assertEquals(
                "driver-001",
                results.get(0).getId()
        );

        assertNotNull(
                results.get(0).getDistanceKm()
        );

        assertTrue(
                results.get(0).getDistanceKm() < 1.0
        );
    }

    @Test
    @DisplayName("Negative Scenario: Should return empty list when no drivers match service area")
    void testFindAvailableDrivers_EmptyWhenNoDriversMatch() {

        when(
                driverRepository
                        .findByOperationalStatusAndAvailabilityStatus(
                                OperationalStatus.ACTIVE,
                                AvailabilityStatus.AVAILABLE
                        )
        ).thenReturn(Collections.emptyList());

        List<AvailableDriverResponse> results =
                driverService.findAvailableDrivers(
                        "Galle",
                        null,
                        null,
                        null,
                        null
                );

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should accurately calculate distance via Haversine formula")
    void testCalculateDistanceKm() {

        double distance =
                driverService.calculateDistanceKm(
                        6.9271,
                        79.8612,
                        6.8344,
                        79.8654
                );

        assertEquals(
                10.3,
                distance,
                0.5
        );
    }
}