# Architecture Decision Records (ADR)

## What are ADRs?

Architecture Decision Records (ADRs) are documents that capture important architectural decisions made in the project, along with their context and consequences.

**Purpose**:
- Document the reasoning behind significant technical decisions
- Provide context for future developers
- Create a historical record of decision-making process
- Enable informed discussions about architectural changes

**Key Principle**: ADRs are **immutable**. Once accepted, they should not be modified. If a decision changes, create a new ADR that supersedes the old one.

## When to Write an ADR

Create an ADR when making decisions that:

### Technical Decisions
- ✅ Choose between architectural patterns (monolith vs microservices, REST vs GraphQL)
- ✅ Select major libraries or frameworks (Spring Boot, React, database choice)
- ✅ Define API contracts or data models
- ✅ Establish integration patterns with external services
- ✅ Set up infrastructure or deployment strategies

### Process Decisions
- ✅ Establish coding standards or conventions
- ✅ Define testing strategies (unit vs integration test ratios)
- ✅ Choose development workflows (Git workflow, branching strategy)

### MVP-Specific Decisions (FoodiePass)
- ✅ Decisions that impact hypothesis validation (H1, H2, H3)
- ✅ Trade-offs between scope and timeline
- ✅ Feature inclusion/exclusion decisions
- ✅ A/B testing implementation strategies

### When NOT to Write an ADR
- ❌ Routine bug fixes or minor refactoring
- ❌ Decisions that are easily reversible
- ❌ Implementation details that don't affect architecture
- ❌ Temporary workarounds or experiments

**Rule of Thumb**: If the decision will take more than 2 hours to reverse, write an ADR.

## How to Write an ADR

### Step 1: Copy the Template

```bash
cp docs/adr/TEMPLATE.md docs/adr/ADR-XXX-your-decision-title.md
```

### Step 2: Assign a Number

ADR numbers are sequential. Check existing ADRs and use the next available number.

```bash
# Find the highest number
ls docs/adr/ADR-*.md | sort -V | tail -1

# If ADR-003 is the highest, create ADR-004
cp docs/adr/TEMPLATE.md docs/adr/ADR-004-your-title.md
```

### Step 3: Fill Out Each Section

1. **Title**: Clear, concise statement of the decision (e.g., "Use Redis for Session Management")
2. **Metadata**: Date, status, author, deciders
3. **Context**: Explain the problem and background
4. **Decision**: State what was decided and why
5. **Consequences**: List positive and negative outcomes
6. **Alternatives**: Document what else was considered
7. **Trade-offs**: Explicit trade-off analysis (inspired by BPlusTree3)
8. **Hypothesis Validation** (if applicable): How this supports FoodiePass hypothesis testing
9. **Implementation Plan**: Timeline, dependencies, tasks

### Step 4: Request Review

1. Create a PR with the ADR
2. Tag relevant team members for review
3. Discuss in PR comments
4. Address feedback
5. Merge when consensus is reached

### Step 5: Update Status

ADRs go through lifecycle stages:

```
Proposed → Accepted → (Deprecated or Superseded)
```

- **Proposed**: Draft stage, under discussion
- **Accepted**: Decision is approved and being implemented
- **Deprecated**: No longer valid, but kept for historical reference
- **Superseded**: Replaced by a newer ADR (link to the new ADR)

## Naming Convention

Format: `ADR-[NUMBER]-[KEBAB-CASE-TITLE].md`

**Examples**:
- `ADR-001-ab-test-strategy.md`
- `ADR-002-ocr-api-selection.md`
- `ADR-003-database-choice-for-mvp.md`

**Rules**:
- Use 3-digit zero-padded numbers (001, 002, 010, 100)
- Use kebab-case for titles (lowercase, hyphen-separated)
- Keep titles short but descriptive (max 5-6 words)

## ADR Lifecycle

### 1. Creation (Status: Proposed)

```bash
# Create new ADR
cp docs/adr/TEMPLATE.md docs/adr/ADR-XXX-title.md

# Edit the ADR
# Fill out all sections

# Commit and push
git add docs/adr/ADR-XXX-title.md
git commit -m "docs(adr): Add ADR-XXX for [decision]"
git push origin feature/adr-xxx
```

### 2. Review Process

- Create PR with title: `[ADR-XXX] Decision on [topic]`
- Add reviewers: technical lead, affected stakeholders
- Discuss in PR comments
- Iterate on the document
- Update Review History table in the ADR

### 3. Acceptance (Status: Accepted)

- Merge PR when consensus is reached
- Update status to "Accepted"
- Begin implementation

### 4. Implementation Tracking

- Reference the ADR in related PRs: `Implements ADR-XXX`
- Update implementation notes in the ADR if needed (via new PR)

### 5. Deprecation or Superseding

If a decision needs to change:

**Option A: Deprecate**
```markdown
**Status**: Deprecated

**Deprecation Reason**: [Why this is no longer valid]
**Date Deprecated**: YYYY-MM-DD
```

**Option B: Supersede**
```markdown
**Status**: Superseded by ADR-YYY

**Superseded By**: [ADR-YYY-new-decision.md]
**Date Superseded**: YYYY-MM-DD
```

Then create a new ADR (ADR-YYY) explaining the new decision.

## FoodiePass MVP Context

For decisions related to the FoodiePass MVP (hypothesis validation phase), always include:

### Hypothesis Alignment
- Which hypotheses (H1, H2, H3) does this decision impact?
- How does this decision support or hinder hypothesis validation?

### MVP Scope Discipline
- Does this decision add features outside MVP scope?
- What's the justification for scope changes?

### Success Criteria Impact
- How does this affect our ability to meet success criteria?
  - OCR accuracy ≥ 90%
  - Currency accuracy ≥ 95%
  - Food matching relevance ≥ 70%
  - Processing time ≤ 5 seconds
  - Treatment group Yes rate ≥ 70%
  - Treatment/Control ratio ≥ 2.0

### Timeline Implications
- Does this decision impact the 10-day MVP timeline?
- What's the implementation time estimate?

## Trade-off Analysis (BPlusTree3 Pattern)

Every ADR should include explicit trade-off analysis. Common dimensions:

**Performance vs Simplicity**
- High performance often means complex code
- Simple code is easier to maintain but may be slower

**Speed vs Quality**
- Ship fast vs thorough testing
- MVP scope vs complete features

**Flexibility vs Constraints**
- Generic solutions vs specific optimizations
- Future extensibility vs current needs

**Cost vs Capability**
- Free/open-source vs paid services
- DIY vs managed solutions

**Example Trade-off Format**:
```markdown
## Trade-offs

**Performance vs Simplicity**:
- **Decision**: Prioritized simplicity over performance
- **Rationale**: MVP timeline is tight, premature optimization is risky
- **Impact**: May need to refactor for scale later, but reduces time-to-validation

**DIY vs Managed Service**:
- **Decision**: Use managed OCR service (Google Cloud Vision) over building custom
- **Rationale**: OCR accuracy is critical for H2, managed service has proven track record
- **Impact**: Ongoing API costs, but significantly faster to implement and validate
```

## Existing ADRs

| ADR | Title | Status | Date | Related To |
|-----|-------|--------|------|------------|
| [ADR-001](./ADR-001-ab-test-strategy.md) | A/B Test Strategy for MVP | Accepted | 2025-11-11 | H1, H3 validation |

## Best Practices

### DO
- ✅ Write ADRs before implementing (not after)
- ✅ Keep ADRs concise and focused (2-3 pages max)
- ✅ Use clear, simple language
- ✅ Document the "why" more than the "what"
- ✅ Include explicit trade-off analysis
- ✅ Link to related ADRs, PRs, or documentation
- ✅ Seek feedback from stakeholders before finalizing

### DO NOT
- ❌ Modify accepted ADRs (create new ones instead)
- ❌ Write ADRs for trivial decisions
- ❌ Use jargon without explanation
- ❌ Skip the "Alternatives Considered" section
- ❌ Write ADRs in isolation (involve the team)
- ❌ Create ADRs just to document past decisions (they should inform future ones)

## Tools and Automation

### Find Next ADR Number
```bash
# List all ADRs sorted by number
ls -1 docs/adr/ADR-*.md | sort -V

# Get next number
LAST_NUM=$(ls -1 docs/adr/ADR-*.md | sort -V | tail -1 | sed 's/.*ADR-0*\([0-9]*\).*/\1/')
NEXT_NUM=$(printf "%03d" $((LAST_NUM + 1)))
echo "Next ADR: ADR-$NEXT_NUM"
```

### Create ADR from Template
```bash
# Usage: ./scripts/create-adr.sh "Your Decision Title"
# Creates: docs/adr/ADR-XXX-your-decision-title.md

#!/bin/bash
# (Script to be created in scripts/create-adr.sh)
```

### Validate ADR Format
```bash
# Check if ADR has all required sections
grep -E "(Context|Decision|Consequences|Alternatives|Trade-offs)" ADR-XXX-title.md
```

## References

### ADR Best Practices
- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) - Michael Nygard
- [ADR GitHub Organization](https://adr.github.io/)

### FoodiePass Documentation
- [Project Context](./.claude/CLAUDE.md)
- [Development Principles](./.claude/PRINCIPLES.md)
- [Quality Standards](./.claude/quality_standards.md)
- [Commit Templates](./.claude/commit_templates.md)

### BPlusTree3 Methodology
- Trade-off Analysis Pattern
- Decision Framework

---

**Last Updated**: 2025-11-11
**Maintained By**: FoodiePass Development Team
