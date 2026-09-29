package com.ridelink.payment.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authorities =
                new JwtGrantedAuthoritiesConverter();

        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authorities);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter,
            InternalServiceAuthenticationFilter internalServiceAuthenticationFilter)
            throws Exception {

        AuthenticationEntryPoint unauthorized =
                (request, response, exception) -> {
                    response.setStatus(
                            HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                            "{\"status\":401,"
                                    + "\"message\":\"Authentication required or invalid token\"}");
                };

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR)
                        .permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/fares/estimate")
                        .hasAnyRole(
                                "RIDER",
                                "DRIVER",
                                "ADMIN",
                                "INTERNAL_RIDE_SERVICE")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/fares/rides/*/finalize")
                        .hasAnyRole(
                                "RIDER",
                                "DRIVER",
                                "ADMIN",
                                "INTERNAL_RIDE_SERVICE")

                        .requestMatchers(
                                "/api/fares/rides/**")
                        .hasAnyRole(
                                "RIDER",
                                "DRIVER",
                                "ADMIN")

                        .requestMatchers(
                                "/api/payments/**")
                        .hasAnyRole(
                                "RIDER",
                                "ADMIN")

                        .requestMatchers(
                                "/api/receipts/**")
                        .hasAnyRole(
                                "RIDER",
                                "ADMIN")

                        .anyRequest()
                        .denyAll())

                .addFilterBefore(
                        internalServiceAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(unauthorized))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorized));

        return http.build();
    }
}