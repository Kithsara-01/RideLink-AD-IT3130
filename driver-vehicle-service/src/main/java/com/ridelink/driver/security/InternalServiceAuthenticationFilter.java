package com.ridelink.driver.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    public static final String INTERNAL_ROLE =
            "ROLE_INTERNAL_RIDE_SERVICE";

    private static final String INTERNAL_KEY_HEADER =
            "X-Internal-Service-Key";

    private final String internalServiceKey;

    public InternalServiceAuthenticationFilter(String internalServiceKey) {
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean availableDriversRequest =
                HttpMethod.GET.matches(method)
                        && "/api/drivers/available".equals(path);

        boolean driverAssignmentRequest =
                HttpMethod.PATCH.matches(method)
                        && path.matches(
                                "^/api/drivers/[^/]+/(assign|release)$");

        return !availableDriversRequest
                && !driverAssignmentRequest;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String suppliedKey =
                request.getHeader(INTERNAL_KEY_HEADER);

        if (internalServiceKey.equals(suppliedKey)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "ride-management-service",
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            INTERNAL_ROLE)));

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}