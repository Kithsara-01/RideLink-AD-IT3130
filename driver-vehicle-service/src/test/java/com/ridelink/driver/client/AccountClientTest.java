package com.ridelink.driver.client;

import com.ridelink.driver.dto.AccountResponse;
import com.ridelink.driver.exception.ApiException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AccountClientTest {

    private HttpServer server;
    private AccountClient accountClient;
    private String receivedAuthorizationHeader;

    @BeforeEach
    void setUp() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        server.start();

        int port = server.getAddress().getPort();

        accountClient = new AccountClient(
                "http://localhost:" + port
        );
    }

    @AfterEach
    void tearDown() {

        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Should forward Bearer token and return account response")
    void testGetCurrentAccount_Success() {

        server.createContext(
                "/api/accounts/me",
                exchange -> {

                    receivedAuthorizationHeader =
                            exchange.getRequestHeaders()
                                    .getFirst("Authorization");

                    String response = """
                            {
                              "id": "user-101",
                              "fullName": "Test Driver",
                              "email": "driver@example.com",
                              "telephoneNumber": "+94771234567",
                              "role": "DRIVER",
                              "active": true
                            }
                            """;

                    sendResponse(
                            exchange,
                            200,
                            response
                    );
                }
        );

        AccountResponse response =
                accountClient.getCurrentAccount(
                        "test-driver-token"
                );

        assertNotNull(response);

        assertEquals(
                "user-101",
                response.getId()
        );

        assertEquals(
                "DRIVER",
                response.getRole()
        );

        assertTrue(response.isActive());

        assertEquals(
                "Bearer test-driver-token",
                receivedAuthorizationHeader
        );
    }

    @Test
    @DisplayName("Should map Account Service 401 to 401")
    void testGetCurrentAccount_Unauthorized() {

        server.createContext(
                "/api/accounts/me",
                exchange -> sendResponse(
                        exchange,
                        401,
                        "{\"message\":\"Unauthorized\"}"
                )
        );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> accountClient.getCurrentAccount(
                        "invalid-token"
                )
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("rejected authentication")
        );
    }

    @Test
    @DisplayName("Should map Account Service 403 to 403")
    void testGetCurrentAccount_Forbidden() {

        server.createContext(
                "/api/accounts/me",
                exchange -> sendResponse(
                        exchange,
                        403,
                        "{\"message\":\"Forbidden\"}"
                )
        );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> accountClient.getCurrentAccount(
                        "test-driver-token"
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("not allowed")
        );
    }

    @Test
    @DisplayName("Should reject invalid Account Service response")
    void testGetCurrentAccount_InvalidResponse() {

        server.createContext(
                "/api/accounts/me",
                exchange -> sendResponse(
                        exchange,
                        200,
                        "{}"
                )
        );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> accountClient.getCurrentAccount(
                        "test-driver-token"
                )
        );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid response")
        );
    }

    @Test
    @DisplayName("Should return 503 when Account Service is unavailable")
    void testGetCurrentAccount_ServiceUnavailable() {

        int port = server.getAddress().getPort();

        server.stop(0);
        server = null;

        AccountClient unavailableClient =
                new AccountClient(
                        "http://localhost:" + port
                );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> unavailableClient.getCurrentAccount(
                        "test-driver-token"
                )
        );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatus()
        );

        assertTrue(
                exception.getMessage()
                        .contains("Account Service is unavailable")
        );
    }

    private void sendResponse(
            HttpExchange exchange,
            int status,
            String body) throws IOException {

        byte[] responseBytes =
                body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json"
                );

        exchange.sendResponseHeaders(
                status,
                responseBytes.length
        );

        exchange.getResponseBody()
                .write(responseBytes);

        exchange.close();
    }
}