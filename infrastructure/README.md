# Local Infrastructure

This directory contains the infrastructure required for local OpsFlow development.

Currently, it provides a PostgreSQL 18 database managed with Docker Compose. It is intended only for local development and does not define a production deployment architecture.

## Prerequisites

Install and start Docker Desktop before running the environment.

Verify that Docker and Docker Compose are available:

```powershell
docker version
docker compose version
```

The `docker version` output must include both `Client` and `Server` sections.

## First-time setup

Commands in this document must be executed from the repository root.

Create your local environment file:

```powershell
Copy-Item infrastructure\.env.example infrastructure\.env
```

The `.env` file is ignored by Git and may be customized for your machine. Do not commit local credentials.

Available settings:

| Variable            | Default development value | Purpose                          |
| ------------------- | ------------------------- | -------------------------------- |
| `POSTGRES_DB`       | `opsflow`                 | Initial database name            |
| `POSTGRES_USER`     | `opsflow`                 | Initial database user            |
| `POSTGRES_PASSWORD` | `opsflow_local_password`  | Local-only database password     |
| `POSTGRES_PORT`     | `5432`                    | Port exposed on the host machine |

The example password is intended exclusively for local development and must never be reused in production.

## Validate the configuration

Render and validate the resolved Compose configuration without creating containers:

```powershell
docker compose -f infrastructure\compose.yaml config
```

Be aware that this command prints the resolved development password in the terminal.

## Start PostgreSQL

```powershell
docker compose -f infrastructure\compose.yaml up -d --wait
```

This command creates the required network and named volume, starts PostgreSQL in the background, and waits until the database reports a healthy status.

On the first run, Docker also downloads the official PostgreSQL 18 image.

## Check the service status

```powershell
docker compose -f infrastructure\compose.yaml ps
```

The PostgreSQL service should report `healthy`.

To inspect its logs:

```powershell
docker compose -f infrastructure\compose.yaml logs postgres
```

## Connect with psql

Run the PostgreSQL client inside the active container:

```powershell
docker compose -f infrastructure\compose.yaml exec postgres psql -U opsflow -d opsflow
```

Exit the interactive client with:

```text
\q
```

The database is also accessible from the host at `127.0.0.1` using the port configured in `infrastructure/.env`.

## Stop the environment and preserve data

```powershell
docker compose -f infrastructure\compose.yaml down
```

This removes the container and Compose network but preserves the `opsflow_postgres_data` named volume. Starting the environment again reuses the existing database.

## Reset the local database

> **Warning:** The following command permanently deletes all data stored in the local OpsFlow PostgreSQL volume.

```powershell
docker compose -f infrastructure\compose.yaml down --volumes
```

Start the environment again to initialize a new empty database:

```powershell
docker compose -f infrastructure\compose.yaml up -d --wait
```

The initialization variables are applied when PostgreSQL creates a new database in an empty volume. Changing them in `.env` does not reconfigure an existing database automatically.

## Troubleshooting

### Docker server is unavailable

Start Docker Desktop and wait until its engine is running. Confirm that `docker version` displays a `Server` section.

### Port 5432 is already in use

Change `POSTGRES_PORT` in `infrastructure/.env`, for example:

```dotenv
POSTGRES_PORT=5433
```

Recreate the environment afterward:

```powershell
docker compose -f infrastructure\compose.yaml down
docker compose -f infrastructure\compose.yaml up -d --wait
```

Only the host port changes. PostgreSQL continues listening on port `5432` inside the container.

## Official documentation

- [Docker Compose overview](https://docs.docker.com/compose/)
- [Docker Compose `up`](https://docs.docker.com/reference/cli/docker/compose/up/)
- [Docker Compose `down`](https://docs.docker.com/reference/cli/docker/compose/down/)
- [Docker volumes](https://docs.docker.com/engine/storage/volumes/)
- [PostgreSQL official Docker image](https://hub.docker.com/_/postgres)
- [PostgreSQL `psql`](https://www.postgresql.org/docs/18/app-psql.html)
