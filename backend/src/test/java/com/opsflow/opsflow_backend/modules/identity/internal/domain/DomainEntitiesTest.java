package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class DomainEntitiesTest {

    @Test
    void userProfileCreationGeneratesLocalIdentity() {
        ExternalIdentity externalIdentity = new ExternalIdentity("issuer", "subject");

        UserProfile profile = UserProfile.create(externalIdentity);

        assertNotNull(profile.id());
        assertEquals(externalIdentity, profile.externalIdentity());
    }

    @Test
    void userProfilesCompareOnlyByLocalIdentity() {
        UserProfileId id = UserProfileId.generate();
        UserProfile original = new UserProfile(id, new ExternalIdentity("issuer", "subject-a"));
        UserProfile reconstructed = new UserProfile(id, new ExternalIdentity("issuer", "subject-b"));

        assertEquals(original, reconstructed);
        assertEquals(original.hashCode(), reconstructed.hashCode());
        assertNotEquals(original, UserProfile.create(original.externalIdentity()));
    }

    @Test
    void ownerMembershipConnectsProfileAndOrganization() {
        UserProfileId userProfileId = UserProfileId.generate();
        OrganizationId organizationId = OrganizationId.generate();

        Membership membership = Membership.createOwner(userProfileId, organizationId);

        assertNotNull(membership.id());
        assertEquals(userProfileId, membership.userProfileId());
        assertEquals(organizationId, membership.organizationId());
        assertEquals(MembershipRole.OWNER, membership.role());
    }

    @Test
    void membershipsCompareOnlyByTheirIdentity() {
        MembershipId id = MembershipId.generate();
        Membership original = new Membership(
                id,
                UserProfileId.generate(),
                OrganizationId.generate(),
                MembershipRole.OWNER);
        Membership reconstructed = new Membership(
                id,
                UserProfileId.generate(),
                OrganizationId.generate(),
                MembershipRole.OWNER);

        assertEquals(original, reconstructed);
        assertEquals(original.hashCode(), reconstructed.hashCode());
    }

    @Test
    void organizationCreationIncludesItsOwnerMembership() {
        UserProfileId ownerId = UserProfileId.generate();
        OrganizationName name = new OrganizationName("Acme Operations");

        Organization organization = Organization.create(name, ownerId);

        assertNotNull(organization.id());
        assertEquals(name, organization.name());
        assertEquals(1, organization.memberships().size());
        Membership ownerMembership = organization.memberships().getFirst();
        assertEquals(ownerId, ownerMembership.userProfileId());
        assertEquals(organization.id(), ownerMembership.organizationId());
        assertEquals(MembershipRole.OWNER, ownerMembership.role());
    }

    @Test
    void organizationRejectsAnEmptyMembershipCollection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Organization(
                        OrganizationId.generate(),
                        new OrganizationName("Acme"),
                        List.of()));
    }

    @Test
    void organizationRejectsMembershipFromAnotherOrganization() {
        OrganizationId organizationId = OrganizationId.generate();
        Membership foreignMembership = Membership.createOwner(
                UserProfileId.generate(),
                OrganizationId.generate());

        assertThrows(
                IllegalArgumentException.class,
                () -> new Organization(
                        organizationId,
                        new OrganizationName("Acme"),
                        List.of(foreignMembership)));
    }

    @Test
    void organizationRejectsDuplicateMembershipRelationship() {
        OrganizationId organizationId = OrganizationId.generate();
        UserProfileId userProfileId = UserProfileId.generate();
        Membership firstMembership = Membership.createOwner(userProfileId, organizationId);
        Membership duplicateMembership = Membership.createOwner(userProfileId, organizationId);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Organization(
                        organizationId,
                        new OrganizationName("Acme"),
                        List.of(firstMembership, duplicateMembership)));
    }

    @Test
    void organizationDoesNotExposeAMutableMembershipCollection() {
        Organization organization = Organization.create(
                new OrganizationName("Acme"),
                UserProfileId.generate());
        Membership anotherMembership = Membership.createOwner(
                UserProfileId.generate(),
                organization.id());

        assertThrows(
                UnsupportedOperationException.class,
                () -> organization.memberships().add(anotherMembership));
    }
}
