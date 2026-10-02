package com.opsflow.opsflow_backend.modules.identity.internal.application;

import java.util.Objects;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opsflow.opsflow_backend.modules.identity.api.AuthorizedTenant;
import com.opsflow.opsflow_backend.modules.identity.api.MembershipAuthority;
import com.opsflow.opsflow_backend.modules.identity.api.TenantAuthorization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

@Service
public class TenantAuthorizationService implements TenantAuthorization {

    private final CurrentExternalIdentityProvider currentExternalIdentityProvider;
    private final UserProfileRepository userProfileRepository;
    private final OrganizationRepository organizationRepository;

    public TenantAuthorizationService(CurrentExternalIdentityProvider currentExternalIdentityProvider,
            UserProfileRepository userProfileRepository, OrganizationRepository organizationRepository) {
        this.currentExternalIdentityProvider = currentExternalIdentityProvider;
        this.userProfileRepository = userProfileRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorizedTenant requireAccess(UUID organizationId) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");

        ExternalIdentity externalIdentity = currentExternalIdentityProvider.getCurrent();
        Objects.requireNonNull(externalIdentity, "externalIdentity must not be null");

        UserProfile userProfile = userProfileRepository.findByExternalIdentity(externalIdentity)
                .orElseThrow(() -> new AccessDeniedException("Tenant access denied"));

        OrganizationId orgId = new OrganizationId(organizationId);
        Organization organization = organizationRepository.findByIdForMember(orgId, userProfile.id())
                .orElseThrow(() -> new AccessDeniedException("Tenant access denied"));

        Membership membership = organization.membershipFor(userProfile.id());

        MembershipAuthority authority = switch (membership.role()) {
            case OWNER -> MembershipAuthority.OWNER;
        };

        return new AuthorizedTenant(userProfile.id().value(), organization.id().value(), authority);

    }

}
