# ADR-0010: Authorize tenants with local memberships

- Status: Accepted
- Date: 2026-10-02
- Supersedes: None
- Superseded by: None

## Context

A valid access token proves that a trusted provider authenticated a subject and
issued a token for the OpsFlow API. It does not prove that the subject belongs
to a requested OpsFlow organization.

Putting every organization membership and business role into access tokens
would couple product rules to the identity provider and allow authorization
data to remain stale until token expiry. Trusting an organization ID supplied
by the browser without a server-side membership lookup would create a direct
cross-tenant access risk.

## Decision

Keep tenant membership and business roles in OpsFlow PostgreSQL and authorize
each tenant boundary from local state.

For a protected organization operation, the backend must:

1. obtain the external identity only from the validated Spring Security JWT;
2. resolve that `(issuer, subject)` to an OpsFlow `UserProfile`;
3. query the requested organization through a repository operation scoped by
   both `OrganizationId` and `UserProfileId`;
4. require an `ACTIVE` membership;
5. map the local membership role to a module-owned authority; and
6. return a trusted `AuthorizedTenant` context to the caller.

An authenticated principal without the required active membership receives
`403 Forbidden`. Authentication failures remain `401 Unauthorized`. Error
messages must not disclose another tenant's membership or internal data.

Application-facing repository contracts do not expose an unscoped organization
lookup. Future modules must receive or derive an authorized tenant context and
scope their reads and writes by it; Angular route guards are navigation aids,
not an authorization boundary.

## Alternatives considered

| Alternative | Reason not selected for M1 |
| --- | --- |
| Trust a client-supplied organization ID | Identifies the requested tenant but provides no evidence that the caller belongs to it. |
| Store all memberships in JWT claims | Creates stale business authorization and couples membership lifecycle to provider configuration and token renewal. |
| Use only coarse OAuth scopes | Scopes can grant API capabilities but do not express current membership in a specific OpsFlow organization. |
| Depend only on frontend route guards | Browser code is user-controlled and cannot enforce server-side data isolation. |

## Consequences

### Positive

- Membership changes become authoritative on the next backend request rather
  than after token expiry.
- Authentication infrastructure remains independent from OpsFlow tenancy.
- Repository APIs make accidental unscoped organization access harder.
- `401` and `403` retain distinct and useful meanings.

### Negative

- Protected tenant operations require local profile and membership queries.
- Every future business module must preserve tenant scoping; one correct module
  does not secure unrelated queries automatically.
- Caching membership decisions later will require an explicit invalidation and
  staleness policy.

### Neutral

- PostgreSQL constraints preserve relationship integrity but do not replace
  authorization checks.
- `OWNER` is the only current role, while the boundary allows deliberate role
  expansion later.

## Assumptions and revisit triggers

- Revisit the aggregate and query design when measured membership lookup cost
  exceeds a use-case budget.
- Consider database row-level security only as an additional defense after its
  operational and connection-context model is designed; it does not silently
  replace application authorization.
- New organization switching, invitations, support access, service accounts, or
  cross-organization workflows require explicit authorization rules and
  negative isolation tests.

## Validation

- `TenantAuthorizationServiceTest` covers active membership, missing local
  profile, and membership denial.
- `JpaOrganizationRepositoryAdapterTest` verifies inactive membership and
  two-user/two-organization isolation against PostgreSQL.
- `IdentityControllerTest` verifies `401` for anonymous requests, success for a
  member, and `403` for a valid identity without membership.
- `OrganizationRepository` requires both organization and profile identity for
  tenant-scoped lookup.

## References

- [Angular route guard security warning](https://angular.dev/guide/routing/route-guards)
- [Spring Security authorization architecture](https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html)
- [PostgreSQL constraints](https://www.postgresql.org/docs/18/ddl-constraints.html)
- [ADR-0008: Use JPA with separate persistence models](0008-use-jpa-with-separate-persistence-models.md)

