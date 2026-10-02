package com.opsflow.opsflow_backend.modules.identity.internal.application;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

@Service
public class GetCurrentIdentityContextService {

    private final CurrentExternalIdentityProvider currentExternalIdentityProvider;
    private final UserProfileRepository userProfileRepository;
    private final OrganizationRepository organizationRepository;

    public GetCurrentIdentityContextService(CurrentExternalIdentityProvider currentExternalIdentityProvider,
            UserProfileRepository userProfileRepository, OrganizationRepository organizationRepository) {
        this.currentExternalIdentityProvider = Objects.requireNonNull(currentExternalIdentityProvider,
                "currentExternalIdentityProvider must not be null");
        this.userProfileRepository = Objects.requireNonNull(userProfileRepository,
                "userProfileRepository must not be null");
        this.organizationRepository = Objects.requireNonNull(organizationRepository,
                "organizationRepository must not be null");
    }

    @Transactional(readOnly = true)
    public CurrentIdentityContextResult getCurrent() {
        ExternalIdentity externalIdentity = currentExternalIdentityProvider.getCurrent();

        Optional<UserProfile> userProfileResult = userProfileRepository.findByExternalIdentity(externalIdentity);

        if (userProfileResult.isEmpty()) {
            return new CurrentIdentityContextResult.OnboardingRequired();
        }

        UserProfile userProfile = userProfileResult.orElseThrow();

        Organization organization = organizationRepository.findForMember(userProfile.id())
                .orElseThrow(() -> new AccessDeniedException("Tenant access denied"));

        Membership userMembership = organization.membershipFor(userProfile.id());

        return new CurrentIdentityContextResult.Active(userProfile.id(), organization.id(), organization.name(),
                userMembership.role());

    }
}
