package com.opsflow.opsflow_backend.platform.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SecurityProbeController.class)
@Import(SecurityConfiguration.class)
@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
class SecurityProbeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void statusEndpointIsPublic() throws Exception {
        mockMvc.perform(get(SecurityConfiguration.PUBLIC_STATUS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void authenticatedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/security/authenticated"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail")
                        .value("Authentication is required to access this resource"))
                .andExpect(jsonPath("$.instance").value("/api/security/authenticated"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void authenticatedEndpointAcceptsAuthenticatedJwt() throws Exception {
        mockMvc.perform(get("/api/security/authenticated")
                .with(jwt().jwt(token -> token
                        .issuer("http://localhost:8081/realms/opsflow")
                        .subject("test-user")
                        .audience(List.of("opsflow-api")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void corsPreflightAllowsTheConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/security/authenticated")
                .header(ORIGIN, "http://localhost:4200")
                .header(ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:4200"));
    }

    @Test
    void corsPreflightRejectsAnUntrustedOrigin() throws Exception {
        mockMvc.perform(options("/api/security/authenticated")
                .header(ORIGIN, "https://untrusted.example")
                .header(ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void authorityProbeRejectsJwtWithoutRequiredAuthority() throws Exception {
        mockMvc.perform(get(SecurityConfiguration.AUTHORITY_PROBE_ENDPOINT)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_something-else"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail")
                        .value("You do not have permission to access this resource"))
                .andExpect(jsonPath("$.instance")
                        .value(SecurityConfiguration.AUTHORITY_PROBE_ENDPOINT))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void authorityProbeAcceptsJwtWithRequiredAuthority() throws Exception {
        mockMvc.perform(get(SecurityConfiguration.AUTHORITY_PROBE_ENDPOINT)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority(SecurityConfiguration.REQUIRED_PROBE_AUTHORITY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorized").value(true));
    }

}
