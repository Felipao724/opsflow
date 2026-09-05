package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, UUID> {

    @Query("""
                SELECT organization
                FROM OrganizationJpaEntity organization
                WHERE organization.id = :organizationId
                  AND EXISTS (
                      SELECT membership.id
                      FROM MembershipJpaEntity membership
                      WHERE membership.organizationId = organization.id
                        AND membership.userProfileId = :userProfileId
                  )
            """)
    Optional<OrganizationJpaEntity> findByIdForMember(
            @Param("organizationId") UUID organizationId,
            @Param("userProfileId") UUID userProfileId);
}
