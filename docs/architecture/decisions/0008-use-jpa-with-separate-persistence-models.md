# ADR-0008: Use JPA with separate persistence models

- Status: Accepted
- Date: 2026-09-04
- Supersedes: None
- Superseded by: None

## Context

Issue [#32](https://github.com/Felipao724/opsflow/issues/32) requires a mapping
decision before implementing the first persistent business module. ADR-0005
selected PostgreSQL, not an ORM; ADR-0006 established Flyway schema ownership.

The identity domain contains immutable entities and value objects. Organization
requires a nonempty membership collection, matching organization IDs, and no
duplicate profile membership. Constructor validation currently protects these
invariants when reconstructing an aggregate. The contributor has JPA experience
and wants to learn separate persistence models without building a mapping framework.

## Decision

Use Spring Data JPA with Hibernate as the default persistence approach for the
first business module, with persistence entities separate from domain objects.
Keep existing JDBC use for technical database checks; this is not a ban on SQL.

- Domain types stay in `modules.identity.internal.domain`, without JPA or Spring
  dependencies. They retain their constructors, value objects, and invariants.
- Production JPA entities, Spring Data repositories, mappers, and adapters will
  belong in `modules.identity.internal.infrastructure.persistence`.
- Application-facing persistence contracts expose domain types or deliberate
  read projections, not JPA entities or Spring Data repository interfaces.
- Use small handwritten mappers initially. They translate supplied state, never
  query the database, generate replacement IDs, or authenticate users.
- Adapters load the required state and reconstruct valid aggregates inside a
  deliberate transaction boundary. Mapping does not imply automatic dirty
  checking of the separate domain object. Updates must explicitly transfer
  validated state to the managed persistence representation.
- Define fetch plans per use case. Do not blindly replace membership collections
  or enable broad cascades. Summary reads may use projections without loading a
  complete Organization aggregate. Large membership collections require review.
- Flyway remains the sole schema evolution owner. Set Hibernate to `validate`
  and disable Open EntityManager in View. Neither mapping annotations nor schema
  validation substitute for migration-defined constraints and their tests.
- For tenant-owned data, application-facing operations must require a trusted
  OrganizationId and constrain queries by it. Global identity lookup by issuer
  and subject is a separate operation. JPA entities and unscoped CRUD repositories
  must not escape the adapter. Authorization and negative isolation tests remain
  required; this ADR does not claim tenant isolation is implemented.
- Preserve domain-generated UUIDs. Before introducing Spring Data `save`, decide
  and test new-entity detection explicitly: an assigned non-null ID must not be
  assumed to mean an existing row. The experiment uses EntityManager.persist.

Only the JPA starter is added. Boot manages compatible dependency versions.
No MapStruct or Spring Data JDBC dependency is introduced. Keep the JDBC starter
as an explicit dependency for existing JDBC consumers.

## Alternatives considered

| Approach | Mapping and query control | Testability and coupling | Decision in this context |
| --- | --- | --- | --- |
| JPA with separate models | Extra entities and translation; generated SQL with explicit fetch planning | Pure domain tests; real database tests still needed for ORM behavior | Selected: familiar JPA plus deliberate practice of the persistence boundary |
| JPA directly on domain | Less translation; JPA lifecycle and collection requirements shape domain classes | Business methods can still be unit tested; reconstruction bypasses the business constructor | Valid alternative, but would change current immutable design and architecture rules |
| Spring Data JDBC | Aggregate-oriented mapping, explicit saves, no lazy loading or dirty tracking | Simpler lifecycle; aggregate mapping and domain reconstruction still need evaluation | Plausible, but not selected for this learning iteration; not experimentally rejected |
| JdbcClient | Explicit SQL and row-to-domain assembly; no intermediate JPA model required | Domain can remain independent; query and aggregate assembly tests required | Strong query-control alternative; accepts more handwritten persistence work |

Spring Data JDBC offers a simpler lifecycle without sessions or dirty tracking
([official rationale](https://docs.spring.io/spring-data/relational/reference/jdbc/why.html)).
JdbcClient supports explicit SQL and result mapping
([Spring JDBC reference](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html)).
Neither alternative is inherently slower or less suitable for DDD. No comparative
benchmark was performed. JPA is not selected on an unverified market-share claim.

## Consequences

### Positive

- Preserve framework-independent domain validation and value objects.
- Reuse JPA experience while learning persistence contexts, mapping, and fetching.
- Keep database representation changes behind a module-internal boundary.

### Negative

- Maintain two representations and test translation for lost or altered state.
- Domain changes are not automatically persisted by Hibernate dirty checking.
- Lazy loading, N+1 queries, collection synchronization, and transaction lifetime
  remain responsibilities even with separate models.
- Reconstructing a whole Organization may become expensive as memberships grow.

### Neutral

- This decision supplements, rather than supersedes, ADR-0005 and ADR-0006.
- Production schema and repository adapters are now implemented; onboarding is
  owned by a later issue.

## Assumptions and revisit triggers

- Initial membership collections are small enough for aggregate reconstruction.
  Revisit when measured latency, memory, or query count exceeds a use-case budget.
- Handwritten mapping stays small. Revisit automation if repetitive translation
  causes recurring defects; do not introduce a generic mapper preemptively.
- Review the approach if multiple adapters are dominated by native SQL, mapping
  duplicates business rules, or collection updates produce unintended writes.
- Confirm fetch and update semantics during Organization/Membership adapter work.
  Failure to preserve invariants with bounded queries warrants revisiting the
  aggregate boundary or persistence approach, not merely adding eager loading.

## Validation

- [JPA dependency](../../../backend/pom.xml) and
  [configuration](../../../backend/src/main/resources/application.yaml).
- [ArchUnit rules](../../../backend/src/test/java/com/opsflow/opsflow_backend/architecture/BackendArchitectureTest.java)
  enforce framework independence of the identity domain.
- The production [persistence entities and adapters](../../../backend/src/main/java/com/opsflow/opsflow_backend/modules/identity/internal/infrastructure/persistence)
  map profiles, organizations, and memberships without annotating the domain.
- [User profile adapter tests](../../../backend/src/test/java/com/opsflow/opsflow_backend/modules/identity/internal/infrastructure/persistence/JpaUserProfileRepositoryAdapterTest.java)
  cover creation, lookup, missing identities, and translation of the external
  identity uniqueness constraint.
- [Organization adapter tests](../../../backend/src/test/java/com/opsflow/opsflow_backend/modules/identity/internal/infrastructure/persistence/JpaOrganizationRepositoryAdapterTest.java)
  cover aggregate round trips, complete membership reconstruction, and a
  negative non-member lookup against PostgreSQL 18.6 with Flyway migrations.
- Adapters use `EntityManager.persist` for domain-assigned IDs rather than asking
  Spring Data to infer new state from a non-null ID. A flush exposes database
  conflicts inside the persistence boundary without committing the transaction.
- This evidence does not cover concurrent onboarding, update/delete semantics,
  or authorization beyond the repository's scoped lookup. Those require evidence
  in subsequent application issues.

## References

- [Jakarta Persistence specification](https://jakarta.ee/specifications/persistence/3.2/jakarta-persistence-spec-3.2.html)
- [Spring Data JPA entity persistence and new-state detection](https://docs.spring.io/spring-data/jpa/reference/jpa/entity-persistence.html)
- [Flyway migration locations](https://documentation.red-gate.com/flyway/reference/configuration/flyway-namespace/flyway-locations-setting)
