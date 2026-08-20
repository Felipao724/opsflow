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

## Schema ownership

The backend does not use Hibernate or Spring SQL initialization to create the database schema.

```yaml
spring:
  sql:
    init:
      mode: never
```

Database schema changes will be owned by versioned Flyway migrations in a dedicated change.

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
