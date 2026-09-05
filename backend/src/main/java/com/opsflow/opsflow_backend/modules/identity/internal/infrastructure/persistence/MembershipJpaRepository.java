package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipJpaRepository extends JpaRepository<MembershipJpaEntity, UUID> {

    List<MembershipJpaEntity> findAllByOrganizationId(
            UUID organizationId);

}
