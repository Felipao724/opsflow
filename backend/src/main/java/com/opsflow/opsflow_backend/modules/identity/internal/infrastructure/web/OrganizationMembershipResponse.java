package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.web;

import java.util.Objects;
import java.util.UUID;

public record OrganizationMembershipResponse(UUID userProfileId, UUID organizationId, String authority) {

    public OrganizationMembershipResponse {
        Objects.requireNonNull(userProfileId, "userProfileId must not be null");
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(authority, "authority must not be null");
    }

}
