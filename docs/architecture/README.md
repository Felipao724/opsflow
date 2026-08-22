# Architecture

This directory describes the architecture that OpsFlow has implemented and the
decisions that constrain its evolution.

OpsFlow currently consists of:

- one repository containing the application, local infrastructure, automation,
  and documentation;
- one Spring Boot backend structured as a modular monolith;
- one Angular web frontend;
- one PostgreSQL database whose schema is managed by Flyway;
- automated unit, component, architecture, and integration tests executed by
  continuous integration.

These statements describe the M0 foundation. They do not imply a final domain
model, production deployment topology, authentication model, or future service
boundaries.

## Decision log

[Architecture Decision Records](decisions/README.md) capture why significant
choices were made, which alternatives were considered, and what consequences
the project accepts. Read the decision log before proposing a change that
conflicts with an accepted decision.

## Architecture guides

- [Backend module boundaries](backend-modules.md) defines the package structure,
  dependency direction, public module APIs, and ArchUnit enforcement used by the
  modular monolith.

Guides explain how the current architecture works. ADRs explain why the project
chose that direction. A guide may evolve as implementation details improve,
while an accepted ADR remains a historical record until another ADR supersedes
it.
