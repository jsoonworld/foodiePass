# FoodiePass MVP v2 - Merge Strategy

**Purpose**: 3개 feature 브랜치를 develop에 안전하게 병합하기 위한 상세 전략

**Version**: 1.0
**Updated**: 2025-11-04

---

## 🎯 Merge Goals

### Primary Objectives
1. **Zero Downtime**: 기존 기능에 영향 없이 새 기능 추가
2. **Conflict Minimization**: 충돌 최소화를 위한 순차적 병합
3. **Test Coverage**: 각 병합 후 전체 테스트 실행
4. **Rollback Capability**: 문제 발생 시 롤백 가능

### Success Criteria
- ✅ All tests pass after each merge
- ✅ No regression in existing features
- ✅ Build successful
- ✅ Integration tests pass

---

## 🔄 Merge Order & Rationale

### Why Sequential (Not Simultaneous)?

**Dependency Chain**:
```
ABTest (독립)
  ↓ FK dependency
Survey (ABTest에 의존)
  ↓ Service dependency
Menu API (ABTest + Survey 통합)
```

**Risk Management**:
- 순차적 병합으로 각 단계 검증
- 문제 발생 시 원인 격리 쉬움
- 롤백 범위 최소화

---

## 📊 Merge Sequence

### Merge 1: ABTest Module (First)

**Branch**: `feature/mvp-abtest` → `develop`

**Why First?**:
- 다른 모듈에 의존성 없음 (독립적)
- Survey가 MenuScan 테이블 필요
- Menu API가 ABTestService 필요

**Checklist Before Merge**:
- [ ] `foodiePass-abtest/backend` 디렉토리에서:
  - [ ] `./gradlew clean build` 성공
  - [ ] `./gradlew test --tests "foodiepass.server.abtest.*"` 전체 통과
  - [ ] `./gradlew jacocoTestReport` → Coverage ≥80%
  - [ ] `git status` → 모든 변경사항 커밋됨
  - [ ] `git log --oneline -5` → 커밋 메시지 확인

**Merge Commands**:
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# 1. Update develop
git checkout develop
git pull origin develop

# 2. Review changes
git log develop..feature/mvp-abtest --oneline
git diff develop...feature/mvp-abtest --stat

# 3. Merge with no-ff (create merge commit)
git merge feature/mvp-abtest --no-ff

# 4. Resolve conflicts (if any)
# See "Conflict Resolution" section below

# 5. Verify build
cd backend
./gradlew clean build

# 6. Run all tests
./gradlew test

# 7. If success, push
git push origin develop

# 8. Tag (optional)
git tag v2.0.0-abtest
git push origin v2.0.0-abtest
```

**Expected New Files**:
```
backend/src/main/java/foodiepass/server/abtest/
├── domain/
│   ├── ABGroup.java
│   └── MenuScan.java
├── application/
│   └── ABTestService.java
├── repository/
│   └── MenuScanRepository.java
├── api/
│   └── ABTestController.java
└── dto/...
```

**Verification**:
```bash
# Check new packages exist
ls -la backend/src/main/java/foodiepass/server/abtest/

# Run ABTest tests
./gradlew test --tests "foodiepass.server.abtest.*"

# Check no regression
./gradlew test
```

**Rollback (if needed)**:
```bash
git reset --hard HEAD~1  # ⚠️ Only if not pushed
# Or if pushed:
git revert HEAD
git push origin develop
```

---

### Merge 2: Survey Module (Second)

**Branch**: `feature/mvp-survey` → `develop`

**Why Second?**:
- Depends on ABTest (FK: survey_response.scan_id → menu_scan.id)
- Menu API needs Survey service for analytics

**Checklist Before Merge**:
- [ ] ABTest already merged ✅
- [ ] `foodiePass-survey/backend` 디렉토리에서:
  - [ ] `./gradlew clean build` 성공
  - [ ] `./gradlew test --tests "foodiepass.server.survey.*"` 전체 통과
  - [ ] Coverage ≥80%
  - [ ] All changes committed

**Merge Commands**:
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# 1. Update develop (now includes ABTest)
git checkout develop
git pull origin develop

# 2. Review changes
git log develop..feature/mvp-survey --oneline
git diff develop...feature/mvp-survey --stat

# 3. Merge
git merge feature/mvp-survey --no-ff

# 4. Verify
cd backend
./gradlew clean build
./gradlew test

# 5. Test FK relationship
# Run integration test to verify survey → scan FK works

# 6. Push
git push origin develop

# 7. Tag
git tag v2.0.0-survey
git push origin v2.0.0-survey
```

**Expected New Files**:
```
backend/src/main/java/foodiepass/server/survey/
├── domain/
│   └── SurveyResponse.java
├── application/
│   └── SurveyService.java
├── repository/
│   └── SurveyResponseRepository.java
├── api/
│   └── SurveyController.java
└── dto/...
```

**Critical Verification**:
```bash
# Test FK constraint
./gradlew test --tests "SurveyResponseRepositoryTest"

# Test analytics calculation
./gradlew test --tests "SurveyServiceTest"

# Integration test: ABTest + Survey
./gradlew test --tests "*ABTestSurveyIntegrationTest"
```

---

### Merge 3: Menu API (Last)

**Branch**: `feature/mvp-menu-api` → `develop`

**Why Last?**:
- Integrates both ABTest and Survey
- Modifies existing Menu module
- Highest risk of conflicts

**Checklist Before Merge**:
- [ ] ABTest merged ✅
- [ ] Survey merged ✅
- [ ] `foodiePass-menu-api/backend` 디렉토리에서:
  - [ ] `./gradlew clean build` 성공
  - [ ] `./gradlew test` 전체 통과
  - [ ] Coverage ≥80%
  - [ ] Processing time ≤5초 검증
  - [ ] Control vs Treatment 응답 차이 검증

**Merge Commands**:
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# 1. Update develop (now includes ABTest + Survey)
git checkout develop
git pull origin develop

# 2. Review changes (may be extensive)
git log develop..feature/mvp-menu-api --oneline
git diff develop...feature/mvp-menu-api --stat

# 3. Merge (expect conflicts)
git merge feature/mvp-menu-api --no-ff

# 4. Resolve conflicts
# See "Likely Conflicts" section below

# 5. Comprehensive verification
cd backend
./gradlew clean build
./gradlew test

# 6. E2E integration test
./gradlew bootRun --args='--spring.profiles.active=local' &
# Wait for startup...
# Run manual tests (see E2E_TEST_SCENARIOS.md)
# Test: /api/menus/scan → Control/Treatment responses
# Test: /api/surveys → Survey submission
# Test: /api/admin/ab-test/results
# Kill app: pkill -f bootRun

# 7. Performance test
# Verify processing time ≤5초

# 8. Push
git push origin develop

# 9. Tag final
git tag v2.0.0-mvp
git push origin v2.0.0-mvp
```

**Expected Changes**:
```
backend/src/main/java/foodiepass/server/menu/
├── api/
│   ├── MenuController.java (MODIFIED - new endpoint)
│   └── MenuScanController.java (NEW)
├── application/
│   └── MenuScanService.java (NEW - orchestration)
├── dto/
│   ├── MenuScanRequest.java (NEW)
│   ├── MenuScanResponse.java (NEW)
│   └── MenuItemDto.java (NEW)
└── ...
```

**Critical Verification**:
```bash
# Test full pipeline
curl -X POST http://localhost:8080/api/menus/scan \
  -H "Content-Type: application/json" \
  -d @test-data/sample-menu-request.json

# Verify Control response (no FoodInfo)
# Verify Treatment response (with FoodInfo)

# Test survey submission
curl -X POST http://localhost:8080/api/surveys \
  -H "Content-Type: application/json" \
  -d '{"scanId": "...", "hasConfidence": true}'

# Check analytics
curl http://localhost:8080/api/admin/surveys/analytics
```

---

## ⚠️ Likely Conflicts

### Conflict 1: build.gradle

**Where**: `backend/build.gradle`

**Why**: All 3 modules may add dependencies

**Resolution**:
```gradle
dependencies {
    // Keep all unique dependencies
    // ABTest module deps
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Survey module deps (may overlap)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa' // Duplicate - keep one

    // Menu API deps
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Remove duplicates, keep all unique
}
```

**Command**:
```bash
# After merge conflict
git checkout --ours build.gradle   # Start with current
# Manually add new dependencies from --theirs
# Then:
git add build.gradle
```

---

### Conflict 2: application.yml

**Where**: `backend/src/main/resources/application.yml`

**Why**: Multiple modules may add configurations

**Resolution**:
```yaml
# Keep all configurations, merge sections
spring:
  datasource:
    # Existing config
  jpa:
    # Existing config
    # + New from Survey (if any)

# ABTest config (if any)
abtest:
  assignment-strategy: random

# Survey config (if any)
survey:
  analytics-cache-ttl: 3600
```

---

### Conflict 3: MenuController.java

**Where**: `backend/src/main/java/foodiepass/server/menu/api/MenuController.java`

**Why**: Menu API may modify existing controller

**Resolution Strategy**:
- **Option A**: Keep existing endpoints unchanged, add new controller (`MenuScanController`)
- **Option B**: Add new endpoint to existing controller

**Recommendation**: Option A (less conflict)

---

### Conflict 4: Test files

**Where**: `backend/src/test/java/**`

**Why**: Test file organization

**Resolution**: Keep all tests, merge imports

---

## 🛠️ Conflict Resolution Process

### General Steps

1. **Identify Conflicts**:
```bash
git status
# Look for "both modified" files
```

2. **Review Conflict Markers**:
```java
<<<<<<< HEAD (develop)
// Current code
=======
// Incoming code
>>>>>>> feature/mvp-xxx
```

3. **Choose Strategy**:
- **Accept Ours**: `git checkout --ours <file>` (keep develop)
- **Accept Theirs**: `git checkout --theirs <file>` (use feature branch)
- **Manual Merge**: Edit file, keep both (most common)

4. **Verify Resolution**:
```bash
# After resolving
./gradlew clean build
./gradlew test
```

5. **Complete Merge**:
```bash
git add <resolved-files>
git merge --continue
```

---

### Conflict Resolution Checklist

For each conflicted file:
- [ ] Understand what each side changed
- [ ] Decide: ours / theirs / both / custom
- [ ] Edit file to resolve
- [ ] Remove conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`)
- [ ] Test the file (compile, run tests)
- [ ] `git add <file>`

After all conflicts resolved:
- [ ] `git status` → All conflicts resolved
- [ ] `./gradlew clean build` → Success
- [ ] `./gradlew test` → All pass
- [ ] `git merge --continue`

---

## 🔍 Pre-Merge Validation

### For Each Module

**Before merging any branch**:

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass-<module>/backend

# 1. Clean build
./gradlew clean build
# Expected: BUILD SUCCESSFUL

# 2. Run tests
./gradlew test
# Expected: All tests pass

# 3. Check coverage
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
# Expected: ≥80%

# 4. Check for uncommitted changes
git status
# Expected: nothing to commit, working tree clean

# 5. Review commits
git log develop..HEAD --oneline
# Expected: Clean, descriptive commit messages

# 6. Check for large files
git diff develop..HEAD --stat
# Expected: No unexpected large files
```

---

## 🚨 Rollback Strategies

### Scenario 1: Merge Not Yet Pushed

**Problem**: Merge caused issues locally

**Solution**:
```bash
# Reset to before merge
git reset --hard HEAD~1

# Or reset to specific commit
git log --oneline -10
git reset --hard <commit-before-merge>

# Lost changes? Use reflog
git reflog
git reset --hard HEAD@{N}
```

---

### Scenario 2: Merge Already Pushed

**Problem**: Merge caused issues in remote develop

**Solution Option A - Revert (Safe)**:
```bash
# Create revert commit
git revert -m 1 HEAD
git push origin develop

# -m 1 means "revert to first parent" (develop)
```

**Solution Option B - Reset (Dangerous)**:
```bash
# ⚠️ Only if no one else pulled

# Reset local
git reset --hard HEAD~1

# Force push
git push --force origin develop

# ⚠️ Notify team immediately
```

---

### Scenario 3: Partial Rollback

**Problem**: Only one module needs rollback

**Solution**:
```bash
# Identify bad merge commit
git log --oneline

# Revert specific merge
git revert -m 1 <bad-merge-commit-hash>

# Keep other merges intact
git push origin develop
```

---

## ✅ Post-Merge Validation

### After Each Merge

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass/backend

# 1. Build check
./gradlew clean build
# ✅ Must succeed

# 2. Test check
./gradlew test
# ✅ All tests pass

# 3. Regression check
./gradlew test --tests "*ExistingFeatureTest"
# ✅ Old features still work

# 4. Integration check
./gradlew test --tests "*IntegrationTest"
# ✅ New + old features work together

# 5. Smoke test
./gradlew bootRun --args='--spring.profiles.active=local' &
sleep 30
curl http://localhost:8080/actuator/health
# ✅ {"status":"UP"}
pkill -f bootRun
```

---

### After Final Merge (All 3 Merged)

```bash
# 1. Full build
./gradlew clean build

# 2. All tests
./gradlew test

# 3. E2E tests
# See E2E_TEST_SCENARIOS.md

# 4. Performance test
# Verify processing time ≤5초

# 5. Database migration
# Apply V2_0__add_abtest_tables.sql

# 6. Manual smoke test
# Test all critical flows

# 7. Code review
# Review final merged code

# 8. Documentation update
# Update README, API docs
```

---

## 📋 Merge Checklist Summary

### Pre-Merge (Each Module)
- [ ] All tests pass in worktree
- [ ] Coverage ≥80%
- [ ] All changes committed
- [ ] Build successful
- [ ] Git history clean

### During Merge
- [ ] `git checkout develop && git pull`
- [ ] `git merge feature/mvp-xxx --no-ff`
- [ ] Resolve conflicts (if any)
- [ ] `./gradlew clean build` → Success
- [ ] `./gradlew test` → All pass
- [ ] `git push origin develop`

### Post-Merge
- [ ] All tests pass
- [ ] No regressions
- [ ] Integration tests pass
- [ ] Tag created
- [ ] Team notified

---

## 🔗 Related Documents

- [INTEGRATION_CHECKLIST.md](./INTEGRATION_CHECKLIST.md) - Overall integration process
- [E2E_TEST_SCENARIOS.md](./E2E_TEST_SCENARIOS.md) - Test scenarios
- [WORKTREE_USAGE.md](./WORKTREE_USAGE.md) - Worktree management

---

## 📞 Emergency Contacts

**Merge Lead**: Session 4 (Current)

**Module Owners**:
- ABTest: Session 1
- Survey: Session 2
- Menu API: Session 3

**Escalation**: If merge issues, pause and discuss before forcing

---

**Last Updated**: 2025-11-04
**Version**: 1.0
**Status**: Ready for execution
