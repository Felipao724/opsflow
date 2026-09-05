package com.opsflow.opsflow_backend.modules.identity.internal.application;

import java.util.Optional;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

public interface UserProfileRepository {

    Optional<UserProfile> findByExternalIdentity(
            ExternalIdentity externalIdentity);

    void save(UserProfile userProfile);
}