package com.ridelink.payment.controller;

import com.ridelink.payment.config.SecurityConfig;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.ReceiptNotFoundException;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.service.PaymentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceiptController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRetrieveReceiptByReceiptNumber() throws Exception {

        ReceiptResponse response = new ReceiptResponse();

        response.setReceiptNumber("RCT-001");
        response.setPaymentId("PAY001");
        response.setRideId("RIDE001");
        response.setAmountPaid(new BigDecimal("910.00"));
        response.setCurrency("LKR");
        response.setPaymentMethod(PaymentMethod.CARD);
        response.setActualDistanceKm(8.5);
        response.setActualDurationMinutes(25);
        response.setBaseFare(new BigDecimal("110.00"));
        response.setDistanceCharge(new BigDecimal("675.00"));
        response.setDurationCharge(new BigDecimal("125.00"));
        response.setIssuedAt(
                Instant.parse("2026-09-28T01:30:00Z"));

        when(paymentService.getReceiptByReceiptNumber("RCT-001"))
                .thenReturn(response);

        mockMvc.perform(get("/api/receipts/RCT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").value("RCT-001"))
                .andExpect(jsonPath("$.paymentId").value("PAY001"))
                .andExpect(jsonPath("$.rideId").value("RIDE001"))
                .andExpect(jsonPath("$.amountPaid").value(910.00));
    }

    @Test
    void shouldReturn404WhenReceiptNumberDoesNotExist() throws Exception {

        when(paymentService.getReceiptByReceiptNumber("RCT-MISSING"))
                .thenThrow(
                        new ReceiptNotFoundException(
                                "Receipt not found: RCT-MISSING"));

        mockMvc.perform(get("/api/receipts/RCT-MISSING"))
                .andExpect(status().isNotFound());
    }
}