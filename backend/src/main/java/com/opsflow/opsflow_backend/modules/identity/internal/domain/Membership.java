package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import java.util.Objects;

public final class Membership {

    private final MembershipId id;
    private final UserProfileId userProfileId;
    private final OrganizationId organizationId;
    private final MembershipRole role;

    public Membership(MembershipId id, UserProfileId userProfileId, OrganizationId organizationId,
            MembershipRole role) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userProfileId = Objects.requireNonNull(userProfileId, "userProfileId must not be null");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
    }

    public static Membership createOwner(UserProfileId userProfileId, OrganizationId organizationId) {
        return new Membership(
                MembershipId.generate(),
                userProfileId,
                organizationId,
                MembershipRole.OWNER);
    }

    public MembershipId id() {
        return id;
    }

    public UserProfileId userProfileId() {
        return userProfileId;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public MembershipRole role() {
        return role;
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }

        if (!(candidate instanceof Membership membership)) {
            return false;
        }

        return id.equals(membership.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}