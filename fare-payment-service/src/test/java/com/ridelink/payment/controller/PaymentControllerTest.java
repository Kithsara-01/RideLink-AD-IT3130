package com.ridelink.payment.controller;

import com.ridelink.payment.config.SecurityConfig;
import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.FinalFareNotFoundException;
import com.ridelink.payment.exception.InvalidPaymentStateException;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.service.PaymentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRecordSuccessfulPayment() throws Exception {

        PaymentResponse response = createSuccessfulPayment();

        when(paymentService.recordPayment(any(PaymentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": "RIDE001",
                                  "paymentMethod": "CARD",
                                  "simulationOutcome": "SUCCESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY001"))
                .andExpect(jsonPath("$.rideId").value("RIDE001"))
                .andExpect(jsonPath("$.amount").value(910.00))
                .andExpect(jsonPath("$.currency").value("LKR"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldReturn400ForInvalidPaymentRequest() throws Exception {

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": "",
                                  "paymentMethod": null,
                                  "simulationOutcome": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenFinalFareDoesNotExist() throws Exception {

        when(paymentService.recordPayment(any(PaymentRequest.class)))
                .thenThrow(
                        new FinalFareNotFoundException(
                                "Final fare not found for ride: RIDE999"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": "RIDE999",
                                  "paymentMethod": "CARD",
                                  "simulationOutcome": "SUCCESS"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRetrievePaymentByPaymentId() throws Exception {

        when(paymentService.getPaymentById("PAY001"))
                .thenReturn(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/PAY001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY001"))
                .andExpect(jsonPath("$.rideId").value("RIDE001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldRetrievePaymentByRideId() throws Exception {

        when(paymentService.getPaymentByRideId("RIDE001"))
                .thenReturn(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/rides/RIDE001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value("RIDE001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldReturn404WhenPaymentDoesNotExist() throws Exception {

        when(paymentService.getPaymentById("MISSING"))
                .thenThrow(
                        new PaymentNotFoundException(
                                "Payment not found: MISSING"));

        mockMvc.perform(get("/api/payments/MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRetrieveReceiptByPaymentId() throws Exception {

        ReceiptResponse receipt = createReceipt();

        when(paymentService.getReceiptByPaymentId("PAY001"))
                .thenReturn(receipt);

        mockMvc.perform(get("/api/payments/PAY001/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").value("RCT-001"))
                .andExpect(jsonPath("$.paymentId").value("PAY001"))
                .andExpect(jsonPath("$.amountPaid").value(910.00));
    }

    @Test
    void shouldReturn409WhenFailedPaymentHasNoReceipt() throws Exception {

        when(paymentService.getReceiptByPaymentId("PAY002"))
                .thenThrow(
                        new InvalidPaymentStateException(
                                "Receipt is not available for a failed payment"));

        mockMvc.perform(get("/api/payments/PAY002/receipt"))
                .andExpect(status().isConflict());
    }

    private PaymentResponse createSuccessfulPayment() {

        PaymentResponse response = new PaymentResponse();

        response.setPaymentId("PAY001");
        response.setRideId("RIDE001");
        response.setFinalFareId("FARE001");
        response.setAmount(new BigDecimal("910.00"));
        response.setCurrency("LKR");
        response.setPaymentMethod(PaymentMethod.CARD);
        response.setStatus(PaymentStatus.SUCCESS);
        response.setAttemptCount(1);
        response.setCreatedAt(
                Instant.parse("2026-09-28T01:30:00Z"));
        response.setUpdatedAt(
                Instant.parse("2026-09-28T01:30:00Z"));
        response.setPaidAt(
                Instant.parse("2026-09-28T01:30:00Z"));

        return response;
    }

    private ReceiptResponse createReceipt() {

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

        return response;
    }
}