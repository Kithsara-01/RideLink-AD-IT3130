package com.ridelink.payment.controller;

import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.service.PaymentService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final PaymentService paymentService;

    public ReceiptController(
            PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{receiptNumber}")
    public ResponseEntity<ReceiptResponse> getReceiptByReceiptNumber(
            @PathVariable String receiptNumber,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader) {

        ReceiptResponse response =
                paymentService.getReceiptByReceiptNumber(
                        receiptNumber,
                        authentication,
                        authorizationHeader);

        return ResponseEntity.ok(response);
    }
}