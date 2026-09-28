package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.opsflow.opsflow_backend.modules.identity.internal.application.CurrentIdentityContextResult;
import com.opsflow.opsflow_backend.modules.identity.internal.application.GetCurrentIdentityContextService;
import com.opsflow.opsflow_backend.modules.identity.internal.application.OnboardOrganizationCommand;
import com.opsflow.opsflow_backend.modules.identity.internal.application.OnboardOrganizationResult;
import com.opsflow.opsflow_backend.modules.identity.internal.application.OnboardOrganizationService;
import com.opsflow.opsflow_backend.modules.identity.internal.application.UserAlreadyOnboardedException;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.MembershipRole;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationId;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.OrganizationName;
import com.opsflow.opsflow_backend.modules.identity.internal.domain.UserProfileId;
import com.opsflow.opsflow_backend.platform.security.SecurityConfiguration;

@WebMvcTest(IdentityController.class)
@Import(SecurityConfiguration.class)
@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
class IdentityControllerTest {

    private static final UUID USER_PROFILE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCurrentIdentityContextService currentIdentityService;

    @MockitoBean
    private OnboardOrganizationService onboardOrganizationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void reportsWhenCurrentIdentityRequiresOnboarding() throws Exception {
        when(currentIdentityService.getCurrent())
                .thenReturn(new CurrentIdentityContextResult.OnboardingRequired());

        mockMvc.perform(get("/api/identity/context").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONBOARDING_REQUIRED"))
                .andExpect(jsonPath("$.userProfileId").doesNotExist())
                .andExpect(jsonPath("$.organizationId").doesNotExist());
    }

    @Test
    void reportsActiveCurrentIdentityContext() throws Exception {
        when(currentIdentityService.getCurrent()).thenReturn(activeContextResult());

        mockMvc.perform(get("/api/identity/context").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userProfileId").value(USER_PROFILE_ID.toString()))
                .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID.toString()))
                .andExpect(jsonPath("$.organizationName").value("Acme Operations"))
                .andExpect(jsonPath("$.membershipRole").value("OWNER"));
    }

    @Test
    void createsInitialOrganizationForAuthenticatedIdentity() throws Exception {
        when(onboardOrganizationService.onboard(any())).thenReturn(onboardResult());

        mockMvc.perform(post("/api/identity/onboarding")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "organizationName": "Acme Operations"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userProfileId").value(USER_PROFILE_ID.toString()))
                .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID.toString()))
                .andExpect(jsonPath("$.organizationName").value("Acme Operations"))
                .andExpect(jsonPath("$.membershipRole").value("OWNER"));

        verify(onboardOrganizationService).onboard(
                new OnboardOrganizationCommand("Acme Operations"));
    }

    @Test
    void rejectsBlankOrganizationNameBeforeCallingUseCase() throws Exception {
        mockMvc.perform(post("/api/identity/onboarding")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "organizationName": "   "
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("The request contains invalid fields"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.organizationName")
                        .value("organizationName must not be blank"));

        verifyNoInteractions(onboardOrganizationService);
    }

    @Test
    void rejectsMalformedRequestBodyWithoutExposingParserDetails() throws Exception {
        mockMvc.perform(post("/api/identity/onboarding")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "organizationName":
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"))
                .andExpect(jsonPath("$.detail").value("The request body could not be read"))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        verifyNoInteractions(onboardOrganizationService);
    }

    @Test
    void reportsConflictWhenCurrentIdentityIsAlreadyOnboarded() throws Exception {
        doThrow(new UserAlreadyOnboardedException())
                .when(onboardOrganizationService)
                .onboard(any());

        mockMvc.perform(post("/api/identity/onboarding")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "organizationName": "Acme Operations"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Onboarding conflict"))
                .andExpect(jsonPath("$.detail")
                        .value("User has already completed organization onboarding"))
                .andExpect(jsonPath("$.code").value("USER_ALREADY_ONBOARDED"));
    }

    @Test
    void hidesUnexpectedErrorDetails() throws Exception {
        when(currentIdentityService.getCurrent())
                .thenThrow(new IllegalStateException("sensitive internal detail"));

        mockMvc.perform(get("/api/identity/context").with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sensitive"))))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void rejectsAnonymousContextRequest() throws Exception {
        mockMvc.perform(get("/api/identity/context"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentIdentityService);
    }

    @Test
    void rejectsAnonymousOnboardingRequest() throws Exception {
        mockMvc.perform(post("/api/identity/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "organizationName": "Acme Operations"
                        }
                        """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(onboardOrganizationService);
    }

    private CurrentIdentityContextResult.Active activeContextResult() {
        return new CurrentIdentityContextResult.Active(
                new UserProfileId(USER_PROFILE_ID),
                new OrganizationId(ORGANIZATION_ID),
                new OrganizationName("Acme Operations"),
                MembershipRole.OWNER);
    }

    private OnboardOrganizationResult onboardResult() {
        return new OnboardOrganizationResult(
                new UserProfileId(USER_PROFILE_ID),
                new OrganizationId(ORGANIZATION_ID),
                new OrganizationName("Acme Operations"),
                MembershipId.generate(),
                MembershipRole.OWNER);
    }
}
