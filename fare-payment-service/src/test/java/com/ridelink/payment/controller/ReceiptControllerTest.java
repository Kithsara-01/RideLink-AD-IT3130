package com.ridelink.payment.controller;

import com.ridelink.payment.config.InternalServiceAuthenticationFilter;
import com.ridelink.payment.config.SecurityConfig;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.ReceiptNotFoundException;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.service.PaymentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiptController.class)
@Import({
        SecurityConfig.class,
        InternalServiceAuthenticationFilter.class
})
class ReceiptControllerTest {

    private static final String AUTHORIZATION_HEADER =
            "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRetrieveReceiptByReceiptNumber()
            throws Exception {

        mockJwt("USER001", "ADMIN");

        when(paymentService.getReceiptByReceiptNumber(
                eq("RCT-001"),
                any(Authentication.class),
                eq(AUTHORIZATION_HEADER)))
                .thenReturn(createReceipt());

        mockMvc.perform(
                        get("/api/receipts/RCT-001")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        AUTHORIZATION_HEADER))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.receiptNumber")
                                .value("RCT-001"))
                .andExpect(
                        jsonPath("$.paymentId")
                                .value("PAY001"))
                .andExpect(
                        jsonPath("$.rideId")
                                .value("RIDE001"))
                .andExpect(
                        jsonPath("$.amountPaid")
                                .value(910.00));
    }

    @Test
    void shouldReturn404WhenReceiptDoesNotExist()
            throws Exception {

        mockJwt("USER001", "ADMIN");

        when(paymentService.getReceiptByReceiptNumber(
                eq("MISSING"),
                any(Authentication.class),
                eq(AUTHORIZATION_HEADER)))
                .thenThrow(
                        new ReceiptNotFoundException(
                                "Receipt not found: MISSING"));

        mockMvc.perform(
                        get("/api/receipts/MISSING")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        AUTHORIZATION_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    void driverShouldNotAccessReceipts()
            throws Exception {

        mockJwt("DRIVER001", "DRIVER");

        mockMvc.perform(
                        get("/api/receipts/RCT-001")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        AUTHORIZATION_HEADER))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingTokenShouldReturn401()
            throws Exception {

        mockMvc.perform(
                        get("/api/receipts/RCT-001"))
                .andExpect(status().isUnauthorized());
    }

    private void mockJwt(
            String subject,
            String role) {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(
                        Instant.parse(
                                "2026-09-29T00:00:00Z"))
                .expiresAt(
                        Instant.parse(
                                "2026-09-30T00:00:00Z"))
                .claim("role", role)
                .build();

        when(jwtDecoder.decode("test-token"))
                .thenReturn(jwt);
    }

    private ReceiptResponse createReceipt() {

        ReceiptResponse response =
                new ReceiptResponse();

        response.setReceiptNumber("RCT-001");
        response.setPaymentId("PAY001");
        response.setRideId("RIDE001");

        response.setAmountPaid(
                new BigDecimal("910.00"));

        response.setCurrency("LKR");

        response.setPaymentMethod(
                PaymentMethod.CARD);

        response.setActualDistanceKm(8.5);
        response.setActualDurationMinutes(25);

        response.setBaseFare(
                new BigDecimal("110.00"));

        response.setDistanceCharge(
                new BigDecimal("675.00"));

        response.setDurationCharge(
                new BigDecimal("125.00"));

        response.setIssuedAt(
                Instant.parse(
                        "2026-09-28T01:30:00Z"));

        return response;
    }
}