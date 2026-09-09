package com.opsflow.opsflow_backend.modules.identity.internal.application;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipRole;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;

public record OnboardOrganizationResult(UserProfileId userProfileId, OrganizationId organizationId,
        OrganizationName organizationName, MembershipId ownerMembershipId, MembershipRole membershipRole) {

}
