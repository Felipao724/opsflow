package com.opsflow.opsflow_backend.modules.customer.internal.domain;

import java.util.Objects;
import java.util.UUID;

public record ContactId(UUID value) {

    public ContactId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ContactId generate() {
        return new ContactId(UUID.randomUUID());
    }

}
