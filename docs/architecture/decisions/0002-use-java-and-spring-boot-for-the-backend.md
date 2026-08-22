# ADR-0002: Use Java and Spring Boot for the backend

- **Status:** Accepted
- **Date:** 2026-08-21
- **Supersedes:** None
- **Superseded by:** None

## Context

The backend needs an HTTP application foundation, dependency injection,
configuration, relational database integration, and automated testing. Building
these capabilities from unrelated libraries would add decisions before OpsFlow
has business functionality. The implementation framework and the application's
internal architecture are separate decisions.

## Decision

We will build the backend with Java 25, Spring Boot 4.1, and Maven. The Maven
Wrapper committed to the repository will be the canonical way to run the build.
Spring will provide application bootstrapping, configuration, dependency
injection, HTTP integration, and database conventions.

Routine compatible upgrades do not require a new ADR. A change of language,
framework family, or build strategy does.

## Alternatives considered

- **Plain Java or Jakarta EE assembled manually:** provides more direct control,
  but requires more framework selection and integration work.
- **Node.js with TypeScript:** could share a language with the frontend, but
  language uniformity alone does not provide a demonstrated project advantage.
- **Quarkus or Micronaut:** offer strong startup and native-image capabilities,
  but those qualities are not current constraints.

## Consequences

### Positive

- The project gains a mature JVM ecosystem and strong static typing.
- Spring supplies cohesive conventions for common backend concerns.
- The Maven Wrapper makes the build version reproducible for contributors and
  CI.

### Negative

- Application code and configuration become coupled to Spring conventions.
- Contributors must learn the Spring lifecycle and ecosystem.
- JVM startup and resource use may be higher than leaner alternatives.

### Neutral

- Spring features will be adopted only when a concrete need exists.
- Hosting and deployment remain undecided.

## Assumptions and revisit triggers

This decision assumes Java expertise is available and that native-image startup
or extreme cold-start limits are not primary requirements. Reconsider it if an
organizational platform standard changes or measured runtime constraints make
the selected stack unsuitable.

## Validation

- The [Maven build](../../../backend/pom.xml) declares Java and Spring Boot.
- The [application entry point](../../../backend/src/main/java/com/opsflow/opsflow_backend/OpsflowBackendApplication.java)
  boots the Spring application.
- The [CI workflow](../../../.github/workflows/ci.yml) builds through the Maven
  Wrapper.
