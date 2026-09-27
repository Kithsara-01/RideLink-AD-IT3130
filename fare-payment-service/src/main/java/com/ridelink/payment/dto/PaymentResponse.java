package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Stored simulated payment information and current payment status")
public class PaymentResponse {

    @Schema(description = "Unique payment identifier", example = "6ab979aec32308606a547091")
    private String paymentId;

    @Schema(description = "Ride associated with this payment", example = "RIDE001")
    private String rideId;

    @Schema(description = "Identifier of the saved final fare used for this payment")
    private String finalFareId;

    @Schema(
            description = "Payment amount obtained from the saved FinalFare, not from the client",
            example = "910.00"
    )
    private BigDecimal amount;

    @Schema(description = "Payment currency obtained from the saved FinalFare", example = "LKR")
    private String currency;

    @Schema(description = "Simulated payment method", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Current simulated payment status", example = "SUCCESS")
    private PaymentStatus status;

    @Schema(
            description = "Number of simulated payment attempts. Failed payments may be retried.",
            example = "1"
    )
    private int attemptCount;

    @Schema(
            description = "Time when the payment record was first created",
            example = "2026-09-28T01:30:00Z"
    )
    private Instant createdAt;

    @Schema(
            description = "Time when the payment record was last updated",
            example = "2026-09-28T01:30:00Z"
    )
    private Instant updatedAt;

    @Schema(
            description = "Time when payment became successful. Null while payment is failed.",
            example = "2026-09-28T01:30:00Z",
            nullable = true
    )
    private Instant paidAt;

    public PaymentResponse() {
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getFinalFareId() {
        return finalFareId;
    }

    public void setFinalFareId(String finalFareId) {
        this.finalFareId = finalFareId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }
}