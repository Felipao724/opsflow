package com.opsflow.opsflow_backend.platform.security;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityProbeController {

    @GetMapping(SecurityConfiguration.PUBLIC_STATUS_ENDPOINT)
    public Map<String, String> getStatus() {
        return Map.of(
                "status", "UP");
    }

    @GetMapping("/api/security/authenticated")
    public Map<String, Boolean> getAuthenticatedStatus() {
        return Map.of(
                "authenticated", true);
    }

    @GetMapping(SecurityConfiguration.AUTHORITY_PROBE_ENDPOINT)
    public Map<String, Boolean> getAuthorityProbe() {
        return Map.of(
                "authorized", true);
    }

}
