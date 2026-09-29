package com.ridelink.ride.client;

import com.ridelink.ride.dto.FareEstimateRequest;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.dto.FinalFareRequest;
import com.ridelink.ride.dto.FinalFareResponse;
import com.ridelink.ride.exception.ApiException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FareClient {

    private static final String INTERNAL_KEY_HEADER =
            "X-Internal-Service-Key";

    private final RestClient restClient;
    private final String internalServiceKey;

    public FareClient(
            @Value("${app.fare-service.base-url}") String baseUrl,
            @Value("${app.fare-service.internal-key}")
            String internalServiceKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.internalServiceKey = internalServiceKey;
    }

    public FareEstimateResponse estimateFare(
            FareEstimateRequest request) {

        try {
            FareEstimateResponse response =
                    restClient.post()
                            .uri("/api/fares/estimate")
                            .header(
                                    INTERNAL_KEY_HEADER,
                                    internalServiceKey)
                            .body(request)
                            .retrieve()
                            .body(FareEstimateResponse.class);

            if (response == null
                    || response.estimatedFare() == null) {

                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid response from Fare Service");
            }

            return response;

        } catch (ApiException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Fare Service is unavailable");
        }
    }

    public FinalFareResponse finalizeFare(
            String rideId,
            FinalFareRequest request) {

        try {
            FinalFareResponse response =
                    restClient.post()
                            .uri(
                                    "/api/fares/rides/{rideId}/finalize",
                                    rideId)
                            .header(
                                    INTERNAL_KEY_HEADER,
                                    internalServiceKey)
                            .body(request)
                            .retrieve()
                            .body(FinalFareResponse.class);

            if (response == null
                    || response.totalFare() == null) {

                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid response from Fare Service");
            }

            return response;

        } catch (ApiException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Fare Service is unavailable");
        }
    }
}