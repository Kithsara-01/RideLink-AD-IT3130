package com.ridelink.payment.security;

import com.ridelink.payment.client.RideClient;
import com.ridelink.payment.dto.RideResponse;
import com.ridelink.payment.exception.PaymentAccessDeniedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAuthorizationServiceTest {

    private static final String BEARER_TOKEN =
            "Bearer test-token";

    @Mock
    private RideClient rideClient;

    private PaymentAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService =
                new PaymentAuthorizationService(
                        rideClient);
    }

    @Test
    void owningRiderShouldAccessRide() {

        RideResponse ride =
                new RideResponse(
                        "RIDE001",
                        "USER-A",
                        "COMPLETED");

        when(rideClient.getRide(
                "RIDE001",
                BEARER_TOKEN))
                .thenReturn(ride);

        Authentication authentication =
                riderAuthentication("USER-A");

        RideResponse result =
                authorizationService.authorizeRideAccess(
                        "RIDE001",
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                "RIDE001",
                result.id());

        assertEquals(
                "USER-A",
                result.passengerId());
    }

    @Test
    void differentRiderShouldBeForbidden() {

        RideResponse ride =
                new RideResponse(
                        "RIDE001",
                        "USER-A",
                        "COMPLETED");

        when(rideClient.getRide(
                "RIDE001",
                BEARER_TOKEN))
                .thenReturn(ride);

        Authentication authentication =
                riderAuthentication("USER-B");

        assertThrows(
                PaymentAccessDeniedException.class,
                () -> authorizationService
                        .authorizeRideAccess(
                                "RIDE001",
                                authentication,
                                BEARER_TOKEN));
    }

    @Test
    void adminShouldAccessAnotherPassengersRide() {

        RideResponse ride =
                new RideResponse(
                        "RIDE001",
                        "USER-A",
                        "COMPLETED");

        when(rideClient.getRide(
                "RIDE001",
                BEARER_TOKEN))
                .thenReturn(ride);

        Authentication authentication =
                adminAuthentication("ADMIN-001");

        RideResponse result =
                authorizationService.authorizeRideAccess(
                        "RIDE001",
                        authentication,
                        BEARER_TOKEN);

        assertEquals(
                "RIDE001",
                result.id());
    }

    @Test
    void driverShouldBeForbidden() {

        RideResponse ride =
                new RideResponse(
                        "RIDE001",
                        "USER-A",
                        "COMPLETED");

        when(rideClient.getRide(
                "RIDE001",
                BEARER_TOKEN))
                .thenReturn(ride);

        Authentication authentication =
                driverAuthentication("DRIVER-001");

        assertThrows(
                PaymentAccessDeniedException.class,
                () -> authorizationService
                        .authorizeRideAccess(
                                "RIDE001",
                                authentication,
                                BEARER_TOKEN));
    }

    @Test
    void missingAuthenticationShouldBeForbidden() {

        assertThrows(
                PaymentAccessDeniedException.class,
                () -> authorizationService
                        .authorizeRideAccess(
                                "RIDE001",
                                null,
                                BEARER_TOKEN));

        verifyNoInteractions(rideClient);
    }

    private Authentication riderAuthentication(
            String subject) {

        return authentication(
                subject,
                "ROLE_RIDER");
    }

    private Authentication adminAuthentication(
            String subject) {

        return authentication(
                subject,
                "ROLE_ADMIN");
    }

    private Authentication driverAuthentication(
            String subject) {

        return authentication(
                subject,
                "ROLE_DRIVER");
    }

    private Authentication authentication(
            String subject,
            String authority) {

        Jwt jwt = Jwt.withTokenValue(
                        "test-token")
                .header(
                        "alg",
                        "HS256")
                .subject(subject)
                .issuedAt(
                        Instant.parse(
                                "2026-09-29T00:00:00Z"))
                .expiresAt(
                        Instant.parse(
                                "2026-09-30T00:00:00Z"))
                .build();

        return new UsernamePasswordAuthenticationToken(
                jwt,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                authority)));
    }
}