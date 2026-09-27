package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(
        description = """
                Permanent receipt snapshot generated when a simulated payment
                becomes successful. Receipt values are stored permanently and
                are not recalculated using later fare rates.
                """
)
public class ReceiptResponse {

    @Schema(
            description = "Unique and permanent receipt number",
            example = "RCT-856B635B-F8F1-49CE-9876-53854D5CE503"
    )
    private String receiptNumber;

    @Schema(
            description = "Unique identifier of the successful payment",
            example = "6ab979aec32308606a547091"
    )
    private String paymentId;

    @Schema(
            description = "Ride associated with this receipt",
            example = "RIDE001"
    )
    private String rideId;

    @Schema(
            description = "Amount paid, copied from the saved final fare when the payment succeeds",
            example = "910.00"
    )
    private BigDecimal amountPaid;

    @Schema(
            description = "Currency of the payment",
            example = "LKR"
    )
    private String currency;

    @Schema(
            description = "Simulated payment method used for the successful payment",
            example = "CARD"
    )
    private PaymentMethod paymentMethod;

    @Schema(
            description = "Actual distance travelled by the ride in kilometres",
            example = "8.5"
    )
    private Double actualDistanceKm;

    @Schema(
            description = "Actual ride duration in minutes",
            example = "25"
    )
    private Integer actualDurationMinutes;

    @Schema(
            description = "Stored base fare for the first kilometre",
            example = "110.00"
    )
    private BigDecimal baseFare;

    @Schema(
            description = "Stored charge for distance after the first kilometre",
            example = "675.00"
    )
    private BigDecimal distanceCharge;

    @Schema(
            description = "Stored charge based on ride duration",
            example = "125.00"
    )
    private BigDecimal durationCharge;

    @Schema(
            description = "Time when this permanent receipt snapshot was generated",
            example = "2026-09-28T01:30:00Z"
    )
    private Instant issuedAt;

    public ReceiptResponse() {
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
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

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
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

    public Double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public void setActualDistanceKm(Double actualDistanceKm) {
        this.actualDistanceKm = actualDistanceKm;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(BigDecimal distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public BigDecimal getDurationCharge() {
        return durationCharge;
    }

    public void setDurationCharge(BigDecimal durationCharge) {
        this.durationCharge = durationCharge;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}