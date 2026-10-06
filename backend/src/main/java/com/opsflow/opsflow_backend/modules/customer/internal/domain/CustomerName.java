package com.opsflow.opsflow_backend.modules.customer.internal.domain;

public record CustomerName(String value) {

    public static final int MAX_LENGTH = 120;

    public CustomerName {
        if (value == null) {
            throw new IllegalArgumentException("customer name must not be null");
        }
        value = value.strip();

        if (value.isBlank()) {
            throw new IllegalArgumentException("customer name must not be blank");
        }

        if (value.codePointCount(0, value.length()) > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "customer name must not exceed %d characters".formatted(MAX_LENGTH));
        }
    }

}
