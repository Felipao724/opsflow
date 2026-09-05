package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import java.util.Objects;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.opsflow.opsflow_backend.modules.identity.internal.application.ExternalIdentityAlreadyRegisteredException;
import com.opsflow.opsflow_backend.modules.identity.internal.application.UserProfileRepository;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

import jakarta.persistence.EntityManager;

@Repository
public class JpaUserProfileRepositoryAdapter implements UserProfileRepository {

    private static final String EXTERNAL_IDENTITY_UNIQUE_CONSTRAINT = "uq_user_profiles_external_identity";

    private final UserProfileJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public JpaUserProfileRepositoryAdapter(
            UserProfileJpaRepository jpaRepository,
            EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UserProfile> findByExternalIdentity(
            ExternalIdentity externalIdentity) {
        Objects.requireNonNull(
                externalIdentity,
                "externalIdentity must not be null");

        return jpaRepository
                .findByIssuerAndSubject(
                        externalIdentity.issuer(),
                        externalIdentity.subject())
                .map(UserProfilePersistenceMapper::toDomain);
    }

    @Override
    public void save(UserProfile userProfile) {
        UserProfileJpaEntity entity = UserProfilePersistenceMapper.toJpaEntity(userProfile);

        try {
            entityManager.persist(entity);
            jpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            if (isCausedByConstraint(exception, EXTERNAL_IDENTITY_UNIQUE_CONSTRAINT)) {
                throw new ExternalIdentityAlreadyRegisteredException(exception);
            }

            throw exception;
        }
    }

    private static boolean isCausedByConstraint(Throwable failure, String constraintName) {
        Throwable current = failure;

        while (current != null) {
            if (current instanceof ConstraintViolationException violation
                    && constraintName.equals(violation.getConstraintName())) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
