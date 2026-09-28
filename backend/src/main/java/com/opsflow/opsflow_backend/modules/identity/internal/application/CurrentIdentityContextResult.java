package com.opsflow.opsflow_backend.modules.identity.internal.application;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipRole;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;

public sealed interface CurrentIdentityContextResult {

    record OnboardingRequired() implements CurrentIdentityContextResult {
    }

    record Active(
            UserProfileId userProfileId,
            OrganizationId organizationId,
            OrganizationName organizationName,
            MembershipRole membershipRole)
            implements CurrentIdentityContextResult {
    }
}
