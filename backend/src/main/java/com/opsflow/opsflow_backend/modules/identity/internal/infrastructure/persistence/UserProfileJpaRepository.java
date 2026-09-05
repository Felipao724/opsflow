package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, UUID> {

    Optional<UserProfileJpaEntity> findByIssuerAndSubject(
            String issuer,
            String subject);

}
