package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserProfileJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "subject", nullable = false)
    private String subject;

    protected UserProfileJpaEntity() {
    }

    public UserProfileJpaEntity(UUID id, String issuer, String subject) {
        this.id = id;
        this.issuer = issuer;
        this.subject = subject;
    }

    public UUID getId() {
        return id;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getSubject() {
        return subject;
    }
}
