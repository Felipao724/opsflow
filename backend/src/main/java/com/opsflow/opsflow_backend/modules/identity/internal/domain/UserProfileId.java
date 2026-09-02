package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import java.util.Objects;
import java.util.UUID;

public record UserProfileId(UUID value) {

    public UserProfileId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UserProfileId generate() {
        return new UserProfileId(UUID.randomUUID());
    }

}
