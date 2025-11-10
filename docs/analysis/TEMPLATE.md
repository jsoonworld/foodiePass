# Trade-off Analysis: [TOPIC]

**Date**: YYYY-MM-DD
**Author**: [Name]
**Status**: [Draft | Under Review | Finalized | Superseded]
**Related ADR**: [Link to ADR if applicable]

---

## Executive Summary

**Decision to Make**: [One-sentence statement of what needs to be decided]

**Recommendation**: [Clear, concise recommendation - which option to choose]

**Key Trade-off**: [The primary trade-off dimension, e.g., "Speed vs Quality", "Cost vs Capability"]

**Rationale**: [One-paragraph summary of why this recommendation makes sense]

---

## Problem Statement

**Context**: [What situation are we in? What constraints exist?]

**Why This Matters**: [Why is this decision important? What's the impact if we get it wrong?]

**Goals**: [What are we trying to achieve with this decision?]

**Non-Goals**: [What are we explicitly NOT trying to solve?]

**Constraints**:
- [Constraint 1: e.g., Budget, Timeline, Technical]
- [Constraint 2]
- [Constraint 3]

---

## Options Under Consideration

### Option 1: [Name]
**Brief Description**: [1-2 sentence overview]

### Option 2: [Name]
**Brief Description**: [1-2 sentence overview]

### Option 3: [Name]
**Brief Description**: [1-2 sentence overview]

---

## Evaluation Criteria

**Criteria Selection Rationale**: [Why are these criteria important for this decision?]

| Criterion | Weight | Description |
|-----------|--------|-------------|
| [Criterion 1] | High/Medium/Low | [What we're measuring] |
| [Criterion 2] | High/Medium/Low | [What we're measuring] |
| [Criterion 3] | High/Medium/Low | [What we're measuring] |
| [Criterion 4] | High/Medium/Low | [What we're measuring] |
| [Criterion 5] | High/Medium/Low | [What we're measuring] |

**Example Criteria**:
- Implementation Time (how long to build)
- Maintenance Burden (ongoing cost)
- Performance (speed, scalability)
- Flexibility (future extensibility)
- Cost (financial expense)
- Complexity (cognitive load)
- Risk (probability of failure)

---

## Trade-off Matrix

| Criterion | Option 1 | Option 2 | Option 3 |
|-----------|----------|----------|----------|
| [Criterion 1] | ⭐⭐⭐ | ⭐⭐ | ⭐ |
| [Criterion 2] | ⭐ | ⭐⭐⭐ | ⭐⭐ |
| [Criterion 3] | ⭐⭐ | ⭐ | ⭐⭐⭐ |
| [Criterion 4] | ⭐⭐⭐ | ⭐⭐ | ⭐ |
| [Criterion 5] | ⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| **Total Score** | X/15 | Y/15 | Z/15 |

**Rating Scale**:
- ⭐⭐⭐ Excellent (3 points)
- ⭐⭐ Good (2 points)
- ⭐ Acceptable (1 point)
- ❌ Poor (0 points)

**Notes**:
- [Any important clarifications about the ratings]
- [Context that affects the comparison]

---

## Detailed Analysis

### Option 1: [Name]

**Strengths**:
- **[Strength 1]**: [Why this is advantageous]
- **[Strength 2]**: [Why this is advantageous]
- **[Strength 3]**: [Why this is advantageous]

**Weaknesses**:
- **[Weakness 1]**: [Why this is problematic]
- **[Weakness 2]**: [Why this is problematic]

**Risks**:
- **[Risk 1]**: [Potential failure mode] - **Likelihood**: High/Medium/Low - **Impact**: High/Medium/Low
- **[Risk 2]**: [Potential failure mode] - **Likelihood**: High/Medium/Low - **Impact**: High/Medium/Low

**Best For**: [What scenarios or contexts favor this option]

---

### Option 2: [Name]

[Same structure as Option 1]

---

### Option 3: [Name]

[Same structure as Option 1]

---

## Trade-off Dimensions

### [Trade-off 1]: [Dimension A] vs [Dimension B]

**Explanation**: [What's the tension between these two dimensions?]

**How Options Compare**:
- **Option 1**: [High Dimension A, Low Dimension B]
- **Option 2**: [Balanced]
- **Option 3**: [Low Dimension A, High Dimension B]

**What This Means**:
- [Practical implications of this trade-off]
- [How to choose based on priorities]

---

### [Trade-off 2]: [Dimension C] vs [Dimension D]

[Same structure as Trade-off 1]

---

## Recommendation

**Chosen Option**: [Option Name]

**Rationale**:
1. **[Reason 1]**: [Why this is the right choice given our context]
2. **[Reason 2]**: [How this aligns with our goals]
3. **[Reason 3]**: [Why the trade-offs are acceptable]

**When This Recommendation Changes**:
- [Condition 1: If this changes, reconsider]
- [Condition 2: If this assumption proves false, revisit]

---

## Decision Framework

**Decision Criteria Prioritization**:
1. [Most Important Criterion] - **Why**: [Justification]
2. [Second Most Important] - **Why**: [Justification]
3. [Third Most Important] - **Why**: [Justification]

**Scenario Analysis**:

| Scenario | Best Option | Reason |
|----------|-------------|--------|
| [Scenario A: e.g., Tight timeline] | Option X | [Why this is best] |
| [Scenario B: e.g., Unlimited budget] | Option Y | [Why this is best] |
| [Scenario C: e.g., High uncertainty] | Option Z | [Why this is best] |

---

## Implementation Implications

**If we choose [Recommended Option]**:

**Timeline**: [How long will implementation take?]

**Resources Required**:
- [Resource 1: e.g., Engineering time, Budget, Infrastructure]
- [Resource 2]
- [Resource 3]

**Dependencies**:
- [Dependency 1: What needs to be in place first]
- [Dependency 2]

**Key Tasks**:
1. [Task 1]
2. [Task 2]
3. [Task 3]

**Success Metrics**: [How will we know this decision was right?]
- [Metric 1: e.g., Performance improvement]
- [Metric 2: e.g., Cost reduction]
- [Metric 3: e.g., User satisfaction]

---

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation Strategy |
|------|------------|--------|---------------------|
| [Risk 1] | High/Medium/Low | High/Medium/Low | [How to reduce or handle] |
| [Risk 2] | High/Medium/Low | High/Medium/Low | [How to reduce or handle] |
| [Risk 3] | High/Medium/Low | High/Medium/Low | [How to reduce or handle] |

---

## Hypothesis Validation Context (FoodiePass Specific)

> **Note**: Include this section only for decisions related to FoodiePass MVP hypothesis validation. Remove for other projects.

**Related Hypotheses**: [H1, H2, H3, or combination]

**How This Decision Impacts Validation**:
- **[Hypothesis X]**: [How this option helps or hinders validation]
- **[Hypothesis Y]**: [Impact on testing or measurement]

**Success Criteria Impact**:
- [How this affects our ability to meet success criteria]
- [Specific metrics affected]

**MVP Scope Considerations**:
- [Does this add/remove features from MVP scope?]
- [Justification for any scope changes]

---

## Alternatives Not Considered

**Why these weren't evaluated**:
- **[Alternative A]**: [Reason for exclusion]
- **[Alternative B]**: [Reason for exclusion]

---

## References

- [Link to related documentation]
- [Link to external resources]
- [Link to similar analyses]
- [Link to prior art]

---

## Stakeholder Feedback

| Stakeholder | Feedback Summary | Incorporated? |
|-------------|------------------|---------------|
| [Name/Role] | [Key points] | Yes/No - [Why] |
| [Name/Role] | [Key points] | Yes/No - [Why] |

---

## Appendix

### Assumptions
- [Assumption 1: Critical assumptions made in this analysis]
- [Assumption 2]
- [Assumption 3]

### Out of Scope
- [Item 1: What was explicitly not analyzed]
- [Item 2]

### Future Considerations
- [Consideration 1: What to revisit later]
- [Consideration 2]

---

## Changelog

| Date | Change | Author | Reason |
|------|--------|--------|--------|
| YYYY-MM-DD | Created | [Name] | Initial analysis |
| YYYY-MM-DD | Updated | [Name] | [What changed and why] |

---

**Last Updated**: YYYY-MM-DD
**Next Review**: YYYY-MM-DD (if periodic review is needed)
