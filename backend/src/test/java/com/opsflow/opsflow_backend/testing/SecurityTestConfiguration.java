package com.opsflow.opsflow_backend.testing;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration(proxyBeanMethods = false)
public class SecurityTestConfiguration {

    @Bean
    JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }
}
