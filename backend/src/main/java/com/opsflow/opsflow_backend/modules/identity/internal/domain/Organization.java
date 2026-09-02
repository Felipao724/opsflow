package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Organization {

    private final OrganizationId id;
    private final OrganizationName name;
    private final List<Membership> memberships;

    public Organization(
            OrganizationId id,
            OrganizationName name,
            Collection<Membership> memberships) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.memberships = validateMemberships(this.id, memberships);
    }

    public static Organization create(
            OrganizationName name,
            UserProfileId ownerUserProfileId) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(
                ownerUserProfileId,
                "ownerUserProfileId must not be null");

        OrganizationId organizationId = OrganizationId.generate();
        Membership ownerMembership = Membership.createOwner(ownerUserProfileId, organizationId);

        return new Organization(
                organizationId,
                name,
                List.of(ownerMembership));
    }

    public OrganizationId id() {
        return id;
    }

    public OrganizationName name() {
        return name;
    }

    public List<Membership> memberships() {
        return memberships;
    }

    private static List<Membership> validateMemberships(OrganizationId organizationId,
            Collection<Membership> memberships) {
        Objects.requireNonNull(memberships, "memberships must not be null");

        if (memberships.isEmpty()) {
            throw new IllegalArgumentException(
                    "organization must have at least one membership");
        }

        Set<UserProfileId> userProfileIds = new HashSet<>();
        for (Membership membership : memberships) {

            if (membership == null) {
                throw new IllegalArgumentException("membership must not be null");
            }

            if (!membership.organizationId().equals(organizationId)) {
                throw new IllegalArgumentException(
                        "All memberships must belong to the same organization");
            }

            if (!userProfileIds.add(membership.userProfileId())) {
                throw new IllegalArgumentException(
                        "Duplicate userProfileId found in memberships");
            }
        }

        return List.copyOf(memberships);

    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }

        if (!(candidate instanceof Organization organization)) {
            return false;
        }

        return id.equals(organization.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}