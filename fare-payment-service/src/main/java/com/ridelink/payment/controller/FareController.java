package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.service.FareCalculationService;
import com.ridelink.payment.service.FinalFareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareCalculationService fareCalculationService;
    private final FinalFareService finalFareService;

    public FareController(
            FareCalculationService fareCalculationService,
            FinalFareService finalFareService) {

        this.fareCalculationService = fareCalculationService;
        this.finalFareService = finalFareService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {

        FareEstimateResponse response =
                fareCalculationService.calculateEstimate(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rides/{rideId}/finalize")
    public ResponseEntity<FinalFareResponse> finalizeFare(
            @PathVariable String rideId,
            @Valid @RequestBody FinalFareRequest request) {

        FinalFareResponse response =
                finalFareService.finalizeFare(rideId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rides/{rideId}")
    public ResponseEntity<FinalFareResponse> getFinalFare(
            @PathVariable String rideId) {

        FinalFareResponse response =
                finalFareService.getFinalFare(rideId);

        return ResponseEntity.ok(response);
    }
}