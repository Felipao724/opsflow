package com.opsflow.opsflow_backend.modules.customer.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerAggregateTest {

    @Test
    void contactCreationGeneratesIdentityAndAcceptsEitherContactMethod() {
        Contact withEmail = Contact.create(
                new ContactName("Ana"),
                new EmailAddress("ana@example.com"),
                null);
        Contact withPhone = Contact.create(
                new ContactName("Luis"),
                null,
                new MexicanPhoneNumber("6141234567"));

        assertNotNull(withEmail.id());
        assertTrue(withEmail.emailAddress().isPresent());
        assertTrue(withEmail.phoneNumber().isEmpty());
        assertTrue(withPhone.emailAddress().isEmpty());
        assertTrue(withPhone.phoneNumber().isPresent());
    }

    @Test
    void contactRejectsTheAbsenceOfEveryContactMethod() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Contact.create(new ContactName("Ana"), null, null));
    }

    @Test
    void contactUpdateReplacesItsDetails() {
        Contact contact = emailContact("Ana", "ana@example.com");

        contact.update(
                new ContactName("Ana López"),
                null,
                new MexicanPhoneNumber("6141234567"));

        assertEquals(new ContactName("Ana López"), contact.name());
        assertTrue(contact.emailAddress().isEmpty());
        assertEquals(
                new MexicanPhoneNumber("6141234567"),
                contact.phoneNumber().orElseThrow());
    }

    @Test
    void invalidContactUpdatePreservesItsPreviousState() {
        Contact contact = emailContact("Ana", "ana@example.com");

        assertThrows(
                IllegalArgumentException.class,
                () -> contact.update(new ContactName("Changed"), null, null));

        assertEquals(new ContactName("Ana"), contact.name());
        assertEquals(new EmailAddress("ana@example.com"), contact.emailAddress().orElseThrow());
    }

    @Test
    void contactsCompareOnlyByIdentity() {
        ContactId id = ContactId.generate();
        Contact original = new Contact(
                id,
                new ContactName("Ana"),
                new EmailAddress("ana@example.com"),
                null);
        Contact reconstructed = new Contact(
                id,
                new ContactName("Changed"),
                null,
                new MexicanPhoneNumber("6141234567"));

        assertEquals(original, reconstructed);
        assertEquals(original.hashCode(), reconstructed.hashCode());
        assertNotEquals(original, emailContact("Ana", "ana@example.com"));
    }

    @Test
    void customerCreationStartsActiveWithItsInitialContact() {
        OrganizationId organizationId = new OrganizationId(UUID.randomUUID());
        Contact initialContact = emailContact("Ana", "ana@example.com");

        Customer customer = Customer.create(
                organizationId,
                new CustomerName("Acme"),
                initialContact);

        assertNotNull(customer.id());
        assertEquals(organizationId, customer.organizationId());
        assertEquals(new CustomerName("Acme"), customer.name());
        assertEquals(CustomerStatus.ACTIVE, customer.status());
        assertTrue(customer.isActive());
        assertEquals(List.of(initialContact), customer.contacts());
    }

    @Test
    void reconstructedCustomerCopiesItsContactCollection() {
        Contact contact = emailContact("Ana", "ana@example.com");
        List<Contact> source = new ArrayList<>(List.of(contact));
        Customer customer = reconstructedCustomer(CustomerStatus.ACTIVE, source);

        source.add(emailContact("Luis", "luis@example.com"));

        assertEquals(List.of(contact), customer.contacts());
        assertThrows(
                UnsupportedOperationException.class,
                () -> customer.contacts().add(emailContact("Eva", "eva@example.com")));
    }

    @Test
    void reconstructionRejectsInvalidContactCollections() {
        ContactId duplicateId = ContactId.generate();
        Contact first = new Contact(
                duplicateId,
                new ContactName("Ana"),
                new EmailAddress("ana@example.com"),
                null);
        Contact duplicate = new Contact(
                duplicateId,
                new ContactName("Luis"),
                null,
                new MexicanPhoneNumber("6141234567"));

        assertThrows(
                IllegalArgumentException.class,
                () -> reconstructedCustomer(CustomerStatus.ACTIVE, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> reconstructedCustomer(CustomerStatus.ACTIVE, List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> reconstructedCustomer(
                        CustomerStatus.ACTIVE,
                        java.util.Arrays.asList(first, null)));
        assertThrows(
                IllegalArgumentException.class,
                () -> reconstructedCustomer(CustomerStatus.ACTIVE, List.of(first, duplicate)));
    }

    @Test
    void customerRenamesItself() {
        Customer customer = activeCustomer();

        customer.rename(new CustomerName("New name"));

        assertEquals(new CustomerName("New name"), customer.name());
        assertThrows(NullPointerException.class, () -> customer.rename(null));
    }

    @Test
    void customerTransitionsBetweenActiveAndInactive() {
        Customer customer = activeCustomer();

        customer.deactivate();

        assertEquals(CustomerStatus.INACTIVE, customer.status());
        assertFalse(customer.isActive());
        assertThrows(IllegalStateException.class, customer::deactivate);

        customer.reactivate();

        assertEquals(CustomerStatus.ACTIVE, customer.status());
        assertTrue(customer.isActive());
        assertThrows(IllegalStateException.class, customer::reactivate);
    }

    @Test
    void customerFindsAContactUsingAnEquivalentIdentifierInstance() {
        Contact contact = emailContact("Ana", "ana@example.com");
        Customer customer = Customer.create(
                new OrganizationId(UUID.randomUUID()),
                new CustomerName("Acme"),
                contact);
        ContactId equivalentId = new ContactId(contact.id().value());

        assertSame(contact, customer.contactFor(equivalentId));
        assertThrows(IllegalArgumentException.class, () -> customer.contactFor(ContactId.generate()));
    }

    @Test
    void customerAddsAContactAndReturnsIt() {
        Customer customer = activeCustomer();

        Contact added = customer.addContact(
                new ContactName("Luis"),
                null,
                new MexicanPhoneNumber("6141234567"));

        assertSame(added, customer.contactFor(added.id()));
        assertEquals(2, customer.contacts().size());
    }

    @Test
    void customerUpdatesAContactThroughTheAggregate() {
        Customer customer = activeCustomer();
        Contact contact = customer.contacts().getFirst();

        customer.updateContact(
                contact.id(),
                new ContactName("Ana López"),
                null,
                new MexicanPhoneNumber("6141234567"));

        assertEquals(new ContactName("Ana López"), contact.name());
        assertTrue(contact.emailAddress().isEmpty());
        assertTrue(contact.phoneNumber().isPresent());
    }

    @Test
    void customerRemovesAContactButNeverItsLastOne() {
        Customer customer = activeCustomer();
        Contact initialContact = customer.contacts().getFirst();
        Contact added = customer.addContact(
                new ContactName("Luis"),
                new EmailAddress("luis@example.com"),
                null);

        customer.removeContact(added.id());

        assertEquals(List.of(initialContact), customer.contacts());
        assertThrows(IllegalStateException.class, () -> customer.removeContact(initialContact.id()));
        assertEquals(List.of(initialContact), customer.contacts());
    }

    @Test
    void removalReportsAnUnknownContactBeforeTheLastContactInvariant() {
        Customer customer = activeCustomer();

        assertThrows(IllegalArgumentException.class, () -> customer.removeContact(ContactId.generate()));
    }

    @Test
    void customersCompareOnlyByIdentity() {
        CustomerId id = CustomerId.generate();
        OrganizationId organizationId = new OrganizationId(UUID.randomUUID());
        Contact contact = emailContact("Ana", "ana@example.com");
        Customer original = new Customer(
                id,
                organizationId,
                new CustomerName("Acme"),
                CustomerStatus.ACTIVE,
                List.of(contact));
        Customer reconstructed = new Customer(
                id,
                organizationId,
                new CustomerName("Changed"),
                CustomerStatus.INACTIVE,
                List.of(contact));

        assertEquals(original, reconstructed);
        assertEquals(original.hashCode(), reconstructed.hashCode());
        assertNotEquals(original, activeCustomer());
    }

    private static Customer activeCustomer() {
        return Customer.create(
                new OrganizationId(UUID.randomUUID()),
                new CustomerName("Acme"),
                emailContact("Ana", "ana@example.com"));
    }

    private static Customer reconstructedCustomer(CustomerStatus status, List<Contact> contacts) {
        return new Customer(
                CustomerId.generate(),
                new OrganizationId(UUID.randomUUID()),
                new CustomerName("Acme"),
                status,
                contacts);
    }

    private static Contact emailContact(String name, String emailAddress) {
        return Contact.create(
                new ContactName(name),
                new EmailAddress(emailAddress),
                null);
    }
}
