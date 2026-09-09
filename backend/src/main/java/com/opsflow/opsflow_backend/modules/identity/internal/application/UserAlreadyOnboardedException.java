package com.opsflow.opsflow_backend.modules.identity.internal.application;

public final class UserAlreadyOnboardedException
        extends RuntimeException {

    private static final String MESSAGE = "User has already completed organization onboarding";

    public UserAlreadyOnboardedException() {
        super(MESSAGE);
    }

    public UserAlreadyOnboardedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}