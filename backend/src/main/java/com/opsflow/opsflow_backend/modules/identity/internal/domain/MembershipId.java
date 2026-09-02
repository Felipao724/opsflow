package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import java.util.Objects;
import java.util.UUID;

public record MembershipId(UUID value) {

    public MembershipId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MembershipId generate() {
        return new MembershipId(UUID.randomUUID());
    }

}
