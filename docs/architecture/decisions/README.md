# Architecture Decision Records

This directory is the decision log for architecturally significant OpsFlow
choices. An ADR records one decision, the context in which it was made, the
alternatives considered, and the consequences accepted by the project.

ADRs are not framework tutorials, implementation plans, or a list of every
dependency. Create one when a choice materially affects system structure,
quality attributes, boundaries, data ownership, integration, deployment, or the
way future changes must be made.

## Decision index

| ADR  | Decision                                                                                           | Status   |
| ---- | -------------------------------------------------------------------------------------------------- | -------- |
| 0001 | [Use a monorepo](0001-use-a-monorepo.md)                                                           | Accepted |
| 0002 | [Use Java and Spring Boot for the backend](0002-use-java-and-spring-boot-for-the-backend.md)       | Accepted |
| 0003 | [Structure the backend as a modular monolith](0003-structure-the-backend-as-a-modular-monolith.md) | Accepted |
| 0004 | [Use Angular for the web frontend](0004-use-angular-for-the-web-frontend.md)                       | Accepted |
| 0005 | [Use PostgreSQL for relational persistence](0005-use-postgresql-for-relational-persistence.md)     | Accepted |
| 0006 | [Manage the database schema with Flyway](0006-manage-the-database-schema-with-flyway.md)           | Accepted |
| 0007 | [Use a layered automated testing strategy](0007-use-a-layered-automated-testing-strategy.md)       | Accepted |

## File naming and numbering

Copy `template.md` and name the new file:

```text
NNNN-short-decision-title.md
```

Examples:

```text
0008-use-domain-events-between-modules.md
0009-adopt-a-production-container-platform.md
```

Use the next integer after the highest number already present, padded to four
digits. Numbers are chronological identifiers, not priorities, and must never
be reused even when a proposal is rejected or a decision is superseded.

Use a short, active title that states the chosen direction. Keep one primary
decision per ADR so a future change can replace it without invalidating
unrelated choices.

## Status lifecycle

An ADR has one of these statuses:

- **Proposed** — under discussion and not yet an architectural commitment;
- **Accepted** — approved and part of the current architecture;
- **Rejected** — considered but not adopted;
- **Superseded by ADR-NNNN** — previously accepted, then replaced by a newer
  decision.

When a decision changes, create a new ADR. Update the old record's status to
link to its replacement, and add a `Supersedes` link in the new record. Preserve
the old context, decision, alternatives, and consequences as history.

Small corrections such as spelling, broken links, or inaccurate validation
paths may be edited in place. A change to the meaning or rationale requires a
new ADR.

## Commitments and assumptions

The `Decision` section contains the current commitment and uses direct language
such as “We will” or “We will not.” The `Assumptions and revisit triggers`
section records conditions that made the decision reasonable but are not
guaranteed forever.

A trigger becoming true does not automatically invalidate an ADR. It requires
the team to evaluate the decision again and, if the direction changes, create a
superseding ADR.

## Review process

1. Confirm that the choice is architecturally significant and not merely a
   local implementation detail.
2. Copy the template and assign the next number.
3. Write the context without assuming the preferred solution.
4. State the decision, meaningful alternatives, and positive and negative
   consequences.
5. Separate current commitments from assumptions and explicit revisit triggers.
6. Link the code, tests, or guides that validate the decision where possible.
7. Add the ADR to the decision index.
8. Review the ADR in the same pull request as its implementation when practical.

An accepted ADR is not proof that a choice can never change. It is evidence that
the trade-off was made deliberately and must be changed deliberately.

## Diagrams and automated validation

An ADR may link to a small diagram when relationships or a sequence cannot be
explained clearly in text. Keep the diagram close to the architecture
documentation and update it with the decision it illustrates. Do not create
speculative system diagrams for components or deployment topology that OpsFlow
has not implemented.

The project does not currently use a dedicated ADR linter. Numbering, required
sections, statuses, and index links are checked during review using this
convention and the template. Reconsider automated validation when the decision
log grows enough that repeated numbering, missing sections, or broken index
entries become a recurring problem.
