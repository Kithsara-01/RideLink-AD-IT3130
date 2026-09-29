package com.ridelink.payment.client;

import com.ridelink.payment.dto.RideResponse;
import com.ridelink.payment.exception.RideServiceException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RideClient {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final RestClient restClient;

    public RideClient(
            @Value("${app.ride-service.base-url}") String baseUrl) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public RideResponse getRide(
            String rideId,
            String bearerToken) {

        try {
            RideResponse response =
                    restClient.get()
                            .uri("/api/rides/{rideId}", rideId)
                            .header("Authorization", bearerToken)
                            .retrieve()
                            .body(RideResponse.class);

            validateResponse(
                    rideId,
                    response);

            return response;

        } catch (RideServiceException exception) {
            throw exception;

        } catch (ResourceAccessException exception) {
            throw new RideServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Ride Service is unavailable");

        } catch (RestClientResponseException exception) {

            int status =
                    exception.getStatusCode().value();

            if (status == 401) {
                throw new RideServiceException(
                        HttpStatus.UNAUTHORIZED,
                        "Authentication is required");
            }

            if (status == 403) {
                throw new RideServiceException(
                        HttpStatus.FORBIDDEN,
                        "Ride access is forbidden");
            }

            if (status == 404) {
                throw new RideServiceException(
                        HttpStatus.NOT_FOUND,
                        "Ride not found: " + rideId);
            }

            if (exception.getStatusCode()
                    .is5xxServerError()) {

                throw new RideServiceException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Ride Service is unavailable");
            }

            throw new RideServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "Ride Service returned an unexpected response");

        } catch (RestClientException exception) {
            throw new RideServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid response from Ride Service");
        }
    }

    private void validateResponse(
            String requestedRideId,
            RideResponse response) {

        if (response == null
                || response.id() == null
                || response.id().isBlank()
                || !requestedRideId.equals(response.id())
                || response.passengerId() == null
                || response.passengerId().isBlank()
                || response.status() == null
                || response.status().isBlank()) {

            throw new RideServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid response from Ride Service");
        }
    }
}