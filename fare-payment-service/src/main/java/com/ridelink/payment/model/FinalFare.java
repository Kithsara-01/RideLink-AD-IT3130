package com.ridelink.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "final_fares")
public class FinalFare {

    @Id
    private String id;

    @Indexed(unique = true)
    private String rideId;

    private Double actualDistanceKm;
    private Integer actualDurationMinutes;

    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal durationCharge;
    private BigDecimal totalFare;

    private String currency;

    private BigDecimal firstKmFare;
    private BigDecimal additionalKmRate;
    private BigDecimal perMinuteRate;

    private Instant finalizedAt;

    public FinalFare() {
    }

    public FinalFare(
            String rideId,
            Double actualDistanceKm,
            Integer actualDurationMinutes,
            BigDecimal baseFare,
            BigDecimal distanceCharge,
            BigDecimal durationCharge,
            BigDecimal totalFare,
            String currency,
            BigDecimal firstKmFare,
            BigDecimal additionalKmRate,
            BigDecimal perMinuteRate,
            Instant finalizedAt) {

        this.rideId = rideId;
        this.actualDistanceKm = actualDistanceKm;
        this.actualDurationMinutes = actualDurationMinutes;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.durationCharge = durationCharge;
        this.totalFare = totalFare;
        this.currency = currency;
        this.firstKmFare = firstKmFare;
        this.additionalKmRate = additionalKmRate;
        this.perMinuteRate = perMinuteRate;
        this.finalizedAt = finalizedAt;
    }

    public String getId() {
        return id;
    }

    public String getRideId() {
        return rideId;
    }

    public Double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public BigDecimal getDistanceCharge() {
        return distanceCharge;
    }

    public BigDecimal getDurationCharge() {
        return durationCharge;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getFirstKmFare() {
        return firstKmFare;
    }

    public BigDecimal getAdditionalKmRate() {
        return additionalKmRate;
    }

    public BigDecimal getPerMinuteRate() {
        return perMinuteRate;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }
}