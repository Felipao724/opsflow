# ADR-0001: Use a monorepo

- **Status:** Accepted
- **Date:** 2026-08-21
- **Supersedes:** None
- **Superseded by:** None

## Context

OpsFlow contains a backend, a frontend, local infrastructure, automation, and
documentation. During the project's early stages, changes will often cross
these boundaries. Contributors need one understandable workflow while each
application retains its own toolchain. There is currently no organizational,
security, or release requirement for separate repositories.

## Decision

We will keep the OpsFlow codebase in one Git repository. Top-level directories
will identify ownership, and the backend and frontend will retain separate
build manifests, dependency locks, and test commands. Root-level configuration
will be reserved for concerns that span the repository.

This decision does not require the applications to share a runtime, release
cadence, or production deployment unit.

## Alternatives considered

- **Separate repositories:** would allow independent permissions and histories,
  but would make cross-application changes non-atomic and duplicate setup and
  automation while the project is small.
- **A monorepo build system such as Nx or Bazel:** could add task graphs and
  caching, but its operational cost is not justified by the current build size.

## Consequences

### Positive

- A contributor can clone and understand the whole system in one place.
- Cross-cutting changes can be reviewed and versioned together.
- Shared documentation and CI have a clear home.

### Negative

- Repository access and history are shared by all applications.
- CI must avoid unnecessary coupling as the codebase grows.
- Directory proximity can encourage accidental coupling unless boundaries stay
  explicit.

### Neutral

- Each application continues to own its dependencies and build lifecycle.
- Production topology remains a separate decision.

## Assumptions and revisit triggers

This decision assumes a team with compatible access needs and frequent
cross-project changes. Reconsider it if compliance requires different access,
teams need genuinely independent release ownership, or repository scale makes
build isolation unmanageable.

## Validation

- The [root project guide](../../../README.md) describes the top-level layout.
- The [CI workflow](../../../.github/workflows/ci.yml) runs independent backend
  and frontend jobs from the same checkout.
