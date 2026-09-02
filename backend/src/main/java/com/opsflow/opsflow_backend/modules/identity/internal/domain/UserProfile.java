package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import java.util.Objects;

public final class UserProfile {

    private final UserProfileId id;
    private final ExternalIdentity externalIdentity;

    public UserProfile(UserProfileId id, ExternalIdentity externalIdentity) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.externalIdentity = Objects.requireNonNull(externalIdentity, "externalIdentity must not be null");
    }

    public UserProfileId id() {
        return id;
    }

    public ExternalIdentity externalIdentity() {
        return externalIdentity;
    }

    public static UserProfile create(ExternalIdentity externalIdentity) {
        return new UserProfile(UserProfileId.generate(), externalIdentity);
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }

        if (!(candidate instanceof UserProfile userProfile)) {
            return false;
        }

        return id.equals(userProfile.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
