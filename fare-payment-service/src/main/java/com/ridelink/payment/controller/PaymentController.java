package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader) {

        PaymentResponse response =
                paymentService.recordPayment(
                        request,
                        authentication,
                        authorizationHeader);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable String paymentId,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader) {

        PaymentResponse response =
                paymentService.getPaymentById(
                        paymentId,
                        authentication,
                        authorizationHeader);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rides/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRideId(
            @PathVariable String rideId,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader) {

        PaymentResponse response =
                paymentService.getPaymentByRideId(
                        rideId,
                        authentication,
                        authorizationHeader);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<ReceiptResponse> getReceiptByPaymentId(
            @PathVariable String paymentId,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader) {

        ReceiptResponse response =
                paymentService.getReceiptByPaymentId(
                        paymentId,
                        authentication,
                        authorizationHeader);

        return ResponseEntity.ok(response);
    }
}