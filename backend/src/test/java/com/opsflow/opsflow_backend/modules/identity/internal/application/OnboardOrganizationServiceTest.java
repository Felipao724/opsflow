package com.opsflow.opsflow_backend.modules.identity.internal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipRole;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

@ExtendWith(MockitoExtension.class)
class OnboardOrganizationServiceTest {

    private static final ExternalIdentity CURRENT_IDENTITY =
            new ExternalIdentity("https://issuer.example", "subject-123");

    @Mock
    private CurrentExternalIdentityProvider currentExternalIdentityProvider;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private OnboardOrganizationService service;

    @BeforeEach
    void setUp() {
        service = new OnboardOrganizationService(
                currentExternalIdentityProvider,
                userProfileRepository,
                organizationRepository);
    }

    @Test
    void onboardsAuthenticatedUserAsOrganizationOwner() {
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.empty());

        OnboardOrganizationResult result = service.onboard(
                new OnboardOrganizationCommand("  Acme Operations  "));

        ArgumentCaptor<UserProfile> userCaptor = ArgumentCaptor.forClass(UserProfile.class);
        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(userProfileRepository).save(userCaptor.capture());
        verify(organizationRepository).save(organizationCaptor.capture());

        UserProfile savedUser = userCaptor.getValue();
        Organization savedOrganization = organizationCaptor.getValue();
        Membership owner = savedOrganization.membershipFor(savedUser.id());

        assertEquals(CURRENT_IDENTITY, savedUser.externalIdentity());
        assertEquals("Acme Operations", savedOrganization.name().value());
        assertEquals(MembershipRole.OWNER, owner.role());
        assertEquals(savedUser.id(), result.userProfileId());
        assertEquals(savedOrganization.id(), result.organizationId());
        assertEquals(savedOrganization.name(), result.organizationName());
        assertEquals(owner.id(), result.ownerMembershipId());
        assertEquals(owner.role(), result.membershipRole());
    }

    @Test
    void rejectsUserThatAlreadyHasALocalProfile() {
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.of(UserProfile.create(CURRENT_IDENTITY)));

        assertThrows(
                UserAlreadyOnboardedException.class,
                () -> service.onboard(new OnboardOrganizationCommand("Acme")));

        verify(userProfileRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidOrganizationNameBeforeResolvingIdentityOrPersisting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.onboard(new OnboardOrganizationCommand("   ")));

        verify(currentExternalIdentityProvider, never()).getCurrent();
        verify(userProfileRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void translatesConcurrentExternalIdentityRegistrationIntoOnboardingConflict() {
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.empty());
        ExternalIdentityAlreadyRegisteredException persistenceConflict =
                new ExternalIdentityAlreadyRegisteredException(
                        new IllegalStateException("unique constraint"));
        org.mockito.Mockito.doThrow(persistenceConflict)
                .when(userProfileRepository)
                .save(any());

        UserAlreadyOnboardedException exception = assertThrows(
                UserAlreadyOnboardedException.class,
                () -> service.onboard(new OnboardOrganizationCommand("Acme")));

        assertSame(persistenceConflict, exception.getCause());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void rejectsNullCommandBeforeUsingDependencies() {
        assertThrows(NullPointerException.class, () -> service.onboard(null));

        verify(currentExternalIdentityProvider, never()).getCurrent();
        verify(userProfileRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());
    }
}
