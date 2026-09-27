package com.ridelink.payment.controller;

import com.ridelink.payment.config.SecurityConfig;
import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.exception.FinalFareAlreadyExistsException;
import com.ridelink.payment.exception.FinalFareNotFoundException;
import com.ridelink.payment.service.FareCalculationService;
import com.ridelink.payment.service.FinalFareService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FareController.class)
@Import(SecurityConfig.class)
class FareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FareCalculationService fareCalculationService;

    @MockitoBean
    private FinalFareService finalFareService;

    @Test
    void shouldEstimateFare() throws Exception {

        FareEstimateResponse response =
                new FareEstimateResponse(
                        "SLIIT Malabe",
                        "Kaduwela",
                        8.5,
                        25,
                        new BigDecimal("110.00"),
                        new BigDecimal("675.00"),
                        new BigDecimal("125.00"),
                        new BigDecimal("910.00"),
                        "LKR"
                );

        when(fareCalculationService.calculateEstimate(
                any(FareEstimateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pickup": "SLIIT Malabe",
                                  "destination": "Kaduwela",
                                  "distanceKm": 8.5,
                                  "durationMinutes": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value(910.00))
                .andExpect(jsonPath("$.currency").value("LKR"));
    }

    @Test
    void shouldReturn400ForInvalidFareEstimate() throws Exception {

        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pickup": "",
                                  "destination": "Kaduwela",
                                  "distanceKm": 0,
                                  "durationMinutes": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFinalizeFare() throws Exception {

        FinalFareResponse response =
                new FinalFareResponse(
                        "RIDE001",
                        8.5,
                        25,
                        new BigDecimal("110.00"),
                        new BigDecimal("675.00"),
                        new BigDecimal("125.00"),
                        new BigDecimal("910.00"),
                        "LKR",
                        new BigDecimal("110.00"),
                        new BigDecimal("90.00"),
                        new BigDecimal("5.00"),
                        Instant.parse("2026-09-28T01:30:00Z")
                );

        when(finalFareService.finalizeFare(
                eq("RIDE001"),
                any(FinalFareRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/fares/rides/RIDE001/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actualDistanceKm": 8.5,
                                  "actualDurationMinutes": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value("RIDE001"))
                .andExpect(jsonPath("$.totalFare").value(910.00));
    }

    @Test
    void shouldReturn409WhenFinalFareAlreadyExists() throws Exception {

        when(finalFareService.finalizeFare(
                eq("RIDE001"),
                any(FinalFareRequest.class)))
                .thenThrow(
                        new FinalFareAlreadyExistsException(
                                "Final fare already exists for ride: RIDE001"));

        mockMvc.perform(post("/api/fares/rides/RIDE001/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actualDistanceKm": 8.5,
                                  "actualDurationMinutes": 25
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenFinalFareDoesNotExist() throws Exception {

        when(finalFareService.getFinalFare("RIDE999"))
                .thenThrow(
                        new FinalFareNotFoundException(
                                "Final fare not found for ride: RIDE999"));

        mockMvc.perform(get("/api/fares/rides/RIDE999"))
                .andExpect(status().isNotFound());
    }
}