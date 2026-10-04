package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareCalculationResult;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.exception.FinalFareAlreadyExistsException;
import com.ridelink.payment.exception.FinalFareNotFoundException;
import com.ridelink.payment.model.FinalFare;
import com.ridelink.payment.repository.FinalFareRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalFareServiceTest {

    @Mock
    private FinalFareRepository finalFareRepository;

    @Mock
    private FareCalculationService fareCalculationService;

    @InjectMocks
    private FinalFareService finalFareService;

    @Test
    void shouldSaveAndReturnFinalFare() {

        FinalFareRequest request =
                new FinalFareRequest(8.5, 25);

        FareCalculationResult calculationResult =
                new FareCalculationResult(
                        new BigDecimal("110.00"),
                        new BigDecimal("675.00"),
                        new BigDecimal("125.00"),
                        new BigDecimal("910.00"),
                        "LKR",
                        new BigDecimal("110.00"),
                        new BigDecimal("90.00"),
                        new BigDecimal("5.00")
                );

        when(finalFareRepository.existsByRideId("RIDE001"))
                .thenReturn(false);

        when(fareCalculationService.calculateFare(8.5, 25))
                .thenReturn(calculationResult);

        when(finalFareRepository.save(any(FinalFare.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FinalFareResponse response =
                finalFareService.finalizeFare("RIDE001", request);

        assertEquals("RIDE001", response.rideId());
        assertEquals(8.5, response.actualDistanceKm());
        assertEquals(25, response.actualDurationMinutes());
        assertEquals(new BigDecimal("910.00"), response.totalFare());
        assertEquals("LKR", response.currency());

        verify(finalFareRepository).save(any(FinalFare.class));
    }

    @Test
    void shouldRejectDuplicateFinalization() {

        FinalFareRequest request =
                new FinalFareRequest(8.5, 25);

        when(finalFareRepository.existsByRideId("RIDE001"))
                .thenReturn(true);

        assertThrows(
                FinalFareAlreadyExistsException.class,
                () -> finalFareService.finalizeFare("RIDE001", request)
        );

        verify(finalFareRepository, never())
                .save(any(FinalFare.class));

        verifyNoInteractions(fareCalculationService);
    }

    @Test
    void shouldThrowExceptionWhenFinalFareDoesNotExist() {

        when(finalFareRepository.findByRideId("RIDE999"))
                .thenReturn(Optional.empty());

        assertThrows(
                FinalFareNotFoundException.class,
                () -> finalFareService.getFinalFare("RIDE999")
        );
    }
}