package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.recordPayment(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/rides/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRideId(
            @PathVariable String rideId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByRideId(rideId));
    }
}