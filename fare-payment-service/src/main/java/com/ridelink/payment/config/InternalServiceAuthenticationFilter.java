package com.ridelink.payment.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalServiceAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String INTERNAL_KEY_HEADER =
            "X-Internal-Service-Key";

    private static final String INTERNAL_ROLE =
            "ROLE_INTERNAL_RIDE_SERVICE";

    private final String internalServiceKey;

    public InternalServiceAuthenticationFilter(
            @Value("${app.internal.service-key}")
            String internalServiceKey) {

        this.internalServiceKey =
                internalServiceKey;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path =
                request.getRequestURI();

        String method =
                request.getMethod();

        boolean estimateEndpoint =
                "POST".equalsIgnoreCase(method)
                        && "/api/fares/estimate"
                        .equals(path);

        boolean finalizeEndpoint =
                "POST".equalsIgnoreCase(method)
                        && path.matches(
                        "^/api/fares/rides/[^/]+/finalize$");

        return !estimateEndpoint
                && !finalizeEndpoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String suppliedKey =
                request.getHeader(
                        INTERNAL_KEY_HEADER);

        if (suppliedKey != null
                && suppliedKey.equals(
                        internalServiceKey)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "ride-management-service",
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            INTERNAL_ROLE)));

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(
                request,
                response);
    }
}