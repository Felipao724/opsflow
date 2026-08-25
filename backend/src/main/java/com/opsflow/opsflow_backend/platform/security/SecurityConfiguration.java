package com.opsflow.opsflow_backend.platform.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

    static final String PUBLIC_STATUS_ENDPOINT = "/api/status";
    static final String AUTHORITY_PROBE_ENDPOINT = "/api/security/authority-probe";
    static final String REQUIRED_PROBE_AUTHORITY = "SCOPE_opsflow.probe";

    @Bean
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_STATUS_ENDPOINT).permitAll()
                        .requestMatchers(AUTHORITY_PROBE_ENDPOINT).hasAuthority(REQUIRED_PROBE_AUTHORITY)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${opsflow.security.cors.allowed-origin}") String allowedOrigin) {
        var configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

}
