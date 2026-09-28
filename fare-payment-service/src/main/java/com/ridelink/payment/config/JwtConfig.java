package com.ridelink.payment.config;

import java.util.Base64;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience) {

        byte[] keyBytes = Base64.getDecoder().decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 decoded bytes"
            );
        }

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(
                                new SecretKeySpec(
                                        keyBytes,
                                        "HmacSHA256"))
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        OAuth2TokenValidator<Jwt> issuerAndTimeValidator =
                JwtValidators.createDefaultWithIssuer(issuer);

        JwtClaimValidator<List<String>> audienceValidator =
                new JwtClaimValidator<>(
                        "aud",
                        audiences -> audiences != null
                                && audiences.contains(audience)
                );

        JwtClaimValidator<Object> expirationRequiredValidator =
                new JwtClaimValidator<>(
                        "exp",
                        expiration -> expiration != null
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerAndTimeValidator,
                        audienceValidator,
                        expirationRequiredValidator
                )
        );

        return decoder;
    }
}