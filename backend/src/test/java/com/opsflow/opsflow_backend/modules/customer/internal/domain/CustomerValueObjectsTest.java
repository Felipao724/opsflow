package com.opsflow.opsflow_backend.modules.customer.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerValueObjectsTest {

    @Test
    void generatedIdentifiersContainValues() {
        assertNotNull(CustomerId.generate().value());
        assertNotNull(ContactId.generate().value());
    }

    @Test
    void typedIdentifiersCompareByTheirWrappedUuid() {
        UUID value = UUID.randomUUID();

        assertEquals(new CustomerId(value), new CustomerId(value));
        assertEquals(new ContactId(value), new ContactId(value));
        assertEquals(new OrganizationId(value), new OrganizationId(value));
        assertNotEquals(new CustomerId(value), CustomerId.generate());
    }

    @Test
    void typedIdentifiersRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new CustomerId(null));
        assertThrows(NullPointerException.class, () -> new ContactId(null));
        assertThrows(NullPointerException.class, () -> new OrganizationId(null));
    }

    @Test
    void namesNormalizeOuterUnicodeWhitespace() {
        assertEquals("Acme", new CustomerName("\u2003 Acme \u2003").value());
        assertEquals("Ana", new ContactName("\u2003 Ana \u2003").value());
    }

    @Test
    void namesRejectMissingOrBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerName(null));
        assertThrows(IllegalArgumentException.class, () -> new CustomerName("   "));
        assertThrows(IllegalArgumentException.class, () -> new ContactName(null));
        assertThrows(IllegalArgumentException.class, () -> new ContactName("   "));
    }

    @Test
    void namesMeasureUnicodeCodePoints() {
        String maximumCustomerName = "😀".repeat(CustomerName.MAX_LENGTH);
        String maximumContactName = "😀".repeat(ContactName.MAX_LENGTH);

        assertEquals(maximumCustomerName, new CustomerName(maximumCustomerName).value());
        assertEquals(maximumContactName, new ContactName(maximumContactName).value());
        assertThrows(
                IllegalArgumentException.class,
                () -> new CustomerName(maximumCustomerName + "😀"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ContactName(maximumContactName + "😀"));
    }

    @Test
    void emailAddressNormalizesOuterWhitespaceAndPreservesCase() {
        EmailAddress emailAddress = new EmailAddress("  Ana.Lopez@Example.com  ");

        assertEquals("Ana.Lopez@Example.com", emailAddress.value());
    }

    @Test
    void emailAddressAcceptsItsMaximumLength() {
        String maximumEmail = "a".repeat(242) + "@example.com";

        assertEquals(EmailAddress.MAX_LENGTH, maximumEmail.length());
        assertEquals(maximumEmail, new EmailAddress(maximumEmail).value());
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmailAddress("a" + maximumEmail));
    }

    @Test
    void emailAddressRejectsMissingOrMalformedValues() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(null));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("   "));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("ana.example.com"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("ana@@example.com"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("ana@example"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("ana lopez@example.com"));
    }

    @Test
    void mexicanPhoneNumberAcceptsExactlyTenAsciiDigits() {
        assertEquals("6141234567", new MexicanPhoneNumber("6141234567").value());
    }

    @Test
    void mexicanPhoneNumberRejectsOtherFormats() {
        assertThrows(IllegalArgumentException.class, () -> new MexicanPhoneNumber(null));
        assertThrows(IllegalArgumentException.class, () -> new MexicanPhoneNumber("614123456"));
        assertThrows(IllegalArgumentException.class, () -> new MexicanPhoneNumber("61412345678"));
        assertThrows(IllegalArgumentException.class, () -> new MexicanPhoneNumber(" 6141234567 "));
        assertThrows(IllegalArgumentException.class, () -> new MexicanPhoneNumber("+526141234567"));
        assertThrows(IllegalArgumentException.class, () -> new MexicanPhoneNumber("614-123-4567"));
        assertThrows(IllegalArgumentException.class, () -> new MexicanPhoneNumber("٦١٤١٢٣٤٥٦٧"));
    }
}
