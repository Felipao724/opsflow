package com.opsflow.opsflow_backend.modules.customer.internal.domain;

public record MexicanPhoneNumber(String value) {

    public MexicanPhoneNumber {
        if (value == null) {
            throw new IllegalArgumentException("phone number must not be null");
        }

        if (!value.matches("[0-9]{10}")) {
            throw new IllegalArgumentException("phone number must contain exactly 10 ASCII digits");
        }
    }

}
