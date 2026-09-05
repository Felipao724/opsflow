package com.opsflow.opsflow_backend.modules.identity.internal.application;

public final class ExternalIdentityAlreadyRegisteredException extends RuntimeException {

    public ExternalIdentityAlreadyRegisteredException(Throwable cause) {
        super("A user profile already exists for the external identity", cause);
    }
}