package com.opsflow.opsflow_backend.modules.identity.internal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

@ExtendWith(MockitoExtension.class)
class GetCurrentIdentityContextServiceTest {

    private static final ExternalIdentity CURRENT_IDENTITY =
            new ExternalIdentity("https://issuer.example", "subject-123");

    @Mock
    private CurrentExternalIdentityProvider currentExternalIdentityProvider;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private GetCurrentIdentityContextService service;

    @BeforeEach
    void setUp() {
        service = new GetCurrentIdentityContextService(
                currentExternalIdentityProvider,
                userProfileRepository,
                organizationRepository);
    }

    @Test
    void requiresOnboardingWhenCurrentIdentityHasNoLocalProfile() {
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.empty());

        CurrentIdentityContextResult result = service.getCurrent();

        assertInstanceOf(CurrentIdentityContextResult.OnboardingRequired.class, result);
        verifyNoInteractions(organizationRepository);
    }

    @Test
    void returnsActiveContextFromLocalMembership() {
        UserProfile userProfile = UserProfile.create(CURRENT_IDENTITY);
        Organization organization = Organization.create(
                new OrganizationName("Acme Operations"),
                userProfile.id());
        Membership membership = organization.membershipFor(userProfile.id());
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.of(userProfile));
        when(organizationRepository.findForMember(userProfile.id()))
                .thenReturn(Optional.of(organization));

        CurrentIdentityContextResult.Active result = assertInstanceOf(
                CurrentIdentityContextResult.Active.class,
                service.getCurrent());

        assertEquals(userProfile.id(), result.userProfileId());
        assertEquals(organization.id(), result.organizationId());
        assertEquals(organization.name(), result.organizationName());
        assertEquals(membership.role(), result.membershipRole());
    }

    @Test
    void rejectsLocalProfileWithoutOrganizationMembership() {
        UserProfile userProfile = UserProfile.create(CURRENT_IDENTITY);
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.of(userProfile));
        when(organizationRepository.findForMember(userProfile.id()))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                service::getCurrent);

        assertEquals(
                "User profile has no organization membership: " + userProfile.id(),
                exception.getMessage());
    }
}
