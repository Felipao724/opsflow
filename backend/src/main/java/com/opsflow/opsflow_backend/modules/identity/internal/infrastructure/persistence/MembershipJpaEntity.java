package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "memberships")
public class MembershipJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_profile_id", nullable = false)
    private UUID userProfileId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    protected MembershipJpaEntity() {
    }

    public MembershipJpaEntity(UUID id, UUID userProfileId, UUID organizationId, String role) {
        this.id = id;
        this.userProfileId = userProfileId;
        this.organizationId = organizationId;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserProfileId() {
        return userProfileId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getRole() {
        return role;
    }
}
