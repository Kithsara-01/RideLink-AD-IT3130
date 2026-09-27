package com.ridelink.payment.controller;

import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
@Tag(
        name = "Receipt Management",
        description = "Retrieval of permanently stored payment receipt snapshots"
)
public class ReceiptController {

    private final PaymentService paymentService;

    public ReceiptController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Retrieve receipt by receipt number",
            description = """
                    Retrieves a permanently stored receipt using its unique receipt number.

                    A receipt is generated only when a simulated payment becomes SUCCESS.

                    The receipt is stored as a snapshot with the payment. Its fare values
                    are not recalculated using current fare rates, so the same successful
                    payment always keeps the same receipt information and receipt number.
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
                    description = "Receipt number not found"
            )
    })
    @GetMapping("/{receiptNumber}")
    public ResponseEntity<ReceiptResponse> getReceiptByReceiptNumber(
            @Parameter(
                    description = "Unique receipt number generated for a successful payment",
                    example = "RCT-856B635B-F8F1-49CE-9876-53854D5CE503",
                    required = true
            )
            @PathVariable String receiptNumber) {

        return ResponseEntity.ok(
                paymentService.getReceiptByReceiptNumber(receiptNumber));
    }
}