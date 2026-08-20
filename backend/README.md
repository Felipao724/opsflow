# Backend

This directory contains the OpsFlow Spring Boot backend, its build configuration, and its automated tests.

The application currently requires Java 25 and PostgreSQL 18.

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

## Run the tests

PostgreSQL must be running before executing the current backend tests.

Run the complete test suite:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml test
```

Run only the datasource connectivity check:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml "-Dtest=DatabaseConnectivityTest" test
```

The connectivity check executes the read-only query `SELECT 1`. It does not create tables or modify developer data.

A later testing-foundation change will use Testcontainers to provide isolated, disposable databases for automated tests.

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
- [PostgreSQL JDBC driver](https://jdbc.postgresql.org/documentation/)
- [HikariCP configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
