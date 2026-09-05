package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.Objects;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipRole;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;

public final class MembershipPersistenceMapper {

    private MembershipPersistenceMapper() {
    }

    public static MembershipJpaEntity toJpaEntity(Membership membership) {
        Objects.requireNonNull(membership, "membership must not be null");

        return new MembershipJpaEntity(
                membership.id().value(),
                membership.userProfileId().value(),
                membership.organizationId().value(),
                membership.role().name());
    }

    public static Membership toDomain(MembershipJpaEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return new Membership(
                new MembershipId(entity.getId()),
                new UserProfileId(entity.getUserProfileId()),
                new OrganizationId(entity.getOrganizationId()),
                MembershipRole.valueOf(entity.getRole()));
    }

}
