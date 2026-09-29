package com.ridelink.ride.client;

import com.ridelink.ride.dto.FareEstimateRequest;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.dto.FinalFareRequest;
import com.ridelink.ride.dto.FinalFareResponse;
import com.ridelink.ride.exception.ApiException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FareClientTest {

    private static final String INTERNAL_KEY =
            "test-internal-key";

    private HttpServer server;

    @AfterEach
    void stopServer() {

        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void estimateFareSendsCorrectPathBodyAndInternalKey()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/fares/estimate",
                exchange -> {

                    String internalKey =
                            exchange.getRequestHeaders()
                                    .getFirst(
                                            "X-Internal-Service-Key");

                    String requestBody =
                            new String(
                                    exchange.getRequestBody()
                                            .readAllBytes(),
                                    StandardCharsets.UTF_8);

                    if (!INTERNAL_KEY.equals(internalKey)
                            || !requestBody.contains(
                                    "\"pickup\":\"Colombo\"")
                            || !requestBody.contains(
                                    "\"destination\":\"Kandy\"")
                            || !requestBody.contains(
                                    "\"distanceKm\":10.5")
                            || !requestBody.contains(
                                    "\"durationMinutes\":25")) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"message\":\"Invalid request\"}");

                        return;
                    }

                    sendResponse(
                            exchange,
                            200,
                            """
                            {
                              "pickup": "Colombo",
                              "destination": "Kandy",
                              "distanceKm": 10.5,
                              "durationMinutes": 25,
                              "baseFare": 110.00,
                              "distanceCharge": 855.00,
                              "durationCharge": 125.00,
                              "estimatedFare": 1090.00,
                              "currency": "LKR"
                            }
                            """);
                });

        server.start();

        FareClient fareClient =
                new FareClient(
                        baseUrl(),
                        INTERNAL_KEY);

        FareEstimateResponse response =
                fareClient.estimateFare(
                        new FareEstimateRequest(
                                "Colombo",
                                "Kandy",
                                10.5,
                                25));

        assertThat(response.estimatedFare())
                .isEqualByComparingTo("1090.00");

        assertThat(response.currency())
                .isEqualTo("LKR");
    }

    @Test
    void finalizeFareSendsCorrectPathBodyAndInternalKey()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/fares/rides/ride-123/finalize",
                exchange -> {

                    String internalKey =
                            exchange.getRequestHeaders()
                                    .getFirst(
                                            "X-Internal-Service-Key");

                    String requestBody =
                            new String(
                                    exchange.getRequestBody()
                                            .readAllBytes(),
                                    StandardCharsets.UTF_8);

                    if (!INTERNAL_KEY.equals(internalKey)
                            || !requestBody.contains(
                                    "\"actualDistanceKm\":12.5")
                            || !requestBody.contains(
                                    "\"actualDurationMinutes\":28")) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"message\":\"Invalid request\"}");

                        return;
                    }

                    sendResponse(
                            exchange,
                            200,
                            """
                            {
                              "rideId": "ride-123",
                              "actualDistanceKm": 12.5,
                              "actualDurationMinutes": 28,
                              "baseFare": 110.00,
                              "distanceCharge": 1035.00,
                              "durationCharge": 140.00,
                              "totalFare": 1285.00,
                              "currency": "LKR",
                              "firstKmFare": 110.00,
                              "additionalKmRate": 90.00,
                              "perMinuteRate": 5.00,
                              "finalizedAt": "2026-09-29T03:00:00Z"
                            }
                            """);
                });

        server.start();

        FareClient fareClient =
                new FareClient(
                        baseUrl(),
                        INTERNAL_KEY);

        FinalFareResponse response =
                fareClient.finalizeFare(
                        "ride-123",
                        new FinalFareRequest(
                                12.5,
                                28));

        assertThat(response.rideId())
                .isEqualTo("ride-123");

        assertThat(response.totalFare())
                .isEqualByComparingTo("1285.00");

        assertThat(response.currency())
                .isEqualTo("LKR");
    }

    @Test
    void invalidEstimateResponseIsRejected()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/fares/estimate",
                exchange ->
                        sendResponse(
                                exchange,
                                200,
                                "{}"));

        server.start();

        FareClient fareClient =
                new FareClient(
                        baseUrl(),
                        INTERNAL_KEY);

        assertThatThrownBy(() ->
                fareClient.estimateFare(
                        new FareEstimateRequest(
                                "Colombo",
                                "Kandy",
                                10.5,
                                25)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Invalid response from Fare Service");
    }

    @Test
    void invalidFinalizeResponseIsRejected()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/fares/rides/ride-123/finalize",
                exchange ->
                        sendResponse(
                                exchange,
                                200,
                                "{}"));

        server.start();

        FareClient fareClient =
                new FareClient(
                        baseUrl(),
                        INTERNAL_KEY);

        assertThatThrownBy(() ->
                fareClient.finalizeFare(
                        "ride-123",
                        new FinalFareRequest(
                                12.5,
                                28)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Invalid response from Fare Service");
    }

    @Test
    void unavailableFareServiceIsMappedTo503()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        int port =
                server.getAddress()
                        .getPort();

        server.start();
        server.stop(0);
        server = null;

        FareClient fareClient =
                new FareClient(
                        "http://localhost:" + port,
                        INTERNAL_KEY);

        assertThatThrownBy(() ->
                fareClient.estimateFare(
                        new FareEstimateRequest(
                                "Colombo",
                                "Kandy",
                                10.5,
                                25)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Fare Service is unavailable");
    }

    private String baseUrl() {

        return "http://localhost:"
                + server.getAddress()
                        .getPort();
    }

    private void sendResponse(
            HttpExchange exchange,
            int status,
            String body)
            throws IOException {

        byte[] response =
                body.getBytes(
                        StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json");

        exchange.sendResponseHeaders(
                status,
                response.length);

        exchange.getResponseBody()
                .write(response);

        exchange.close();
    }
}