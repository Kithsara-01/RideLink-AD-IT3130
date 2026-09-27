package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.exception.FinalFareNotFoundException;
import com.ridelink.payment.exception.InvalidPaymentStateException;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.FinalFare;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.SimulationOutcome;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FinalFareRepository finalFareRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            FinalFareRepository finalFareRepository) {

        this.paymentRepository = paymentRepository;
        this.finalFareRepository = finalFareRepository;
    }

    public PaymentResponse recordPayment(PaymentRequest request) {

        FinalFare finalFare = finalFareRepository
                .findByRideId(request.getRideId())
                .orElseThrow(() -> new FinalFareNotFoundException(
                        "Final fare not found for ride: " + request.getRideId()));

        Optional<Payment> existingPayment =
                paymentRepository.findByRideId(request.getRideId());

        if (existingPayment.isPresent()) {

            Payment payment = existingPayment.get();

            if (payment.getStatus() == PaymentStatus.SUCCESS) {

                if (request.getSimulationOutcome() == SimulationOutcome.FAILED) {
                    throw new InvalidPaymentStateException(
                            "Successful payment cannot be changed to failed");
                }

                return toResponse(payment);
            }

            return retryPayment(payment, request);
        }

        return createPayment(finalFare, request);
    }

    public PaymentResponse getPaymentById(String paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + paymentId));

        return toResponse(payment);
    }

    public PaymentResponse getPaymentByRideId(String rideId) {

        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for ride: " + rideId));

        return toResponse(payment);
    }

    private PaymentResponse createPayment(
            FinalFare finalFare,
            PaymentRequest request) {

        Instant now = Instant.now();

        Payment payment = new Payment();

        payment.setRideId(finalFare.getRideId());
        payment.setFinalFareId(finalFare.getId());

        // Amount and currency always come from the saved FinalFare.
        payment.setAmount(finalFare.getTotalFare());
        payment.setCurrency(finalFare.getCurrency());

        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAttemptCount(1);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        if (request.getSimulationOutcome() == SimulationOutcome.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(now);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaidAt(null);
        }

        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse retryPayment(
            Payment payment,
            PaymentRequest request) {

        Instant now = Instant.now();

        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAttemptCount(payment.getAttemptCount() + 1);
        payment.setUpdatedAt(now);

        if (request.getSimulationOutcome() == SimulationOutcome.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(now);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaidAt(null);
        }

        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse toResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setPaymentId(payment.getId());
        response.setRideId(payment.getRideId());
        response.setFinalFareId(payment.getFinalFareId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setAttemptCount(payment.getAttemptCount());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setPaidAt(payment.getPaidAt());

        return response;
    }
}