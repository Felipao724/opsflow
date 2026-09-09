package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.opsflow.opsflow_backend.modules.identity.internal.application.CurrentExternalIdentityProvider;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;

@Component
public class SpringSecurityCurrentExternalIdentityProvider implements CurrentExternalIdentityProvider {

    @Override
    public ExternalIdentity getCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !jwtAuthentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException(
                    "A validated JWT authentication is required");
        }

        Jwt jwt = jwtAuthentication.getToken();

        if (jwt.getIssuer() == null
                || jwt.getSubject() == null
                || jwt.getSubject().isBlank()) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Validated JWT does not contain the required identity claims");
        }

        String externalSubject = jwt.getSubject();
        String externalIssuer = jwt.getIssuer().toString();

        return new ExternalIdentity(externalIssuer, externalSubject);

    }

}
