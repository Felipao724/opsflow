# Identity persistence schema

This guide describes the identity module schema implemented by Flyway migration
`V2__create_identity_and_organization_schema.sql`. It documents current database
guarantees, not the repositories or onboarding use case planned by later issues.

## Ownership and boundaries

OpsFlow PostgreSQL owns local profiles, organizations, memberships, and business
roles. Keycloak continues to own credentials, authentication sessions, tokens,
and its own database tables. The application schema contains no Keycloak tables.

Flyway is the sole schema evolution owner. Hibernate validates mappings against
the migrated schema and must not create or update it. Once V2 has been applied
in a shared environment, corrections require a new forward-only migration; its
contents and checksum must not be edited.

## Relationships

```text
user_profiles 1 ────< memberships >──── 1 organizations
```

A membership has its own UUID and links one profile to one organization. The
database permits a profile to belong to several organizations, while preventing
the same profile-to-organization relationship from appearing twice.

## Tables and invariants

### `user_profiles`

- `id` is an application-generated UUID primary key.
- `issuer` and `subject` are required and cannot be blank after PostgreSQL
  `btrim` processing.
- `(issuer, subject)` is unique. Subject alone is not globally unique and email
  is not an identity key.
- `created_at` is a required `TIMESTAMPTZ` supplied by PostgreSQL when omitted.

The composite unique constraint also supplies the index for the authenticated
external-identity lookup path.

### `organizations`

- `id` is an application-generated UUID primary key.
- `name` is required, nonblank, and limited to 120 PostgreSQL characters.
- Organization names are deliberately not unique because no business rule
  prevents separate organizations from sharing a name.
- `created_at` is a required `TIMESTAMPTZ` supplied by PostgreSQL when omitted.

The database cannot express “every organization has at least one membership”
as a valid row-local check. The domain model and onboarding transaction must
create the organization and its OWNER membership atomically.

### `memberships`

- `id` is an application-generated UUID primary key.
- `user_profile_id` and `organization_id` are required foreign keys.
- Both foreign keys use `ON DELETE RESTRICT`. Removing referenced profiles or
  organizations requires an explicit business process; membership rows are not
  silently cascaded.
- `(user_profile_id, organization_id)` is unique.
- `role` is required and constrained to the current domain value `OWNER`.
- `created_at` is a required `TIMESTAMPTZ` supplied by PostgreSQL when omitted.

The composite unique constraint starts with `user_profile_id`, supporting a
profile's membership lookup. The explicit `ix_memberships_organization_id`
index supports lookup from an organization. Index presence does not guarantee
that PostgreSQL will choose it for every query; plans depend on data and
statistics.

## Defense in depth

Domain validation and database constraints have different jobs. The domain
rejects invalid operations early in business language. PostgreSQL remains the
final integrity boundary for concurrent writes, adapter defects, and direct SQL.
Adapters must translate constraint violations into safe application outcomes
instead of exposing SQL details.

Some validations are close rather than perfectly identical. For example,
Java's `String.isBlank()` and PostgreSQL's `btrim(value) <> ''` do not classify
every Unicode whitespace character identically. Revisit normalization only if
product requirements demand a canonical policy.

## Application persistence boundary

Application code depends on the module-owned `UserProfileRepository` and
`OrganizationRepository` contracts. These interfaces exchange domain objects
and value objects; they do not expose JPA entities or Spring Data interfaces.

Infrastructure implements those contracts with JPA adapters, flat persistence
entities, handwritten mappers, and internal Spring Data repositories. The
profile adapter supports lookup by the complete external identity. The
organization contract requires both `OrganizationId` and `UserProfileId`, so an
unscoped organization lookup is not part of the application-facing API.

The organization adapter first verifies the requested organization/member pair,
then loads every membership required to reconstruct and validate the aggregate.
The mappers perform no queries: adapters gather persistence state and domain
constructors enforce invariants.

New objects have domain-assigned, non-null UUIDs. Adapters therefore use
`EntityManager.persist` rather than relying on Spring Data `save` to infer new
state from the ID. The external-identity unique constraint is translated into
`ExternalIdentityAlreadyRegisteredException`; other integrity failures remain
technical until the module defines an accurate application outcome.

Repository adapters do not own transaction boundaries. A service-layer use case
must wrap the complete operation so profile, organization, and initial OWNER
membership commit or roll back together. A flush inside an adapter sends pending
SQL so constraints can be translated, but does not commit the transaction.

## Deferred decisions

- Organization lifecycle or soft deletion is not modeled yet.
- `OWNER` is the only current role; new values require domain and migration
  changes.
- UUID version 4 remains generated by the domain. Time-ordered UUIDs require a
  separate measured decision.
- Row-level security, partitioning, production storage, and final query
  optimization remain out of scope.

## Validation

- `DatabaseConnectivityTest` confirms Flyway reaches schema version 2 on an
  empty PostgreSQL 18.6 container.
- `IdentitySchemaConstraintsTest` exercises required values, external identity
  uniqueness, duplicate organization names, foreign keys, relationship
  uniqueness, role values, restricted deletion, name length, and the explicit
  organization membership index against PostgreSQL.

## References

- [PostgreSQL constraints](https://www.postgresql.org/docs/18/ddl-constraints.html)
- [PostgreSQL UUID type](https://www.postgresql.org/docs/18/datatype-uuid.html)
- [PostgreSQL date/time types](https://www.postgresql.org/docs/18/datatype-datetime.html)
- [ADR-0005: Use PostgreSQL for relational persistence](decisions/0005-use-postgresql-for-relational-persistence.md)
- [ADR-0006: Manage the database schema with Flyway](decisions/0006-manage-the-database-schema-with-flyway.md)
- [ADR-0008: Use JPA with separate persistence models](decisions/0008-use-jpa-with-separate-persistence-models.md)
