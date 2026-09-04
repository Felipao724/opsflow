package com.opsflow.persistenceexperiment;

import java.util.Objects;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;

public final class UserProfilePersistenceMapper {

    private UserProfilePersistenceMapper() {
    }

    public static UserProfileJpaEntity toJpaEntity(UserProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");

        return new UserProfileJpaEntity(
                profile.id().value(),
                profile.externalIdentity().issuer(),
                profile.externalIdentity().subject());
    }

    public static UserProfile toDomain(UserProfileJpaEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return new UserProfile(
                new UserProfileId(entity.getId()),
                new ExternalIdentity(entity.getIssuer(), entity.getSubject()));
    }
}