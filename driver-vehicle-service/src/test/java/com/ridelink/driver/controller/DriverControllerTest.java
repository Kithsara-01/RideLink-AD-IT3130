package com.ridelink.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.config.SecurityConfig;
import com.ridelink.driver.dto.AvailableDriverResponse;
import com.ridelink.driver.dto.CreateDriverRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.dto.LocationDto;
import com.ridelink.driver.dto.UpdateAvailabilityRequest;
import com.ridelink.driver.dto.VehicleDto;
import com.ridelink.driver.entity.AvailabilityStatus;
import com.ridelink.driver.entity.Location;
import com.ridelink.driver.entity.OperationalStatus;
import com.ridelink.driver.entity.Vehicle;
import com.ridelink.driver.entity.VehicleType;
import com.ridelink.driver.service.DriverService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DriverService driverService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private DriverResponse sampleResponse;

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
                "Fort Colombo",
                Instant.now()
        );

        sampleResponse = new DriverResponse();
        sampleResponse.setId("drv-123");
        sampleResponse.setUserId("usr-456");
        sampleResponse.setDriverLicenseNumber("DL-12345");
        sampleResponse.setPhoneNumber("+94771234567");
        sampleResponse.setOperationalStatus(OperationalStatus.ACTIVE);
        sampleResponse.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        sampleResponse.setServiceArea("Colombo");
        sampleResponse.setVehicle(vehicle);
        sampleResponse.setCurrentLocation(location);
        sampleResponse.setRating(5.0);
        sampleResponse.setTotalRidesCompleted(0);
    }

    @Test
    @DisplayName("POST /api/drivers - should return 201 Created on valid request")
    void testRegisterDriver_Valid() throws Exception {

        VehicleDto vDto = new VehicleDto(
                "Toyota",
                "Prius",
                2021,
                "WP-CAB-1234",
                "White",
                VehicleType.SEDAN,
                4
        );

        LocationDto lDto = new LocationDto(
                6.9271,
                79.8612,
                "Fort Colombo"
        );

        CreateDriverRequest request = new CreateDriverRequest(
                "usr-456",
                "DL-12345",
                "+94771234567",
                "Colombo",
                vDto,
                lDto
        );

        when(driverService.registerDriver(any(CreateDriverRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(
                        post("/api/drivers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("drv-123"))
                .andExpect(jsonPath("$.userId").value("usr-456"))
                .andExpect(jsonPath("$.operationalStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/drivers - should return 400 Bad Request on invalid coordinates")
    void testRegisterDriver_InvalidCoordinates() throws Exception {

        VehicleDto vDto = new VehicleDto(
                "Toyota",
                "Prius",
                2021,
                "WP-CAB-1234",
                "White",
                VehicleType.SEDAN,
                4
        );

        LocationDto lDto = new LocationDto(
                120.0,
                79.8612,
                "Invalid Location"
        );

        CreateDriverRequest request = new CreateDriverRequest(
                "usr-456",
                "DL-12345",
                "+94771234567",
                "Colombo",
                vDto,
                lDto
        );

        mockMvc.perform(
                        post("/api/drivers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors['initialLocation.latitude']").exists());
    }

    @Test
    @DisplayName("GET /api/drivers/{id} - should return 200 OK")
    void testGetDriverById_Success() throws Exception {

        when(driverService.getDriverById("drv-123"))
                .thenReturn(sampleResponse);

        mockMvc.perform(
                        get("/api/drivers/drv-123")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("drv-123"))
                .andExpect(jsonPath("$.driverLicenseNumber").value("DL-12345"));
    }

    @Test
    @DisplayName("PATCH /api/drivers/{id}/availability - should return 200 OK")
    void testUpdateAvailability_Success() throws Exception {

        sampleResponse.setAvailabilityStatus(
                AvailabilityStatus.AVAILABLE
        );

        when(
                driverService.updateAvailability(
                        eq("drv-123"),
                        any(UpdateAvailabilityRequest.class)
                )
        ).thenReturn(sampleResponse);

        UpdateAvailabilityRequest request =
                new UpdateAvailabilityRequest(
                        AvailabilityStatus.AVAILABLE
                );

        mockMvc.perform(
                        patch("/api/drivers/drv-123/availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.availabilityStatus")
                                .value("AVAILABLE")
                );
    }

    @Test
    @DisplayName("PATCH /api/drivers/{id}/assign - should return 200 OK")
    void testAssignDriver_Success() throws Exception {

        sampleResponse.setAvailabilityStatus(
                AvailabilityStatus.BUSY
        );

        when(driverService.assignDriver("drv-123"))
                .thenReturn(sampleResponse);

        mockMvc.perform(
                        patch("/api/drivers/drv-123/assign")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.availabilityStatus")
                                .value("BUSY")
                );
    }

    @Test
    @DisplayName("PATCH /api/drivers/{id}/release - should return 200 OK")
    void testReleaseDriver_Success() throws Exception {

        sampleResponse.setAvailabilityStatus(
                AvailabilityStatus.AVAILABLE
        );

        sampleResponse.setTotalRidesCompleted(1);

        when(driverService.releaseDriver("drv-123"))
                .thenReturn(sampleResponse);

        mockMvc.perform(
                        patch("/api/drivers/drv-123/release")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.availabilityStatus")
                                .value("AVAILABLE")
                )
                .andExpect(
                        jsonPath("$.totalRidesCompleted")
                                .value(1)
                );
    }

    @Test
    @DisplayName("GET /api/drivers/available - should return 200 OK with candidates")
    void testFindAvailableDrivers_Success() throws Exception {

        AvailableDriverResponse candidate =
                new AvailableDriverResponse();

        candidate.setId("drv-123");
        candidate.setDistanceKm(1.5);
        candidate.setServiceArea("Colombo");

        when(
                driverService.findAvailableDrivers(
                        eq("Colombo"),
                        any(),
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(List.of(candidate));

        mockMvc.perform(
                        get("/api/drivers/available")
                                .param("serviceArea", "Colombo")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value("drv-123")
                )
                .andExpect(
                        jsonPath("$[0].distanceKm")
                                .value(1.5)
                );
    }
}