package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;

public record OnboardOrganizationRequest(
        @NotBlank(message = "organizationName must not be blank")
        @Size(
                max = OrganizationName.MAX_LENGTH,
                message = "organizationName must not exceed 120 characters")
        String organizationName) {

}
