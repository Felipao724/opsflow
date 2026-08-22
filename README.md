# OpsFlow

OpsFlow is organized as a monorepo so the application, its local development
infrastructure, and the documentation that explains it can evolve together.

This repository is currently in **M0 — Foundations**. The folders below define
where application code, local infrastructure, and durable documentation belong.

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

The backend, frontend, local database, automated testing, and continuous
integration foundations are in place. No business functionality has been added
yet. Follow the M0 issues in GitHub for the remaining setup work.

## Before opening a pull request

Run the same checks locally that continuous integration runs on GitHub.

Backend checks require a running Docker daemon because the integration tests use
Testcontainers to start PostgreSQL. From the `backend` directory, run:

```bash
./mvnw --batch-mode verify
```

On Windows PowerShell, use `./mvnw.cmd --batch-mode verify` instead.

From the `frontend` directory, install the locked dependencies, run the test
suite once, and create a production build:

```bash
npm ci
npm test -- --watch=false
npm run build
```

GitHub Actions runs the backend and frontend checks as independent jobs on pull
requests targeting `main` and on pushes to `main`.
