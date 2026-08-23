# OpsFlow

OpsFlow is a monorepo containing the backend, frontend, local development
infrastructure, and the documentation that explains how they evolve together.
The project is currently in **M1 — Identity and Access** and does not yet contain
business functionality.

## Repository structure

```text
opsflow/
├── backend/          # Spring Boot application and backend-owned tests
├── frontend/         # Angular application and frontend-owned tests
├── infrastructure/   # PostgreSQL, Keycloak, and local supporting configuration
├── docs/             # Architecture and durable project documentation
└── .github/          # Repository automation and continuous integration
```

Application code and its tests belong under `backend/` or `frontend/`. Local
supporting services belong under `infrastructure/`, while cross-project
architecture explanations belong under `docs/`.

## Required tools

| Tool    | Supported version                     | Notes                                                                                                        |
| ------- | ------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| Git     | Current Git 2.x                       | Required to clone the repository and preserve executable file metadata.                                      |
| Java    | 25                                    | Eclipse Temurin is used locally and in CI. `mise.toml` declares `temurin-25`.                                |
| Maven   | 3.9.16                                | Installed automatically by the committed Maven Wrapper; no global Maven installation is required.            |
| Node.js | 24.15 or newer within 24.x            | Matches the Angular 22 baseline and the Node 24 CI runtime.                                                  |
| npm     | 11.17.0                               | Declared in `frontend/package.json` and installed explicitly in CI.                                          |
| Docker  | Docker Desktop with Docker Compose v2 | The Docker daemon must be running. Docker Desktop provides the recommended environment on Windows and macOS. |

Any version manager may be used. For Java, the optional [mise](https://mise.jdx.dev/)
configuration can install the declared version after mise has been
[activated in the shell](https://mise.jdx.dev/getting-started.html#activate-mise):

```powershell
mise install
java --version
```

Verify the active tools before setup:

```powershell
git --version
java --version
node --version
npm --version
docker version
docker compose version
```

`docker version` must display both `Client` and `Server` information. A client-only
result means Docker is installed but its daemon is not running.

## First-time setup

The following is the recommended path from a fresh clone to the complete local
environment.

### 1. Clone the repository

```powershell
git clone https://github.com/Felipao724/opsflow.git
cd opsflow
```

Run the remaining commands from the repository root unless a section says
otherwise.

### 2. Create the local environment file

Windows PowerShell:

```powershell
Copy-Item infrastructure/.env.example infrastructure/.env
```

macOS or Linux:

```bash
cp infrastructure/.env.example infrastructure/.env
```

The resulting `infrastructure/.env` is ignored by Git. Replace the example
Keycloak passwords before starting the environment. All credentials in this file
are exclusively for local development and must not be reused elsewhere.

### 3. Install frontend dependencies

```powershell
npm --prefix frontend ci
```

`npm ci` installs exactly the dependency graph recorded in
`frontend/package-lock.json` and replaces any existing `node_modules` directory.

### 4. Start the local infrastructure

```powershell
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml up -d --wait --wait-timeout 180
```

Compose downloads PostgreSQL 18 and Keycloak 26.7.1 when necessary, creates the
local network and separate named volumes, starts both databases and Keycloak in
the background, and waits for every healthcheck to pass.

Confirm that all three infrastructure services report `healthy`:

```powershell
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml ps
```

### 5. Start the backend

Open a new terminal at the repository root.

Windows PowerShell:

```powershell
./backend/mvnw.cmd -f backend/pom.xml spring-boot:run
```

macOS or Linux:

```bash
./backend/mvnw -f backend/pom.xml spring-boot:run
```

Spring Boot validates the database connection and applies pending Flyway
migrations before startup completes. Wait for a log entry containing
`Started OpsflowBackendApplication`.

### 6. Start the frontend

Open another terminal at the repository root:

```powershell
npm --prefix frontend start
```

Angular watches the source files and rebuilds automatically during development.

## Local services and readiness

| Service             | Local address                                  | Expected readiness signal                                                                                                                         |
| ------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| OpsFlow PostgreSQL  | `127.0.0.1:${POSTGRES_PORT}`                   | `docker compose ... ps` reports `healthy`.                                                                                                        |
| Keycloak PostgreSQL | Internal Compose network only                  | `docker compose ... ps` reports `healthy`.                                                                                                        |
| Keycloak            | [http://localhost:8081](http://localhost:8081) | `http://localhost:9000/health/ready` reports `"status": "UP"`.                                                                                    |
| Backend             | [http://localhost:8080](http://localhost:8080) | The backend log contains `Started OpsflowBackendApplication`. An HTTP `404` at the root is currently expected because no API endpoints exist yet. |
| Frontend            | [http://localhost:4200](http://localhost:4200) | The browser displays the `OpsFlow` heading.                                                                                                       |

To confirm that the backend HTTP server responds, use either check below. The
expected status is currently `404`.

PowerShell 7:

```powershell
(Invoke-WebRequest http://localhost:8080 -SkipHttpErrorCheck).StatusCode
```

macOS or Linux:

```bash
curl --include http://localhost:8080
```

## Daily startup

After the first-time setup, start the environment in three terminals.

Terminal 1 — local infrastructure:

```powershell
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml up -d --wait --wait-timeout 180
```

Terminal 2 — backend on Windows PowerShell:

```powershell
./backend/mvnw.cmd -f backend/pom.xml spring-boot:run
```

Terminal 2 — backend on macOS or Linux:

```bash
./backend/mvnw -f backend/pom.xml spring-boot:run
```

Terminal 3 — frontend:

```powershell
npm --prefix frontend start
```

## Build and test before a pull request

Run the same checks that continuous integration executes.

Backend, from the repository root:

Windows PowerShell:

```powershell
./backend/mvnw.cmd -f backend/pom.xml --batch-mode verify
```

macOS or Linux:

```bash
./backend/mvnw -f backend/pom.xml --batch-mode verify
```

The complete backend suite requires a running Docker daemon because
Testcontainers creates an isolated PostgreSQL instance. The Compose PostgreSQL
service does not need to be running for these tests.

Frontend, from the repository root:

```powershell
npm --prefix frontend ci
npm --prefix frontend test -- --watch=false
npm --prefix frontend run build
```

GitHub Actions runs backend and frontend validation as independent jobs on pull
requests targeting `main` and on pushes to `main`.

## Stop the environment and preserve data

Stop the backend and frontend development servers with `Ctrl+C` in their
terminals. Then stop the local infrastructure:

```powershell
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml down
```

This removes the containers and Compose network, but preserves the separate
`opsflow_postgres_data` and `opsflow_keycloak_postgres_data` named volumes. The
next `up` command reuses both application and identity data.

## Delete and recreate all local infrastructure data

> **Warning:** This operation permanently deletes both the local OpsFlow
> database and all Keycloak identity data. It is not equivalent to stopping the
> environment.

Use it only when the local data is disposable and a clean database is
intentionally required:

```powershell
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml down --volumes
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml up -d --wait --wait-timeout 180
```

PostgreSQL applies the values from `infrastructure/.env` when it initializes an
empty volume. Editing database credentials in `.env` does not reconfigure a
previously initialized volume.

## Troubleshooting

### Docker is installed but unavailable

Start Docker Desktop and wait for its engine to become ready. Run
`docker version` and confirm that both client and server details appear.

### Backend tests cannot find a Docker environment

The full Spring test suite uses Testcontainers even when the Compose database is
stopped. Start the Docker daemon and retry the Maven command. Do not manually
start a test database; Testcontainers manages its own disposable PostgreSQL.

### PostgreSQL does not become healthy

Inspect its status and logs:

```powershell
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml ps
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml logs postgres
```

Confirm that `infrastructure/.env` exists and contains every variable from
`.env.example`.

### Port 5432 is already in use

Change `POSTGRES_PORT` in `infrastructure/.env`, for example to `5433`. Confirm
that the chosen port is free; `5433` is an example, not a guaranteed fallback.
The backend must use the same host port.

PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5433/opsflow"
```

macOS or Linux:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5433/opsflow"
```

Recreate only the container and network; this preserves the database volume:

```powershell
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml down
docker compose --env-file infrastructure/.env -f infrastructure/compose.yaml up -d --wait --wait-timeout 180
```

### Port 8080 is already in use

Set `SERVER_PORT=8081` in the backend terminal before starting Spring Boot.

PowerShell:

```powershell
$env:SERVER_PORT="8081"
```

macOS or Linux:

```bash
export SERVER_PORT="8081"
```

### Port 4200 is already in use

Start Angular on another port:

```powershell
npm --prefix frontend start -- --port 4201
```

### Database credentials changed but authentication still fails

PostgreSQL initialization settings do not alter an existing volume. Either
restore the credentials that created the volume or, if its data can be
discarded, follow the explicitly destructive reset procedure above.

### The wrong Java, Node.js, or npm version is active

Re-run `java --version`, `node --version`, and `npm --version` in the same
terminal that starts the application. Version managers modify the current shell
environment; opening a new terminal may require activating the manager again.

### `npm ci` reports a lockfile mismatch

Do not work around the failure with an unreviewed dependency update. Confirm
that `frontend/package.json` and `frontend/package-lock.json` come from the same
commit, then retry `npm --prefix frontend ci`.

### npm reports pending dependency install scripts

npm may warn that dependency install scripts are not yet covered by its
`allowScripts` configuration. Do not approve scripts blindly. The warning is
non-blocking in the current baseline when tests and the production build pass;
review each package and its script before granting future approval.

## Deeper documentation

- [Backend development, testing, database configuration, and migrations](backend/README.md)
- [Frontend development and testing](frontend/README.md)
- [Local PostgreSQL and Docker Compose](infrastructure/README.md)
- [Project documentation index](docs/README.md)
- [Backend module boundaries](docs/architecture/backend-modules.md)

## Official references

- [Angular version compatibility](https://angular.dev/reference/versions)
- [Spring Boot Maven Plugin: running an application](https://docs.spring.io/spring-boot/maven-plugin/run.html)
- [npm clean install](https://docs.npmjs.com/cli/v11/commands/npm-ci/)
- [Docker Desktop](https://docs.docker.com/desktop/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Testcontainers for Java](https://java.testcontainers.org/)
