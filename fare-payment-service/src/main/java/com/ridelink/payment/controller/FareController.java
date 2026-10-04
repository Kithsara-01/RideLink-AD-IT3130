package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.service.FareCalculationService;
import com.ridelink.payment.service.FinalFareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
@Tag(
        name = "Fare Management",
        description = "Fare estimation, final fare calculation, persistence, and retrieval"
)
public class FareController {

    private final FareCalculationService fareCalculationService;
    private final FinalFareService finalFareService;

    public FareController(
            FareCalculationService fareCalculationService,
            FinalFareService finalFareService) {

        this.fareCalculationService = fareCalculationService;
        this.finalFareService = finalFareService;
    }

    @Operation(
            summary = "Estimate a fare",
            description = """
                    Calculates a fare estimate using the RideLink fare rule.

                    Fare rule:
                    - First 1 km: LKR 110.00
                    - Each additional km: LKR 90.00
                    - Duration charge: LKR 5.00 per minute
                    - Final values are rounded to 2 decimal places using HALF_UP.

                    This endpoint only returns an estimate and does not save a final fare.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fare estimate calculated successfully",
                    content = @Content(
                            schema = @Schema(implementation = FareEstimateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid fare estimate request"
            )
    })
    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {

        FareEstimateResponse response =
                fareCalculationService.calculateEstimate(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Finalize fare for a ride",
            description = """
                    Calculates and permanently stores the final fare for a completed ride.

                    The final fare uses the actual distance and actual duration supplied
                    for the ride. Only one final fare can be stored for each ride.

                    Fare rule:
                    - First 1 km: LKR 110.00
                    - Each additional km: LKR 90.00
                    - Duration charge: LKR 5.00 per minute
                    - Final values are rounded to 2 decimal places using HALF_UP.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Final fare calculated and stored successfully",
                    content = @Content(
                            schema = @Schema(implementation = FinalFareResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid final fare request"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A final fare already exists for this ride"
            )
    })
    @PostMapping("/rides/{rideId}/finalize")
    public ResponseEntity<FinalFareResponse> finalizeFare(
            @Parameter(
                    description = "Unique identifier of the ride",
                    example = "RIDE001",
                    required = true
            )
            @PathVariable String rideId,

            @Valid @RequestBody FinalFareRequest request) {

        FinalFareResponse response =
                finalFareService.finalizeFare(rideId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Retrieve final fare for a ride",
            description = """
                    Retrieves the previously stored final fare for the specified ride.

                    The stored final fare is returned without recalculating it using
                    current fare rates.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Final fare retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = FinalFareResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Final fare not found for the specified ride"
            )
    })
    @GetMapping("/rides/{rideId}")
    public ResponseEntity<FinalFareResponse> getFinalFare(
            @Parameter(
                    description = "Unique identifier of the ride",
                    example = "RIDE001",
                    required = true
            )
            @PathVariable String rideId) {

        FinalFareResponse response =
                finalFareService.getFinalFare(rideId);

        return ResponseEntity.ok(response);
    }
}