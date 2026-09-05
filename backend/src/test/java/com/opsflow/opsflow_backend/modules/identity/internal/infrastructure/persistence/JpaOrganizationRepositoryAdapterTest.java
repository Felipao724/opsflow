package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import com.opsflow.opsflow_backend.modules.identity.internal.application.OrganizationRepository;
import com.opsflow.opsflow_backend.modules.identity.internal.application.UserProfileRepository;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipRole;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;
import com.opsflow.opsflow_backend.testing.PostgreSqlTestConfiguration;
import com.opsflow.opsflow_backend.testing.SecurityTestConfiguration;

@SpringBootTest
@Import({ PostgreSqlTestConfiguration.class, SecurityTestConfiguration.class })
class JpaOrganizationRepositoryAdapterTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void savesAndReconstructsOrganizationWithAllMemberships() {
        Fixture fixture = createFixtureWithTwoMembers();

        Organization loaded = transactionTemplate.execute(status -> organizationRepository
                .findByIdForMember(fixture.organization().id(), fixture.owner().id())
                .orElseThrow());

        assertNotSame(fixture.organization(), loaded);
        assertEquals(fixture.organization().id(), loaded.id());
        assertEquals(fixture.organization().name(), loaded.name());
        assertEquals(
                membershipIds(fixture.organization()),
                membershipIds(loaded));
        assertEquals(
                memberProfileIds(fixture.organization()),
                memberProfileIds(loaded));
        assertTrue(loaded.memberships().stream()
                .allMatch(membership -> membership.role() == MembershipRole.OWNER));
    }

    @Test
    void doesNotReturnOrganizationToNonMember() {
        Fixture fixture = createFixtureWithTwoMembers();
        UserProfileId nonMemberId = UserProfileId.generate();

        var result = transactionTemplate.execute(status -> organizationRepository
                .findByIdForMember(fixture.organization().id(), nonMemberId));

        assertTrue(result.isEmpty());
    }

    private Fixture createFixtureWithTwoMembers() {
        UserProfile owner = createProfile();
        UserProfile secondMember = createProfile();
        OrganizationId organizationId = OrganizationId.generate();
        Organization organization = new Organization(
                organizationId,
                new OrganizationName("Persistence Adapter Organization"),
                List.of(
                        new Membership(
                                MembershipId.generate(),
                                owner.id(),
                                organizationId,
                                MembershipRole.OWNER),
                        new Membership(
                                MembershipId.generate(),
                                secondMember.id(),
                                organizationId,
                                MembershipRole.OWNER)));

        transactionTemplate.executeWithoutResult(status -> {
            userProfileRepository.save(owner);
            userProfileRepository.save(secondMember);
            organizationRepository.save(organization);
        });

        return new Fixture(owner, organization);
    }

    private UserProfile createProfile() {
        return UserProfile.create(new ExternalIdentity(
                "https://issuer.example/realms/opsflow",
                UUID.randomUUID().toString()));
    }

    private Set<MembershipId> membershipIds(Organization organization) {
        return organization.memberships().stream()
                .map(Membership::id)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<UserProfileId> memberProfileIds(Organization organization) {
        return organization.memberships().stream()
                .map(Membership::userProfileId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private record Fixture(UserProfile owner, Organization organization) {
    }
}
