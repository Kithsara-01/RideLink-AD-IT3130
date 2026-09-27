package com.ridelink.account.service;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;

import com.ridelink.account.config.JwtConfig;
import com.ridelink.account.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String ISSUER = "ridelink-account-service";
    private static final String AUDIENCE = "ridelink-services";

    private JwtEncoder encoder;
    private JwtDecoder decoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Test-only key. Does not read or change the application's secret.
        String testSecret = Base64.getEncoder()
                .encodeToString(new byte[32]);

        JwtConfig config = new JwtConfig();
        SecretKey key = config.jwtSecretKey(testSecret);

        encoder = config.jwtEncoder(key);
        decoder = config.jwtDecoder(key, ISSUER, AUDIENCE);
        jwtService = new JwtService(encoder, ISSUER, AUDIENCE, 3600);
    }

    @Test
    void generatedTokenContainsExpectedUserAndSettings() {
        User user = new User(
                "Test Rider",
                "rider@example.com",
                "test-password-hash",
                "RIDER"
        );
        user.setId("test-user-id");

        String token = jwtService.generateToken(user);
        Jwt decoded = decoder.decode(token);

        assertEquals("test-user-id", decoded.getSubject());
        assertEquals("RIDER", decoded.getClaimAsString("role"));
        assertEquals(ISSUER, decoded.getClaimAsString("iss"));
        assertTrue(decoded.getAudience().contains(AUDIENCE));

        assertNotNull(decoded.getIssuedAt());
        assertNotNull(decoded.getExpiresAt());
        assertEquals(
                decoded.getIssuedAt().plusSeconds(3600),
                decoded.getExpiresAt()
        );

        assertFalse(decoded.getClaims().containsKey("password"));
    }

    @Test
    void expiredTokenIsRejected() {
        Instant now = Instant.now();

        String token = createToken(
                encoder,
                ISSUER,
                AUDIENCE,
                now.minusSeconds(600),
                now.minusSeconds(300)
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void wrongIssuerIsRejected() {
        Instant now = Instant.now();

        String token = createToken(
                encoder,
                "another-issuer",
                AUDIENCE,
                now,
                now.plusSeconds(3600)
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void wrongAudienceIsRejected() {
        Instant now = Instant.now();

        String token = createToken(
                encoder,
                ISSUER,
                "another-application",
                now,
                now.plusSeconds(3600)
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void tokenSignedWithDifferentKeyIsRejected() {
        byte[] otherKeyBytes = new byte[32];
        otherKeyBytes[0] = 1;

        String otherSecret = Base64.getEncoder()
                .encodeToString(otherKeyBytes);

        JwtConfig config = new JwtConfig();
        JwtEncoder otherEncoder = config.jwtEncoder(
                config.jwtSecretKey(otherSecret)
        );

        Instant now = Instant.now();

        String token = createToken(
                otherEncoder,
                ISSUER,
                AUDIENCE,
                now,
                now.plusSeconds(3600)
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void tokenWithoutExpirationIsRejected() {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("test-user-id")
                .audience(List.of(AUDIENCE))
                .issuedAt(Instant.now())
                .claim("role", "RIDER")
                .build();

        String token = encode(encoder, claims);

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    private String createToken(
            JwtEncoder tokenEncoder,
            String issuer,
            String audience,
            Instant issuedAt,
            Instant expiresAt) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("test-user-id")
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", "RIDER")
                .build();

        return encode(tokenEncoder, claims);
    }

    private String encode(
            JwtEncoder tokenEncoder,
            JwtClaimsSet claims) {

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return tokenEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}