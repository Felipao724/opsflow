package com.opsflow.opsflow_backend.platform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;

class JwtDecoderConfigurationTest {

    private static final String ISSUER = "http://localhost:8081/realms/opsflow";
    private static final String API_AUDIENCE = "opsflow-api";
    private static final String KEY_ID = "opsflow-test-key";

    @Test
    void decoderAcceptsTokenIssuedForOpsFlowApi() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();

        try (JwkSetServer jwkSetServer = JwkSetServer.start(keyPair)) {
            contextRunner(jwkSetServer).run(context -> {
                JwtDecoder decoder = context.getBean(JwtDecoder.class);

                var jwt = decoder
                        .decode(signedToken(keyPair, API_AUDIENCE, Instant.now(), Instant.now().plusSeconds(300)));

                assertThat(jwt.getIssuer().toString()).isEqualTo(ISSUER);
                assertThat(jwt.getAudience()).contains(API_AUDIENCE);
                assertThat(jwt.getSubject()).isEqualTo("test-user");
            });
        }
    }

    @Test
    void decoderRejectsTokenIssuedForDifferentAudience() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();

        try (JwkSetServer jwkSetServer = JwkSetServer.start(keyPair)) {
            contextRunner(jwkSetServer).run(context -> {
                JwtDecoder decoder = context.getBean(JwtDecoder.class);
                String tokenForWebClient = signedToken(keyPair, "opsflow-web",
                        Instant.now(), Instant.now().plusSeconds(300));

                assertThatThrownBy(() -> decoder.decode(tokenForWebClient))
                        .isInstanceOf(JwtValidationException.class);
            });
        }
    }

    @Test
    void decoderRejectsExpiredToken() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();

        try (JwkSetServer jwkSetServer = JwkSetServer.start(keyPair)) {
            contextRunner(jwkSetServer).run(context -> {
                JwtDecoder decoder = context.getBean(JwtDecoder.class);
                String expiredToken = signedToken(keyPair, API_AUDIENCE, Instant.now().minusSeconds(600),
                        Instant.now().minusSeconds(300));

                assertThatThrownBy(() -> decoder.decode(expiredToken))
                        .isInstanceOf(JwtValidationException.class);
            });
        }
    }

    private ApplicationContextRunner contextRunner(JwkSetServer jwkSetServer) {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OAuth2ResourceServerAutoConfiguration.class))
                .withPropertyValues(
                        "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + ISSUER,
                        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + jwkSetServer.uri(),
                        "spring.security.oauth2.resourceserver.jwt.audiences[0]=" + API_AUDIENCE);
    }

    private KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private String signedToken(KeyPair keyPair, String audience, Instant issuedAt,
            Instant expirationTime) throws Exception {
        var claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject("test-user")
                .audience(audience)
                .issueTime(Date.from(issuedAt))
                .notBeforeTime(Date.from(issuedAt.minusSeconds(5)))
                .expirationTime(Date.from(expirationTime))
                .build();
        var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(KEY_ID)
                .build();
        var token = new SignedJWT(header, claims);
        token.sign(new RSASSASigner((RSAPrivateKey) keyPair.getPrivate()));
        return token.serialize();
    }

    private static final class JwkSetServer implements AutoCloseable {

        private final HttpServer server;

        private JwkSetServer(HttpServer server) {
            this.server = server;
        }

        static JwkSetServer start(KeyPair keyPair) throws IOException {
            var publicKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .keyID(KEY_ID)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
            byte[] response = new JWKSet(publicKey).toString().getBytes(StandardCharsets.UTF_8);
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/jwks", exchange -> {
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                try (var responseBody = exchange.getResponseBody()) {
                    responseBody.write(response);
                }
            });
            server.start();
            return new JwkSetServer(server);
        }

        String uri() {
            return "http://127.0.0.1:" + server.getAddress().getPort() + "/jwks";
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }

}
