package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.FinalFareNotFoundException;
import com.ridelink.payment.exception.InvalidPaymentStateException;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.exception.ReceiptNotFoundException;
import com.ridelink.payment.model.FinalFare;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.Receipt;
import com.ridelink.payment.model.SimulationOutcome;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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

            return retryPayment(payment, request, finalFare);
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

    public ReceiptResponse getReceiptByPaymentId(String paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException(
                    "Receipt is not available for a failed payment");
        }

        if (payment.getReceipt() == null) {
            throw new ReceiptNotFoundException(
                    "Receipt not found for payment: " + paymentId);
        }

        return toReceiptResponse(payment);
    }

    public ReceiptResponse getReceiptByReceiptNumber(String receiptNumber) {

        Payment payment = paymentRepository
                .findByReceiptReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ReceiptNotFoundException(
                        "Receipt not found: " + receiptNumber));

        return toReceiptResponse(payment);
    }

    private PaymentResponse createPayment(
            FinalFare finalFare,
            PaymentRequest request) {

        Instant now = Instant.now();

        Payment payment = new Payment();

        payment.setRideId(finalFare.getRideId());
        payment.setFinalFareId(finalFare.getId());
        payment.setAmount(finalFare.getTotalFare());
        payment.setCurrency(finalFare.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAttemptCount(1);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        if (request.getSimulationOutcome() == SimulationOutcome.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(now);
            payment.setReceipt(createReceipt(finalFare, payment, now));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaidAt(null);
            payment.setReceipt(null);
        }

        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse retryPayment(
            Payment payment,
            PaymentRequest request,
            FinalFare finalFare) {

        Instant now = Instant.now();

        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAttemptCount(payment.getAttemptCount() + 1);
        payment.setUpdatedAt(now);

        if (request.getSimulationOutcome() == SimulationOutcome.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(now);

            if (payment.getReceipt() == null) {
                payment.setReceipt(createReceipt(finalFare, payment, now));
            }

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaidAt(null);
        }

        return toResponse(paymentRepository.save(payment));
    }

    private Receipt createReceipt(
            FinalFare finalFare,
            Payment payment,
            Instant issuedAt) {

        Receipt receipt = new Receipt();

        receipt.setReceiptNumber(
                "RCT-" + UUID.randomUUID().toString().toUpperCase());

        receipt.setRideId(payment.getRideId());
        receipt.setAmountPaid(payment.getAmount());
        receipt.setCurrency(payment.getCurrency());
        receipt.setPaymentMethod(payment.getPaymentMethod());

        receipt.setActualDistanceKm(finalFare.getActualDistanceKm());
        receipt.setActualDurationMinutes(finalFare.getActualDurationMinutes());
        receipt.setBaseFare(finalFare.getBaseFare());
        receipt.setDistanceCharge(finalFare.getDistanceCharge());
        receipt.setDurationCharge(finalFare.getDurationCharge());

        receipt.setIssuedAt(issuedAt);

        return receipt;
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

    private ReceiptResponse toReceiptResponse(Payment payment) {

        Receipt receipt = payment.getReceipt();

        ReceiptResponse response = new ReceiptResponse();

        response.setReceiptNumber(receipt.getReceiptNumber());
        response.setPaymentId(payment.getId());
        response.setRideId(receipt.getRideId());
        response.setAmountPaid(receipt.getAmountPaid());
        response.setCurrency(receipt.getCurrency());
        response.setPaymentMethod(receipt.getPaymentMethod());
        response.setActualDistanceKm(receipt.getActualDistanceKm());
        response.setActualDurationMinutes(receipt.getActualDurationMinutes());
        response.setBaseFare(receipt.getBaseFare());
        response.setDistanceCharge(receipt.getDistanceCharge());
        response.setDurationCharge(receipt.getDurationCharge());
        response.setIssuedAt(receipt.getIssuedAt());

        return response;
    }
}