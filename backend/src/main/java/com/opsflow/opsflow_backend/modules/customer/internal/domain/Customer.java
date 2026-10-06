package com.opsflow.opsflow_backend.modules.customer.internal.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Customer {

    private final CustomerId id;
    private final OrganizationId organizationId;
    private CustomerName name;
    private CustomerStatus status;
    private final List<Contact> contacts;

    public Customer(CustomerId id, OrganizationId organizationId, CustomerName name, CustomerStatus status,
            Collection<Contact> contacts) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");

        validateContacts(contacts);
        this.contacts = new ArrayList<>(contacts);
    }

    public static Customer create(OrganizationId organizationId, CustomerName name, Contact initialContact) {
        return new Customer(CustomerId.generate(), organizationId, name, CustomerStatus.ACTIVE,
                List.of(initialContact));
    }

    public void rename(CustomerName newName) {
        Objects.requireNonNull(newName, "newName must not be null");
        this.name = newName;
    }

    public void deactivate() {
        if (status == CustomerStatus.INACTIVE) {
            throw new IllegalStateException("customer is already inactive");
        }
        this.status = CustomerStatus.INACTIVE;
    }

    public void reactivate() {
        if (status == CustomerStatus.ACTIVE) {
            throw new IllegalStateException("customer is already active");
        }
        this.status = CustomerStatus.ACTIVE;
    }

    public Contact contactFor(ContactId contactId) {
        Objects.requireNonNull(contactId, "contact id must not be null");

        return contacts.stream().filter(contact -> contact.id().equals(contactId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("The contact for %s does not exists", contactId)));
    }

    public Contact addContact(ContactName name, EmailAddress emailAddress, MexicanPhoneNumber phoneNumber) {
        Contact contact = Contact.create(name, emailAddress, phoneNumber);

        contacts.add(contact);

        return contact;
    }

    public void removeContact(ContactId contactId) {
        Contact contact = contactFor(contactId);

        if (contacts.size() == 1) {
            throw new IllegalStateException("customer must retain at least one contact");
        }

        contacts.remove(contact);

    }

    public void updateContact(ContactId contactId, ContactName name, EmailAddress emailAddress,
            MexicanPhoneNumber phoneNumber) {
        contactFor(contactId).update(name, emailAddress, phoneNumber);
    }

    private static void validateContacts(Collection<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            throw new IllegalArgumentException(
                    "contacts must not be null or empty");
        }

        Set<ContactId> contactIds = new HashSet<>();

        for (Contact contact : contacts) {
            if (contact == null) {
                throw new IllegalArgumentException(
                        "contacts must not contain null elements");
            }

            if (!contactIds.add(contact.id())) {
                throw new IllegalArgumentException(
                        "contacts must not contain duplicate contact IDs");
            }
        }
    }

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    public CustomerId id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public CustomerName name() {
        return name;
    }

    public CustomerStatus status() {
        return status;
    }

    public List<Contact> contacts() {
        return List.copyOf(contacts);
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }

        if (!(candidate instanceof Customer customer)) {
            return false;
        }

        return id.equals(customer.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
