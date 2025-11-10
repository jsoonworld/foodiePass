# Analysis Documentation

This directory contains trade-off analyses and technical evaluations that inform architectural decisions.

## What are Trade-off Analyses?

Trade-off analyses are structured documents that compare multiple options for a decision, evaluating each option across relevant criteria to recommend the best choice for our context.

**Purpose**:
- Systematically evaluate options before making decisions
- Document the reasoning process transparently
- Enable informed discussions among stakeholders
- Provide context for future decisions

**Key Principle**: Trade-off analyses are **decision-support tools**, not decisions themselves. The final decision should be recorded in an ADR (Architecture Decision Record).

## Relationship to ADRs

**Trade-off Analysis → ADR Workflow**:

1. **Analysis Phase**: Create a trade-off analysis to evaluate options
   - Compare alternatives systematically
   - Document pros/cons and trade-offs
   - Recommend a direction
2. **Decision Phase**: Create an ADR to record the final decision
   - Reference the trade-off analysis
   - State the chosen option
   - Document the decision context and rationale
3. **Implementation Phase**: Execute based on the ADR
   - ADR is the source of truth for what was decided
   - Analysis provides the "why" behind the decision

**When to use each**:
- **Trade-off Analysis**: Before making a complex decision (2+ options, multiple criteria)
- **ADR**: After the decision is made (single decision, implementation-focused)

**Example Flow**:
```
Analysis: "OCR API Selection: Google Cloud Vision vs AWS Textract vs Azure Computer Vision"
         ↓ (Compare options, recommend Google Cloud Vision)
ADR: "ADR-002: Use Google Cloud Vision for OCR"
         ↓ (Record decision, document implementation)
Implementation: Integrate Google Cloud Vision API
```

## When to Write a Trade-off Analysis

Create a trade-off analysis when:

### Multiple Viable Options Exist
- ✅ 2 or more realistic alternatives to choose from
- ✅ No obviously "correct" choice
- ✅ Each option has significant pros and cons

### High-Impact Decisions
- ✅ Decision affects core architecture or user experience
- ✅ Difficult or costly to reverse (>1 week effort)
- ✅ Impacts multiple components or systems

### Complex Trade-offs
- ✅ Multiple evaluation criteria (performance, cost, complexity, etc.)
- ✅ Conflicting priorities (speed vs quality, simplicity vs flexibility)
- ✅ Uncertain outcomes (need to weigh risks)

### Stakeholder Alignment Needed
- ✅ Decision involves multiple stakeholders
- ✅ Requires buy-in from team or leadership
- ✅ Potential for disagreement or debate

### When NOT to Write a Trade-off Analysis
- ❌ Only one viable option (just use ADR)
- ❌ Trivial decision (easily reversible, low impact)
- ❌ Decision is already obvious (follow established patterns)
- ❌ Time-critical emergency (document decision in ADR afterward)

**Rule of Thumb**: If you're spending >2 hours debating a decision, write a trade-off analysis.

## How to Write a Trade-off Analysis

### Step 1: Copy the Template

```bash
cp docs/analysis/TEMPLATE.md docs/analysis/[topic-name]-tradeoff-analysis.md
```

### Step 2: Define the Problem

- **Problem Statement**: What decision needs to be made?
- **Context**: Why is this decision necessary?
- **Goals**: What are we trying to achieve?
- **Constraints**: What limits our options?

### Step 3: Identify Options

List 2-5 realistic alternatives. For each:
- Provide a clear name
- Write a brief description (1-2 sentences)

**Example**:
```markdown
### Option 1: Session-Based A/B Assignment
**Description**: Assign users to A/B groups randomly on each session, store in Redis

### Option 2: User-Based A/B Assignment
**Description**: Assign users to A/B groups based on user ID, persist in database
```

### Step 4: Define Evaluation Criteria

Choose 4-7 criteria relevant to this decision. Assign weights (High, Medium, Low).

**Common Criteria**:
- **Implementation Time**: How long to build?
- **Maintenance Burden**: Ongoing effort required?
- **Performance**: Speed, scalability, resource usage
- **Flexibility**: Future extensibility, adaptability
- **Cost**: Financial expense (API costs, infrastructure)
- **Complexity**: Cognitive load, learning curve
- **Risk**: Probability and impact of failure
- **MVP Alignment** (FoodiePass): Does this support hypothesis validation?

**Example**:
| Criterion | Weight | Description |
|-----------|--------|-------------|
| Implementation Time | High | Must fit 10-day MVP timeline |
| MVP Alignment | High | Supports H1, H3 validation |
| Maintenance Burden | Medium | Ongoing ops effort |
| Flexibility | Low | Future extensibility needs |

### Step 5: Create Trade-off Matrix

Rate each option against each criterion using a simple scale:
- ⭐⭐⭐ Excellent (3 points)
- ⭐⭐ Good (2 points)
- ⭐ Acceptable (1 point)
- ❌ Poor (0 points)

**Example**:
| Criterion | Option 1 | Option 2 | Option 3 |
|-----------|----------|----------|----------|
| Implementation Time | ⭐⭐⭐ | ⭐ | ⭐⭐ |
| Performance | ⭐⭐ | ⭐⭐⭐ | ⭐ |
| Cost | ⭐⭐⭐ | ⭐⭐ | ⭐ |

### Step 6: Detailed Analysis

For each option, document:
- **Strengths**: Why this option is good
- **Weaknesses**: Why this option is problematic
- **Risks**: What could go wrong (likelihood + impact)
- **Best For**: What scenarios favor this option

### Step 7: Identify Trade-off Dimensions

Name the key trade-offs explicitly:

**Format**:
```markdown
### Performance vs Simplicity
**Explanation**: High-performance solutions often require complex code

**How Options Compare**:
- **Option 1**: Simple, moderate performance
- **Option 2**: Complex, high performance
- **Option 3**: Very simple, low performance

**What This Means**: For MVP, we prioritize simplicity over performance
```

### Step 8: Make a Recommendation

State clearly which option you recommend and why.

**Structure**:
```markdown
**Chosen Option**: Option 1 (Session-Based A/B Assignment)

**Rationale**:
1. Fits 10-day MVP timeline (High weight criterion)
2. Supports H1, H3 validation (High weight criterion)
3. Acceptable trade-off: simple implementation vs persistent assignment
```

### Step 9: Document Implementation Implications

- Timeline
- Resources required
- Dependencies
- Key tasks
- Success metrics

### Step 10: Review and Finalize

1. Share with stakeholders
2. Gather feedback
3. Update analysis
4. Finalize recommendation
5. Create corresponding ADR

## Naming Convention

Format: `[topic-name]-tradeoff-analysis.md`

**Examples**:
- `ocr-api-selection-tradeoff-analysis.md`
- `database-choice-mvp-tradeoff-analysis.md`
- `session-management-strategy-tradeoff-analysis.md`

**Rules**:
- Use kebab-case (lowercase, hyphen-separated)
- End with `-tradeoff-analysis.md`
- Keep titles short but descriptive (max 5-6 words)
- Focus on the decision topic, not the outcome

## Analysis Lifecycle

### 1. Creation (Status: Draft)

```bash
# Create new analysis
cp docs/analysis/TEMPLATE.md docs/analysis/[topic]-tradeoff-analysis.md

# Edit the analysis
# Fill out all sections

# Commit and push
git add docs/analysis/[topic]-tradeoff-analysis.md
git commit -m "docs(analysis): Add trade-off analysis for [topic]"
git push origin feature/analysis-[topic]
```

### 2. Review Process

- Create PR with title: `[Analysis] Trade-off Analysis: [Topic]`
- Add reviewers: decision-makers, affected stakeholders
- Discuss options in PR comments
- Update analysis based on feedback
- Mark as "Under Review"

### 3. Finalization (Status: Finalized)

- Merge PR when consensus is reached
- Update status to "Finalized"
- Create corresponding ADR to record decision

### 4. Decision Recording

After analysis is finalized:
1. Create ADR referencing the analysis
2. Record the chosen option in the ADR
3. Link ADR back to analysis document

### 5. Superseding (Status: Superseded)

If context changes significantly:
- Mark original analysis as "Superseded"
- Create new analysis with updated context
- Document what changed and why

## FoodiePass MVP Context

For analyses related to FoodiePass MVP, always include:

### Hypothesis Alignment
- Which hypotheses (H1, H2, H3) does each option impact?
- How does each option support or hinder hypothesis validation?

### MVP Scope Discipline
- Does any option add features outside MVP scope?
- What's the justification for scope changes?

### Success Criteria Impact
- How does each option affect our ability to meet success criteria?
  - OCR accuracy ≥ 90%
  - Currency accuracy ≥ 95%
  - Food matching relevance ≥ 70%
  - Processing time ≤ 5 seconds
  - Treatment group Yes rate ≥ 70%
  - Treatment/Control ratio ≥ 2.0

### Timeline Implications
- Does any option impact the 10-day MVP timeline?
- What's the implementation time estimate for each?

## Trade-off Analysis Best Practices

### DO
- ✅ Write analysis before making decision (not after)
- ✅ Involve stakeholders early in the process
- ✅ Use objective criteria (measurable when possible)
- ✅ Be honest about weaknesses of recommended option
- ✅ Document assumptions explicitly
- ✅ Keep analysis focused (2-3 pages max)
- ✅ Update analysis if new information emerges

### DO NOT
- ❌ Use analysis to justify predetermined decision (confirmation bias)
- ❌ Cherry-pick criteria to favor one option
- ❌ Skip documenting rejected alternatives
- ❌ Ignore stakeholder feedback
- ❌ Leave analysis unfinished (must reach recommendation)
- ❌ Modify finalized analysis (create new one instead)

## Common Trade-off Dimensions

### Technical Trade-offs
- **Performance vs Simplicity**: Fast but complex vs Slow but simple
- **Flexibility vs Constraints**: Generic vs Specific, Configurable vs Fixed
- **DIY vs Managed**: Build custom vs Use third-party service
- **Monolith vs Microservices**: Simplicity vs Scalability

### Process Trade-offs
- **Speed vs Quality**: Ship fast vs Thorough testing
- **Scope vs Timeline**: Complete features vs MVP discipline
- **Upfront Design vs Iterative**: Plan everything vs Discover as you go

### Business Trade-offs
- **Cost vs Capability**: Cheap but limited vs Expensive but powerful
- **Short-term vs Long-term**: Quick win vs Sustainable solution
- **Risk vs Reward**: Safe bet vs High potential upside

## Examples

### Example 1: Technical Decision
**Topic**: OCR API Selection
**Options**: Google Cloud Vision, AWS Textract, Azure Computer Vision
**Key Trade-off**: Cost vs Accuracy, Ease of Integration vs Features
**Outcome**: Recommend Google Cloud Vision (best balance for MVP)
**ADR**: ADR-002-ocr-api-selection.md

### Example 2: Process Decision
**Topic**: Git Workflow for Monorepo
**Options**: Feature branches, Git worktree, Trunk-based development
**Key Trade-off**: Isolation vs Simplicity, Parallel work vs Merge complexity
**Outcome**: Recommend Git worktree (enables parallel frontend/backend dev)
**ADR**: ADR-003-git-workflow-monorepo.md

## Tools and Automation

### Create Analysis from Template
```bash
# Usage: ./scripts/create-analysis.sh "Your Topic"
# Creates: docs/analysis/your-topic-tradeoff-analysis.md

#!/bin/bash
# (Script to be created in scripts/create-analysis.sh)
```

### Validate Analysis Completeness
```bash
# Check if analysis has all required sections
grep -E "(Problem Statement|Options|Evaluation Criteria|Trade-off Matrix|Recommendation)" [topic]-tradeoff-analysis.md
```

## References

### Trade-off Analysis Best Practices
- [Making Architecture Decisions](https://www.infoq.com/articles/architecture-decision-records/) - InfoQ
- [How to Make Good Decisions](https://fs.blog/mental-models/) - Farnam Street

### FoodiePass Documentation
- [ADR Guide](../adr/README.md) - How to document decisions
- [Project Context](../../.claude/CLAUDE.md) - Project overview
- [Development Principles](../../.claude/PRINCIPLES.md) - Core principles

### BPlusTree3 Methodology
- Trade-off Analysis Pattern
- Decision Framework

---

**Last Updated**: 2025-11-11
**Maintained By**: FoodiePass Development Team
