package com.opsflow.opsflow_backend.modules.identity.internal.domain;

public record ExternalIdentity(String issuer, String subject) {

    public ExternalIdentity {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("issuer must not be null or blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be null or blank");
        }
    }

}
