package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(
        name = "Payment Management",
        description = "Simulated payment recording, payment status retrieval, retry handling, and receipt retrieval"
)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Record or retry a simulated payment",
            description = """
                    Records a simulated payment for a ride using the saved final fare.

                    The client does not provide the payment amount. The server reads the
                    saved FinalFare and uses its stored total fare as the payment amount.

                    Payment processing is simulated for academic purposes. No real payment
                    gateway is used and no card number, CVV, bank account, or banking
                    credentials are collected.

                    If the simulated outcome is SUCCESS, the payment becomes successful
                    and a permanent receipt snapshot is generated.

                    If the simulated outcome is FAILED, the payment is stored as failed
                    without a receipt. A failed payment can later be retried.

                    An already successful payment remains successful and keeps its existing
                    receipt and receipt number.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment recorded, retried, or existing successful payment returned",
                    content = @Content(
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment request"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Final fare not found for the specified ride"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Payment state conflict or duplicate database record"
            )
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.recordPayment(request));
    }

    @Operation(
            summary = "Retrieve payment by payment ID",
            description = """
                    Retrieves the stored payment record and its current payment status
                    using the unique payment ID.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(
                    description = "Unique identifier of the payment",
                    example = "6ab979aec32308606a547091",
                    required = true
            )
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId));
    }

    @Operation(
            summary = "Retrieve payment status by ride ID",
            description = """
                    Retrieves the payment associated with the specified ride.

                    The response includes the current payment status, such as SUCCESS
                    or FAILED, together with the stored payment information.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment status retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found for the specified ride"
            )
    })
    @GetMapping("/rides/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRideId(
            @Parameter(
                    description = "Unique identifier of the ride",
                    example = "RIDE003",
                    required = true
            )
            @PathVariable String rideId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByRideId(rideId));
    }

    @Operation(
            summary = "Retrieve receipt by payment ID",
            description = """
                    Retrieves the permanent receipt snapshot associated with a successful
                    payment.

                    Receipt values are stored when the payment becomes successful and are
                    not recalculated using current fare rates.

                    A failed payment does not have a receipt.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Receipt retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ReceiptResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Receipt is not available because the payment is failed"
            )
    })
    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<ReceiptResponse> getReceiptByPaymentId(
            @Parameter(
                    description = "Unique identifier of the payment",
                    example = "6ab979aec32308606a547091",
                    required = true
            )
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.getReceiptByPaymentId(paymentId));
    }
}