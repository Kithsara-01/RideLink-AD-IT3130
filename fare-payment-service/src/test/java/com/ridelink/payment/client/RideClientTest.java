package com.ridelink.payment.client;

import com.ridelink.payment.dto.RideResponse;
import com.ridelink.payment.exception.RideServiceException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RideClientTest {

    private static final String BEARER_TOKEN =
            "Bearer test-token";

    private HttpServer server;

    @AfterEach
    void tearDown() {

        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldGetRideAndForwardBearerToken()
            throws Exception {

        AtomicReference<String> receivedAuthorization =
                new AtomicReference<>();

        server = createServer(exchange -> {

            receivedAuthorization.set(
                    exchange.getRequestHeaders()
                            .getFirst("Authorization"));

            assertEquals(
                    "GET",
                    exchange.getRequestMethod());

            assertEquals(
                    "/api/rides/RIDE001",
                    exchange.getRequestURI().getPath());

            sendJson(
                    exchange,
                    200,
                    """
                    {
                      "id": "RIDE001",
                      "passengerId": "USER-A",
                      "status": "COMPLETED"
                    }
                    """);
        });

        RideClient rideClient =
                new RideClient(baseUrl());

        RideResponse response =
                rideClient.getRide(
                        "RIDE001",
                        BEARER_TOKEN);

        assertEquals(
                "RIDE001",
                response.id());

        assertEquals(
                "USER-A",
                response.passengerId());

        assertEquals(
                "COMPLETED",
                response.status());

        assertEquals(
                BEARER_TOKEN,
                receivedAuthorization.get());
    }

    @Test
    void missingRideShouldReturn404()
            throws Exception {

        server = createServer(exchange ->
                sendJson(
                        exchange,
                        404,
                        """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "message": "Ride not found"
                        }
                        """));

        RideClient rideClient =
                new RideClient(baseUrl());

        RideServiceException exception =
                assertThrows(
                        RideServiceException.class,
                        () -> rideClient.getRide(
                                "RIDE999",
                                BEARER_TOKEN));

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatus());
    }

    @Test
    void invalidRideResponseShouldReturn502()
            throws Exception {

        server = createServer(exchange ->
                sendJson(
                        exchange,
                        200,
                        """
                        {
                          "id": "WRONG-RIDE",
                          "passengerId": "",
                          "status": "COMPLETED"
                        }
                        """));

        RideClient rideClient =
                new RideClient(baseUrl());

        RideServiceException exception =
                assertThrows(
                        RideServiceException.class,
                        () -> rideClient.getRide(
                                "RIDE001",
                                BEARER_TOKEN));

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatus());
    }

    @Test
    void malformedJsonShouldReturn502()
            throws Exception {

        server = createServer(exchange ->
                sendJson(
                        exchange,
                        200,
                        """
                        {
                          "id":
                        """));

        RideClient rideClient =
                new RideClient(baseUrl());

        RideServiceException exception =
                assertThrows(
                        RideServiceException.class,
                        () -> rideClient.getRide(
                                "RIDE001",
                                BEARER_TOKEN));

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatus());
    }

    @Test
    void rideServiceServerErrorShouldReturn503()
            throws Exception {

        server = createServer(exchange ->
                sendJson(
                        exchange,
                        500,
                        """
                        {
                          "status": 500,
                          "error": "Internal Server Error"
                        }
                        """));

        RideClient rideClient =
                new RideClient(baseUrl());

        RideServiceException exception =
                assertThrows(
                        RideServiceException.class,
                        () -> rideClient.getRide(
                                "RIDE001",
                                BEARER_TOKEN));

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatus());
    }

    @Test
    void unavailableRideServiceShouldReturn503() {

        RideClient rideClient =
                new RideClient(
                        "http://127.0.0.1:1");

        RideServiceException exception =
                assertThrows(
                        RideServiceException.class,
                        () -> rideClient.getRide(
                                "RIDE001",
                                BEARER_TOKEN));

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatus());
    }

    private HttpServer createServer(
            ExchangeHandler handler)
            throws IOException {

        HttpServer httpServer =
                HttpServer.create(
                        new InetSocketAddress(
                                "127.0.0.1",
                                0),
                        0);

        httpServer.createContext(
                "/api/rides",
                exchange -> {

                    try {
                        handler.handle(exchange);

                    } finally {
                        exchange.close();
                    }
                });

        httpServer.start();

        return httpServer;
    }

    private String baseUrl() {

        return "http://127.0.0.1:"
                + server.getAddress().getPort();
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            String body)
            throws IOException {

        byte[] bytes =
                body.getBytes(
                        StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json");

        exchange.sendResponseHeaders(
                status,
                bytes.length);

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(bytes);
        }
    }

    @FunctionalInterface
    private interface ExchangeHandler {

        void handle(
                HttpExchange exchange)
                throws IOException;
    }
}