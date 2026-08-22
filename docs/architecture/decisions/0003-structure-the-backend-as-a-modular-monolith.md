# ADR-0003: Structure the backend as a modular monolith

- **Status:** Accepted
- **Date:** 2026-08-21
- **Supersedes:** None
- **Superseded by:** None

## Context

OpsFlow is expected to contain multiple business capabilities, but their final
boundaries and operational needs are not yet known. Starting with microservices
would introduce distributed communication, deployment, observability, and data
consistency costs before those boundaries are proven. An unstructured monolith,
however, would allow features to become inseparable.

## Decision

We will deploy the backend initially as one Spring Boot application and one
build artifact. Business capabilities will be organized as vertical modules.
Each module will expose an `api` package and keep its implementation in an
`internal` package. Cross-module access to internal and persistence packages is
forbidden, dependencies must remain acyclic, and ArchUnit tests will enforce
the rules that can be checked statically.

Shared technical infrastructure belongs in `platform`; only deliberately small,
stable concepts may enter `sharedkernel`. This choice does not promise that the
modules will become microservices later.

## Alternatives considered

- **A globally layered monolith:** is initially familiar, but feature ownership
  and dependencies become difficult to see as the application grows.
- **Microservices from the beginning:** offer independent deployment and
  scaling, but create operational and consistency complexity without proven
  service boundaries.
- **Separate Maven modules:** strengthen build-time isolation, but add build
  structure that is not yet needed to enforce the current package boundaries.

## Consequences

### Positive

- Local development, deployment, transactions, and debugging remain simple.
- Feature boundaries and allowed dependencies are visible and testable.
- Modules can be refactored while domain understanding is still developing.

### Negative

- The backend remains one deployment, scaling, and failure unit.
- Package rules require ongoing discipline and do not enforce data ownership.
- Independent module releases are not available.

### Neutral

- Module calls are currently in-process and synchronous.
- Asynchronous integration or service extraction requires separate evidence and
  decisions.

## Assumptions and revisit triggers

This decision assumes capabilities do not yet need independent deployment,
scaling, fault isolation, or regulatory boundaries. Reconsider it when measured
requirements demand one of those properties, or when package-level enforcement
no longer protects the boundaries effectively.

## Validation

- The [backend module guide](../backend-modules.md) defines the package rules.
- The [ArchUnit tests](../../../backend/src/test/java/com/opsflow/opsflow_backend/architecture/BackendArchitectureTest.java)
  enforce the current dependency constraints.
