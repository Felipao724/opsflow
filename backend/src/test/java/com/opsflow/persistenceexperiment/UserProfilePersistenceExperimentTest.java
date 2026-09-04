package com.opsflow.persistenceexperiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.flywaydb.core.Flyway;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfile;

class UserProfilePersistenceExperimentTest {

    @Test
    void persistsAndReconstructsDomainWithItsOriginalIdentity() {
        // Own database and migration location: never use the development datasource.
        try (var postgres = new PostgreSQLContainer("postgres:18.6")
                .withDatabaseName("persistence_experiment")
                .withUsername("experiment")
                .withPassword("experiment")) {
            postgres.start();

            Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("classpath:db/persistence-experiment")
                    .cleanDisabled(true)
                    .load()
                    .migrate();

            // Explicit bootstrap keeps the experiment independent of the application context.
            try (var factory = new HibernatePersistenceConfiguration("user-profile-experiment")
                    .managedClass(UserProfileJpaEntity.class)
                    .jdbcUrl(postgres.getJdbcUrl())
                    .jdbcUsername(postgres.getUsername())
                    .jdbcPassword(postgres.getPassword())
                    .schemaToolingAction(Action.VALIDATE)
                    .property("hibernate.cache.use_second_level_cache", false)
                    .property("hibernate.show_sql", true)
                    .createEntityManagerFactory();
                    var entityManager = factory.createEntityManager()) {

                UserProfile original = UserProfile.create(
                        new ExternalIdentity("https://identity.example.test/realms/opsflow", "subject-123"));
                UserProfileJpaEntity entity = UserProfilePersistenceMapper.toJpaEntity(original);

                var transaction = entityManager.getTransaction();
                try {
                    transaction.begin();
                    entityManager.persist(entity);
                    entityManager.flush();
                    transaction.commit();

                    // Force a fresh database read instead of returning the managed instance.
                    entityManager.clear();
                    assertFalse(entityManager.contains(entity));

                    transaction.begin();
                    UserProfileJpaEntity loaded = entityManager.find(
                            UserProfileJpaEntity.class, original.id().value());
                    assertNotNull(loaded);
                    assertNotSame(entity, loaded);

                    UserProfile reconstructed = UserProfilePersistenceMapper.toDomain(loaded);
                    transaction.commit();

                    assertNotSame(original, reconstructed);
                    assertEquals(original.id(), reconstructed.id());
                    // Entity equality only checks ID, so verify the remaining state explicitly.
                    assertEquals(original.externalIdentity(), reconstructed.externalIdentity());
                } finally {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                }
            }
        }
    }
}
