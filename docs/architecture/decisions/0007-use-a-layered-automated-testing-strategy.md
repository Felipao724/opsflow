# ADR-0007: Use a layered automated testing strategy

- **Status:** Accepted
- **Date:** 2026-08-21
- **Supersedes:** None
- **Superseded by:** None

## Context

OpsFlow must validate pure logic, Angular rendering, Spring integration,
architectural boundaries, and PostgreSQL behavior. No single test style gives
useful speed and confidence for all of these concerns. A default of loading the
entire system would make feedback slower and failures harder to diagnose.

## Decision

We will test at the smallest scope that can prove the behavior:

- pure unit tests for framework-independent logic;
- Angular component tests for DOM behavior and unit tests for pure functions;
- Spring context tests only for behavior that requires framework integration;
- Testcontainers with PostgreSQL for persistence integration;
- ArchUnit tests for backend dependency rules; and
- independent backend and frontend builds in CI.

Automated tests will not depend on the developer's Compose database or another
shared external service. End-to-end, visual, performance, security, and coverage
policies are outside the M0 foundation and will be added when their risks and
requirements are known.

## Alternatives considered

- **Full-application tests by default:** provide broad wiring confidence, but
  are slower and give less precise feedback for isolated behavior.
- **Predominantly mocked tests:** run quickly, but can pass while real framework
  or database integration is broken.
- **Manual verification:** is useful for exploration, but is not repeatable
  enough to protect regressions or CI.

## Consequences

### Positive

- Most mistakes receive fast, focused feedback.
- Database tests exercise real PostgreSQL behavior in isolated environments.
- Architectural constraints are executable rather than documentation only.
- CI validates both applications on every relevant change.

### Negative

- Contributors must choose and maintain several test styles.
- Integration tests require Docker and take longer than unit tests.
- Poorly scoped tests can still make the suite slow or brittle.

### Neutral

- M0 establishes the test foundation, not complete business-flow coverage.
- Additional test layers must be justified by the risks they address.

## Assumptions and revisit triggers

This decision assumes Docker is available where integration tests run and the
suite remains fast enough for normal development. Reconsider the balance when
runtime becomes disruptive, flakiness appears, or multi-service business flows
create risks that component-level tests cannot cover.

## Validation

- The [backend tests](../../../backend/src/test/java/com/opsflow/opsflow_backend)
  contain unit, Spring, Testcontainers, and ArchUnit examples.
- The [frontend tests](../../../frontend/src/app) contain component and pure
  function examples.
- The [CI workflow](../../../.github/workflows/ci.yml) runs the backend and
  frontend verification independently.
