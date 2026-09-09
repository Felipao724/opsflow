package com.opsflow.opsflow_backend.modules.identity.internal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.Organization;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;
import com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.opsflow.opsflow_backend.testing.PostgreSqlTestConfiguration;
import com.opsflow.opsflow_backend.testing.SecurityTestConfiguration;

@SpringBootTest
@Import({
        PostgreSqlTestConfiguration.class,
        SecurityTestConfiguration.class,
        OnboardOrganizationTransactionTest.FailureInjectionConfiguration.class
})
class OnboardOrganizationTransactionTest {

    @Autowired
    private OnboardOrganizationService service;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private FailureInjectingOrganizationRepository organizationRepository;

    @AfterEach
    void cleanThreadState() {
        organizationRepository.failAfterSave(false);
        SecurityContextHolder.clearContext();
    }

    @Test
    void commitsProfileOrganizationAndOwnerMembershipTogether() {
        authenticate("transaction-success-subject");

        OnboardOrganizationResult result = service.onboard(
                new OnboardOrganizationCommand("Transactional Success Organization"));

        assertEquals(1L, count(
                "SELECT count(*) FROM user_profiles WHERE id = :id",
                result.userProfileId().value()));
        assertEquals(1L, count(
                "SELECT count(*) FROM organizations WHERE id = :id",
                result.organizationId().value()));
        assertEquals(1L, count(
                "SELECT count(*) FROM memberships WHERE id = :id",
                result.ownerMembershipId().value()));
    }

    @Test
    void rollsBackAllRowsWhenFailureOccursAfterOrganizationSave() {
        String subject = "transaction-rollback-subject";
        String organizationName = "Transactional Rollback Organization";
        authenticate(subject);
        organizationRepository.failAfterSave(true);

        assertThrows(
                SimulatedPostPersistenceFailure.class,
                () -> service.onboard(new OnboardOrganizationCommand(organizationName)));

        assertEquals(0L, jdbcClient.sql("""
                        SELECT count(*)
                        FROM user_profiles
                        WHERE issuer = :issuer AND subject = :subject
                        """)
                .param("issuer", "https://identity.example/realms/opsflow")
                .param("subject", subject)
                .query(Long.class)
                .single());
        assertEquals(0L, jdbcClient.sql("""
                        SELECT count(*)
                        FROM organizations
                        WHERE name = :name
                        """)
                .param("name", organizationName)
                .query(Long.class)
                .single());
        assertEquals(0L, jdbcClient.sql("""
                        SELECT count(*)
                        FROM memberships membership
                        JOIN organizations organization
                          ON organization.id = membership.organization_id
                        WHERE organization.name = :name
                        """)
                .param("name", organizationName)
                .query(Long.class)
                .single());
    }

    private long count(String sql, Object id) {
        return jdbcClient.sql(sql)
                .param("id", id)
                .query(Long.class)
                .single();
    }

    private static void authenticate(String subject) {
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .issuer("https://identity.example/realms/opsflow")
                .subject(subject)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(
                        jwt,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureInjectionConfiguration {

        @Bean
        @Primary
        FailureInjectingOrganizationRepository failureInjectingOrganizationRepository(
                JpaOrganizationRepositoryAdapter delegate) {
            return new FailureInjectingOrganizationRepository(delegate);
        }
    }

    static final class FailureInjectingOrganizationRepository
            implements OrganizationRepository {

        private final JpaOrganizationRepositoryAdapter delegate;
        private boolean failAfterSave;

        FailureInjectingOrganizationRepository(
                JpaOrganizationRepositoryAdapter delegate) {
            this.delegate = delegate;
        }

        void failAfterSave(boolean failAfterSave) {
            this.failAfterSave = failAfterSave;
        }

        @Override
        public void save(Organization organization) {
            delegate.save(organization);
            if (failAfterSave) {
                throw new SimulatedPostPersistenceFailure();
            }
        }

        @Override
        public Optional<Organization> findByIdForMember(
                OrganizationId organizationId,
                UserProfileId userProfileId) {
            return delegate.findByIdForMember(organizationId, userProfileId);
        }
    }

    static final class SimulatedPostPersistenceFailure extends RuntimeException {
    }
}
