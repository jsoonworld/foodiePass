# FoodiePass Development Principles

> Core directive for Claude Code when working on FoodiePass

## Project Philosophy

**Mission**: Validate core hypotheses (H1, H2, H3) through rapid MVP development while maintaining production-quality code.

**Values**:
- **Evidence > Assumptions**: All architectural decisions backed by data or clear hypotheses
- **Hypothesis-Driven**: Every feature exists to validate a specific hypothesis
- **MVP Discipline**: Build only what validates hypotheses, ruthlessly cut scope
- **TDD First**: Test → Implement → Refactor cycle for all production code
- **Fast Iteration**: Ship, measure, learn, adapt

## Core Development Principles

### 1. Hypothesis-Driven Development (HDD)

Every feature must directly validate one of our core hypotheses:

**H1 (Core Value)**: "Travelers gain ordering confidence when visual menus (photos/descriptions/currency) are provided vs. text-only translation"

**H2 (Technical Feasibility)**: "We can deliver ≥70% food matching relevance, ≥95% currency accuracy, and ≥90% OCR accuracy"

**H3 (User Behavior)**: "Treatment group (visual menu) has ≥2x confidence rate vs. Control group (text-only)"

**Rule**: Before implementing any feature, ask:
- "Which hypothesis does this validate?"
- "Can we validate H1-H3 without this feature?"
- If answer is unclear → Don't build it (yet)

### 2. Test-Driven Development (TDD)

**Strict TDD Cycle** (Kent Beck):
```
🔴 RED    → Write failing test first (specify expected behavior)
🟢 GREEN  → Minimal code to pass test (simplest implementation)
🔵 REFACTOR → Improve code structure while tests pass
✅ VERIFY → Run full test suite + commit (all green)
```

**TDD Rules**:
- NEVER write production code without a failing test first
- Tests are contracts, not afterthoughts
- Each commit must pass ALL tests (no exceptions)
- Target: >80% code coverage (>90% for domain logic)

**Quality Gates**:
- Unit tests: Service layer business logic
- Integration tests: API endpoints, database operations
- E2E tests: Critical user flows (Control vs Treatment)

**TDD Benefits for Hypothesis Validation**:
- Tests express hypotheses as executable specifications
- Fast feedback loop (15-30 min cycles)
- Regression protection during rapid iteration
- Confidence in refactoring without breaking behavior

### 3. Tidy First - Structural vs Behavioral Separation

**Core Principle** (Kent Beck): NEVER mix structural changes (refactoring) with behavioral changes (new features) in the same commit.

**Structural Changes** (Refactoring):
- Extract method/class
- Rename variables/methods
- Remove duplication
- Improve code organization
- **Commit prefix**: `refactor:`
- **Test requirement**: All existing tests must pass unchanged

**Behavioral Changes** (Features/Fixes):
- Add new functionality
- Fix bugs
- Change business logic
- Modify API contracts
- **Commit prefix**: `feat:`, `fix:`
- **Test requirement**: New/modified tests must be included

**Workflow Pattern**:
```
1. Tidy (refactor) → Commit with "refactor:" → All tests green
2. Add feature → Commit with "feat:" → New tests included
3. Tidy (refactor) → Commit with "refactor:" → All tests green
4. Add feature → Commit with "feat:" → New tests included
```

**Example - Wrong** ❌:
```bash
git commit -m "feat: Add ABTestService and refactor MenuService"
# Mixed structural + behavioral → hard to review, risky rollback
```

**Example - Right** ✅:
```bash
git commit -m "refactor: Extract menu enrichment logic to separate method"
git commit -m "feat: Add ABTestService with group assignment logic"
git commit -m "test: Add ABTestService unit tests"
```

**Benefits**:
- Easy rollback (revert behavioral without losing refactoring)
- Faster code review (structural changes obvious, behavioral reviewed separately)
- Clear intent (what changed vs. why it changed)
- Safer merges (conflicts easier to resolve)

### 4. MVP Scope Discipline

**In Scope** (MVP for Hypothesis Validation):
- ✅ Menu scan with OCR
- ✅ Translation + Currency conversion
- ✅ Food matching (photos/descriptions)
- ✅ A/B test group assignment (Control/Treatment)
- ✅ Confidence survey (Yes/No)
- ✅ Admin analytics dashboard

**Out of Scope** (Post-validation):
- ❌ User authentication / registration
- ❌ Shopping cart / order placement
- ❌ Restaurant reviews / ratings
- ❌ Payment system
- ❌ Order history
- ❌ Social features

**Decision Rule**: "Does removing this feature prevent us from validating H1, H2, or H3?"
- Yes → Keep it
- No → Cut it

### 5. Code Reuse First

**Principle**: Maximize reuse of existing v1 codebase

**Reuse Strategy**:
- ✅ Reuse: `MenuService`, `LanguageService`, `CurrencyService`, OCR pipeline
- ➕ Add: `ABTestService`, `SurveyService`, A/B UI split
- ⚠️ Do Not Modify: Existing domain logic unless breaking bugs found

**Rule**: Before writing new code, search for existing implementation:
```bash
# Search for existing service
rg "class.*Service" backend/src/main/java

# Check existing domain models
ls backend/src/main/java/foodiepass/server/*/domain/
```

### 6. Small, Incremental Commits

**Commit Frequency**: After EACH TDD cycle completion (every 15-30 min)

**Core Principle**: Small commits enable fast rollback, easier code review, and clearer history.

**Commit Size Guidelines**:
- ✅ Ideal: 50-200 LOC per commit
- ⚠️ Warning: 200-500 LOC (consider splitting)
- ❌ Too Large: >500 LOC (must split into multiple commits)

**Commit Requirements** (STRICT):
- [ ] ALL tests pass (`./gradlew test`)
- [ ] NO compiler warnings (`./gradlew build --warning-mode all`)
- [ ] NO dead code (unused imports, commented code)
- [ ] Code formatted (`./gradlew spotlessApply`)
- [ ] Commit message follows convention

**Commit Message Format**:
```
<type>(<scope>): <subject>

Examples:
feat(abtest): Add ABGroup enum for A/B testing
test(survey): Add SurveyService unit tests
refactor(menu): Extract food matching logic
fix(currency): Handle null exchange rates
docs(adr): Add ADR-001 for A/B test strategy
```

**Types**:
- `feat`: New feature for hypothesis validation (behavioral change)
- `test`: Add/modify tests
- `refactor`: Code improvement (structural change, NO behavior change)
- `fix`: Bug fix (behavioral change)
- `docs`: Documentation only
- `chore`: Build, dependencies, tooling

**Example - Right Sized Commits** ✅:
```bash
# 1. Structural change (50 LOC)
git commit -m "refactor: Extract AB test assignment logic to separate method"

# 2. Behavioral change (80 LOC)
git commit -m "feat: Add ABTestService with random group assignment"

# 3. Test (100 LOC)
git commit -m "test: Add ABTestService unit tests with 90% coverage"

# 4. Structural change (30 LOC)
git commit -m "refactor: Rename assignGroup to assignABGroup for clarity"
```

**Example - Wrong** ❌:
```bash
# One giant commit (800 LOC)
git commit -m "feat: Implement entire A/B test system with surveys"
# Hard to review, impossible to rollback partially
```

**Benefits**:
- Fast rollback (revert single feature without affecting others)
- Faster code review (reviewer can focus on small changes)
- Clear history (git log shows progression of thought)
- Easier merge conflict resolution

### 7. Layered Architecture

**Structure** (Clean Architecture):
```
api/          → Controllers (REST endpoints)
application/  → Services (business logic)
domain/       → Entities, Value Objects, Domain logic
repository/   → Data access
dto/          → Request/Response objects
infra/        → External API clients, config
```

**Dependency Rule**: Outer layers depend on inner layers
- ✅ Controller → Service → Domain
- ❌ Domain → Service (violation)

**Single Responsibility**:
- Controllers: HTTP handling, validation, response formatting
- Services: Business logic, transaction management
- Domain: Core business rules, invariants
- Repositories: Data persistence only

### 7. SOLID Principles

**Single Responsibility**: Each class has one reason to change
- ✅ `ABTestService` handles group assignment logic
- ❌ `ABTestService` should NOT handle HTTP responses

**Open/Closed**: Open for extension, closed for modification
- ✅ Add new food scrapers by implementing `FoodScraper` interface
- ❌ Modify existing `MenuService` for every new scraper

**Liskov Substitution**: Derived classes substitutable for base classes
- ✅ Any `FoodScraper` implementation works with `MenuService`

**Interface Segregation**: Many specific interfaces > one general interface
- ✅ `MenuScanRepository`, `SurveyResponseRepository` (specific)
- ❌ `GenericRepository<T>` with unused methods

**Dependency Inversion**: Depend on abstractions, not concretions
- ✅ `MenuService` depends on `FoodScraper` interface
- ❌ `MenuService` depends on `TasteAtlasScraper` concrete class

### 8. Error Handling Strategy

**Domain Validation**: Throw `IllegalArgumentException` at entity creation
```java
if (userId == null || userId.isBlank()) {
    throw new IllegalArgumentException("userId cannot be null or blank");
}
```

**Application Errors**: Throw custom exceptions
```java
throw new MenuNotFoundException("Menu scan not found: " + scanId);
```

**API Layer**: Global exception handler converts to HTTP responses
```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handleValidation(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
}
```

**External API Failures**: Log and gracefully degrade
```java
try {
    return tasteAtlasScraper.fetchFoodInfo(foodName);
} catch (ApiException e) {
    log.warn("TasteAtlas API failed, returning mock data", e);
    return mockFoodScraper.fetchFoodInfo(foodName);
}
```

### 9. Performance Targets (H2 Technical Validation)

**Hypothesis H2 Success Criteria**:
- OCR Accuracy: ≥90% (measured on 100 sample menus)
- Currency Accuracy: ≥95% (real-time exchange rates)
- Food Matching Relevance: ≥70% (human evaluation)
- Processing Time: ≤5 seconds (P95 latency)

**Optimization Rules**:
- Measure before optimizing (no premature optimization)
- Cache external API responses (Redis TTL: 1 hour for food data, 5 min for currency)
- Parallel API calls where possible (OCR || Food Matching)
- Database query optimization (proper indexes on `user_id`, `created_at`)

### 10. Security & Data Privacy

**MVP Security Scope** (minimal for internal testing):
- ✅ Input validation (prevent injection attacks)
- ✅ HTTPS in production
- ✅ API rate limiting (prevent abuse)
- ✅ CORS configuration (restrict origins)

**Out of Scope** (add post-MVP):
- ❌ User authentication (no login required for MVP)
- ❌ Admin authentication (internal testing only)
- ❌ PII encryption (no user accounts in MVP)
- ❌ GDPR compliance (no personal data storage)

**Data Handling**:
- No PII collected (only anonymous session IDs)
- Menu images deleted after processing (24h TTL)
- Survey responses anonymized (no IP tracking)

### 11. Documentation Standards

**Code Documentation**:
- JavaDoc for all public methods (especially hypothesis-critical code)
- Inline comments for complex business logic
- README.md for setup instructions

**Architecture Documentation**:
- ADRs (Architecture Decision Records) for key decisions
- Hypothesis validation plan in docs/HYPOTHESES.md
- API spec in docs/API_SPEC.md

**Test Documentation**:
- @DisplayName annotations explain what test validates
- Test names follow Given_When_Then pattern
- Integration test plan documents E2E scenarios

### 12. Code Quality Standards

**Core Principles** (Kent Beck):

**1. Remove Duplication**:
- Extract common logic into shared methods
- Use inheritance/composition for shared behavior
- Consolidate similar code patterns

**Example - Wrong** ❌:
```java
public void processControlGroup(MenuScan scan) {
    List<MenuItem> items = ocrService.extract(scan.getImage());
    List<MenuItem> translated = translationService.translate(items);
    return translated;
}

public void processTreatmentGroup(MenuScan scan) {
    List<MenuItem> items = ocrService.extract(scan.getImage());
    List<MenuItem> translated = translationService.translate(items);
    List<MenuItem> enriched = enrichmentService.enrich(translated);
    return enriched;
}
```

**Example - Right** ✅:
```java
private List<MenuItem> processCommonPipeline(MenuScan scan) {
    List<MenuItem> items = ocrService.extract(scan.getImage());
    return translationService.translate(items);
}

public void processControlGroup(MenuScan scan) {
    return processCommonPipeline(scan);
}

public void processTreatmentGroup(MenuScan scan) {
    List<MenuItem> translated = processCommonPipeline(scan);
    return enrichmentService.enrich(translated);
}
```

**2. Make Intent Clear**:
- Use descriptive names (no abbreviations unless domain standard)
- Extract magic numbers to named constants
- Extract complex conditions to named methods

**Example - Wrong** ❌:
```java
if (r.nextInt(100) < 50) { ... }  // What does this mean?

public void process(MenuScan s, int t) { ... }  // What are s and t?
```

**Example - Right** ✅:
```java
private static final int AB_TEST_SPLIT_RATIO = 50;

if (isControlGroup(sessionId)) { ... }

public void processMenuScan(MenuScan scan, int maxRetries) { ... }
```

**3. Keep Methods Small**:
- Target: 5-15 lines per method (ideal)
- Max: 30 lines (warning threshold)
- One level of abstraction per method
- Extract helpers for complex logic

**Example - Wrong** ❌:
```java
public MenuScanResponse scanMenu(MenuScanRequest request) {
    // 100+ lines of OCR, translation, enrichment, caching...
}
```

**Example - Right** ✅:
```java
public MenuScanResponse scanMenu(MenuScanRequest request) {
    ABGroup group = assignABGroup(request.getSessionId());
    List<MenuItem> items = extractAndTranslate(request.getImage());

    if (group == ABGroup.TREATMENT) {
        items = enrichWithVisuals(items);
    }

    return buildResponse(items, group);
}
```

**4. Minimize State**:
- Prefer immutable objects (final fields)
- Use method parameters over instance variables
- Stateless services (no instance state)
- Database for persistent state, not memory

**Example - Wrong** ❌:
```java
public class MenuService {
    private List<MenuItem> cachedItems;  // Mutable state
    private String currentSessionId;     // Thread-unsafe

    public void process() {
        this.cachedItems = ...;  // Side effect
    }
}
```

**Example - Right** ✅:
```java
public class MenuService {
    private final OcrService ocrService;  // Immutable dependencies
    private final RedisCache cache;       // External state management

    public List<MenuItem> process(String sessionId, byte[] image) {
        // No instance state, pure function
        return cache.computeIfAbsent(sessionId,
            () -> ocrService.extract(image));
    }
}
```

**5. Fail Fast**:
- Validate inputs at entry points
- Throw exceptions for invalid states
- Don't return null, use Optional<T>
- Make invalid states unrepresentable

**Example - Wrong** ❌:
```java
public MenuItem findMenuItem(String id) {
    if (id == null) return null;  // Silent failure
    return repository.findById(id);  // May return null
}
```

**Example - Right** ✅:
```java
public Optional<MenuItem> findMenuItem(String id) {
    Objects.requireNonNull(id, "MenuItem ID cannot be null");
    return repository.findById(id);  // Returns Optional<MenuItem>
}
```

**Quality Checklist**:
- [ ] No duplicated code blocks (>3 lines)
- [ ] All methods <30 lines
- [ ] No magic numbers (use named constants)
- [ ] All public methods have JavaDoc
- [ ] No mutable static state
- [ ] Use Optional<T> instead of null returns
- [ ] Input validation at public method boundaries

## Development Workflow

### Daily Development Cycle

1. **Plan** (5 min): Which hypothesis am I validating today?
2. **TDD Loop** (2-4 hours):
   - Write failing test (RED)
   - Implement minimal code (GREEN)
   - Refactor (REFACTOR)
   - Commit (VERIFY)
3. **Review** (15 min): Check coverage, run full test suite
4. **Push**: Create PR with hypothesis context

### Before Every Commit

**Pre-Commit Checklist** (BPlusTree3 Standard):

**1. Test Quality**:
- [ ] ALL tests pass (`./gradlew test`)
- [ ] Code coverage >80% for new code (`./gradlew jacocoTestReport`)
- [ ] New tests added for behavioral changes
- [ ] Existing tests unchanged for structural changes (refactoring)

**2. Code Quality**:
- [ ] NO compiler warnings (`./gradlew build --warning-mode all`)
- [ ] NO Checkstyle violations (`./gradlew check`)
- [ ] Code formatted (`./gradlew spotlessApply` or IDE formatter)
- [ ] NO dead code (unused imports, commented code, unused methods)
- [ ] NO debugging code (`System.out.println`, debug logs)

**3. Commit Discipline**:
- [ ] Commit type correct (`feat`, `test`, `refactor`, `fix`, `docs`, `chore`)
- [ ] Structural vs Behavioral separation maintained (no mixed commits)
- [ ] Commit message follows convention: `<type>(<scope>): <subject>`
- [ ] Commit size reasonable (50-200 LOC ideal, <500 LOC max)

**4. Hypothesis Alignment**:
- [ ] Changes align with hypothesis validation goal (H1, H2, or H3)
- [ ] No scope creep (out-of-scope features rejected)
- [ ] Feature exists to validate specific hypothesis

**5. Documentation**:
- [ ] JavaDoc updated for public methods
- [ ] README updated if setup/config changed
- [ ] ADR created for architectural decisions

**Quick Pre-Commit Script**:
```bash
# Run this before every commit
./gradlew clean build test jacocoTestReport --warning-mode all

# Check results
echo "✅ All quality gates passed - ready to commit"
```

### PR Review Criteria

**Must Have**:
- [ ] Tests for all new business logic
- [ ] Hypothesis validation context in PR description
- [ ] No breaking changes to existing tests
- [ ] Documentation updated (if API/behavior changed)

**Nice to Have**:
- Performance benchmarks (if H2 related)
- UI screenshots (if Treatment group UI)
- Manual testing evidence

## Anti-Patterns to Avoid

### ❌ Don't Do

1. **Feature Creep**: Adding "nice to have" features not in hypothesis validation scope
   - Bad: "Let's add user profiles while we're at it"
   - Good: "Does this help validate H1, H2, or H3? No → Skip it"

2. **Premature Abstraction**: Creating complex interfaces before they're needed
   - Bad: Creating `AbstractFoodScraperFactory` for 2 scrapers
   - Good: Simple concrete classes, refactor when 3+ similar classes emerge

3. **Testing Shortcuts**: Writing tests after code, or skipping tests
   - Bad: "I'll add tests later" (never happens)
   - Good: Test first, always

4. **Scope Negotiation**: Treating out-of-scope features as "small additions"
   - Bad: "Login is just one endpoint"
   - Good: "Login doesn't validate H1-H3, adding to post-MVP backlog"

5. **Data-Free Decisions**: Architectural choices without evidence
   - Bad: "NoSQL is always better for scale"
   - Good: "MySQL works for <10K daily users (our MVP target), switch if needed"

6. **Monolithic Commits**: Commits with >500 LOC or multiple unrelated changes
   - Bad: "feat: Add ABTest module, Survey module, and fix currency bug"
   - Good: 3 separate commits, each focused on one change

7. **Ignoring TDD Cycle**: Writing multiple features before testing
   - Bad: Implementing ABTestService + SurveyService, then testing both
   - Good: TDD cycle for ABTestService → commit → TDD cycle for SurveyService

### ✅ Do Instead

1. **Question Every Feature**: "Why are we building this? Which hypothesis?"
2. **Test First, Always**: No exceptions to TDD cycle
3. **Small Commits**: Commit after each TDD cycle (15-30 min)
4. **Reuse Before Build**: Search codebase before writing new code
5. **Measure, Then Optimize**: Profile performance before optimization
6. **Document Decisions**: Write ADRs for architecture choices
7. **Hypothesis Context**: Every PR includes hypothesis validation goal

## Claude Code Specific Guidelines

When working with Claude Code on FoodiePass:

### Preferred Workflow

1. **Start with Hypothesis**: State which hypothesis (H1/H2/H3) you're validating
2. **TDD Mode**: Explicitly follow RED-GREEN-REFACTOR-VERIFY cycle
3. **Code Reuse Check**: Search existing codebase before implementing
4. **Small Steps**: Implement one feature at a time, test, commit
5. **Context Preservation**: Reference hypothesis in commit messages

### Communication Style

- Be concise and direct (terminal-friendly output)
- State assumptions clearly ("Assuming OCR service is already implemented...")
- Flag MVP scope violations ("This feature is out of scope for H1-H3 validation")
- Provide evidence for claims ("Test coverage: 87% [show report link]")

### Task Breakdown

For complex features, break into phases:
```
Phase 1: Domain layer (entities, repositories) → Test → Commit
Phase 2: Application layer (services) → Test → Commit
Phase 3: API layer (controllers) → Test → Commit
Phase 4: Integration test → Test → Commit
```

### Quality Checks

Before marking any task complete:
```bash
# Run tests
./gradlew test

# Check coverage (target: >80%)
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html

# Check style
./gradlew check
```

## Hypothesis Validation Milestones

### Phase 1: MVP Development (10 days) ✅ Current Phase
- Implement A/B test system (Control vs Treatment)
- Build survey system (confidence measurement)
- Integrate existing menu pipeline (OCR, translation, currency, food matching)

### Phase 2: Internal Technical Validation (H2)
- Test OCR accuracy on 100 sample menus
- Validate currency conversion accuracy
- Human evaluation of food matching relevance

### Phase 3: User Testing (H1, H3)
- Deploy to staging environment
- Run A/B test with 100+ travelers
- Collect confidence survey responses
- Analyze: Is Treatment Yes rate ≥2x Control Yes rate?

### Phase 4: Hypothesis Evaluation
- **H2 Pass**: All technical metrics meet targets → Proceed
- **H2 Fail**: Accuracy too low → Pivot to R&D phase
- **H3 Pass**: Treatment/Control ratio ≥2.0 → Validated
- **H3 Fail**: Ratio <2.0 → Re-evaluate value hypothesis

## Living Document

This document evolves as we learn from hypothesis validation:
- Add learnings from each experiment
- Update anti-patterns as we discover new pitfalls
- Refine TDD practices based on team feedback

**Last Updated**: 2025-11-10
**Next Review**: After Phase 2 completion (Technical Validation)
