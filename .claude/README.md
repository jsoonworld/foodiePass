# Claude Code Context Guide

This directory (`.claude/`) contains documentation that provides context to Claude Code (AI coding assistant) for working on the FoodiePass project.

**Purpose**: Enable Claude Code to:
- Understand project context, goals, and constraints
- Follow established development principles and standards
- Make decisions aligned with project methodology
- Maintain consistency across sessions

## Directory Structure

```
.claude/
├── README.md                    # This file - context guide
├── CLAUDE.md                    # Project overview and quick reference
├── PRINCIPLES.md                # Development principles (BPlusTree3)
├── quality_standards.md         # Code quality standards
├── commit_templates.md          # Git commit guidelines
├── PHASE1_IMPLEMENTATION.md     # Current phase checklist
└── SESSION_*_ANALYSIS.md        # Session work logs
```

## Document Hierarchy

When documents conflict, follow this priority order:

1. **CLAUDE.md** (Highest Priority)
   - Project-specific context and constraints
   - Current phase and focus area
   - DO/DO NOT rules for this project

2. **PRINCIPLES.md**
   - General development principles
   - Methodology and workflow patterns
   - Universal best practices

3. **quality_standards.md**
   - Specific quality criteria and checklists
   - Technical standards and thresholds

4. **commit_templates.md**
   - Git commit message formats
   - Version control workflow

5. **PHASE1_IMPLEMENTATION.md** (Context-Specific)
   - Current phase tasks and checklist
   - Time-bound session plans

6. **SESSION_*_ANALYSIS.md** (Historical)
   - Past work logs and decisions
   - Reference only (not binding)

**Conflict Resolution**: If CLAUDE.md says "no authentication for MVP" but PRINCIPLES.md mentions "authentication best practices", follow CLAUDE.md (project-specific constraint overrides general principle).

---

## Document Descriptions

### CLAUDE.md (Project Context)

**Purpose**: Single source of truth for FoodiePass project context

**When to Reference**:
- ✅ Starting new work session
- ✅ Making architectural decisions
- ✅ Evaluating feature scope
- ✅ Understanding project constraints

**Key Sections**:
- **Project Status**: Current branch, phase, focus area
- **Project Vision**: Core problem, solution, MVP goal
- **Core Hypotheses**: H1, H2, H3 (hypothesis validation focus)
- **Success Criteria**: Quantitative goals to achieve
- **MVP Scope**: What's in/out of scope
- **Tech Stack**: Technologies and external APIs
- **Working Principles**: Project-specific guidelines
- **DO/DO NOT**: Explicit rules for this project

**Usage Pattern**:
```
User: "Let's add a user profile feature"
Claude: [Checks CLAUDE.md]
        → "❌ Out of MVP scope: 로그인/회원 시스템"
        → "This feature is explicitly excluded as it doesn't validate H1, H2, H3"
```

---

### PRINCIPLES.md (Development Methodology)

**Purpose**: BPlusTree3-inspired development principles applicable across all work

**When to Reference**:
- ✅ Writing code (TDD, small commits, refactoring)
- ✅ Making technical decisions
- ✅ Planning work breakdown
- ✅ Reviewing code quality

**Key Sections**:
1. **Philosophy** (Tidy First, Progressive Enhancement)
2. **Development Cycle** (TDD cycle, incremental commits)
3. **Code Organization** (naming, structure, patterns)
4. **Refactoring Guidelines** (when and how to refactor)
5. **Test Strategy** (test types, coverage goals)
6. **Error Handling** (exception patterns, logging)
7. **Performance Principles** (measurement, optimization)
8. **Git Workflow** (branching, commits, reviews)
9. **Documentation Standards** (what to document, when)
10. **Quality Mindset** (code review, fail fast, continuous improvement)

**BPlusTree3 Integration**:
- **Tidy First**: Separate structural changes (refactoring) from behavioral changes (features)
- **Small Commits**: 50-200 LOC ideal, >500 LOC requires split
- **Fail Fast**: Validate early, test before commit, pre-commit checks
- **Progressive Enhancement**: Build incrementally, ship working code frequently

**Usage Pattern**:
```
User: "Let's refactor this and add a new feature"
Claude: [Checks PRINCIPLES.md → "Tidy First"]
        → "Let's separate this: First commit (refactor only), Second commit (new feature)"
```

---

### quality_standards.md (Quality Criteria)

**Purpose**: Specific, measurable quality standards and checklists

**When to Reference**:
- ✅ Before committing code (pre-commit checklist)
- ✅ During code review
- ✅ Writing tests
- ✅ Handling errors or edge cases

**Key Sections**:
1. **Code Quality Standards**
   - Test coverage targets (>80%)
   - Code complexity limits
   - Naming conventions
2. **Pre-Commit Quality Gates**
   - Code compiles
   - Tests pass
   - No debug code
   - Formatted correctly
3. **Code Review Checklist**
   - Readability checks
   - Test quality checks
   - Error handling checks
4. **Domain-Specific Standards**
   - Exception handling patterns
   - Optional usage guidelines
   - Logging standards
   - Performance validation

**Usage Pattern**:
```
Claude: [Before commit, checks quality_standards.md]
        → Run tests (./gradlew test)
        → Check coverage (>80%)
        → Verify no System.out.println()
        → Run spotlessCheck
        → Only then: git commit
```

---

### commit_templates.md (Git Standards)

**Purpose**: Conventional Commits templates and commit workflow

**When to Reference**:
- ✅ Writing commit messages
- ✅ Deciding commit scope and type
- ✅ Splitting large changes into commits

**Key Sections**:
1. **Conventional Commits Standard** (format rules)
2. **Commit Types** (feat, fix, refactor, test, docs, chore)
3. **Structural vs Behavioral Separation** (Kent Beck's "Tidy First")
4. **Commit Message Templates** (by type)
5. **BPlusTree3 Integration** (pre-commit checklist)
6. **Commit Size Guidelines** (50-200 LOC ideal)
7. **Real Examples** (from FoodiePass history)

**Commit Format**:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Usage Pattern**:
```
Claude: [After completing feature]
        → [Checks commit_templates.md]
        → Type: "feat" (new A/B test feature)
        → Scope: "(abtest)"
        → Subject: "Enable random A/B group assignment"
        → Body: "Implements H1, H3 validation..."
        → Pre-commit checks → Commit
```

---

### PHASE1_IMPLEMENTATION.md (Current Phase)

**Purpose**: Track current phase progress and session plans

**When to Reference**:
- ✅ Starting work session (check current session)
- ✅ Planning next tasks
- ✅ Updating progress

**Key Sections**:
- **Overview**: Phase goal and expected output
- **Session Breakdown**: 5 sessions with detailed tasks
- **Progress Tracking**: Session status and time tracking
- **Completion Criteria**: Phase completion checklist

**Current Phase**: Phase 1 - BPlusTree3 Methodology Adoption
- **Goal**: Establish constitutional documents (principles, standards, frameworks)
- **Duration**: 2-3 hours
- **Sessions**: 1-1 through 1-5

**Usage Pattern**:
```
User: "What should we work on next?"
Claude: [Checks PHASE1_IMPLEMENTATION.md]
        → "Next session: Session 1-5 (Directory structure and templates)"
        → [Creates TodoWrite with session tasks]
```

---

### SESSION_*_ANALYSIS.md (Work Logs)

**Purpose**: Historical record of work completed in each session

**When to Reference**:
- ✅ Understanding past decisions
- ✅ Reviewing what was already done
- ✅ Avoiding duplicate work

**Contents**:
- Analysis performed
- Decisions made
- Files created/modified
- Insights gained

**Usage Pattern**:
```
User: "Have we already analyzed BPlusTree3?"
Claude: [Checks SESSION_1-1_ANALYSIS.md]
        → "Yes, analyzed in Session 1-1"
        → [Provides summary of findings]
```

---

## Using This Context Effectively

### On Session Start

1. **Read CLAUDE.md** (project context)
   - What phase are we in?
   - What's the current focus?
   - What are the DO/DO NOT rules?

2. **Check PHASE1_IMPLEMENTATION.md** (current work)
   - What session are we on?
   - What tasks are pending?
   - What's been completed?

3. **Review PRINCIPLES.md** (how to work)
   - What methodology should I follow?
   - What workflow patterns apply?

### During Development

1. **Follow PRINCIPLES.md** for coding practices
   - TDD cycle
   - Small commits
   - Refactoring patterns

2. **Apply quality_standards.md** for quality checks
   - Pre-commit checklist
   - Error handling patterns
   - Test coverage

3. **Use commit_templates.md** for version control
   - Commit message format
   - Commit size guidelines
   - Pre-commit validation

### Before Commit

1. **Run Pre-Commit Checklist** (quality_standards.md)
   - Tests pass
   - Coverage >80%
   - No debug code
   - Formatted correctly

2. **Write Commit Message** (commit_templates.md)
   - Choose correct type (feat, fix, refactor, etc.)
   - Add scope
   - Reference hypothesis if applicable

3. **Verify MVP Scope** (CLAUDE.md)
   - Does this align with H1, H2, H3?
   - Is this in MVP scope?

---

## BPlusTree3 Methodology Integration

FoodiePass adopts BPlusTree3 development methodology, which emphasizes:

### Core Principles

1. **Tidy First (Kent Beck)**
   - Separate structural changes (refactoring) from behavioral changes (features)
   - Make code easy to change before changing it
   - Commit structural changes separately

2. **Small Commits**
   - Ideal: 50-200 LOC per commit
   - Max: 500 LOC (split if larger)
   - Each commit should be independently reviewable

3. **Progressive Enhancement**
   - Build incrementally
   - Ship working code frequently
   - Validate early and often

4. **Fail Fast**
   - Validate at commit time (pre-commit hooks)
   - Test before commit, not after
   - Catch issues early when cheap to fix

5. **Test-Driven Development (TDD)**
   - Red → Green → Refactor cycle
   - Tests as design tool
   - >80% coverage target

### Document Mapping

| BPlusTree3 Concept | FoodiePass Document |
|-------------------|---------------------|
| Tidy First | PRINCIPLES.md (Section 3) |
| Small Commits | commit_templates.md (Section 6) |
| Quality Gates | quality_standards.md (Section 2) |
| TDD Cycle | PRINCIPLES.md (Section 2) |
| Refactoring Guidelines | PRINCIPLES.md (Section 4) |
| Code Review Standards | quality_standards.md (Section 3) |

### Workflow Example

**Scenario**: Add A/B test feature to validate H3

```
1. Plan (CLAUDE.md)
   → Check: Is this in MVP scope? Yes (H3 validation)
   → Check: What hypothesis does this validate? H3

2. Design (PRINCIPLES.md)
   → Use TDD: Write tests first
   → Plan small commits:
     - Commit 1: Create ABGroup enum (structural)
     - Commit 2: Implement ABTestService (behavioral)
     - Commit 3: Integrate with MenuController (behavioral)

3. Implement (PRINCIPLES.md + quality_standards.md)
   → TDD cycle: Red → Green → Refactor
   → Follow naming conventions
   → Handle errors properly
   → Log appropriately

4. Pre-Commit (quality_standards.md)
   → Run ./gradlew test
   → Check coverage (>80%)
   → Verify no System.out.println()
   → Run spotlessCheck

5. Commit (commit_templates.md)
   → Type: feat
   → Scope: (abtest)
   → Subject: "Enable random A/B group assignment"
   → Body: "Implements H3 validation by randomly assigning users..."

6. Review (quality_standards.md)
   → Code review checklist
   → Verify tests cover edge cases
   → Check error handling
```

---

## Common Scenarios

### Scenario 1: User Requests Out-of-Scope Feature

```
User: "Let's add a user profile page"
Claude: [Checks CLAUDE.md → MVP Scope]
        → "❌ Out of Scope: 로그인/회원 시스템"
        → "This doesn't validate H1, H2, H3"
        → "Should we focus on in-scope features instead?"
```

### Scenario 2: Large Refactoring Needed

```
User: "Refactor MenuService and add new feature"
Claude: [Checks PRINCIPLES.md → Tidy First]
        → "Let's separate this:"
        → "Commit 1: Refactor MenuService (structural)"
        → "Commit 2: Add new feature (behavioral)"
        → "This follows Kent Beck's Tidy First principle"
```

### Scenario 3: Code Quality Issue

```
Claude: [Implements feature]
        [Checks quality_standards.md → Pre-Commit Checklist]
        → Run tests: ✅ Pass
        → Check coverage: ❌ 75% (need >80%)
        → "Need to add more tests before committing"
        → [Writes additional tests]
        → Re-check: ✅ 82%
        → [Proceeds with commit]
```

### Scenario 4: Commit Message Formatting

```
Claude: [Completes feature]
        [Checks commit_templates.md]
        → Type: feat (new functionality)
        → Scope: (abtest)
        → Subject: "Enable random A/B group assignment"
        → Body: "Implements H1, H3 validation..."
        → Footer: "Related to ADR-001"
        → [Commits with proper format]
```

---

## Maintaining These Documents

### When to Update

**CLAUDE.md**: Update when:
- Project phase changes
- MVP scope changes
- Key constraints change

**PRINCIPLES.md**: Update when:
- Adopting new methodology
- Establishing new patterns
- Refining development workflow

**quality_standards.md**: Update when:
- Quality thresholds change
- New quality checks added
- Tooling updates (linters, formatters)

**commit_templates.md**: Update when:
- Commit conventions change
- New commit types needed
- Workflow adjustments

**PHASE1_IMPLEMENTATION.md**: Update after:
- Each session completion
- Phase milestone reached
- Timeline adjustments

### How to Update

1. **Create feature branch**
   ```bash
   git checkout -b docs/update-[document-name]
   ```

2. **Edit document**
   - Make focused changes
   - Update "Last Updated" date
   - Document reason for change

3. **Create PR**
   ```bash
   git add .claude/[document-name].md
   git commit -m "docs(claude): Update [document-name] - [reason]"
   git push origin docs/update-[document-name]
   ```

4. **Review and merge**
   - Get team review
   - Merge to main branch
   - Delete feature branch

---

## Related Documentation

### Project Documentation
- [Project 1-Pager](../docs/1-PAGER.md) - Vision and hypotheses
- [PRD](../docs/PRD.md) - Requirements specification
- [ADR Directory](../docs/adr/) - Architecture decisions
- [Analysis Directory](../docs/analysis/) - Trade-off analyses

### Backend Documentation
- [Backend CLAUDE.md](../backend/.claude/CLAUDE.md) - Backend-specific context
- [Backend Architecture](../backend/docs/ARCHITECTURE.md)
- [API Specification](../backend/docs/API_SPEC.md)

### Frontend Documentation
- [Frontend CLAUDE.md](../frontend/.claude/CLAUDE.md) - Frontend-specific context
- [Component Design](../frontend/docs/COMPONENT_DESIGN.md)
- [User Flow](../frontend/docs/USER_FLOW.md)

---

## Best Practices for Claude Code

### DO
- ✅ Read CLAUDE.md at session start
- ✅ Check PHASE1_IMPLEMENTATION.md for current tasks
- ✅ Follow PRINCIPLES.md methodology
- ✅ Apply quality_standards.md before commit
- ✅ Use commit_templates.md for all commits
- ✅ Ask user if context conflicts
- ✅ Reference specific sections when explaining decisions

### DO NOT
- ❌ Ignore project-specific constraints (CLAUDE.md)
- ❌ Skip pre-commit quality checks
- ❌ Commit without proper message format
- ❌ Add out-of-scope features without user confirmation
- ❌ Violate BPlusTree3 principles (Tidy First, Small Commits)
- ❌ Assume context from previous sessions (re-read docs)

---

## Questions?

If context is unclear or documents conflict:
1. **Ask the user** for clarification
2. **Refer to document hierarchy** (CLAUDE.md > PRINCIPLES.md > others)
3. **Document the decision** (create ADR or update appropriate document)

**Feedback**: If these documents are unclear or incomplete, suggest improvements to the user.

---

**Last Updated**: 2025-11-11
**Maintained By**: FoodiePass Development Team
**Methodology**: BPlusTree3-inspired development principles
