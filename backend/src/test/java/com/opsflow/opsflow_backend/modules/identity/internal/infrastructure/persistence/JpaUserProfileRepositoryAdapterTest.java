package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import com.opsflow.opsflow_backend.modules.identity.internal.application.UserProfileRepository;
import com.opsflow.opsflow_backend.modules.identity.internal.application.ExternalIdentityAlreadyRegisteredException;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;
import com.opsflow.opsflow_backend.testing.PostgreSqlTestConfiguration;
import com.opsflow.opsflow_backend.testing.SecurityTestConfiguration;

@SpringBootTest
@Import({ PostgreSqlTestConfiguration.class, SecurityTestConfiguration.class })
class JpaUserProfileRepositoryAdapterTest {

    @Autowired
    private UserProfileRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void savesAndFindsProfileByExternalIdentity() {
        ExternalIdentity externalIdentity = uniqueExternalIdentity();
        UserProfile original = UserProfile.create(externalIdentity);

        transactionTemplate.executeWithoutResult(status -> repository.save(original));

        UserProfile loaded = transactionTemplate.execute(status -> repository
                .findByExternalIdentity(externalIdentity)
                .orElseThrow());

        assertNotSame(original, loaded);
        assertEquals(original.id(), loaded.id());
        assertEquals(original.externalIdentity(), loaded.externalIdentity());
    }

    @Test
    void returnsEmptyWhenExternalIdentityDoesNotExist() {
        ExternalIdentity missingIdentity = uniqueExternalIdentity();

        var result = transactionTemplate.execute(
                status -> repository.findByExternalIdentity(missingIdentity));

        assertTrue(result.isEmpty());
    }

    @Test
    void translatesDuplicateExternalIdentityIntoApplicationException() {
        ExternalIdentity externalIdentity = uniqueExternalIdentity();
        UserProfile firstProfile = UserProfile.create(externalIdentity);
        UserProfile duplicateProfile = UserProfile.create(externalIdentity);
        transactionTemplate.executeWithoutResult(status -> repository.save(firstProfile));

        ExternalIdentityAlreadyRegisteredException exception = assertThrows(
                ExternalIdentityAlreadyRegisteredException.class,
                () -> transactionTemplate.executeWithoutResult(
                        status -> repository.save(duplicateProfile)));

        assertEquals(
                "A user profile already exists for the external identity",
                exception.getMessage());
    }

    private ExternalIdentity uniqueExternalIdentity() {
        return new ExternalIdentity(
                "https://issuer.example/realms/opsflow",
                UUID.randomUUID().toString());
    }
}
