# Local OpsFlow realm

The committed [`opsflow-realm.json`](opsflow-realm.json) file bootstraps the
local OpenID Connect contract used by OpsFlow. It is mounted read-only into the
Keycloak container and imported when Keycloak starts with `--import-realm`.

This configuration is for local development. It is not a production identity
deployment or a backup of the Keycloak database.

## Realm boundary

The `opsflow` realm is the local identity boundary for the application. It owns
authentication identities, credentials, sessions, authorization codes, and
tokens.

An OpsFlow business organization is not a Keycloak realm. Organizations,
memberships, and business authorization remain application data owned by the
OpsFlow backend and its PostgreSQL database.

The stable external identity consumed by OpsFlow is the validated pair:

```text
(issuer, subject)
```

For the default local environment, the issuer is:

```text
http://localhost:8081/realms/opsflow
```

Keycloak's hostname setting fixes this value so that accessing discovery through
`localhost` or `127.0.0.1` does not produce competing issuers.

## OAuth clients

### `opsflow-web`

`opsflow-web` represents the Angular application:

- it is a public client because browser code cannot protect a client secret;
- it uses Authorization Code flow;
- PKCE with the `S256` challenge method is mandatory;
- implicit flow, Direct Access Grants, service accounts, and authorization
  services are disabled;
- its exact local callback is
  `http://localhost:4200/auth/callback`;
- its exact local web origin is `http://localhost:4200`;
- its exact post-logout redirect is `http://localhost:4200/`.

Redirects and origins are intentionally narrow. Wildcards would allow a larger
set of browser locations to receive authorization responses or call Keycloak.

### `opsflow-api`

`opsflow-api` represents the Spring Boot Resource Server. It is a bearer-only
client: it does not start interactive login, receive authorization codes, keep a
client secret, or request tokens for itself.

Its identifier is used as the required audience for API access tokens.

## Audience contract

The `opsflow-api-audience` client scope contains an audience mapper and is a
default scope of `opsflow-web`. It adds this claim only to access tokens:

```json
{
  "aud": "opsflow-api"
}
```

It does not add the API audience to ID tokens. The intended distinction is:

| Token        | Expected audience | Intended consumer             |
| ------------ | ----------------- | ----------------------------- |
| Access token | `opsflow-api`     | Spring Boot Resource Server   |
| ID token     | `opsflow-web`     | Angular OpenID Connect client |

The backend must validate access-token signature, algorithm, issuer, audience,
and time claims before trusting the subject. Decoding a JWT payload is not
validation, and an ID token must not be accepted as an API bearer credential.

## Local realm behavior

- Self-registration is enabled to support local development without manually
  provisioning every application user.
- Email verification and password reset are disabled because the local
  environment has no configured email delivery service.
- Access tokens live for 300 seconds.
- Tokens are signed with RS256.
- Optional profile and email scopes are not assigned by default. Claims should
  be introduced only when an application use case requires them.

The bootstrap administrator belongs to Keycloak's `master` realm. A user who
registers through OpsFlow belongs to the `opsflow` realm; these are separate
identity populations.

## Startup import semantics

Compose mounts the committed file at:

```text
/opt/keycloak/data/import/opsflow-realm.json
```

On an empty Keycloak database, startup import creates the realm and its clients.
When the realm already exists, Keycloak skips it to preserve users and other
runtime state. Therefore, editing the JSON does not reconcile an already
imported realm automatically.

The JSON is the reproducible bootstrap definition, while the Keycloak database
is the active runtime state. To prove that the committed definition can recreate
the environment, reset only local identity data using the procedure in
[`infrastructure/README.md`](../README.md#reset-only-the-local-identity-data).

The committed realm file deliberately contains no users, passwords, client
secrets, sessions, or environment-specific production credentials. Local users
are disposable runtime data and must not be committed.

## Inspect the active configuration

Open [http://localhost:8081](http://localhost:8081), enter the Administration
Console with the bootstrap administrator, and select the `opsflow` realm.

The OpenID Provider metadata is available at:

```text
http://localhost:8081/realms/opsflow/.well-known/openid-configuration
```

The metadata should report the same issuer and advertise `S256` as a supported
PKCE challenge method. A real Authorization Code + PKCE login should produce:

- an access token with `iss=http://localhost:8081/realms/opsflow`,
  `aud=opsflow-api`, and `azp=opsflow-web`;
- an ID token with the same issuer and `aud=opsflow-web`;
- a stable, non-empty `sub` in both tokens.

Authorization codes and tokens are credentials. Never paste them into issue
comments, logs, documentation, commits, or chat messages.

## Official references

- [Keycloak realm import and export](https://www.keycloak.org/server/importExport)
- [Running Keycloak in a container](https://www.keycloak.org/server/containers)
- [Keycloak hostname configuration](https://www.keycloak.org/server/hostname)
- [Keycloak Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/)
- [OAuth 2.0 Authorization Code grant](https://www.rfc-editor.org/rfc/rfc6749.html#section-4.1)
- [Proof Key for Code Exchange (PKCE)](https://www.rfc-editor.org/rfc/rfc7636.html)
- [OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0-final.html)
