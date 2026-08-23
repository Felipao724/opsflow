package com.opsflow.opsflow_backend.platform.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(status().isUnauthorized());
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
    void authorityProbeRejectsJwtWithoutRequiredAuthority() throws Exception {
        mockMvc.perform(get(SecurityConfiguration.AUTHORITY_PROBE_ENDPOINT)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_something-else"))))
                .andExpect(status().isForbidden());
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
