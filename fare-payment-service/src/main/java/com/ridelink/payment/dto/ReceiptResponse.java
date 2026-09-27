package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public class ReceiptResponse {

    private String receiptNumber;
    private String paymentId;
    private String rideId;
    private BigDecimal amountPaid;
    private String currency;
    private PaymentMethod paymentMethod;
    private Double actualDistanceKm;
    private Integer actualDurationMinutes;
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal durationCharge;
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