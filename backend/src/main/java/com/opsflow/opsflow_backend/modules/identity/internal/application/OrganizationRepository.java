package com.opsflow.opsflow_backend.modules.identity.internal.application;

import java.util.Optional;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;

public interface OrganizationRepository {

    void save(Organization organization);

    Optional<Organization> findByIdForMember(OrganizationId organizationId, UserProfileId userProfileId);

}
