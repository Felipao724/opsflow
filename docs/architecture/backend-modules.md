# Backend module boundaries

OpsFlow uses a modular monolith architecture. The backend is built and deployed as one Spring Boot application, while business capabilities remain separated through explicit package and dependency boundaries.

## Package structure

```text
com.opsflow.opsflow_backend
├── modules
│   └── <module-name>
│       ├── api
│       └── internal
│           ├── application
│           ├── domain
│           └── infrastructure
├── platform
└── sharedkernel
```

Each direct subpackage of `modules` represents one business capability.

The packages shown under `internal` are optional. They should only be introduced when the module requires them; they are not global layers that every module must reproduce.

## Business modules

A business module owns a complete vertical slice of functionality, including its use cases, domain concepts, persistence implementation, and external adapters.

New functionality must be organized by business capability:

```text
modules/workorders
modules/customers
modules/inventory
```

It must not be organized using global technical layers:

```text
controller
service
repository
entity
```

### Public API

A module exposes the contracts intended for other modules through:

```text
modules.<module-name>.api
```

The API should remain small and stable. It can contain contracts such as commands, queries, results, events, and interfaces that represent supported interactions.

### Internal implementation

Implementation details belong under:

```text
modules.<module-name>.internal
```

Other modules must not import classes from this package. Internal classes may be reorganized without affecting consumers as long as the public API remains compatible.

## Dependency rules

The permitted dependency direction is:

```text
modules ───────► platform
modules ───────► sharedkernel
module A ──────► module B api
```

The following dependencies are prohibited:

```text
platform ──X──► modules
sharedkernel ──X──► modules
sharedkernel ──X──► platform
module A ──X──► module B internal
```

Business modules must also remain free of dependency cycles.

## Cross-module communication

A module may communicate synchronously with another module only through the target module's `api` package.

A module must not:

- access another module's internal classes;
- access another module's persistence implementation;
- query or modify tables owned by another module directly;
- create a shared utility package to bypass module boundaries.

Asynchronous messaging may be introduced later through an explicit architectural decision. It is not required to preserve module boundaries inside the current application.

## Platform

`platform` contains reusable technical capabilities such as database connectivity, messaging infrastructure, security integration, and external system adapters.

Platform code must remain independent of business modules. A technical capability must not contain business rules or import classes from a specific business module.

## Shared kernel

`sharedkernel` is reserved for minimal, stable, and framework-independent concepts that genuinely belong to multiple business modules.

It must not become a generic utilities package. Adding a type to the shared kernel requires stronger justification than sharing code through duplication or an explicit module API.

Shared-kernel types must not depend on:

- business modules;
- platform infrastructure;
- Spring Framework;
- Jakarta APIs.

## Spring component scanning

`OpsflowBackendApplication` remains in the root package:

```text
com.opsflow.opsflow_backend
```

Spring Boot therefore scans its descendant packages, including `modules`, `platform`, and `sharedkernel`.

Module-specific component scanning must not be added without an explicit need. New Spring components must remain below the application root package so they are discovered by the existing configuration.

## Automated enforcement

Architecture rules are implemented with ArchUnit in:

```text
backend/src/test/java/com/opsflow/opsflow_backend/architecture/BackendArchitectureTest.java
```

Run them from the repository root:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml "-Dtest=BackendArchitectureTest" test
```

The tests currently verify that:

- platform does not depend on business modules;
- shared kernel remains independent and framework-free;
- modules communicate only through public API packages;
- business modules do not form dependency cycles.

Any intentional exception requires an explicit architectural decision and a corresponding review of the automated rules.

## Creating a future module

Before creating a new module:

1. Identify the business capability it owns.
2. Create it as a direct child of `modules`.
3. Expose only the contracts required by other modules through `api`.
4. Keep implementation details under `internal`.
5. Avoid introducing optional internal packages until they are needed.
6. Run the architecture tests before opening a pull request.

## Official references

- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [Spring Boot `@SpringBootApplication`](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/SpringBootApplication.html)
