package com.opsflow.opsflow_backend.modules.identity.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class DomainValueObjectsTest {

    @Test
    void externalIdentityUsesIssuerAndSubjectTogetherForEquality() {
        ExternalIdentity identity = new ExternalIdentity("issuer-a", "subject-1");

        assertEquals(identity, new ExternalIdentity("issuer-a", "subject-1"));
        assertNotEquals(identity, new ExternalIdentity("issuer-b", "subject-1"));
        assertNotEquals(identity, new ExternalIdentity("issuer-a", "subject-2"));
    }

    @Test
    void externalIdentityRejectsMissingComponents() {
        assertThrows(IllegalArgumentException.class, () -> new ExternalIdentity(null, "subject"));
        assertThrows(IllegalArgumentException.class, () -> new ExternalIdentity("   ", "subject"));
        assertThrows(IllegalArgumentException.class, () -> new ExternalIdentity("issuer", null));
        assertThrows(IllegalArgumentException.class, () -> new ExternalIdentity("issuer", "   "));
    }

    @Test
    void organizationNameNormalizesOuterUnicodeWhitespace() {
        OrganizationName name = new OrganizationName("\u2003 Acme Operations \u2003");

        assertEquals("Acme Operations", name.value());
    }

    @Test
    void organizationNameRejectsMissingOrBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> new OrganizationName(null));
        assertThrows(IllegalArgumentException.class, () -> new OrganizationName("   "));
    }

    @Test
    void organizationNameMeasuresUnicodeCodePoints() {
        String maximumValidName = "😀".repeat(OrganizationName.MAX_LENGTH);
        String oversizedName = maximumValidName + "😀";

        assertEquals(maximumValidName, new OrganizationName(maximumValidName).value());
        assertThrows(IllegalArgumentException.class, () -> new OrganizationName(oversizedName));
    }

    @Test
    void typedIdentifiersCompareByTheirWrappedUuid() {
        UUID value = UUID.randomUUID();

        assertEquals(new UserProfileId(value), new UserProfileId(value));
        assertEquals(new OrganizationId(value), new OrganizationId(value));
        assertEquals(new MembershipId(value), new MembershipId(value));
    }

    @Test
    void typedIdentifiersRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new UserProfileId(null));
        assertThrows(NullPointerException.class, () -> new OrganizationId(null));
        assertThrows(NullPointerException.class, () -> new MembershipId(null));
    }
}
