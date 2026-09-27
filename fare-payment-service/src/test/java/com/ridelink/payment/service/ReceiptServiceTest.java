package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.InvalidPaymentStateException;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.exception.ReceiptNotFoundException;
import com.ridelink.payment.model.*;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FinalFareRepository finalFareRepository;

    @InjectMocks
    private PaymentService paymentService;

    private FinalFare finalFare;

    @BeforeEach
    void setUp() {

        finalFare = new FinalFare(
                "RIDE001",
                8.5,
                25,
                new BigDecimal("110.00"),
                new BigDecimal("675.00"),
                new BigDecimal("125.00"),
                new BigDecimal("910.00"),
                "LKR",
                new BigDecimal("110.00"),
                new BigDecimal("90.00"),
                new BigDecimal("5.00"),
                Instant.now()
        );
    }

    @Test
    void successfulNewPaymentShouldGenerateReceipt() {

        PaymentRequest request = createRequest(SimulationOutcome.SUCCESS);

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.recordPayment(request);

        verify(paymentRepository).save(argThat(payment ->
                payment.getStatus() == PaymentStatus.SUCCESS
                        && payment.getReceipt() != null
                        && payment.getReceipt().getReceiptNumber() != null
        ));
    }

    @Test
    void failedPaymentShouldNotGenerateReceipt() {

        PaymentRequest request = createRequest(SimulationOutcome.FAILED);

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.recordPayment(request);

        verify(paymentRepository).save(argThat(payment ->
                payment.getStatus() == PaymentStatus.FAILED
                        && payment.getReceipt() == null
        ));
    }

    @Test
    void failedToSuccessRetryShouldGenerateOneReceipt() {

        Payment payment = createFailedPayment();

        PaymentRequest request = createRequest(SimulationOutcome.SUCCESS);

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.recordPayment(request);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNotNull(payment.getReceipt());
        assertNotNull(payment.getReceipt().getReceiptNumber());
        assertEquals(2, payment.getAttemptCount());
    }

    @Test
    void repeatedSuccessShouldPreserveSameReceiptNumber() {

        Payment payment = createSuccessfulPaymentWithReceipt();

        String originalReceiptNumber =
                payment.getReceipt().getReceiptNumber();

        PaymentRequest request = createRequest(SimulationOutcome.SUCCESS);

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(payment));

        PaymentResponse response =
                paymentService.recordPayment(request);

        assertEquals(
                originalReceiptNumber,
                payment.getReceipt().getReceiptNumber()
        );

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void receiptValuesShouldComeFromSavedFinalFare() {

        PaymentRequest request = createRequest(SimulationOutcome.SUCCESS);

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.recordPayment(request);

        verify(paymentRepository).save(argThat(payment -> {

            Receipt receipt = payment.getReceipt();

            return receipt != null
                    && new BigDecimal("910.00")
                    .compareTo(receipt.getAmountPaid()) == 0
                    && "LKR".equals(receipt.getCurrency())
                    && Double.valueOf(8.5)
                    .equals(receipt.getActualDistanceKm())
                    && Integer.valueOf(25)
                    .equals(receipt.getActualDurationMinutes())
                    && new BigDecimal("110.00")
                    .compareTo(receipt.getBaseFare()) == 0
                    && new BigDecimal("675.00")
                    .compareTo(receipt.getDistanceCharge()) == 0
                    && new BigDecimal("125.00")
                    .compareTo(receipt.getDurationCharge()) == 0;
        }));
    }

    @Test
    void shouldRetrieveReceiptByPaymentId() {

        Payment payment = createSuccessfulPaymentWithReceipt();

        when(paymentRepository.findById("PAY001"))
                .thenReturn(Optional.of(payment));

        ReceiptResponse response =
                paymentService.getReceiptByPaymentId("PAY001");

        assertEquals("RCT-TEST-001", response.getReceiptNumber());
        assertEquals("PAY001", response.getPaymentId());
        assertEquals("RIDE001", response.getRideId());
        assertEquals(new BigDecimal("910.00"), response.getAmountPaid());
    }

    @Test
    void shouldRetrieveReceiptByReceiptNumber() {

        Payment payment = createSuccessfulPaymentWithReceipt();

        when(paymentRepository.findByReceiptReceiptNumber("RCT-TEST-001"))
                .thenReturn(Optional.of(payment));

        ReceiptResponse response =
                paymentService.getReceiptByReceiptNumber("RCT-TEST-001");

        assertEquals("RCT-TEST-001", response.getReceiptNumber());
        assertEquals("PAY001", response.getPaymentId());
        assertEquals("RIDE001", response.getRideId());
    }

    @Test
    void missingPaymentShouldReturnNotFound() {

        when(paymentRepository.findById("PAY999"))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getReceiptByPaymentId("PAY999")
        );
    }

    @Test
    void receiptRequestForFailedPaymentShouldBeRejected() {

        Payment payment = createFailedPayment();

        when(paymentRepository.findById("PAY001"))
                .thenReturn(Optional.of(payment));

        assertThrows(
                InvalidPaymentStateException.class,
                () -> paymentService.getReceiptByPaymentId("PAY001")
        );
    }

    @Test
    void missingReceiptNumberShouldReturnNotFound() {

        when(paymentRepository.findByReceiptReceiptNumber("RCT-NOT-FOUND"))
                .thenReturn(Optional.empty());

        assertThrows(
                ReceiptNotFoundException.class,
                () -> paymentService.getReceiptByReceiptNumber(
                        "RCT-NOT-FOUND")
        );
    }

    private PaymentRequest createRequest(
            SimulationOutcome simulationOutcome) {

        PaymentRequest request = new PaymentRequest();

        request.setRideId("RIDE001");
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setSimulationOutcome(simulationOutcome);

        return request;
    }

    private Payment createFailedPayment() {

        Payment payment = new Payment();

        payment.setId("PAY001");
        payment.setRideId("RIDE001");
        payment.setFinalFareId("FARE001");
        payment.setAmount(new BigDecimal("910.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setAttemptCount(1);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        payment.setPaidAt(null);
        payment.setReceipt(null);

        return payment;
    }

    private Payment createSuccessfulPaymentWithReceipt() {

        Payment payment = new Payment();

        payment.setId("PAY001");
        payment.setRideId("RIDE001");
        payment.setFinalFareId("FARE001");
        payment.setAmount(new BigDecimal("910.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setAttemptCount(1);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        payment.setPaidAt(Instant.now());

        Receipt receipt = new Receipt();

        receipt.setReceiptNumber("RCT-TEST-001");
        receipt.setRideId("RIDE001");
        receipt.setAmountPaid(new BigDecimal("910.00"));
        receipt.setCurrency("LKR");
        receipt.setPaymentMethod(PaymentMethod.CARD);
        receipt.setActualDistanceKm(8.5);
        receipt.setActualDurationMinutes(25);
        receipt.setBaseFare(new BigDecimal("110.00"));
        receipt.setDistanceCharge(new BigDecimal("675.00"));
        receipt.setDurationCharge(new BigDecimal("125.00"));
        receipt.setIssuedAt(Instant.now());

        payment.setReceipt(receipt);

        return payment;
    }
}