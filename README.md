# OpsFlow

OpsFlow is organized as a monorepo so the application, its local development
infrastructure, and the documentation that explains it can evolve together.

This repository is currently in **M0 — Foundations**. The folders below define
where future work belongs; the backend, frontend, database, and CI toolchains
will be introduced in their own focused changes.

## Repository structure

```text
opsflow/
├── backend/          # Spring Boot application and backend-owned tests
├── frontend/         # Angular application and frontend-owned tests
├── infrastructure/   # Local development services and supporting configuration
├── docs/             # Architecture and project documentation
├── .gitignore        # Repository-wide exclusions
└── README.md         # Repository overview and navigation
```

Each top-level folder contains a README describing its responsibility and what
does not belong there.

## Ownership rules

- Application code lives with the application that builds and tests it:
  backend code under `backend/`, frontend code under `frontend/`.
- Local supporting services belong under `infrastructure/`; production
  deployment concerns require a separate, explicit design decision.
- Durable explanations and architecture decisions belong under `docs/`.
- Files at the repository root are reserved for cross-project concerns such as
  repository documentation, version-control configuration, and future
  orchestration that genuinely spans multiple areas.

## Current status

No business functionality or application framework has been added yet. Follow
the M0 issues in GitHub for the incremental setup work.
