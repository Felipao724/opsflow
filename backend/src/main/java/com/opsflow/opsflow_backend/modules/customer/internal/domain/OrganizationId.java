package com.opsflow.opsflow_backend.modules.customer.internal.domain;

import java.util.Objects;
import java.util.UUID;

public record OrganizationId(UUID value) {

    public OrganizationId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
