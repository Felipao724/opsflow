# ADR-0005: Use PostgreSQL for relational persistence

- **Status:** Accepted
- **Date:** 2026-08-21
- **Supersedes:** None
- **Superseded by:** None

## Context

The anticipated OpsFlow domain contains relationships, constraints, and state
changes that benefit from transactional integrity. M0 needs a representative
database for development and integration testing without prematurely defining
the business schema. Selecting the database engine is distinct from choosing a
schema migration tool or Java data-access style.

## Decision

We will use PostgreSQL 18 as the relational database. Local development will
run it with Docker Compose, and backend integration tests will start an isolated
PostgreSQL instance with Testcontainers.

This decision does not define production hosting, high availability, backups,
replication, tenancy, table ownership, or an ORM.

## Alternatives considered

- **MySQL or MariaDB:** are capable relational alternatives, but changing to
  them offers no current advantage that offsets differing SQL behavior and
  tooling.
- **SQLite:** is lightweight for local use, but would not exercise the same
  database engine intended for the application.
- **A document database:** provides flexible document storage, but the expected
  relationships and constraints currently favor a relational model.

## Consequences

### Positive

- Transactions, constraints, and mature relational capabilities are available.
- Development and tests can exercise the same database engine.
- PostgreSQL has broad tooling and ecosystem support.

### Negative

- Local integration requires Docker and additional resources.
- SQL and operational behavior may become PostgreSQL-specific.
- Moving to another database later could require migration work.

### Neutral

- M0 intentionally creates no business tables.
- Data-access libraries and production infrastructure remain separate choices.

## Assumptions and revisit triggers

This decision assumes relational integrity fits the emerging domain and a
supported PostgreSQL service will be available in production. Reconsider it if
the dominant data model proves fundamentally non-relational, regulations impose
different storage, or an unavoidable hosting constraint excludes PostgreSQL.

## Validation

- The [Compose definition](../../../infrastructure/compose.yaml) provides the
  local PostgreSQL service.
- The [backend build](../../../backend/pom.xml) declares the PostgreSQL driver
  and Testcontainers module.
- The [test configuration](../../../backend/src/test/java/com/opsflow/opsflow_backend/testing/PostgreSqlTestConfiguration.java)
  supplies an isolated PostgreSQL container.
