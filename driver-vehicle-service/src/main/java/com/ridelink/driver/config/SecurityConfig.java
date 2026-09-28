package com.ridelink.driver.config;

import com.ridelink.driver.security.InternalServiceAuthenticationFilter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

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
    public InternalServiceAuthenticationFilter
            internalServiceAuthenticationFilter(
                    @Value("${app.internal.service-key}")
                    String internalServiceKey) {

        return new InternalServiceAuthenticationFilter(
                internalServiceKey);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter,
            InternalServiceAuthenticationFilter internalFilter)
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
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/drivers/available")
                        .hasAnyAuthority(
                                InternalServiceAuthenticationFilter
                                        .INTERNAL_ROLE,
                                "ROLE_ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/drivers/*/assign",
                                "/api/drivers/*/release")
                        .hasAnyAuthority(
                                InternalServiceAuthenticationFilter
                                        .INTERNAL_ROLE,
                                "ROLE_ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/drivers/**")
                        .hasAnyRole(
                                "RIDER",
                                "DRIVER",
                                "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/drivers/**")
                        .hasAnyRole(
                                "DRIVER",
                                "ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/drivers/**")
                        .hasAnyRole(
                                "DRIVER",
                                "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/drivers/**")
                        .hasAnyRole(
                                "DRIVER",
                                "ADMIN")

                        .anyRequest()
                        .denyAll())

                .addFilterBefore(
                        internalFilter,
                        BearerTokenAuthenticationFilter.class)

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(unauthorized))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorized));

        return http.build();
    }
}