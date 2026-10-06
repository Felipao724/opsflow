package com.opsflow.opsflow_backend.modules.customer.internal.domain;

import java.util.Objects;
import java.util.Optional;

public final class Contact {

    private final ContactId id;
    private ContactName contactName;
    private EmailAddress emailAddress;
    private MexicanPhoneNumber phoneNumber;

    public Contact(ContactId contactId, ContactName contactName, EmailAddress emailAddress,
            MexicanPhoneNumber phoneNumber) {
        this.id = Objects.requireNonNull(contactId, "contactId must not be null");
        this.contactName = Objects.requireNonNull(contactName, "contactName must not be null");
        validateContactMethods(emailAddress, phoneNumber);
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    public static Contact create(ContactName contactName, EmailAddress emailAddress, MexicanPhoneNumber phoneNumber) {
        return new Contact(ContactId.generate(), contactName, emailAddress, phoneNumber);
    }

    void update(ContactName contactName, EmailAddress emailAddress, MexicanPhoneNumber phoneNumber) {
        Objects.requireNonNull(contactName, "contactName must not be null");
        validateContactMethods(emailAddress, phoneNumber);

        this.contactName = contactName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    private static void validateContactMethods(EmailAddress emailAddress, MexicanPhoneNumber phoneNumber) {
        if (emailAddress == null && phoneNumber == null) {
            throw new IllegalArgumentException("at least one contact method must be provided");
        }
    }

    public ContactId id() {
        return id;
    }

    public ContactName name() {
        return contactName;
    }

    public Optional<EmailAddress> emailAddress() {
        return Optional.ofNullable(emailAddress);
    }

    public Optional<MexicanPhoneNumber> phoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }

        if (!(candidate instanceof Contact contact)) {
            return false;
        }

        return id.equals(contact.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
