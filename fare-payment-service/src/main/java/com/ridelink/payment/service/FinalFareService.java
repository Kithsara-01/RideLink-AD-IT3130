package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareCalculationResult;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.exception.FinalFareAlreadyExistsException;
import com.ridelink.payment.exception.FinalFareNotFoundException;
import com.ridelink.payment.model.FinalFare;
import com.ridelink.payment.repository.FinalFareRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FinalFareService {

    private final FinalFareRepository finalFareRepository;
    private final FareCalculationService fareCalculationService;

    public FinalFareService(
            FinalFareRepository finalFareRepository,
            FareCalculationService fareCalculationService) {

        this.finalFareRepository = finalFareRepository;
        this.fareCalculationService = fareCalculationService;
    }

    public FinalFareResponse finalizeFare(
            String rideId,
            FinalFareRequest request) {

        if (finalFareRepository.existsByRideId(rideId)) {
            throw new FinalFareAlreadyExistsException(
                    "Final fare already exists for ride: " + rideId
            );
        }

        FareCalculationResult result =
                fareCalculationService.calculateFare(
                        request.actualDistanceKm(),
                        request.actualDurationMinutes()
                );

        FinalFare finalFare = new FinalFare(
                rideId,
                request.actualDistanceKm(),
                request.actualDurationMinutes(),
                result.baseFare(),
                result.distanceCharge(),
                result.durationCharge(),
                result.totalFare(),
                result.currency(),
                result.firstKmFare(),
                result.additionalKmRate(),
                result.perMinuteRate(),
                Instant.now()
        );

        FinalFare savedFare = finalFareRepository.save(finalFare);

        return toResponse(savedFare);
    }

    public FinalFareResponse getFinalFare(String rideId) {

        FinalFare finalFare = finalFareRepository.findByRideId(rideId)
                .orElseThrow(() -> new FinalFareNotFoundException(
                        "Final fare not found for ride: " + rideId
                ));

        return toResponse(finalFare);
    }

    private FinalFareResponse toResponse(FinalFare finalFare) {

        return new FinalFareResponse(
                finalFare.getRideId(),
                finalFare.getActualDistanceKm(),
                finalFare.getActualDurationMinutes(),
                finalFare.getBaseFare(),
                finalFare.getDistanceCharge(),
                finalFare.getDurationCharge(),
                finalFare.getTotalFare(),
                finalFare.getCurrency(),
                finalFare.getFirstKmFare(),
                finalFare.getAdditionalKmRate(),
                finalFare.getPerMinuteRate(),
                finalFare.getFinalizedAt()
        );
    }
}