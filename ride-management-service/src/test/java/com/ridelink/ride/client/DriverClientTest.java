package com.ridelink.ride.client;

import com.ridelink.ride.dto.AvailableDriverResponse;
import com.ridelink.ride.exception.ApiException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverClientTest {

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
    void findAvailableDriversForwardsInternalKey()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/drivers/available",
                exchange -> {

                    String suppliedKey =
                            exchange.getRequestHeaders()
                                    .getFirst(
                                            "X-Internal-Service-Key");

                    if (!INTERNAL_KEY.equals(suppliedKey)) {

                        sendResponse(
                                exchange,
                                401,
                                "{\"message\":\"Unauthorized\"}"
                        );

                        return;
                    }

                    sendResponse(
                            exchange,
                            200,
                            """
                            [
                              {
                                "id": "driver-1",
                                "distanceKm": 1.2,
                                "serviceArea": "Colombo"
                              }
                            ]
                            """
                    );
                });

        server.start();

        DriverClient driverClient =
                new DriverClient(
                        baseUrl(),
                        INTERNAL_KEY);

        List<AvailableDriverResponse> response =
                driverClient.findAvailableDrivers(
                        null,
                        null,
                        6.9271,
                        79.8612);

        assertThat(response)
                .hasSize(1);

        assertThat(response.get(0).getId())
                .isEqualTo("driver-1");

        assertThat(response.get(0).getDistanceKm())
                .isEqualTo(1.2);
    }

    @Test
    void assignDriverForwardsInternalKey()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/drivers/driver-1/assign",
                exchange -> {

                    String suppliedKey =
                            exchange.getRequestHeaders()
                                    .getFirst(
                                            "X-Internal-Service-Key");

                    if (!INTERNAL_KEY.equals(suppliedKey)) {

                        sendResponse(
                                exchange,
                                401,
                                "{\"message\":\"Unauthorized\"}"
                        );

                        return;
                    }

                    sendResponse(
                            exchange,
                            200,
                            "{}"
                    );
                });

        server.start();

        DriverClient driverClient =
                new DriverClient(
                        baseUrl(),
                        INTERNAL_KEY);

        driverClient.assignDriver(
                "driver-1");
    }

    @Test
    void completedRideReleaseSendsCompletedTrue()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/drivers/driver-1/release",
                exchange -> {

                    String suppliedKey =
                            exchange.getRequestHeaders()
                                    .getFirst(
                                            "X-Internal-Service-Key");

                    String query =
                            exchange.getRequestURI()
                                    .getQuery();

                    if (!INTERNAL_KEY.equals(suppliedKey)
                            || !"completed=true".equals(query)) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"message\":\"Invalid request\"}"
                        );

                        return;
                    }

                    sendResponse(
                            exchange,
                            200,
                            "{}"
                    );
                });

        server.start();

        DriverClient driverClient =
                new DriverClient(
                        baseUrl(),
                        INTERNAL_KEY);

        driverClient.releaseDriver(
                "driver-1",
                true);
    }

    @Test
    void cancelledRideReleaseSendsCompletedFalse()
            throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0);

        server.createContext(
                "/api/drivers/driver-1/release",
                exchange -> {

                    String suppliedKey =
                            exchange.getRequestHeaders()
                                    .getFirst(
                                            "X-Internal-Service-Key");

                    String query =
                            exchange.getRequestURI()
                                    .getQuery();

                    if (!INTERNAL_KEY.equals(suppliedKey)
                            || !"completed=false".equals(query)) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"message\":\"Invalid request\"}"
                        );

                        return;
                    }

                    sendResponse(
                            exchange,
                            200,
                            "{}"
                    );
                });

        server.start();

        DriverClient driverClient =
                new DriverClient(
                        baseUrl(),
                        INTERNAL_KEY);

        driverClient.releaseDriver(
                "driver-1",
                false);
    }

    @Test
    void unavailableDriverServiceIsRejected()
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

        DriverClient driverClient =
                new DriverClient(
                        "http://localhost:" + port,
                        INTERNAL_KEY);

        assertThatThrownBy(() ->
                driverClient.findAvailableDrivers(
                        null,
                        null,
                        6.9271,
                        79.8612))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Driver service is unavailable");
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