package com.ridelink.payment.controller;

import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final PaymentService paymentService;

    public ReceiptController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{receiptNumber}")
    public ResponseEntity<ReceiptResponse> getReceiptByReceiptNumber(
            @PathVariable String receiptNumber) {

        return ResponseEntity.ok(
                paymentService.getReceiptByReceiptNumber(receiptNumber));
    }
}