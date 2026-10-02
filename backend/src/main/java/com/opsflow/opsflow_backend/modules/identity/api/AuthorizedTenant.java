package com.opsflow.opsflow_backend.modules.identity.api;

import java.util.Objects;
import java.util.UUID;

public record AuthorizedTenant(UUID userProfileId, UUID organizationId, MembershipAuthority authority) {

    public AuthorizedTenant {
        Objects.requireNonNull(userProfileId, "userProfileId must not be null");
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(authority, "authority must not be null");
    }

}
