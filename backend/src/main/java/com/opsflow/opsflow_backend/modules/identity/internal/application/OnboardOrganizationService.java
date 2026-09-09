package com.opsflow.opsflow_backend.modules.identity.internal.application;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

@Service
public class OnboardOrganizationService {

    private final CurrentExternalIdentityProvider currentExternalIdentityProvider;
    private final UserProfileRepository userProfileRepository;
    private final OrganizationRepository organizationRepository;

    public OnboardOrganizationService(CurrentExternalIdentityProvider currentExternalIdentityProvider,
            UserProfileRepository userProfileRepository, OrganizationRepository organizationRepository) {

        this.currentExternalIdentityProvider = Objects.requireNonNull(currentExternalIdentityProvider,
                "currentExternalIdentityProvider must not be null");
        this.userProfileRepository = Objects.requireNonNull(userProfileRepository,
                "userProfileRepository must not be null");
        this.organizationRepository = Objects.requireNonNull(organizationRepository,
                "organizationRepository must not be null");
    }

    @Transactional
    public OnboardOrganizationResult onboard(OnboardOrganizationCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        OrganizationName organizationName = new OrganizationName(command.organizationName());

        ExternalIdentity currentExternalIdentity = currentExternalIdentityProvider.getCurrent();

        userProfileRepository.findByExternalIdentity(currentExternalIdentity)
                .ifPresent(userProfile -> {
                    throw new UserAlreadyOnboardedException();
                });

        UserProfile userProfile = UserProfile.create(currentExternalIdentity);
        Organization organization = Organization.create(organizationName, userProfile.id());

        Membership ownerMembership = organization.membershipFor(userProfile.id());

        try {
            userProfileRepository.save(userProfile);
            organizationRepository.save(organization);
        } catch (ExternalIdentityAlreadyRegisteredException e) {
            throw new UserAlreadyOnboardedException(e);

        }

        return new OnboardOrganizationResult(
                userProfile.id(),
                organization.id(),
                organization.name(),
                ownerMembership.id(),
                ownerMembership.role());
    }

}
