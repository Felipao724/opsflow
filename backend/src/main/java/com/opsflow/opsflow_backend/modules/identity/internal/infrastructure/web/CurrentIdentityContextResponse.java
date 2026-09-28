package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.web;

import java.util.UUID;

public sealed interface CurrentIdentityContextResponse {

    record OnboardingRequired(String status) implements CurrentIdentityContextResponse {
        public OnboardingRequired() {
            this("ONBOARDING_REQUIRED");
        }
    }

    record Active(String status, UUID userProfileId, UUID organizationId, String organizationName,
            String membershipRole) implements CurrentIdentityContextResponse {
        public Active(UUID userProfileId, UUID organizationId, String organizationName, String membershipRole) {
            this("ACTIVE", userProfileId, organizationId, organizationName, membershipRole);
        }
    }
}
