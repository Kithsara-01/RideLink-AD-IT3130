package com.ridelink.ride.client;

import com.ridelink.ride.dto.AccountResponse;
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

class AccountClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void forwardsBearerTokenAndReturnsAccount() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/api/accounts/me", exchange -> {
            String authorization =
                    exchange.getRequestHeaders().getFirst("Authorization");

            if (!"Bearer test-token".equals(authorization)) {
                sendResponse(exchange, 401,
                        "{\"message\":\"Unauthorized\"}");
                return;
            }

            sendResponse(
                    exchange,
                    200,
                    """
                    {
                      "id": "account-rider-1",
                      "role": "RIDER",
                      "active": true
                    }
                    """
            );
        });

        server.start();

        AccountClient accountClient =
                new AccountClient(baseUrl());

        AccountResponse response =
                accountClient.getCurrentAccount("test-token");

        assertThat(response.getId())
                .isEqualTo("account-rider-1");

        assertThat(response.getRole())
                .isEqualTo("RIDER");

        assertThat(response.isActive())
                .isTrue();
    }

    @Test
    void accountServiceUnauthorizedIsReturnedAsUnauthorized()
            throws IOException {

        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/api/accounts/me", exchange ->
                sendResponse(
                        exchange,
                        401,
                        "{\"message\":\"Unauthorized\"}"
                ));

        server.start();

        AccountClient accountClient =
                new AccountClient(baseUrl());

        assertThatThrownBy(() ->
                accountClient.getCurrentAccount("bad-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Account Service rejected authentication");
    }

    @Test
    void accountServiceForbiddenIsReturnedAsForbidden()
            throws IOException {

        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/api/accounts/me", exchange ->
                sendResponse(
                        exchange,
                        403,
                        "{\"message\":\"Forbidden\"}"
                ));

        server.start();

        AccountClient accountClient =
                new AccountClient(baseUrl());

        assertThatThrownBy(() ->
                accountClient.getCurrentAccount("test-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Account is not allowed to create a ride");
    }

    @Test
    void invalidAccountResponseIsRejected()
            throws IOException {

        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/api/accounts/me", exchange ->
                sendResponse(
                        exchange,
                        200,
                        "{}"
                ));

        server.start();

        AccountClient accountClient =
                new AccountClient(baseUrl());

        assertThatThrownBy(() ->
                accountClient.getCurrentAccount("test-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Invalid response from Account Service");
    }

    @Test
    void unavailableAccountServiceIsRejected()
            throws IOException {

        server = HttpServer.create(new InetSocketAddress(0), 0);

        int port = server.getAddress().getPort();

        server.start();
        server.stop(0);
        server = null;

        AccountClient accountClient =
                new AccountClient(
                        "http://localhost:" + port);

        assertThatThrownBy(() ->
                accountClient.getCurrentAccount("test-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(
                        "Account Service is unavailable");
    }

    private String baseUrl() {
        return "http://localhost:"
                + server.getAddress().getPort();
    }

    private void sendResponse(
            HttpExchange exchange,
            int status,
            String body) throws IOException {

        byte[] response =
                body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        exchange.sendResponseHeaders(
                status,
                response.length);

        exchange.getResponseBody().write(response);
        exchange.close();
    }
}