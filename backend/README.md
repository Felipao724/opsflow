# Backend

This directory contains the OpsFlow Spring Boot backend, its build configuration, and its automated tests.

The application currently requires Java 25 and PostgreSQL 18.

## Architecture

The backend follows a modular monolith architecture with enforceable package and dependency boundaries.

See [Backend module boundaries](../docs/architecture/backend-modules.md) for module structure, public APIs, internal implementation rules, dependency direction, and automated architecture tests.

## Prerequisites

Before running the backend, verify that Java 25 is active:

```powershell
java --version
```

Start the local PostgreSQL environment from the repository root:

```powershell
docker compose -f infrastructure\compose.yaml up -d --wait
```

Confirm that PostgreSQL reports a healthy status:

```powershell
docker compose -f infrastructure\compose.yaml ps
```

See [`infrastructure/README.md`](../infrastructure/README.md) for the complete infrastructure workflow.

## Run the backend

Execute the application from the repository root:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

The backend starts on port `8080` by default. Stop it with `Ctrl+C`.

During startup, the application establishes and validates a real database connection. Startup fails if PostgreSQL is unavailable or the datasource configuration is invalid.

## Local datasource defaults

The default configuration in `src/main/resources/application.yaml` matches the local Docker Compose environment.

| Property                 | Local value                                |
| ------------------------ | ------------------------------------------ |
| JDBC URL                 | `jdbc:postgresql://127.0.0.1:5432/opsflow` |
| Username                 | `opsflow`                                  |
| Password                 | `opsflow_local_password`                   |
| Maximum pool size        | `5`                                        |
| Minimum idle connections | `1`                                        |
| Connection timeout       | `5000 ms`                                  |
| Validation timeout       | `3000 ms`                                  |

The local password is intended exclusively for development and must never be used in another environment.

## External configuration

Spring Boot configuration can be overridden without changing committed files.

The primary datasource environment variables are:

| Environment variable                       | Purpose                      |
| ------------------------------------------ | ---------------------------- |
| `SPRING_DATASOURCE_URL`                    | JDBC connection URL          |
| `SPRING_DATASOURCE_USERNAME`               | Database username            |
| `SPRING_DATASOURCE_PASSWORD`               | Database password            |
| `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE` | Maximum connection-pool size |

For example, PowerShell variables can be assigned before starting the backend:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/opsflow"
$env:SPRING_DATASOURCE_USERNAME="opsflow"
$env:SPRING_DATASOURCE_PASSWORD="local_password"
```

Environment variables have higher precedence than values in `application.yaml`.

Local development requires no overrides when using the documented Docker Compose defaults. Any non-local environment must supply its own URL, username, and password through external configuration. Production secrets management is outside the current milestone scope.

## OAuth 2.0 Resource Server

The backend is an OAuth 2.0 Resource Server. It does not authenticate passwords,
start browser login, or issue tokens. It accepts bearer access tokens and uses
Spring Security to validate them before a request reaches an OpsFlow controller.

The local defaults are:

| Property           | Local value                                                             |
| ------------------ | ----------------------------------------------------------------------- |
| Trusted issuer     | `http://localhost:8081/realms/opsflow`                                  |
| Required audience  | `opsflow-api`                                                           |
| Discovery metadata | `http://localhost:8081/realms/opsflow/.well-known/openid-configuration` |

Spring discovers Keycloak's published signing keys from its OpenID Provider
metadata. Incoming JWT access tokens must have a valid signature, the exact
issuer, the `opsflow-api` audience, and a valid time window defined by `nbf` and
`exp`.

The defaults can be overridden without changing committed configuration:

| Environment variable        | Purpose                                  |
| --------------------------- | ---------------------------------------- |
| `OPSFLOW_OAUTH2_ISSUER_URI` | Trusted authorization-server issuer      |
| `OPSFLOW_OAUTH2_AUDIENCE`   | Audience that identifies the OpsFlow API |

The current technical proof endpoints are:

| Endpoint                            | Requirement                     | Expected result                     |
| ----------------------------------- | ------------------------------- | ----------------------------------- |
| `GET /api/status`                   | Public                          | `200` with `{"status":"UP"}`        |
| `GET /api/security/authenticated`   | Valid OpsFlow API access token  | `200` with `{"authenticated":true}` |
| `GET /api/security/authority-probe` | `SCOPE_opsflow.probe` authority | `200` with `{"authorized":true}`    |

An absent, malformed, expired, incorrectly signed, wrong-issuer, or
wrong-audience token produces HTTP `401 Unauthorized`. A valid authenticated
principal that lacks an authority required by an endpoint produces HTTP `403
Forbidden`.

The authority probe is a technical demonstration of the `403` boundary. It does
not define a business permission or grant organization access. Tenant
authorization will be derived from OpsFlow-owned membership data in a later M1
ticket.

The API is stateless and does not create an HTTP session for bearer
authentication. CSRF protection is disabled for this bearer-token-only API; this
decision must be revisited if cookie-based authentication or a Backend for
Frontend is introduced.

JWT values, authorization codes, bearer headers, and complete claim sets must
not be written to application logs. A JWT payload is signed but not encrypted
and must not be treated as confidential storage.

## Run the tests

Docker Desktop or another Testcontainers-compatible Docker runtime must be running for the complete suite. The PostgreSQL service from Docker Compose does not need to be running.

Run the complete test suite:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml test
```

The suite contains distinct test categories:

| Category             | Purpose                                                                | Spring                | Docker |
| -------------------- | ---------------------------------------------------------------------- | --------------------- | ------ |
| Unit                 | Tests one class with mocked collaborators                              | No                    | No     |
| Architecture         | Enforces package and module boundaries with ArchUnit                   | No                    | No     |
| Spring context       | Verifies framework configuration and application startup               | Yes                   | Yes    |
| Database integration | Uses real PostgreSQL and verifies Flyway migrations                    | Yes                   | Yes    |
| Security MVC         | Verifies public, authenticated, and authority boundaries with MockMvc  | Yes (web slice)       | No     |
| JWT decoder          | Verifies RSA signatures and issuer, audience, and timestamp validation | Yes (focused context) | No     |

Run only the focused database-verifier unit tests:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml "-Dtest=DatabaseConnectionVerifierTest" test
```

Run only the architecture rules:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml "-Dtest=BackendArchitectureTest" test
```

Run only the PostgreSQL integration tests:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml "-Dtest=DatabaseConnectivityTest" test
```

Unit tests should be the default for isolated application logic. Load the Spring context only when testing framework integration, configuration, or real infrastructure behavior. Test method names should describe observable behavior.

### Isolated PostgreSQL tests

Spring-managed Testcontainers configuration lives in:

```text
src/test/java/com/opsflow/opsflow_backend/testing/PostgreSqlTestConfiguration.java
```

It starts PostgreSQL 18.6 with temporary credentials and a dynamically assigned host port. `@ServiceConnection` supplies those connection details to Spring and overrides the local datasource properties for the test context.

Flyway validates and applies all migrations before integration test methods execute. Tests use a new disposable database and never read or modify the developer's Docker Compose database or volume.

The first Testcontainers execution can take longer while Docker downloads the PostgreSQL and Ryuk images. Containers are stopped and removed automatically when the test process finishes.

Do not duplicate container declarations in individual tests. Import the shared configuration when a Spring test requires PostgreSQL so compatible test classes can reuse the same cached application context and container lifecycle.

### Isolated security tests

Security controller tests use Spring Security's MockMvc support to exercise the
filter-chain authorization rules without contacting Keycloak. The `jwt()` test
request processor supplies an already authenticated principal; it does not
validate token signatures or claims.

Decoder tests cover the cryptographic and semantic layer separately. They
generate disposable RSA keys, serve the public key from an in-process JWK Set
endpoint, and use Spring Boot's configured `JwtDecoder` to accept or reject
signed tokens. No test key, bearer token, user, or credential is committed.

Full-context database tests import `SecurityTestConfiguration`, which supplies a
mock decoder so those tests remain independent from a running identity provider.
The local Keycloak container is not required to execute the automated backend
suite.

## Database schema migrations

Flyway is the sole owner of the OpsFlow relational database schema.

Migrations run automatically during Spring Boot startup and before tests that load the application context. If validation or migration fails, application startup is aborted.

Spring SQL initialization remains disabled:

```yaml
spring:
  sql:
    init:
      mode: never
```

OpsFlow does not currently include an ORM. If one is introduced later, it must not create or mutate the schema implicitly.

### Migration location

Production migrations are stored in:

```text
src/main/resources/db/migration
```

Flyway manages the PostgreSQL `public` schema and records applied migrations in:

```text
public.flyway_schema_history
```

The initial `V1__baseline.sql` migration establishes the version history without creating business-domain tables. It is a normal versioned migration, not an automatic Flyway baseline operation.

### Naming and ordering

Versioned migrations follow this convention:

```text
V<version>__<description_in_snake_case>.sql
```

Examples:

```text
V1__baseline.sql
V2__create_organizations.sql
V3__add_work_order_status.sql
```

Rules:

- Use one unique, increasing integer version per migration.
- Separate the version and description with exactly two underscores.
- Use a concise snake-case description.
- Never reuse a version.
- Migrations are applied in numeric version order.
- Out-of-order migrations are disabled.
- Migration names are validated during startup.

### Immutability

A versioned migration is immutable after it has been merged or applied to a shared environment.

Flyway stores a checksum for every applied migration. Editing an applied file causes validation to fail because its current checksum no longer matches the recorded value.

Make subsequent changes by adding a new migration instead of modifying existing history.

### Repeatable migrations

Repeatable migrations using the `R__` prefix are not currently permitted.

They may be introduced later only with an explicit architectural decision defining their allowed use cases and review expectations. Schema evolution should use versioned migrations by default.

### Applying migrations

Start PostgreSQL:

```powershell
docker compose -f infrastructure\compose.yaml up -d --wait
```

Start the backend:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Spring Boot automatically validates the migration history and applies pending migrations before the application completes startup.

Running the application again against an up-to-date database is safe. Flyway validates the existing history and does not execute an applied version again.

### Inspecting migration history

```powershell
docker compose -f infrastructure\compose.yaml exec postgres psql -U opsflow -d opsflow -c "SELECT installed_rank, version, description, script, checksum, success FROM public.flyway_schema_history ORDER BY installed_rank;"
```

This query is read-only and shows the migration order, versions, scripts, checksums, and results recorded by Flyway.

### Failure and recovery policy

A failed migration must stop application startup. Migration errors must never be ignored to allow the backend to continue against an unknown schema state.

If a migration fails before it has been merged or applied to a shared environment:

1. Inspect the database and Flyway error.
2. Confirm whether the database transaction was rolled back.
3. Correct the pending migration.
4. Retry it against a clean or verified local state.

If a migration has already succeeded in a shared environment:

1. Do not edit or delete the applied migration.
2. Create a new forward-only migration that corrects the schema.
3. Verify the complete migration history from an empty database.

`flyway repair` must not be used as a routine fix or as a replacement for a corrective migration. It is reserved for deliberate metadata reconciliation after the database state has been investigated and the action has been explicitly approved.

Flyway clean operations are disabled. Resetting the local Docker volume remains a separate, explicitly destructive development operation documented in [`infrastructure/README.md`](../infrastructure/README.md).

## Troubleshooting

### Database connection fails during startup

Confirm that PostgreSQL is running and healthy:

```powershell
docker compose -f infrastructure\compose.yaml ps
```

Check the database logs:

```powershell
docker compose -f infrastructure\compose.yaml logs postgres
```

Verify that the datasource URL, username, and password match the local infrastructure configuration.

### Port 5432 was changed locally

If `POSTGRES_PORT` was changed in `infrastructure/.env`, override the backend URL with the same host port:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5433/opsflow"
```

### Port 8080 is already in use

Override the Spring Boot server port for the current PowerShell session:

```powershell
$env:SERVER_PORT="8081"
```

## Official documentation

- [Spring Boot SQL databases](https://docs.spring.io/spring-boot/reference/data/sql.html)
- [Spring Boot externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/)
- [Spring Boot OAuth 2.0 Resource Server](https://docs.spring.io/spring-boot/reference/security/oauth2.html)
- [Spring Security JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Spring Security OAuth 2.0 testing](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/oauth2.html)
- [Spring Boot Testcontainers](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
- [Testcontainers PostgreSQL module](https://java.testcontainers.org/modules/databases/postgres/)
- [JUnit user guide](https://docs.junit.org/current/user-guide/)
- [Mockito documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html)
- [PostgreSQL JDBC driver](https://jdbc.postgresql.org/documentation/)
- [HikariCP configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
