package com.opsflow.opsflow_backend.modules.customer.internal.domain;

import java.util.regex.Pattern;

public record EmailAddress(String value) {

    public static final int MAX_LENGTH = 254;
    private static final Pattern VALID_FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public EmailAddress {
        if (value == null) {
            throw new IllegalArgumentException("email address must not be null");
        }

        value = value.strip();

        if (value.isBlank()) {
            throw new IllegalArgumentException("email address must not be blank");
        }

        if (value.codePointCount(0, value.length()) > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "email address must not exceed %d characters".formatted(MAX_LENGTH));
        }

        if (!VALID_FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("email address must be a valid email format");
        }
    }

}
