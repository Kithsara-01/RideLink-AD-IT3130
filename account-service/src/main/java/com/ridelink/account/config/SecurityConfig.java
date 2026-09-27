package com.ridelink.account.config;

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
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityConfig {

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

                authoritiesConverter.setAuthoritiesClaimName("role");
                authoritiesConverter.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();

                authenticationConverter.setJwtGrantedAuthoritiesConverter(
                                authoritiesConverter);

                return authenticationConverter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter jwtAuthenticationConverter)
                        throws Exception {

                AuthenticationEntryPoint unauthorizedHandler = (request, response, exception) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setHeader("WWW-Authenticate", "Bearer");
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(
                                        "{\"status\":401,"
                                                        + "\"message\":\"Authentication required or invalid token\"}");
                };

                AccessDeniedHandler forbiddenHandler = (request, response, exception) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(
                                        "{\"status\":403,"
                                                        + "\"message\":\"You do not have permission to access this resource\"}");
                };

                http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth
                                                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/accounts/register",
                                                                "/api/accounts/login")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/accounts/me")
                                                .hasAnyRole("RIDER", "DRIVER", "ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/accounts/me")
                                                .hasAnyRole("RIDER", "DRIVER", "ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/accounts/email/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/accounts/*/status")
                                                .hasRole("ADMIN")

                                                .anyRequest().denyAll())

                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter))
                                                .authenticationEntryPoint(unauthorizedHandler)
                                                .accessDeniedHandler(forbiddenHandler))

                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(unauthorizedHandler)
                                                .accessDeniedHandler(forbiddenHandler));

                return http.build();
        }
}