package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.opsflow.opsflow_backend.modules.identity.internal.application.OrganizationRepository;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;

import jakarta.persistence.EntityManager;

@Repository
public class JpaOrganizationRepositoryAdapter implements OrganizationRepository {

    private final OrganizationJpaRepository organizationJpaRepository;
    private final MembershipJpaRepository membershipJpaRepository;
    private final EntityManager entityManager;

    public JpaOrganizationRepositoryAdapter(OrganizationJpaRepository organizationJpaRepository,
            MembershipJpaRepository membershipJpaRepository, EntityManager entityManager) {
        this.organizationJpaRepository = organizationJpaRepository;
        this.membershipJpaRepository = membershipJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void save(Organization organization) {
        Objects.requireNonNull(organization, "organization must not be null");
        OrganizationJpaEntity entity = OrganizationPersistenceMapper.toJpaEntity(organization);

        entityManager.persist(entity);
        organization.memberships()
                .forEach(membership -> entityManager.persist(MembershipPersistenceMapper.toJpaEntity(membership)));
        organizationJpaRepository.flush();
    }

    @Override
    public Optional<Organization> findByIdForMember(OrganizationId organizationId, UserProfileId userProfileId) {
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(userProfileId, "userProfileId must not be null");

        Optional<OrganizationJpaEntity> organizationEntity = organizationJpaRepository
                .findByIdForMember(organizationId.value(), userProfileId.value());

        if (organizationEntity.isEmpty()) {
            return Optional.empty();
        }

        List<MembershipJpaEntity> membershipEntities = membershipJpaRepository
                .findAllByOrganizationId(organizationId.value());

        return Optional.of(OrganizationPersistenceMapper.toDomain(organizationEntity.get(), membershipEntities));

    }

}
