package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.opsflow.opsflow_backend.testing.PostgreSqlTestConfiguration;
import com.opsflow.opsflow_backend.testing.SecurityTestConfiguration;

@SpringBootTest
@Import({ PostgreSqlTestConfiguration.class, SecurityTestConfiguration.class })
class IdentitySchemaConstraintsTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rejectsDuplicateExternalIdentity() {
        insertUserProfile(UUID.randomUUID(), "https://issuer.example", "subject-1");

        assertThrows(DataIntegrityViolationException.class,
                () -> insertUserProfile(UUID.randomUUID(), "https://issuer.example", "subject-1"));
    }

    @Test
    void allowsSameSubjectFromDifferentIssuers() {
        String subject = "shared-subject";

        insertUserProfile(UUID.randomUUID(), "https://issuer-a.example", subject);
        insertUserProfile(UUID.randomUUID(), "https://issuer-b.example", subject);
    }

    @Test
    void rejectsBlankExternalIdentityValues() {
        assertThrows(DataIntegrityViolationException.class,
                () -> insertUserProfile(UUID.randomUUID(), "   ", "subject-2"));
        assertThrows(DataIntegrityViolationException.class,
                () -> insertUserProfile(UUID.randomUUID(), "https://issuer.example", "   "));
    }

    @Test
    void rejectsBlankOrganizationName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> insertOrganization(UUID.randomUUID(), "   "));
    }

    @Test
    void rejectsOrganizationNameLongerThanDomainLimit() {
        assertThrows(DataIntegrityViolationException.class,
                () -> insertOrganization(UUID.randomUUID(), "a".repeat(121)));
    }

    @Test
    void allowsDifferentOrganizationsToHaveTheSameName() {
        insertOrganization(UUID.randomUUID(), "Shared Organization Name");
        insertOrganization(UUID.randomUUID(), "Shared Organization Name");
    }

    @Test
    void rejectsMembershipWithMissingReferences() {
        UUID missingUserProfileId = UUID.randomUUID();
        UUID missingOrganizationId = UUID.randomUUID();

        assertThrows(DataIntegrityViolationException.class,
                () -> insertMembership(
                        UUID.randomUUID(), missingUserProfileId, missingOrganizationId, "OWNER"));
    }

    @Test
    void rejectsDuplicateMembershipForUserAndOrganization() {
        UUID userProfileId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        insertUserProfile(userProfileId, "https://issuer.example", UUID.randomUUID().toString());
        insertOrganization(organizationId, "Example Organization");
        insertMembership(UUID.randomUUID(), userProfileId, organizationId, "OWNER");

        assertThrows(DataIntegrityViolationException.class,
                () -> insertMembership(UUID.randomUUID(), userProfileId, organizationId, "OWNER"));
    }

    @Test
    void rejectsUnknownMembershipRole() {
        UUID userProfileId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        insertUserProfile(userProfileId, "https://issuer.example", UUID.randomUUID().toString());
        insertOrganization(organizationId, "Another Organization");

        assertThrows(DataIntegrityViolationException.class,
                () -> insertMembership(UUID.randomUUID(), userProfileId, organizationId, "ADMIN"));
    }

    @Test
    void restrictsDeletingReferencedUserProfileAndOrganization() {
        UUID userProfileId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        insertUserProfile(userProfileId, "https://issuer.example", UUID.randomUUID().toString());
        insertOrganization(organizationId, "Protected Organization");
        insertMembership(UUID.randomUUID(), userProfileId, organizationId, "OWNER");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("DELETE FROM user_profiles WHERE id = ?", userProfileId));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("DELETE FROM organizations WHERE id = ?", organizationId));
    }

    @Test
    void indexesMembershipLookupByOrganization() {
        Boolean indexExists = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM pg_indexes
                            WHERE schemaname = current_schema()
                              AND tablename = 'memberships'
                              AND indexname = 'ix_memberships_organization_id'
                        )
                        """,
                Boolean.class);

        assertTrue(indexExists);
    }

    private void insertUserProfile(UUID id, String issuer, String subject) {
        jdbcTemplate.update(
                "INSERT INTO user_profiles (id, issuer, subject) VALUES (?, ?, ?)",
                id, issuer, subject);
    }

    private void insertOrganization(UUID id, String name) {
        jdbcTemplate.update(
                "INSERT INTO organizations (id, name) VALUES (?, ?)",
                id, name);
    }

    private void insertMembership(UUID id, UUID userProfileId, UUID organizationId, String role) {
        jdbcTemplate.update(
                """
                        INSERT INTO memberships (id, user_profile_id, organization_id, role)
                        VALUES (?, ?, ?, ?)
                        """,
                id, userProfileId, organizationId, role);
    }
}
