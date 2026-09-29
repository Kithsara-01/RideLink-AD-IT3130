package com.ridelink.payment.security;

import com.ridelink.payment.client.RideClient;
import com.ridelink.payment.dto.RideResponse;
import com.ridelink.payment.exception.PaymentAccessDeniedException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class PaymentAuthorizationService {

    private final RideClient rideClient;

    public PaymentAuthorizationService(RideClient rideClient) {
        this.rideClient = rideClient;
    }

    public RideResponse authorizeRideAccess(
            String rideId,
            Authentication authentication,
            String bearerToken) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new PaymentAccessDeniedException(
                    "You are not allowed to access this ride payment");
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        RideResponse ride = rideClient.getRide(
                rideId,
                bearerToken);

        if (isAdmin) {
            return ride;
        }

        boolean isRider = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_RIDER"::equals);

        if (!isRider) {
            throw new PaymentAccessDeniedException(
                    "You are not allowed to access this ride payment");
        }

        String callerAccountId = jwt.getSubject();

        if (callerAccountId == null
                || callerAccountId.isBlank()
                || !callerAccountId.equals(ride.passengerId())) {

            throw new PaymentAccessDeniedException(
                    "You are not allowed to access this ride payment");
        }

        return ride;
    }
}