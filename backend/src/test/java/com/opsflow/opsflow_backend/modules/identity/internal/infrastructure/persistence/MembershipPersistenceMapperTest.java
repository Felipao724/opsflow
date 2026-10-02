package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.Membership;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipRole;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipStatus;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;

class MembershipPersistenceMapperTest {

    @Test
    void preservesMembershipDataWhenMappingToPersistenceAndBack() {
        Membership membership = new Membership(
                MembershipId.generate(),
                UserProfileId.generate(),
                OrganizationId.generate(),
                MembershipRole.OWNER,
                MembershipStatus.INACTIVE);

        MembershipJpaEntity entity = MembershipPersistenceMapper.toJpaEntity(membership);
        Membership reconstructed = MembershipPersistenceMapper.toDomain(entity);

        assertEquals(membership.id(), reconstructed.id());
        assertEquals(membership.userProfileId(), reconstructed.userProfileId());
        assertEquals(membership.organizationId(), reconstructed.organizationId());
        assertEquals(membership.role(), reconstructed.role());
        assertEquals(membership.status(), reconstructed.status());
    }
}
