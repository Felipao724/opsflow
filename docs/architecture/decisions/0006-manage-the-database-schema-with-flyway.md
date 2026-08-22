# ADR-0006: Manage the database schema with Flyway

- **Status:** Accepted
- **Date:** 2026-08-21
- **Supersedes:** None
- **Superseded by:** None

## Context

Every environment must reach a known schema from an empty or older database.
Schema history must be repeatable, reviewable, and auditable; manual changes or
implicit framework-generated schemas cannot provide that reliably.

## Decision

Flyway will be the sole owner of schema evolution. Changes will use ordered,
versioned `V` migrations and run during application startup and integration
tests. An applied migration is immutable: later corrections require a new
forward migration.

Baseline-on-migrate, out-of-order execution, repeatable migrations, and clean
will remain disabled. If an ORM is introduced later, it will not generate or
update the schema.

## Alternatives considered

- **Manual SQL applied by developers or operators:** is simple to begin with,
  but creates untracked and inconsistent environments.
- **Automatic ORM DDL:** is convenient in development, but hides schema intent
  and is unsafe as the authoritative production history.
- **Liquibase:** offers rich, database-agnostic change descriptions, but its
  additional abstraction is unnecessary for the current SQL-first workflow.

## Consequences

### Positive

- A database can be built deterministically from version-controlled history.
- Migration checksums expose accidental modification of applied changes.
- Invalid migrations fail early during startup and integration testing.

### Negative

- Contributors must coordinate migration version numbers.
- A failed migration can block application startup.
- Large or zero-downtime migrations may eventually need a separate operational
  process.

### Neutral

- Migrations may use PostgreSQL-specific SQL.
- The initial baseline establishes infrastructure only, not a domain schema.

## Assumptions and revisit triggers

This decision assumes migrations remain safe to run at application startup and
complete within an acceptable time. Reconsider the execution process when
zero-downtime releases, long data transformations, or separate production
approval duties become requirements. Introduce repeatable migrations only with
a concrete need and an explicit policy.

## Validation

- The [backend configuration](../../../backend/src/main/resources/application.yaml)
  configures Flyway.
- The [baseline migration](../../../backend/src/main/resources/db/migration/V1__baseline.sql)
  starts the versioned history.
- The [database integration test](../../../backend/src/test/java/com/opsflow/opsflow_backend/platform/database/DatabaseConnectivityTest.java)
  verifies startup against PostgreSQL.
