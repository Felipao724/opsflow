# ADR-0004: Use Angular for the web frontend

- **Status:** Accepted
- **Date:** 2026-08-21
- **Supersedes:** None
- **Superseded by:** None

## Context

OpsFlow needs an interactive browser interface with consistent conventions for
components, routing, forms, dependency injection, building, and testing. The
frontend must remain an application boundary rather than becoming coupled to
backend rendering or its build lifecycle.

## Decision

We will build the web frontend with Angular 22, standalone components, the
Angular Router, strict TypeScript and template checks, the Angular CLI, Vitest,
and an npm lockfile.

This ADR does not select a component library, visual language, rendering mode,
deployment platform, authentication mechanism, or API contract.

## Alternatives considered

- **React with an assembled toolchain:** offers a large ecosystem and flexible
  composition, but requires the project to select and maintain more foundational
  conventions independently.
- **Vue:** offers a smaller approachable core, but Angular's integrated
  conventions better match the desired structured application foundation.
- **Spring-rendered server templates:** reduce the number of application
  toolchains, but couple the user interface more closely to backend delivery and
  are less suited to the anticipated interactive client.

## Consequences

### Positive

- The frontend receives cohesive, documented application conventions.
- Strict compilation catches many template and type errors early.
- Standalone components reduce dependence on Angular module boilerplate.

### Negative

- Contributors must learn Angular's broad API and release cycle.
- Node.js and npm remain a separate toolchain from the backend.
- Major framework upgrades can require coordinated migration work.

### Neutral

- Backend and frontend remain independently buildable.
- Server-side rendering, deployment, and API integration remain future
  decisions.

## Assumptions and revisit triggers

This decision assumes OpsFlow will be a rich browser-based application and that
TypeScript and Angular skills are available. Reconsider it if content-first
server rendering becomes the primary need, the target ceases to be the web, or
an organizational frontend standard materially changes the trade-off.

## Validation

- The [frontend manifest](../../../frontend/package.json) declares Angular and
  Vitest dependencies.
- The [Angular workspace configuration](../../../frontend/angular.json) defines
  the application build and test targets.
- The [frontend tests](../../../frontend/src/app/app.spec.ts) demonstrate the
  current test setup.
