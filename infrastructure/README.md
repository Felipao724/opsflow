# Local Infrastructure

This directory contains the infrastructure required for local OpsFlow development.

It provides the OpsFlow PostgreSQL 18 database and a local Keycloak 26.7.1
identity environment backed by its own PostgreSQL 18 database. Docker Compose
manages these services for local development only; this is not a production
deployment architecture.

## Prerequisites

Install and start Docker Desktop before running the environment.

Verify that Docker and Docker Compose are available:

```powershell
docker version
docker compose version
```

The `docker version` output must include both `Client` and `Server` sections.

## First-time setup

Commands in this document must be executed from the repository root.

Create your local environment file:

```powershell
Copy-Item infrastructure\.env.example infrastructure\.env
```

The `.env` file is ignored by Git and may be customized for your machine. Do not commit local credentials.

Available settings:

| Variable                            | Example value                         | Purpose                                       |
| ----------------------------------- | ------------------------------------- | --------------------------------------------- |
| `POSTGRES_DB`                       | `opsflow`                             | OpsFlow database name                         |
| `POSTGRES_USER`                     | `opsflow`                             | OpsFlow database user                         |
| `POSTGRES_PASSWORD`                 | `opsflow_local_password`              | Local-only OpsFlow database password          |
| `POSTGRES_PORT`                     | `5432`                                | OpsFlow database port exposed to the host     |
| `KEYCLOAK_DB_NAME`                  | `keycloak`                            | Keycloak-owned database name                  |
| `KEYCLOAK_DB_USER`                  | `keycloak`                            | Keycloak-owned database user                  |
| `KEYCLOAK_DB_PASSWORD`              | `replace_with_a_local_password`       | Local-only Keycloak database password         |
| `KEYCLOAK_BOOTSTRAP_ADMIN_USERNAME` | `admin`                               | Initial Keycloak administrator                |
| `KEYCLOAK_BOOTSTRAP_ADMIN_PASSWORD` | `replace_with_a_local_admin_password` | Initial administrator password                |
| `KEYCLOAK_PORT`                     | `8081`                                | Keycloak application port exposed to the host |
| `KEYCLOAK_MANAGEMENT_PORT`          | `9000`                                | Keycloak health and metrics port              |

The example password is intended exclusively for local development and must never be reused in production.

## Validate the configuration

Render and validate the resolved Compose configuration without creating containers:

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml config
```

Be aware that this command prints the resolved development password in the terminal.

## Start the local infrastructure

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml up -d --wait --wait-timeout 180
```

This command creates the internal network and named volumes, starts the OpsFlow
database, waits for the Keycloak database to become healthy, starts Keycloak,
and waits until all services report a healthy status.

On the first run, Docker downloads the official PostgreSQL 18 and Keycloak
26.7.1 images. Keycloak can take longer because it initializes its database
schema before becoming ready.

## Check the service status

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml ps
```

All three services should report `healthy`:

- `postgres` stores OpsFlow application data and exposes its configured host port.
- `keycloak-postgres` stores identity data and is reachable only inside Compose.
- `keycloak` provides the identity server and administration console.

To inspect its logs:

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml logs postgres
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml logs keycloak-postgres keycloak
```

Verify the live OpenID Provider metadata:

```powershell
$metadata = Invoke-RestMethod http://localhost:8081/realms/opsflow/.well-known/openid-configuration
$metadata.issuer
$metadata.authorization_endpoint
$metadata.token_endpoint
$metadata.jwks_uri
$metadata.code_challenge_methods_supported
```

The issuer must be exactly `http://localhost:8081/realms/opsflow`, and the
supported PKCE methods must contain `S256`. This confirms that the provider is
ready and publishing the protocol URLs expected by the frontend and backend; it
does not authenticate a user or validate tenant membership.

CI performs a stronger disposable compatibility check. It validates the
committed realm's web client, API client, PKCE, and audience mapper, starts only
Keycloak and its database under an isolated Compose project name, verifies the
live metadata, and removes its volume afterward. Backend and frontend CI jobs do
not depend on a developer's persistent Keycloak database.

## Access Keycloak

Open the local server at [http://localhost:8081](http://localhost:8081) and use
the bootstrap administrator credentials from `infrastructure/.env` to enter the
Administration Console.

On a fresh identity database, Keycloak imports the committed `opsflow` realm,
its Angular and API clients, and their OAuth settings from
[`keycloak/opsflow-realm.json`](keycloak/opsflow-realm.json). See the
[`Local OpsFlow realm`](keycloak/README.md) guide for the client contract,
startup import behavior, and expected token claims.

Readiness is available separately at
[http://localhost:9000/health/ready](http://localhost:9000/health/ready). A
healthy instance responds with HTTP `200` and `"status": "UP"`. Enabling
Keycloak metrics also adds the database connection check to its readiness
response.

Both ports bind to `127.0.0.1`, so they are not intentionally exposed to other
machines on the local network. The `start-dev` command and bootstrap credentials
are development conveniences and must not be used as a production configuration.

## Connect with psql

Run the PostgreSQL client inside the active container:

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml exec postgres psql -U opsflow -d opsflow
```

Exit the interactive client with:

```text
\q
```

The database is also accessible from the host at `127.0.0.1` using the port configured in `infrastructure/.env`.

## Stop the environment and preserve data

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml down
```

This removes the containers and Compose network but preserves both named volumes:

- `opsflow_postgres_data` contains OpsFlow application data.
- `opsflow_keycloak_postgres_data` contains users, realms, clients, roles, and
  other Keycloak state.

Using `docker compose stop` is less extensive: it stops the existing containers
without removing them. A later `start` or `up` resumes them. Neither `stop` nor
`down` deletes named volumes.

## Reset only the local identity data

> **Warning:** This permanently deletes all local Keycloak users, realms,
> clients, roles, sessions, and administrator state. It preserves the OpsFlow
> application database.

Stop the environment, delete only the Keycloak database volume, and recreate the
services:

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml down
docker volume rm opsflow_keycloak_postgres_data
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml up -d --wait --wait-timeout 180
```

Keycloak initializes a fresh database, recreates the bootstrap administrator
from the current values in `infrastructure/.env`, and imports the committed
`opsflow` realm. Local realm users and any changes made only through the Admin
Console are not restored.

## Reset all local infrastructure data

> **Warning:** The following command permanently deletes both OpsFlow application
> data and all Keycloak identity data.

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml down --volumes
```

Start the environment again to initialize a new empty database:

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml up -d --wait --wait-timeout 180
```

The initialization variables are applied when PostgreSQL creates a new database in an empty volume. Changing them in `.env` does not reconfigure an existing database automatically.

## Troubleshooting

### Docker server is unavailable

Start Docker Desktop and wait until its engine is running. Confirm that `docker version` displays a `Server` section.

### Port 5432 is already in use

Change `POSTGRES_PORT` in `infrastructure/.env`, for example:

```dotenv
POSTGRES_PORT=5433
```

Recreate the environment afterward:

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml down
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml up -d --wait --wait-timeout 180
```

Only the host port changes. PostgreSQL continues listening on port `5432` inside the container.

### Port 8081 or 9000 is already in use

Change `KEYCLOAK_PORT` or `KEYCLOAK_MANAGEMENT_PORT` in
`infrastructure/.env`, then recreate the environment with `down` followed by
`up`. The internal container ports remain `8080` and `9000`.

### Keycloak does not become healthy

Inspect both Keycloak and its database because readiness depends on both:

```powershell
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml ps
docker compose --env-file infrastructure\.env -f infrastructure\compose.yaml logs keycloak-postgres keycloak
```

Confirm that every Keycloak variable from `.env.example` exists in the local
`.env`. A PostgreSQL log entry stating that `aurora_version()` does not exist can
appear during database detection and is harmless when Keycloak subsequently
starts and reports healthy.

### The token issuer is rejected

Issuer comparison is exact. The local backend expects:

```text
http://localhost:8081/realms/opsflow
```

Using `127.0.0.1`, another port, another realm, or a token issued before a
hostname change produces a different issuer. Inspect the discovery metadata and
the token's `iss` claim without logging or sharing the full token. Confirm that
`OPSFLOW_OAUTH2_ISSUER_URI`, when set, matches the metadata exactly. Sign in
again after correcting the configuration so the browser receives a new token.

### The API rejects a token because of its audience

OpsFlow requires the access token audience to contain `opsflow-api`. An ID token
is not an API credential even though it also represents the signed-in person.

Inspect the active `opsflow-api-audience` client scope and its audience mapper
in the Keycloak Admin Console. If the committed realm JSON was changed after the
realm was first imported, remember that startup import skips an existing realm;
either update the active local realm deliberately or use the identity-only reset
when its local users and sessions are disposable.

### Keycloak reports `Invalid parameter: redirect_uri`

The browser redirect must exactly match a registered client URL. The local
`opsflow-web` client expects:

```text
http://localhost:4200/auth/callback
http://localhost:4200/silent-check-sso.html
http://localhost:4200/
```

Check the browser origin, Angular port, configured valid redirect URIs, web
origin, and post-logout redirect. Do not solve this by adding a broad wildcard;
register the exact development URLs that the application uses.

### Authentication fails around token issue or expiry time

JWT time claims depend on the clocks of Keycloak, the backend host, and the
browser host. Synchronize the operating-system clock and timezone, then obtain a
new token. The local access-token lifetime is intentionally short, so an old
token may expire while debugging. Do not increase clock tolerance or token
lifetime merely to hide a visibly incorrect system clock.

### A protected request returns `401` or `403`

- `401 Unauthorized` means authentication did not succeed: the bearer token is
  missing or failed signature, issuer, audience, or time validation.
- `403 Forbidden` means the identity is authenticated but lacks the required
  technical authority or active OpsFlow membership for the requested resource.

For `401`, inspect provider and Resource Server configuration. For `403`, inspect
the local profile and membership relationship. A valid Keycloak login alone does
not grant access to an OpsFlow organization.

## Official documentation

- [Docker Compose overview](https://docs.docker.com/compose/)
- [Docker Compose `up`](https://docs.docker.com/reference/cli/docker/compose/up/)
- [Docker Compose `down`](https://docs.docker.com/reference/cli/docker/compose/down/)
- [Docker volumes](https://docs.docker.com/engine/storage/volumes/)
- [PostgreSQL official Docker image](https://hub.docker.com/_/postgres)
- [PostgreSQL `psql`](https://www.postgresql.org/docs/18/app-psql.html)
- [Keycloak container guide](https://www.keycloak.org/server/containers)
- [Keycloak database configuration](https://www.keycloak.org/server/db)
- [Keycloak health checks](https://www.keycloak.org/observability/health)
- [Keycloak realm import and export](https://www.keycloak.org/server/importExport)
- [Keycloak hostname configuration](https://www.keycloak.org/server/hostname)
- [Keycloak JavaScript adapter](https://www.keycloak.org/securing-apps/javascript-adapter)
- [Spring Security OAuth 2.0 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [OAuth 2.0 for Browser-Based Applications — RFC 10017](https://www.rfc-editor.org/rfc/rfc10017.html)
- [Angular HTTP interceptors](https://angular.dev/guide/http/interceptors)
- [Angular route guards](https://angular.dev/guide/routing/route-guards)
