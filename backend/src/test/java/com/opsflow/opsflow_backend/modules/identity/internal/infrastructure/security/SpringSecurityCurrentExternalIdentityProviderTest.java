package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;

class SpringSecurityCurrentExternalIdentityProviderTest {

    private final SpringSecurityCurrentExternalIdentityProvider provider =
            new SpringSecurityCurrentExternalIdentityProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsIdentityFromValidatedJwtAuthentication() {
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .issuer("https://identity.example/realms/opsflow")
                .subject("user-subject-123")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                authenticated(jwt));

        ExternalIdentity identity = provider.getCurrent();

        assertEquals("https://identity.example/realms/opsflow", identity.issuer());
        assertEquals("user-subject-123", identity.subject());
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                provider::getCurrent);
    }

    @Test
    void rejectsNonJwtAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user",
                        "password",
                        List.of()));

        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                provider::getCurrent);
    }

    @Test
    void rejectsJwtWithoutIssuer() {
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .subject("user-subject-123")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                authenticated(jwt));

        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                provider::getCurrent);
    }

    @Test
    void rejectsJwtWithoutSubject() {
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .issuer("https://identity.example/realms/opsflow")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                authenticated(jwt));

        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                provider::getCurrent);
    }

    private static JwtAuthenticationToken authenticated(Jwt jwt) {
        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
