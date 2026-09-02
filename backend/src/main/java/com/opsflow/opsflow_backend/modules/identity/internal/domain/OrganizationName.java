package com.opsflow.opsflow_backend.modules.identity.internal.domain;

public record OrganizationName(String value) {

    public static final int MAX_LENGTH = 120;

    public OrganizationName {
        if (value == null) {
            throw new IllegalArgumentException("organization name must not be null");
        }

        value = value.strip();

        if (value.isBlank()) {
            throw new IllegalArgumentException("organization name must not be blank");
        }

        if (value.codePointCount(0, value.length()) > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "organization name must not exceed %d characters".formatted(MAX_LENGTH));
        }
    }

}
