package com.opsflow.opsflow_backend.platform.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

    static final String PUBLIC_STATUS_ENDPOINT = "/api/status";
    static final String AUTHORITY_PROBE_ENDPOINT = "/api/security/authority-probe";
    static final String REQUIRED_PROBE_AUTHORITY = "SCOPE_opsflow.probe";

    @Bean
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_STATUS_ENDPOINT).permitAll()
                        .requestMatchers(AUTHORITY_PROBE_ENDPOINT).hasAuthority(REQUIRED_PROBE_AUTHORITY)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }

}
