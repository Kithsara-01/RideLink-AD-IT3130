package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.RideResponse;
import com.ridelink.payment.exception.FinalFareNotFoundException;
import com.ridelink.payment.exception.InvalidPaymentStateException;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.FinalFare;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.SimulationOutcome;
import com.ridelink.payment.repository.FinalFareRepository;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.security.PaymentAuthorizationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String BEARER_TOKEN =
            "Bearer test-token";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FinalFareRepository finalFareRepository;

    @Mock
    private PaymentAuthorizationService paymentAuthorizationService;

    @Mock
    private Authentication authentication;

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
    void successfulPaymentShouldUseAmountAndCurrencyFromFinalFare() {

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.SUCCESS
        );

        allowCompletedRide("RIDE001");

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                new BigDecimal("910.00"),
                response.getAmount());

        assertEquals(
                "LKR",
                response.getCurrency());

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getStatus());

        assertEquals(
                1,
                response.getAttemptCount());

        assertNotNull(response.getPaidAt());

        verify(paymentRepository)
                .save(any(Payment.class));
    }

    @Test
    void failedSimulationShouldCreateFailedPayment() {

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.FAILED
        );

        allowCompletedRide("RIDE001");

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                PaymentStatus.FAILED,
                response.getStatus());

        assertEquals(
                1,
                response.getAttemptCount());

        assertNull(response.getPaidAt());
    }

    @Test
    void failedPaymentShouldBeRetriedSuccessfully() {

        Payment existingPayment =
                createExistingPayment(
                        PaymentStatus.FAILED,
                        1);

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.SUCCESS
        );

        allowCompletedRide("RIDE001");

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(existingPayment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getStatus());

        assertEquals(
                2,
                response.getAttemptCount());

        assertNotNull(response.getPaidAt());
    }

    @Test
    void retryShouldIncrementAttemptCount() {

        Payment existingPayment =
                createExistingPayment(
                        PaymentStatus.FAILED,
                        2);

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.FAILED
        );

        allowCompletedRide("RIDE001");

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(existingPayment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                PaymentStatus.FAILED,
                response.getStatus());

        assertEquals(
                3,
                response.getAttemptCount());
    }

    @Test
    void successfulPaymentCannotBeDowngraded() {

        Payment existingPayment =
                createExistingPayment(
                        PaymentStatus.SUCCESS,
                        1);

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.FAILED
        );

        allowCompletedRide("RIDE001");

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(existingPayment));

        assertThrows(
                InvalidPaymentStateException.class,
                () -> paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void repeatedSuccessShouldReturnExistingPaymentWithoutCreatingAnotherRecord() {

        Payment existingPayment =
                createExistingPayment(
                        PaymentStatus.SUCCESS,
                        1);

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.SUCCESS
        );

        allowCompletedRide("RIDE001");

        when(finalFareRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(finalFare));

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(existingPayment));

        PaymentResponse response =
                paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getStatus());

        assertEquals(
                1,
                response.getAttemptCount());

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void missingFinalFareShouldReturnNotFound() {

        PaymentRequest request = createRequest(
                "RIDE999",
                PaymentMethod.CARD,
                SimulationOutcome.SUCCESS
        );

        allowCompletedRide("RIDE999");

        when(finalFareRepository.findByRideId("RIDE999"))
                .thenReturn(Optional.empty());

        assertThrows(
                FinalFareNotFoundException.class,
                () -> paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN)
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void incompleteRideShouldRejectPaymentBeforePaymentChanges() {

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.SUCCESS
        );

        when(paymentAuthorizationService.authorizeRideAccess(
                "RIDE001",
                authentication,
                BEARER_TOKEN))
                .thenReturn(new RideResponse(
                        "RIDE001",
                        "USER001",
                        "IN_PROGRESS"));

        assertThrows(
                InvalidPaymentStateException.class,
                () -> paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN)
        );

        verifyNoInteractions(finalFareRepository);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void unauthorizedRetryShouldNotChangePayment() {

        PaymentRequest request = createRequest(
                "RIDE001",
                PaymentMethod.CARD,
                SimulationOutcome.SUCCESS
        );

        when(paymentAuthorizationService.authorizeRideAccess(
                "RIDE001",
                authentication,
                BEARER_TOKEN))
                .thenThrow(new RuntimeException(
                        "Access denied"));

        assertThrows(
                RuntimeException.class,
                () -> paymentService.recordPayment(
                        request,
                        authentication,
                        BEARER_TOKEN)
        );

        verifyNoInteractions(finalFareRepository);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void shouldRetrievePaymentByPaymentId() {

        Payment payment =
                createExistingPayment(
                        PaymentStatus.SUCCESS,
                        1);

        when(paymentRepository.findById("PAY001"))
                .thenReturn(Optional.of(payment));

        when(paymentAuthorizationService.authorizeRideAccess(
                "RIDE001",
                authentication,
                BEARER_TOKEN))
                .thenReturn(new RideResponse(
                        "RIDE001",
                        "USER001",
                        "COMPLETED"));

        PaymentResponse response =
                paymentService.getPaymentById(
                        "PAY001",
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                "RIDE001",
                response.getRideId());

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getStatus());

        assertEquals(
                new BigDecimal("910.00"),
                response.getAmount());
    }

    @Test
    void shouldRetrievePaymentByRideId() {

        Payment payment =
                createExistingPayment(
                        PaymentStatus.FAILED,
                        1);

        when(paymentRepository.findByRideId("RIDE001"))
                .thenReturn(Optional.of(payment));

        when(paymentAuthorizationService.authorizeRideAccess(
                "RIDE001",
                authentication,
                BEARER_TOKEN))
                .thenReturn(new RideResponse(
                        "RIDE001",
                        "USER001",
                        "COMPLETED"));

        PaymentResponse response =
                paymentService.getPaymentByRideId(
                        "RIDE001",
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                "RIDE001",
                response.getRideId());

        assertEquals(
                PaymentStatus.FAILED,
                response.getStatus());
    }

    @Test
    void missingPaymentShouldReturnNotFound() {

        when(paymentRepository.findById("PAY999"))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentById(
                        "PAY999",
                        authentication,
                        BEARER_TOKEN)
        );

        verifyNoInteractions(
                paymentAuthorizationService);
    }

    private void allowCompletedRide(
            String rideId) {

        when(paymentAuthorizationService.authorizeRideAccess(
                rideId,
                authentication,
                BEARER_TOKEN))
                .thenReturn(new RideResponse(
                        rideId,
                        "USER001",
                        "COMPLETED"));
    }

    private PaymentRequest createRequest(
            String rideId,
            PaymentMethod paymentMethod,
            SimulationOutcome simulationOutcome) {

        PaymentRequest request =
                new PaymentRequest();

        request.setRideId(rideId);
        request.setPaymentMethod(paymentMethod);
        request.setSimulationOutcome(simulationOutcome);

        return request;
    }

    private Payment createExistingPayment(
            PaymentStatus status,
            int attemptCount) {

        Payment payment =
                new Payment();

        payment.setId("PAY001");
        payment.setRideId("RIDE001");
        payment.setFinalFareId("FARE001");
        payment.setAmount(
                new BigDecimal("910.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(
                PaymentMethod.CARD);
        payment.setStatus(status);
        payment.setAttemptCount(
                attemptCount);
        payment.setCreatedAt(
                Instant.now());
        payment.setUpdatedAt(
                Instant.now());

        if (status == PaymentStatus.SUCCESS) {
            payment.setPaidAt(
                    Instant.now());
        }

        return payment;
    }
}