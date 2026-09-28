package com.ridelink.ride.client;

import com.ridelink.ride.dto.AvailableDriverResponse;
import com.ridelink.ride.entity.VehicleType;
import com.ridelink.ride.exception.ApiException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
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

    public void assignDriver(String driverId) {
        callDriverAction(driverId, "assign");
    }

    public void releaseDriver(String driverId) {
        callDriverAction(driverId, "release");
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