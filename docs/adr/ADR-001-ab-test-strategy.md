# ADR-001: A/B Test Strategy for MVP Hypothesis Validation

**Date**: 2025-11-11
**Status**: Accepted
**Author**: FoodiePass Team
**Deciders**: Product Lead, Tech Lead

## Context

**Problem Statement**: We need to validate hypothesis H1 and H3 by comparing user confidence levels between two different menu presentation approaches:
- **Control Group**: Text-only translated menu (baseline)
- **Treatment Group**: Visual menu with food photos, descriptions, and currency conversion

**Background**:
- FoodiePass MVP aims to validate three core hypotheses (H1, H2, H3)
- H1: Travelers lack confidence with text-only translations but gain confidence with visual menus
- H3: Visual menu users have 2x higher confidence than text-only users
- We have a tight 10-day timeline for MVP development
- Budget constraints favor simple, cost-effective solutions

**Goals**:
- Randomly assign users to Control or Treatment groups
- Collect confidence survey responses from both groups
- Ensure statistically valid comparison (eliminate selection bias)
- Minimize implementation complexity

**Non-Goals**:
- User personalization based on past behavior
- Long-term user tracking across sessions
- Complex feature flags system

## Decision

**What we decided**: Implement **session-based random A/B group assignment** with 50/50 split

**Rationale**:
1. **Statistical Validity**: Random assignment eliminates selection bias, ensuring valid hypothesis testing
2. **Simplicity**: Session-based assignment is simpler to implement than user-based tracking (no login required)
3. **Privacy-Friendly**: No long-term user tracking or cookies, aligns with privacy principles
4. **MVP-Appropriate**: Minimal infrastructure, can be implemented within timeline constraints

**Implementation Summary**:
- Assign A/B group on first menu scan request (`POST /api/menus/scan`)
- Store group assignment in Redis session (TTL: 1 hour)
- Return group assignment in API response (`abGroup: "CONTROL" | "TREATMENT"`)
- Frontend renders different UI based on group assignment
- Survey responses include `scanId` to link back to group assignment

## Consequences

### Positive Consequences
- Simple implementation with minimal infrastructure
- No user authentication required (reduces scope)
- Privacy-friendly (no persistent tracking)
- Statistically valid comparison for hypothesis testing
- Easy to implement within 10-day timeline

### Negative Consequences
- Same user may see different experiences across sessions (inconsistent UX)
- Cannot track long-term user behavior or preferences
- Session loss (timeout/browser close) resets group assignment
- Requires Redis for session management

### Risks
- **Session timeout before survey completion**: User assigned to group but doesn't complete survey
  - **Mitigation**: Set generous session TTL (1 hour), track completion rate
- **Unequal group sizes**: Random assignment may not yield exactly 50/50 split with small sample
  - **Mitigation**: Monitor group sizes, aim for minimum 100 users per group
- **Data quality**: Users may scan multiple menus, creating correlated data points
  - **Mitigation**: Analyze at scan level (not user level), document in analysis

## Alternatives Considered

### Alternative 1: User-Based Assignment (Persistent)
**Description**: Assign A/B group to user ID, persist in database

**Pros**:
- Consistent experience for returning users
- Can track long-term behavior
- Better UX (no group switching)

**Cons**:
- Requires user authentication (login system)
- Adds significant scope to MVP (out of 10-day timeline)
- More infrastructure complexity

**Why rejected**: MVP does not include authentication system, violates scope discipline

### Alternative 2: Cookie-Based Assignment
**Description**: Store group assignment in browser cookie

**Pros**:
- Persistent within browser (consistent experience)
- No backend session management
- Simple to implement

**Cons**:
- Users can clear cookies (loses assignment)
- Privacy concerns (tracking cookies)
- Difficult to enforce on mobile web
- Cookie consent requirements (GDPR)

**Why rejected**: Privacy concerns and cookie consent complexity outweigh benefits for MVP

### Alternative 3: No A/B Test (All Treatment)
**Description**: Show visual menu to all users, compare to external baseline

**Pros**:
- Simplest implementation
- No group assignment logic
- All users get best experience

**Cons**:
- Cannot validate H3 (relative comparison)
- No control for confounding variables
- Hypothesis testing is invalid without control group
- Cannot measure confidence increase

**Why rejected**: Eliminates ability to validate H1 and H3, defeats MVP purpose

## Trade-offs

**Simplicity vs Consistency**:
- **Decision**: Prioritized simplicity (session-based) over consistency (user-based)
- **Rationale**: MVP timeline is tight (10 days), consistency is not critical for hypothesis validation
- **Impact**: Users may see different experiences across sessions, but this doesn't affect statistical validity of single-session confidence measurement

**Privacy vs Personalization**:
- **Decision**: Prioritized privacy (no long-term tracking) over personalization
- **Rationale**: MVP focuses on hypothesis validation, not personalized experiences; privacy-friendly approach reduces regulatory concerns
- **Impact**: Cannot offer personalized recommendations, but aligns with MVP scope discipline

**Infrastructure Complexity vs Feature Richness**:
- **Decision**: Minimal infrastructure (Redis only) vs complex feature flags system
- **Rationale**: Redis already required for session management, adding A/B logic is incremental; full feature flags system is over-engineering for MVP
- **Impact**: Cannot easily add multiple experiments or gradual rollouts, but sufficient for single A/B test

## Hypothesis Validation

**Related Hypotheses**: H1 (core value), H3 (user behavior)

**How this decision supports hypothesis validation**:
- **H1 Validation**: By comparing Control (text-only) vs Treatment (visual menu), we directly test whether visual elements increase confidence
- **H3 Validation**: Confidence survey responses from both groups enable quantitative comparison (confidence rate ratio)
- **Statistical Rigor**: Random assignment ensures unbiased comparison, making results credible

**Impact on success criteria**:
- **Directly Enables**: Treatment group Yes rate ≥ 70% (need Treatment group to measure)
- **Directly Enables**: Treatment/Control ratio ≥ 2.0 (need both groups to compare)
- **No Impact**: OCR accuracy, currency accuracy, food matching (H2 technical criteria)

## Implementation Plan

**Timeline**: Day 3-4 of MVP development (Phase 2)

**Dependencies**:
- Redis setup (session management)
- `MenuScan` entity (database model)
- `ABTestService` (group assignment logic)
- Frontend routing logic (Control vs Treatment UI)

**Key Tasks**:
1. **Backend** (Day 3):
   - Create `ABGroup` enum (CONTROL, TREATMENT)
   - Implement `ABTestService.assignGroup()` with random 50/50 logic
   - Add `abGroup` field to `MenuScan` entity
   - Store group assignment in Redis session
   - Return `abGroup` in `/api/menus/scan` response
2. **Frontend** (Day 4):
   - Parse `abGroup` from API response
   - Route to Control UI (`/menu?view=control`) or Treatment UI (`/menu?view=treatment`)
   - Render appropriate components
3. **Testing** (Day 4):
   - Unit test: Random assignment produces ~50/50 split
   - Integration test: Session persists group assignment
   - E2E test: Both UIs render correctly based on group

**Validation**:
- Monitor group sizes in admin dashboard (should be ~50/50)
- Check Redis session TTL (should be 1 hour)
- Verify survey responses link to correct group via `scanId`

## References

- [FoodiePass 1-Pager](../1-PAGER.md) - Core hypotheses H1, H3
- [PRD](../PRD.md) - A/B test requirements (F-08, F-09)
- [Backend Architecture](../../backend/docs/ARCHITECTURE.md) - ABTestService design
- [A/B Testing Best Practices](https://www.optimizely.com/optimization-glossary/ab-testing/) - External reference

## Notes

**Session TTL Rationale**: 1 hour chosen based on expected user flow:
1. Upload menu (1 min)
2. View translated menu (3-5 min)
3. Complete survey (1 min)
4. Total: ~5-7 min, with generous buffer

**Group Size Target**: Aim for minimum 100 users per group for statistical significance (p < 0.05, power = 0.8, assuming large effect size)

**Future Considerations**:
- If MVP succeeds, Phase 2 may add user authentication and persistent group assignment
- Could implement more sophisticated A/B testing framework (e.g., LaunchDarkly) for multi-experiment scenarios
- Consider multivariate testing if we want to test individual components (photos vs descriptions vs currency)

---

## Review History

| Date | Reviewer | Comments | Outcome |
|------|----------|----------|---------|
| 2025-11-11 | Product Lead | Approved session-based approach for MVP | Approved |
| 2025-11-11 | Tech Lead | Confirmed Redis capacity and TTL settings | Approved |

---

## Changelog

| Date | Change | Reason |
|------|--------|--------|
| 2025-11-11 | Created | Initial ADR for A/B test strategy |
