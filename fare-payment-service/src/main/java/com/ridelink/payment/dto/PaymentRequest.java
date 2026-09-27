package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.SimulationOutcome;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = """
                Request used to record or retry a simulated payment.
                The client does not provide the amount or currency.
                The server obtains them from the saved FinalFare.
                No real card or banking information is collected.
                """
)
public class PaymentRequest {

    @Schema(
            description = "Unique ride identifier. A FinalFare must already exist for this ride.",
            example = "RIDE001"
    )
    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @Schema(
            description = "Simulated payment method. No real payment credentials are required.",
            example = "CARD"
    )
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(
            description = "Requested simulated payment result. Supported outcomes are SUCCESS and FAILED.",
            example = "SUCCESS"
    )
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