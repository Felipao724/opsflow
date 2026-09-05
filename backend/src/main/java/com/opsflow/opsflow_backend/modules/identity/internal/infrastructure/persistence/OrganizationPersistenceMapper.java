package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.Collection;
import java.util.Objects;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;

public final class OrganizationPersistenceMapper {

    private OrganizationPersistenceMapper() {
    }

    public static OrganizationJpaEntity toJpaEntity(Organization organization) {
        Objects.requireNonNull(organization, "organization must not be null");

        return new OrganizationJpaEntity(
                organization.id().value(),
                organization.name().value());
    }

    public static Organization toDomain(OrganizationJpaEntity entity,
            Collection<MembershipJpaEntity> membershipEntities) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(membershipEntities, "membershipEntities must not be null");

        return new Organization(
                new OrganizationId(entity.getId()),
                new OrganizationName(entity.getName()),
                membershipEntities.stream()
                        .map(MembershipPersistenceMapper::toDomain)
                        .toList());
    }

}
