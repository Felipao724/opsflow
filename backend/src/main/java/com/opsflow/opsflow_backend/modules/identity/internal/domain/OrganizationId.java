package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import java.util.Objects;
import java.util.UUID;

public record OrganizationId(UUID value) {

    public OrganizationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID());
    }

}
