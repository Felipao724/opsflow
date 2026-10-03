# ADR-0009: Use an external OpenID Connect provider

- Status: Accepted
- Date: 2026-10-02
- Supersedes: None
- Superseded by: None

## Context

OpsFlow needs user authentication, but credentials, password recovery,
multi-factor authentication, sessions, token issuance, and signing-key rotation
are a security domain of their own. Implementing those capabilities inside the
product would increase risk and distract from OpsFlow's business domain.

The Angular frontend is browser-delivered code and therefore cannot protect a
client secret. The Spring Boot backend needs a standard way to validate the
identity presented on each stateless API request. Local development must remain
reproducible without committing users, passwords, tokens, or provider secrets.

## Decision

Use an external OpenID Connect provider for authentication and OAuth 2.0 token
issuance.

- Angular is a public client and uses Authorization Code with PKCE `S256`.
- Spring Boot is a stateless OAuth 2.0 Resource Server and accepts only access
  tokens with a trusted signature, exact issuer, expected `opsflow-api`
  audience, and valid time claims.
- OpsFlow identifies an external principal by the validated `(issuer, subject)`
  pair. Browser request bodies never supply that identity.
- OpsFlow does not store or verify passwords. The provider owns credentials,
  authentication factors, provider sessions, authorization codes, tokens, and
  signing keys.
- The Angular adapter keeps tokens in memory. OpsFlow does not copy them into
  local storage, session storage, application databases, logs, or URLs.
- Keycloak 26.7.1 is the reproducible provider for local development. Its
  committed realm contains protocol configuration, not runtime users or
  credentials. This does not select Keycloak as the production provider.

The local web client uses exact callback, origin, and logout URLs. Keycloak's
configured hostname fixes the local issuer at
`http://localhost:8081/realms/opsflow`.

## Alternatives considered

| Alternative | Reason not selected for M1 |
| --- | --- |
| Build authentication inside OpsFlow | Would make the product responsible for credential storage and security capabilities already standardized by OpenID Connect providers. |
| Store provider passwords in OpsFlow | Duplicates sensitive state and breaks the provider boundary. |
| Implicit flow | Exposes tokens in the browser front channel and is superseded by Authorization Code with PKCE for browser clients. |
| Backend for Frontend | Reduces direct browser token exposure, but adds server-side OAuth sessions, cookie and CSRF controls, and proxying that are not yet justified. |
| Hosted provider for every developer | Adds an external dependency and cost/availability concerns to the local workflow. |

## Consequences

### Positive

- Password and token issuance concerns stay outside the business application.
- Standards-based tokens allow Spring Security to enforce a narrow trust
  contract.
- Local identity infrastructure is reproducible and disposable.
- A future provider can be adopted without using email as an account-linking
  shortcut.

### Negative

- Authentication depends on provider availability and correct issuer, audience,
  redirect, origin, and clock configuration.
- Browser-held bearer tokens remain exposed to successful script execution in
  the page, despite avoiding persistent browser storage.
- Changing issuer or subject values requires explicit account linking or data
  migration.

### Neutral

- Keycloak administration is not OpsFlow user administration.
- Provider authentication does not grant access to an OpsFlow organization;
  tenant authorization is a separate application decision.

## Assumptions and revisit triggers

- Reconsider the local provider when a supported development environment or
  production identity provider imposes incompatible protocol requirements.
- Reconsider the browser-client pattern when data sensitivity, organizational
  policy, XSS exposure, multiple APIs, or session requirements justify a BFF.
- Multi-issuer support, social login, account linking, MFA policy, and production
  provider operations require new decisions rather than silent expansion of
  this ADR.

## Validation

- [`opsflow-realm.json`](../../../infrastructure/keycloak/opsflow-realm.json)
  defines the public web client, bearer-only API client, PKCE, exact redirects,
  and API audience mapper without runtime credentials.
- [`AuthenticationClient`](../../../frontend/src/app/platform/authentication/authentication-client.ts)
  initializes Authorization Code flow with PKCE and obtains refreshed tokens
  from the in-memory Keycloak adapter.
- [`SecurityConfiguration`](../../../backend/src/main/java/com/opsflow/opsflow_backend/platform/security/SecurityConfiguration.java)
  configures a stateless JWT Resource Server.
- `JwtDecoderConfigurationTest` verifies trusted tokens and rejects wrong
  issuer, audience, expiry, and signing key.
- The `Keycloak contract` CI job imports a disposable realm and verifies its
  OpenID Provider metadata.

## References

- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0-final.html)
- [OAuth 2.0 for Browser-Based Applications — RFC 10017](https://www.rfc-editor.org/rfc/rfc10017.html)
- [OAuth 2.0 Security Best Current Practice — RFC 9700](https://www.rfc-editor.org/rfc/rfc9700.html)
- [Keycloak JavaScript adapter](https://www.keycloak.org/securing-apps/javascript-adapter)
- [Spring Security OAuth 2.0 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

