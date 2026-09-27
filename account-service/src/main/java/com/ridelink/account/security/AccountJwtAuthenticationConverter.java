package com.ridelink.account.security;

import com.ridelink.account.entity.User;
import com.ridelink.account.repository.UserRepository;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class AccountJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;
    private final JwtAuthenticationConverter delegate;

    public AccountJwtAuthenticationConverter(
            UserRepository userRepository) {

        this.userRepository = userRepository;

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        this.delegate = new JwtAuthenticationConverter();
        this.delegate.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String accountId = jwt.getSubject();

        if (accountId == null || accountId.isBlank()) {
            throw invalidToken();
        }

        User user = userRepository.findById(accountId)
                .orElseThrow(() -> invalidToken());

        if (!user.isActive()) {
            throw invalidToken();
        }

        Object tokenVersion = jwt.getClaims().get("tokenVersion");

        if (!(tokenVersion instanceof Long)
                && !(tokenVersion instanceof Integer)) {
            throw invalidToken();
        }

        long version = ((Number) tokenVersion).longValue();

        if (version != user.getTokenVersion()) {
            throw invalidToken();
        }

        Object tokenRole = jwt.getClaims().get("role");

        if (user.getRole() == null
                || !user.getRole().equals(tokenRole)) {
            throw invalidToken();
        }

        return delegate.convert(jwt);
    }

    private OAuth2AuthenticationException invalidToken() {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token"));
    }
}