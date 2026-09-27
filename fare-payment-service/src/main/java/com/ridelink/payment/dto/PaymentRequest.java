package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.SimulationOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Simulation outcome is required")
    private SimulationOutcome simulationOutcome;

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public SimulationOutcome getSimulationOutcome() {
        return simulationOutcome;
    }

    public void setSimulationOutcome(SimulationOutcome simulationOutcome) {
        this.simulationOutcome = simulationOutcome;
    }
}