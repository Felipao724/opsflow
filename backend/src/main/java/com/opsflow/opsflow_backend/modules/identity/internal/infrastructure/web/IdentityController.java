package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.web;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.opsflow.opsflow_backend.modules.identity.internal.application.CurrentIdentityContextResult;
import com.opsflow.opsflow_backend.modules.identity.internal.application.GetCurrentIdentityContextService;
import com.opsflow.opsflow_backend.modules.identity.internal.application.OnboardOrganizationCommand;
import com.opsflow.opsflow_backend.modules.identity.internal.application.OnboardOrganizationResult;
import com.opsflow.opsflow_backend.modules.identity.internal.application.OnboardOrganizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final GetCurrentIdentityContextService currentIdentityService;
    private final OnboardOrganizationService onboardOrganizationService;

    public IdentityController(GetCurrentIdentityContextService currentIdentityService,
            OnboardOrganizationService onboardOrganizationService) {
        this.currentIdentityService = Objects.requireNonNull(
                currentIdentityService,
                "currentIdentityService must not be null");

        this.onboardOrganizationService = Objects.requireNonNull(
                onboardOrganizationService,
                "onboardOrganizationService must not be null");
    }

    @GetMapping("/context")
    public CurrentIdentityContextResponse getContext() {
        CurrentIdentityContextResult current = currentIdentityService.getCurrent();

        return toResponse(current);
    }

    @PostMapping("/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrentIdentityContextResponse.Active onboard(@Valid @RequestBody OnboardOrganizationRequest request) {
        String organizationName = request.organizationName();

        OnboardOrganizationCommand organizationCommand = new OnboardOrganizationCommand(organizationName);

        OnboardOrganizationResult result = onboardOrganizationService.onboard(organizationCommand);

        return toResponse(result);
    }

    private CurrentIdentityContextResponse toResponse(CurrentIdentityContextResult result) {
        return switch (result) {
            case CurrentIdentityContextResult.OnboardingRequired ignored ->
                new CurrentIdentityContextResponse.OnboardingRequired();

            case CurrentIdentityContextResult.Active active ->
                new CurrentIdentityContextResponse.Active(active.userProfileId().value(),
                        active.organizationId().value(), active.organizationName().value(),
                        active.membershipRole().name());
        };
    }

    private CurrentIdentityContextResponse.Active toResponse(OnboardOrganizationResult result) {
        return new CurrentIdentityContextResponse.Active(result.userProfileId().value(),
                result.organizationId().value(), result.organizationName().value(),
                result.membershipRole().name());
    }
}
