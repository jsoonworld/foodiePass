# FoodiePass Commit Message Templates

**Purpose**: 일관된 커밋 메시지 작성 가이드 (Conventional Commits + BPlusTree3)

**Context**:
- Kent Beck의 "Tidy First" 원칙 기반 (Structural vs Behavioral 분리)
- FoodiePass 가설 검증 프로젝트 맥락 반영
- BPlusTree3 방법론 통합

**Last Updated**: 2025-01-10 (Phase 1)

---

## 1. Conventional Commits Standard

### 1.1 기본 형식

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**구성 요소**:
- **type** (필수): 커밋 타입 (feat, fix, refactor, test, docs, chore)
- **scope** (선택): 영향 범위 (backend, frontend, abtest, survey, menu, etc.)
- **subject** (필수): 간결한 설명 (50자 이내, 명령형, 소문자 시작)
- **body** (선택): 상세 설명 (Why, What, How)
- **footer** (선택): Breaking changes, Issue 참조

### 1.2 Subject 작성 규칙

**✅ RIGHT**:
```
feat(abtest): add random group assignment logic
fix(currency): handle null exchange rates gracefully
refactor: extract menu enrichment to separate method
```

**❌ WRONG**:
```
feat(abtest): Added random group assignment logic  # 과거형
Fix currency bug  # 대문자 시작, scope 없음
Refactored code  # 모호함
```

**작성 원칙**:
- 명령형 현재 시제 사용 (add, not added)
- 첫 글자 소문자
- 마침표 없음
- 구체적 행동 표현 (what was done)

---

## 2. Commit Types & Usage

### 2.1 Behavioral Changes (행동 변경)

행동 변경은 **기능 추가, 버그 수정, 비즈니스 로직 변경** 등 코드의 동작을 변경하는 커밋입니다.

#### feat: 새 기능 추가

**목적**: 가설 검증을 위한 새로운 기능 구현

**패턴**:
```
feat(<scope>): <implement new feature for hypothesis validation>

- Related hypothesis: H1/H2/H3
- Feature description
- Expected behavior
```

**예시 - RIGHT** ✅:
```
feat(abtest): add ABTestService with random group assignment

- Related hypothesis: H3 (Treatment vs Control comparison)
- Implement 50/50 random assignment for A/B testing
- Store group assignment in Redis with 24h TTL
```

```
feat(survey): implement confidence survey endpoint

- Related hypothesis: H1, H3 (confidence measurement)
- POST /api/surveys endpoint for confidence data collection
- Store responses with scan_id and ab_group
```

**예시 - WRONG** ❌:
```
feat: add new feature  # 모호함, scope 없음

feat(backend): implement everything  # 너무 큼, 여러 기능 혼합
```

**체크리스트**:
- [ ] 가설(H1, H2, H3) 중 하나와 연계
- [ ] 새로운 테스트 포함
- [ ] MVP 범위 내 기능
- [ ] 단일 기능에 집중 (50-200 LOC)

---

#### fix: 버그 수정

**목적**: 의도하지 않은 동작 수정

**패턴**:
```
fix(<scope>): <resolve specific bug>

- Problem: [what was broken]
- Solution: [how it was fixed]
- Impact: [affected features]
```

**예시 - RIGHT** ✅:
```
fix(currency): handle null exchange rates from API

- Problem: NullPointerException when API returns null
- Solution: Add Optional handling with default fallback
- Impact: Currency conversion resilience improved
```

```
fix(menu): correct price parsing for formatted strings

- Problem: Failed to parse "1,234.56" format
- Solution: Remove commas before parsing
- Impact: Price detection accuracy increased
```

**예시 - WRONG** ❌:
```
fix: bug fix  # 무엇을 고쳤는지 불명확

fix(backend): fix various issues  # 여러 버그 혼합
```

**체크리스트**:
- [ ] 버그가 명확히 설명됨
- [ ] 재현 테스트 추가
- [ ] 회귀 방지 테스트 포함
- [ ] 단일 버그에 집중

---

### 2.2 Structural Changes (구조 변경)

구조 변경은 **리팩토링, 코드 정리, 최적화** 등 동작은 유지하면서 코드 구조만 개선하는 커밋입니다.

#### refactor: 코드 구조 개선

**원칙**: Kent Beck의 "Tidy First"
- 기존 테스트가 **변경 없이** 통과해야 함
- 동작 변경 없음 (No behavioral change)
- 가독성, 유지보수성, 성능 개선

**패턴**:
```
refactor(<scope>): <improve code structure>

- What: [what was refactored]
- Why: [reason for refactoring]
- Impact: [performance/readability improvement]
```

**예시 - RIGHT** ✅:
```
refactor(menu): extract food matching logic to separate service

- What: Move food matching from MenuService to FoodMatchingService
- Why: Single Responsibility Principle, better testability
- Impact: 150 LOC moved, MenuService simplified
```

```
refactor: rename assignGroup to assignABGroup for clarity

- What: Method rename across ABTestService
- Why: Clarify that this assigns A/B test groups specifically
- Impact: Improved code readability
```

**예시 - WRONG** ❌:
```
refactor: improve code  # 모호함

refactor(menu): fix bug and refactor  # ❌ behavioral + structural 혼합
```

**체크리스트**:
- [ ] 모든 기존 테스트 통과 (unchanged)
- [ ] 동작 변경 없음 검증
- [ ] 단일 리팩토링 집중
- [ ] 가독성/성능 개선 설명

---

### 2.3 Supporting Changes (지원 변경)

#### test: 테스트 추가/수정

**목적**: 테스트 코드 추가, 수정, 개선

**패턴**:
```
test(<scope>): <add/modify tests>

- Coverage: [what is tested]
- Scenarios: [test cases added]
```

**예시 - RIGHT** ✅:
```
test(abtest): add ABTestService unit tests

- Coverage: 90% for group assignment logic
- Scenarios: random assignment, edge cases, null handling
```

```
test(menu): add integration test for full menu scan pipeline

- Coverage: OCR → Translation → Enrichment → Currency
- Scenarios: happy path, API failures, timeout handling
```

**예시 - WRONG** ❌:
```
test: add tests  # 모호함

test(backend): add all tests  # 너무 큼
```

**체크리스트**:
- [ ] 테스트 커버리지 목표 명시
- [ ] 주요 시나리오 설명
- [ ] TDD 사이클 준수 (Red-Green-Refactor)

---

#### docs: 문서화

**목적**: 문서 추가, 수정, 개선

**패턴**:
```
docs(<scope>): <document changes>

- What: [what was documented]
- Audience: [who will read this]
```

**예시 - RIGHT** ✅:
```
docs(adr): add ADR-001 for A/B test strategy

- What: Document decision to use 50/50 random assignment
- Audience: Development team, future maintainers
```

```
docs(api): update menu scan endpoint specification

- What: Add abGroup field to MenuScanResponse
- Audience: Frontend developers, API consumers
```

**예시 - WRONG** ❌:
```
docs: update docs  # 모호함

docs: add documentation  # 무엇을 문서화했는지 불명확
```

**체크리스트**:
- [ ] 문서 목적 명확
- [ ] 대상 독자 고려
- [ ] 실행 가능한 가이드 (not just abstract principles)

---

#### chore: 빌드, 도구, 의존성

**목적**: 빌드 설정, 의존성 업데이트, 개발 도구 설정

**패턴**:
```
chore(<scope>): <build/tool changes>

- What: [what was changed]
- Impact: [build/dev environment impact]
```

**예시 - RIGHT** ✅:
```
chore(deps): upgrade Spring Boot to 3.5.3

- What: Bump Spring Boot from 3.4.0 to 3.5.3
- Impact: Security patches, performance improvements
```

```
chore(ci): configure CodeRabbit for develop branch

- What: Add CodeRabbit config for automatic PR reviews
- Impact: Faster code review turnaround
```

**예시 - WRONG** ❌:
```
chore: update stuff  # 모호함

chore: various changes  # 여러 변경 혼합
```

**체크리스트**:
- [ ] 변경 사항 명확
- [ ] 영향 범위 설명
- [ ] 코드 변경 없음 (빌드/도구만)

---

## 3. Structural vs Behavioral Separation

### 3.1 Kent Beck's "Tidy First" Principle

**핵심 원칙**: NEVER mix structural changes (refactoring) with behavioral changes (features/fixes) in the same commit.

**이유**:
1. **Easy Rollback**: 기능 추가 실패 시 리팩토링은 유지 가능
2. **Faster Review**: 구조 변경과 기능 변경을 분리하여 리뷰 속도 향상
3. **Clear Intent**: 무엇을 변경했는지 (structural) vs 왜 변경했는지 (behavioral) 구분
4. **Safer Merges**: 충돌 해결 시 의도 파악 용이

### 3.2 Workflow Pattern

**✅ RIGHT**: Structural → Behavioral 순서로 커밋
```bash
# 1. 리팩토링 먼저 (Structural)
git commit -m "refactor(menu): extract food matching logic to separate service"

# 2. 기능 추가 (Behavioral)
git commit -m "feat(menu): add food photo enrichment for Treatment group"

# 3. 테스트 추가 (Supporting)
git commit -m "test(menu): add food enrichment integration tests"

# 4. 다시 리팩토링 (Structural)
git commit -m "refactor: simplify enrichment pipeline with functional style"
```

**❌ WRONG**: 구조 + 기능 혼합
```bash
# ❌ Mixed commit (hard to review, risky rollback)
git commit -m "feat: Add food enrichment and refactor MenuService"
```

### 3.3 How to Separate

**Case 1: 리팩토링 후 기능 추가**
```bash
# Step 1: 기존 코드 정리 (Structural)
git add src/main/java/foodiepass/server/menu/MenuService.java
git commit -m "refactor(menu): extract common pipeline to processCommonPipeline method"

# Step 2: 새 기능 추가 (Behavioral)
git add src/main/java/foodiepass/server/menu/MenuService.java
git commit -m "feat(menu): add food enrichment for Treatment group"
```

**Case 2: 버그 수정과 리팩토링 분리**
```bash
# Step 1: 버그 수정 (Behavioral)
git commit -m "fix(currency): handle null exchange rates with Optional"

# Step 2: 코드 정리 (Structural)
git commit -m "refactor(currency): rename variables for clarity"
```

### 3.4 Verification Checklist

**Structural Commit 검증**:
- [ ] 모든 기존 테스트가 **변경 없이** 통과하는가?
- [ ] 동작 변경이 **전혀** 없는가?
- [ ] 리팩토링 목적이 **명확**한가? (가독성, 성능, 구조)

**Behavioral Commit 검증**:
- [ ] 새 테스트 또는 수정된 테스트가 포함되는가?
- [ ] 가설(H1, H2, H3) 중 하나와 연계되는가?
- [ ] 단일 기능에 집중하는가?

---

## 4. Commit Message Templates

### 4.1 Feature Commit Template

```
feat(<scope>): <implement new feature>

Related Hypothesis: H1/H2/H3
Feature: [brief description]
Implementation:
- [key change 1]
- [key change 2]
- [key change 3]

Testing:
- [test coverage]
- [scenarios covered]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Example**:
```
feat(abtest): add ABTestService with random group assignment

Related Hypothesis: H3 (Treatment vs Control comparison)
Feature: Random 50/50 A/B group assignment
Implementation:
- Create ABTestService with assignGroup() method
- Use SecureRandom for unbiased assignment
- Store assignments in Redis with 24h TTL

Testing:
- Unit tests for assignment logic (90% coverage)
- Integration tests for Redis persistence
- Edge cases: null userId, concurrent assignments

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

### 4.2 Bug Fix Template

```
fix(<scope>): <resolve specific bug>

Problem: [what was broken]
Root Cause: [why it happened]
Solution: [how it was fixed]
Impact: [affected features]

Testing:
- [reproduction test]
- [regression prevention]

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Example**:
```
fix(currency): handle null exchange rates from API

Problem: NullPointerException when Currency API returns null
Root Cause: Missing null check in CurrencyService.convert()
Solution: Wrap API response with Optional, fallback to 1.0 rate
Impact: Currency conversion now resilient to API failures

Testing:
- Unit test: API returns null → fallback rate used
- Integration test: Full pipeline with API failure
- Regression test: Previous bug scenario

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

### 4.3 Refactor Template

```
refactor(<scope>): <improve code structure>

What: [what was refactored]
Why: [reason for refactoring]
How: [approach taken]

Before:
[brief description of old structure]

After:
[brief description of new structure]

Impact:
- [readability improvement]
- [performance improvement]
- [maintainability improvement]

All tests pass: ✅

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Example**:
```
refactor(menu): extract food matching logic to separate service

What: Move food matching from MenuService to FoodMatchingService
Why: Single Responsibility Principle, better testability
How: Extract methods, inject new service, maintain contracts

Before:
- MenuService had 300+ LOC with OCR, translation, matching
- Tight coupling, hard to test in isolation

After:
- MenuService: 150 LOC (orchestration only)
- FoodMatchingService: 120 LOC (matching logic)
- Clear separation of concerns

Impact:
- Readability: Each service has single responsibility
- Testability: FoodMatchingService can be tested independently
- Maintainability: Easier to swap matching implementations

All tests pass: ✅

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

### 4.4 Test Template

```
test(<scope>): <add/modify tests>

Coverage: [what is tested]
Scenarios:
- [scenario 1]
- [scenario 2]
- [scenario 3]

Results:
- Coverage: [percentage]
- All tests pass: ✅

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Example**:
```
test(abtest): add ABTestService unit tests

Coverage: Group assignment logic and Redis persistence
Scenarios:
- Random assignment distributes 50/50 across 1000 runs
- Same userId returns same group within 24h
- Concurrent assignments handled correctly
- Null userId throws IllegalArgumentException

Results:
- Coverage: 92% (43/47 lines)
- All tests pass: ✅

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

## 5. BPlusTree3 Integration

### 5.1 Pre-Commit Checklist (from PRINCIPLES.md)

Before every commit, verify:

**1. Test Quality**:
- [ ] ALL tests pass (`./gradlew test`)
- [ ] Code coverage >80% for new code
- [ ] New tests for behavioral changes
- [ ] Existing tests unchanged for structural changes

**2. Code Quality**:
- [ ] NO compiler warnings
- [ ] NO Checkstyle violations
- [ ] Code formatted (`./gradlew spotlessApply`)
- [ ] NO dead code (unused imports, commented code)
- [ ] NO debugging code (`System.out.println`)

**3. Commit Discipline**:
- [ ] Commit type correct (`feat`, `test`, `refactor`, `fix`, `docs`, `chore`)
- [ ] Structural vs Behavioral separation maintained
- [ ] Commit message follows convention: `<type>(<scope>): <subject>`
- [ ] Commit size reasonable (50-200 LOC ideal, <500 LOC max)

**4. Hypothesis Alignment**:
- [ ] Changes align with H1, H2, or H3
- [ ] No scope creep (out-of-scope features)
- [ ] Feature validates specific hypothesis

**5. Documentation**:
- [ ] JavaDoc updated for public methods
- [ ] README updated if setup/config changed
- [ ] ADR created for architectural decisions

### 5.2 Quality Standards Integration (from quality_standards.md)

**Exception Handling**:
- [ ] Domain Exception extends BaseException
- [ ] ErrorCode enum defined
- [ ] Exception message has sufficient context

**Optional Usage**:
- [ ] Repository returns Optional
- [ ] Avoid `isPresent()` + `get()` pattern
- [ ] Collections return empty list (not Optional)

**Logging**:
- [ ] Use `@Slf4j` annotation
- [ ] Appropriate log level
- [ ] Exception logging includes stack trace
- [ ] No sensitive information in logs

**Performance**:
- [ ] Performance test exists (if H2 related)
- [ ] Processing time ≤ 5 seconds
- [ ] Performance metrics logged

---

## 6. Commit Size Guidelines

### 6.1 Size Ranges

| Size | LOC Range | Status | Recommendation |
|------|-----------|--------|----------------|
| **Ideal** | 50-200 LOC | ✅ | Perfect commit size |
| **Acceptable** | 200-500 LOC | ⚠️ | Consider splitting |
| **Too Large** | >500 LOC | ❌ | Must split into multiple commits |

### 6.2 How to Split Large Commits

**Strategy 1: Structural → Behavioral**
```bash
# Instead of:
git commit -m "feat: Implement entire A/B test system (800 LOC)"

# Do:
git commit -m "refactor: Prepare MenuService for A/B testing (150 LOC)"
git commit -m "feat(abtest): Add ABTestService with group assignment (200 LOC)"
git commit -m "test(abtest): Add ABTestService unit tests (180 LOC)"
git commit -m "feat(menu): Integrate A/B test with menu pipeline (150 LOC)"
git commit -m "test(menu): Add A/B test integration tests (120 LOC)"
```

**Strategy 2: Layer by Layer**
```bash
# Instead of:
git commit -m "feat: Add survey system (600 LOC)"

# Do:
git commit -m "feat(survey): Add SurveyResponse domain entity (80 LOC)"
git commit -m "feat(survey): Add SurveyService with business logic (150 LOC)"
git commit -m "feat(survey): Add SurveyController REST API (120 LOC)"
git commit -m "test(survey): Add survey system tests (180 LOC)"
```

**Strategy 3: Feature by Feature**
```bash
# Instead of:
git commit -m "feat: Add multiple features (700 LOC)"

# Do:
git commit -m "feat(menu): Add OCR pipeline (200 LOC)"
git commit -m "feat(menu): Add translation pipeline (180 LOC)"
git commit -m "feat(menu): Add currency conversion (150 LOC)"
git commit -m "test(menu): Add menu pipeline tests (170 LOC)"
```

---

## 7. Real Examples from FoodiePass History

### 7.1 Good Examples ✅

**Example 1: Clear Feature Scope**
```
feat(abtest): Enable random A/B group assignment and add test data generators
```
- Type: `feat` (behavioral)
- Scope: `abtest` (clear domain)
- Subject: Clear what was added

**Example 2: Specific Bug Fix**
```
fix(config): Move path_instructions inside reviews section
```
- Type: `fix` (behavioral)
- Scope: `config` (clear domain)
- Subject: Specific problem solved

**Example 3: Clear Refactoring**
```
refactor: Apply additional CodeRabbit feedback for code quality
```
- Type: `refactor` (structural)
- No scope (multiple files)
- Subject: Clear intent

### 7.2 Could Be Improved ⚠️

**Example 1: Too Generic**
```
fix: Fix CORS issue and CurrencySelector React key warning
```
❌ Problem: Two unrelated fixes in one commit

✅ Better:
```
fix(backend): resolve CORS configuration issue
fix(frontend): fix React key warning in CurrencySelector
```

**Example 2: Scope Mismatch**
```
config: Configure local development environment for full-stack testing
```
❌ Problem: `config` is not a standard type

✅ Better:
```
chore(env): configure local development for full-stack testing
```

**Example 3: Too Vague**
```
refactor: Remove unused ObjectMapper from LanguageControllerTest
```
✅ Actually good! (Clear what was removed)

---

## 8. Common Mistakes & Solutions

### 8.1 Mistake: Mixed Commits

**❌ Wrong**:
```
feat: Add ABTestService and refactor MenuService
```
Problem: Mixes behavioral (feat) with structural (refactor)

**✅ Right**:
```
# Commit 1 (Structural)
refactor(menu): extract common pipeline logic

# Commit 2 (Behavioral)
feat(abtest): add ABTestService with group assignment
```

---

### 8.2 Mistake: Generic Messages

**❌ Wrong**:
```
fix: bug fix
refactor: improve code
feat: add feature
```

**✅ Right**:
```
fix(currency): handle null exchange rates from API
refactor(menu): extract food matching to separate service
feat(survey): implement confidence survey endpoint
```

---

### 8.3 Mistake: Too Large Commits

**❌ Wrong**:
```
feat: Implement entire MVP (2000 LOC)
```
Problem: Impossible to review, risky to rollback

**✅ Right**:
```
# 10 smaller commits, each 150-200 LOC
feat(abtest): add AB group assignment
feat(abtest): add Redis persistence
test(abtest): add unit tests
feat(survey): add survey domain
feat(survey): add survey service
feat(survey): add survey API
test(survey): add survey tests
feat(menu): integrate A/B test
test(menu): add integration tests
docs(api): update API specification
```

---

### 8.4 Mistake: No Hypothesis Context

**❌ Wrong**:
```
feat(abtest): add A/B testing
```
Problem: Why are we adding this? What hypothesis does it validate?

**✅ Right**:
```
feat(abtest): add A/B testing for confidence comparison

Related Hypothesis: H3 (Treatment vs Control comparison)
- Random 50/50 group assignment
- Enables comparison of confidence rates between groups
```

---

## 9. Automation & Git Hooks

### 9.1 Pre-Commit Hook Script

Create `.git/hooks/pre-commit` (optional):

```bash
#!/bin/bash

echo "🔍 Running pre-commit checks..."

# 1. Run tests
echo "📝 Running tests..."
./gradlew test --quiet
if [ $? -ne 0 ]; then
    echo "❌ Tests failed. Commit aborted."
    exit 1
fi

# 2. Check code formatting
echo "🎨 Checking code formatting..."
./gradlew spotlessCheck --quiet
if [ $? -ne 0 ]; then
    echo "❌ Code formatting issues found. Run './gradlew spotlessApply'"
    exit 1
fi

# 3. Check for debug code
echo "🐛 Checking for debug code..."
if git diff --cached --name-only | xargs grep -n "System.out.println" 2>/dev/null; then
    echo "❌ System.out.println found. Remove debug code."
    exit 1
fi

# 4. Validate commit message format
echo "✉️ Validating commit message format..."
COMMIT_MSG_FILE=$1
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

if ! echo "$COMMIT_MSG" | grep -qE "^(feat|fix|refactor|test|docs|chore)(\(.+\))?: .+"; then
    echo "❌ Invalid commit message format."
    echo "Expected: <type>(<scope>): <subject>"
    echo "Example: feat(abtest): add group assignment logic"
    exit 1
fi

echo "✅ All pre-commit checks passed!"
exit 0
```

**Installation**:
```bash
chmod +x .git/hooks/pre-commit
```

### 9.2 Commit Message Validation

Create `.git/hooks/commit-msg`:

```bash
#!/bin/bash

COMMIT_MSG_FILE=$1
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

# Validate format: <type>(<scope>): <subject>
if ! echo "$COMMIT_MSG" | grep -qE "^(feat|fix|refactor|test|docs|chore)(\(.+\))?: .+"; then
    echo "❌ Invalid commit message format."
    echo ""
    echo "Expected format:"
    echo "  <type>(<scope>): <subject>"
    echo ""
    echo "Types: feat, fix, refactor, test, docs, chore"
    echo ""
    echo "Examples:"
    echo "  feat(abtest): add group assignment logic"
    echo "  fix(currency): handle null exchange rates"
    echo "  refactor: extract food matching service"
    echo ""
    exit 1
fi

# Validate subject length (max 72 characters)
SUBJECT=$(echo "$COMMIT_MSG" | head -n 1)
if [ ${#SUBJECT} -gt 72 ]; then
    echo "⚠️ Commit subject too long (${#SUBJECT} chars, max 72)"
    echo "Consider shortening: $SUBJECT"
    exit 1
fi

echo "✅ Commit message format valid"
exit 0
```

**Installation**:
```bash
chmod +x .git/hooks/commit-msg
```

---

## 10. Quick Reference

### Type Decision Tree

```
Is this a code change?
├─ Yes → Does it change behavior?
│   ├─ Yes → Is it adding new functionality?
│   │   ├─ Yes → feat
│   │   └─ No → fix
│   └─ No → refactor
└─ No → Is it documentation?
    ├─ Yes → docs
    ├─ No → Is it test-only?
    │   ├─ Yes → test
    │   └─ No → chore
```

### Scope Selection

**Backend**:
- `abtest`: A/B testing logic
- `survey`: Survey system
- `menu`: Menu processing pipeline
- `currency`: Currency conversion
- `language`: Translation
- `food`: Food matching/enrichment

**Frontend**:
- `ui`: UI components
- `api`: API client integration
- `state`: State management

**Cross-cutting**:
- `config`: Configuration
- `deps`: Dependencies
- `ci`: CI/CD
- `docs`: Documentation

---

## 11. Summary: Commit Checklist

Before every commit, verify:

**Format**:
- [ ] Type is one of: feat, fix, refactor, test, docs, chore
- [ ] Scope clearly indicates affected domain
- [ ] Subject is concise (<72 chars), imperative, lowercase

**Content**:
- [ ] Single responsibility (one logical change)
- [ ] Structural vs Behavioral separation maintained
- [ ] Size reasonable (50-200 LOC ideal)

**Quality**:
- [ ] All tests pass
- [ ] Code formatted
- [ ] No compiler warnings
- [ ] No debug code

**Context**:
- [ ] Related hypothesis mentioned (H1/H2/H3)
- [ ] Body explains "why" if not obvious
- [ ] Breaking changes documented in footer

---

## Appendix: Template Quick Copy

### Minimal Commit
```
<type>(<scope>): <subject>
```

### Standard Commit
```
<type>(<scope>): <subject>

- Brief explanation
- Key change
- Impact

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

### Detailed Commit
```
<type>(<scope>): <subject>

Related Hypothesis: H1/H2/H3

[Detailed explanation of what, why, how]

Implementation:
- Change 1
- Change 2
- Change 3

Testing:
- Test coverage
- Scenarios covered

Impact:
- Performance/readability/maintainability improvement

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

**End of Commit Templates Guide**

For questions or improvements, refer to:
- `.claude/PRINCIPLES.md` - Development principles
- `.claude/quality_standards.md` - Quality standards
- `docs/adr/` - Architecture decisions
