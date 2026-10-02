package com.opsflow.opsflow_backend.modules.identity.internal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.opsflow.opsflow_backend.modules.identity.api.AuthorizedTenant;
import com.opsflow.opsflow_backend.modules.identity.api.MembershipAuthority;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

@ExtendWith(MockitoExtension.class)
class TenantAuthorizationServiceTest {

    private static final ExternalIdentity CURRENT_IDENTITY =
            new ExternalIdentity("https://issuer.example", "subject-123");

    @Mock
    private CurrentExternalIdentityProvider currentExternalIdentityProvider;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private TenantAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new TenantAuthorizationService(
                currentExternalIdentityProvider,
                userProfileRepository,
                organizationRepository);
    }

    @Test
    void returnsAuthorizedTenantForActiveMembership() {
        UserProfile profile = UserProfile.create(CURRENT_IDENTITY);
        Organization organization = Organization.create(
                new OrganizationName("Authorized Organization"),
                profile.id());
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.of(profile));
        when(organizationRepository.findByIdForMember(organization.id(), profile.id()))
                .thenReturn(Optional.of(organization));

        AuthorizedTenant result = service.requireAccess(organization.id().value());

        assertEquals(profile.id().value(), result.userProfileId());
        assertEquals(organization.id().value(), result.organizationId());
        assertEquals(MembershipAuthority.OWNER, result.authority());
    }

    @Test
    void deniesAccessWhenAuthenticatedIdentityHasNoLocalProfile() {
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.requireAccess(OrganizationId.generate().value()));

        assertEquals("Tenant access denied", exception.getMessage());
        verifyNoInteractions(organizationRepository);
    }

    @Test
    void deniesAccessToOrganizationWithoutCurrentUsersMembership() {
        UserProfile profile = UserProfile.create(CURRENT_IDENTITY);
        OrganizationId requestedOrganizationId = OrganizationId.generate();
        when(currentExternalIdentityProvider.getCurrent()).thenReturn(CURRENT_IDENTITY);
        when(userProfileRepository.findByExternalIdentity(CURRENT_IDENTITY))
                .thenReturn(Optional.of(profile));
        when(organizationRepository.findByIdForMember(requestedOrganizationId, profile.id()))
                .thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.requireAccess(requestedOrganizationId.value()));

        assertEquals("Tenant access denied", exception.getMessage());
        verify(organizationRepository).findByIdForMember(requestedOrganizationId, profile.id());
    }
}
