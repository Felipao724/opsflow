# OpsFlow development and learning plan

This plan connects the M1 delivery work with deliberate preparation for Full
Stack Java and Angular interviews. It is a working plan: durable architecture
decisions continue to belong in the architecture guides and ADRs.

## Weekly structure

| Time | Activity | Intended result |
| --- | --- | --- |
| 09:00–10:30 | Focused technical study | Understand and rehearse one relevant concept |
| 10:30–10:45 | Easy walk and screen break | Recover attention without adding a hard training session |
| 10:45–12:00 | Interview practice | Solve and explain a bounded problem |
| 13:30–15:30 | OpsFlow implementation | Produce a tested, reviewable project increment |

Longer 20–30 minute walks can be used for oral recall, simple documentation,
videos, or flashcards. Active programming, architecture, and debugging remain
desk activities.

## Interview-practice rotation

| Day | Focus | Minimum evidence |
| --- | --- | --- |
| Monday | Data structures and algorithms | Explained solution, complexity, and edge cases |
| Tuesday | Java and Spring Boot | Backend exercise or code diagnosis with tests |
| Wednesday | Data structures and algorithms | A second weekly problem pattern |
| Thursday | Angular and TypeScript | Component, state, async flow, forms, or debugging exercise |
| Friday | Timed Full Stack simulation | Solution plus a short communication retrospective |

Problems should generally progress from Easy to Medium. Interview practice is
not measured only by a passing implementation: the solution must be explained
from the initial approach through optimization, trade-offs, complexity, and
edge cases.

## Learning loop

When practical, each technical topic follows this sequence:

**Concept → isolated exercise → OpsFlow application → interview explanation**

Examples for M1 include:

- OAuth 2.0 and OIDC → explain Authorization Code with PKCE → verify the Angular
  and Keycloak flow → explain why a browser client cannot keep a secret.
- Spring Security → test JWT claim validation → protect an OpsFlow endpoint →
  distinguish authentication from membership-based authorization.
- SQL and Flyway → design relational constraints → migrate profiles,
  organizations, and memberships → explain uniqueness and referential integrity.
- Transactions → model first-login onboarding → make creation atomic and
  idempotent → explain race handling and transaction boundaries.
- Angular state and HTTP → isolate authentication state → implement onboarding
  UI and API calls → explain signals, RxJS, interception, and error handling.

## Ticket-to-learning workflow

GitHub issues remain the source of truth for deliverable project work. This
learning plan adds a pedagogical structure around each issue without replacing
its objective or acceptance criteria.

Each issue should generally follow this sequence:

1. **Ticket kickoff** — Synchronize `main`, create the issue branch, review the
   acceptance criteria, and identify the knowledge required.
2. **Focused lesson** — Study the concept, problem, alternatives, trade-offs,
   common mistakes, and its specific relevance to OpsFlow.
3. **Guided example** — Implement and explain the first representative pattern.
4. **Practical challenge** — Apply the pattern manually in architecture or
   application code. Routine tests and documentation are not challenges unless
   they are the learning objective.
5. **Reasoned review** — Examine responsibilities, coupling, naming, invalid
   states, security, error handling, and viable alternatives.
6. **OpsFlow integration** — Connect and exercise the increment in the real
   application flow.
7. **Interview explanation** — Explain the problem, design, trade-offs, failure
   modes, and verification evidence without relying entirely on notes.
8. **Ticket close-out** — Record the recap, learning, evidence, remaining risk,
   commit, pull request, and issue status.

For visual work, the lesson and review also cover CSS selectors, cascade,
specificity, layout, responsive behavior, accessibility, and pseudo-elements
such as `::before` and `::after` when relevant.

One issue may span multiple project blocks. A session is complete when it
produces one understandable and verifiable increment; it does not need to close
the entire issue.

## Progressive implementation autonomy

Learning sessions should deliberately reduce copy-and-paste implementation as
concepts become familiar. The amount of guidance depends on whether an artifact
or pattern is new, practiced, or already understood.

### First use of an artifact

Before introducing a new Angular, Spring, or architectural artifact, explain:

- What it is and which problem it solves.
- Its naming and file conventions.
- Where it belongs in the architecture.
- Its lifecycle and dependencies, when applicable.
- Relevant alternatives and trade-offs.
- How OpsFlow will use it.
- The applicable official documentation.

Then follow this progression:

**Concept → naming → design → guided example → manual implementation → review**

### Repeated use of an artifact

After an artifact type has already been introduced:

- Provide only a short conceptual reminder.
- Prefer framework generators, such as Angular CLI, for repeated scaffolding.
- Explain relevant generator commands and non-default options rather than
  applying them mechanically.
- Provide requirements and constraints instead of a complete implementation.
- Let the developer implement the application or architecture code.
- Review the result for correctness, naming, coupling, security, and edge cases.

For example, `ng generate` creates artifacts inside an Angular project, while
`ng new` creates a new project or workspace. Generator use should follow an
understanding of the underlying artifact instead of replacing it.

### Mature concepts

Once a concept has been practiced repeatedly, guidance should normally contain
only:

- The objective.
- Architectural constraints.
- Acceptance criteria.
- Relevant official documentation.

The developer should propose and implement the solution. The reviewer should
focus on architecture, correctness, security, maintainability, and failure
modes.

### Autonomy progression

1. Complete guided example with explanation.
2. Partially specified implementation completed by the developer.
3. Contracts, constraints, and acceptance criteria only.
4. Developer-proposed design and implementation.
5. Technical and architectural review.

Generate repetitive boilerplate when practical, but write educational code
manually until the concept is understood. Routine tests may be implemented by
the reviewer, while architecture and application code remain the primary
learning challenges.

## Current project baseline

As of 2026-08-30, OpsFlow is in **M1 — Identity and Access** and does not yet
contain business functionality.

- `main` contains the reproducible Keycloak realm, Spring Boot JWT resource
  server, and the completed Angular Authorization Code with PKCE integration
  from issue `#29`.
- Angular keeps tokens in memory, restricts bearer propagation to the OpsFlow
  API, and can complete a real authenticated request to the backend.
- Backend verification passes 19 tests.
- Frontend verification passes 10 tests across 3 files, and the production
  bundle builds successfully.

## Next OpsFlow learning arcs

These arcs describe the expected progression, not a requirement to complete an
entire topic in one two-hour project block. The active GitHub issue determines
the exact delivery order.

### Arc 1 — Implement the Angular authentication lifecycle (`#30`)

- Model loading, authenticated, unauthenticated, and failure as explicit state
  rather than independent booleans.
- Expose application-owned, read-only authentication state with Angular
  signals and derive permitted actions with `computed()`.
- Restore the provider session after reload without persisting tokens.
- Implement deliberate login, token renewal, logout, safe return navigation,
  and route protection.
- Exercise success, provider failure, expired session, refresh failure, and
  anonymous navigation before closing the ticket.

Learning focus: finite state machines, writable and read-only signals,
`computed()`, signals versus RxJS, token lifecycle, route guards, safe redirects,
and the distinction between navigation behavior and backend security.

### Arc 2 — Design the local identity model

- Turn the proposed `UserProfile`, `Organization`, and `Membership` concepts
  into explicit domain and persistence constraints.
- Decide identifiers, timestamps, organization roles, uniqueness rules, and the
  stable `(issuer, subject)` external identity key.
- Define the smallest onboarding API contract before implementation.

Learning focus: relational modeling, aggregate boundaries, normalization,
constraints, and API contracts.

### Arc 3 — Persist identity and tenancy safely

- Add a forward-only Flyway migration for profiles, organizations, and
  memberships.
- Implement the first-login onboarding transaction in the appropriate backend
  module.
- Make repeated or concurrent onboarding deterministic and safe.
- Cover schema and application behavior with integration tests.

Learning focus: JDBC, transactions, idempotency, race conditions, and PostgreSQL
constraints.

### Arc 4 — Expose membership-derived tenant context

- Resolve the authenticated external subject to an OpsFlow profile.
- Derive organization access from stored membership rather than a client-sent
  tenant identifier.
- Add tests proving that missing or foreign membership cannot access tenant
  data.

Learning focus: authentication versus authorization, trust boundaries, secure
defaults, and negative security tests.

### Arc 5 — Complete the onboarding vertical slice

- Add the Angular onboarding state and initial-organization form.
- Connect it to the authenticated backend contract.
- Handle loading, validation, retryable failure, conflict, and completed states.
- Run the full backend and frontend validation, then record remaining M1 gaps.

Learning focus: typed forms, async state, HTTP error handling, accessibility,
and explaining an end-to-end design under interview conditions.

## Daily close-out

Each 13:30–15:30 project block ends with four short notes:

1. What now works?
2. What technical concept was applied?
3. What evidence exists (test, code, screenshot, or decision)?
4. What is the next concrete action?

## Friday retrospective

- What could be explained without notes?
- Where did implementation or communication stall?
- What part of OpsFlow is now demonstrable in an interview?
- Did the study and exercise load preserve concentration and recovery?
- Did walking help as a transition or interrupt deep work?
- Select only one or two weaknesses for the next week.
