# Documentation

This directory contains durable project and architecture documentation that is
broader than a single application folder.

Development instructions should stay close to the workflow they explain and be
linked from the root README.

Generated reports and temporary notes do not belong here.

## Architecture guides

- [Architecture overview](architecture/README.md) — Summarizes the implemented M1 architecture and links guides to their supporting decisions.
- [Architecture Decision Records](architecture/decisions/README.md) — Indexes significant decisions and defines the convention for future records.
- [Backend module boundaries](architecture/backend-modules.md) — Defines the modular monolith package structure, dependency rules, and ArchUnit enforcement.
- [M1 identity and tenancy architecture](architecture/m1-identity-and-tenancy-design.md) — Explains the implemented OAuth 2.0, OpenID Connect, JWT, onboarding, and tenant trust boundaries.
- [M2 customer directory design](architecture/m2-customer-directory-design.md) — Proposes the customer aggregate, contact lifecycle, tenant-scoped operations, and HTTP contract for M2.

## Development planning

- [Development and learning plan](development-learning-plan.md) — Connects M1 tickets with focused lessons, isolated exercises, OpsFlow implementation, interview practice, recovery, and review.

## API contracts

- [Identity context and organization onboarding API](api/identity-onboarding.md) — Defines the authenticated context, onboarding request, response states, and stable error contract consumed by the Angular application.
