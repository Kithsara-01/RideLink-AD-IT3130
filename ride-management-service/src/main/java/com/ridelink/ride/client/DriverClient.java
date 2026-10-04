package com.ridelink.ride.client;

import com.ridelink.ride.dto.AvailableDriverResponse;
import com.ridelink.ride.dto.DriverProfileResponse;
import com.ridelink.ride.entity.VehicleType;
import com.ridelink.ride.exception.ApiException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Component
public class DriverClient {

    private static final String INTERNAL_KEY_HEADER =
            "X-Internal-Service-Key";

    private final RestClient restClient;
    private final String internalServiceKey;

    public DriverClient(
            @Value("${app.driver-service.base-url}") String baseUrl,
            @Value("${app.driver-service.internal-key}")
            String internalServiceKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.internalServiceKey = internalServiceKey;
    }

    public List<AvailableDriverResponse> findAvailableDrivers(
            String serviceArea,
            VehicleType vehicleType,
            Double latitude,
            Double longitude) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/drivers/available")
                            .queryParamIfPresent(
                                    "serviceArea",
                                    Optional.ofNullable(serviceArea))
                            .queryParamIfPresent(
                                    "vehicleType",
                                    Optional.ofNullable(vehicleType))
                            .queryParamIfPresent(
                                    "pickupLat",
                                    Optional.ofNullable(latitude))
                            .queryParamIfPresent(
                                    "pickupLng",
                                    Optional.ofNullable(longitude))
                            .build())
                    .header(
                            INTERNAL_KEY_HEADER,
                            internalServiceKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

        } catch (RestClientException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Driver service is unavailable");
        }
    }

    public DriverProfileResponse getDriverByUserId(
            String userId,
            String bearerToken) {

        if (userId == null
                || userId.isBlank()
                || bearerToken == null
                || bearerToken.isBlank()) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Driver identity could not be verified");
        }

        try {
            DriverProfileResponse response = restClient.get()
                    .uri(
                            "/api/drivers/user/{userId}",
                            userId)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + bearerToken)
                    .retrieve()
                    .body(DriverProfileResponse.class);

            if (response == null
                    || response.id() == null
                    || response.id().isBlank()
                    || response.userId() == null
                    || response.userId().isBlank()
                    || !userId.equals(response.userId())) {

                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "Driver Service returned an invalid driver profile");
            }

            return response;

        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Driver profile was not found for the authenticated account");

        } catch (HttpClientErrorException.Unauthorized exception) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Driver Service rejected authentication");

        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Driver Service rejected authorization");

        } catch (ApiException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Driver service is unavailable");
        }
    }

    public void assignDriver(String driverId) {
        callDriverAction(driverId, "assign");
    }

    public void releaseDriver(
            String driverId,
            boolean completedRide) {

        try {
            restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/drivers/{id}/release")
                            .queryParam(
                                    "completed",
                                    completedRide)
                            .build(driverId))
                    .header(
                            INTERNAL_KEY_HEADER,
                            internalServiceKey)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Driver service could not release driver");
        }
    }

    private void callDriverAction(
            String driverId,
            String action) {

        try {
            restClient.patch()
                    .uri(
                            "/api/drivers/{id}/{action}",
                            driverId,
                            action)
                    .header(
                            INTERNAL_KEY_HEADER,
                            internalServiceKey)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Driver service could not "
                            + action
                            + " driver");
        }
    }
}