# FoodiePass MVP v2 - Integration Checklist

**Purpose**: 3개 모듈(ABTest, Survey, Menu API) 병합 및 통합 절차

**Updated**: 2025-11-04

---

## 📊 Current Status

| Module | Branch | Status | Assignee | Notes |
|--------|--------|--------|----------|-------|
| ABTest | feature/mvp-abtest | 🟡 In Progress | Session 1 | - |
| Survey | feature/mvp-survey | 🟡 In Progress | Session 2 | - |
| Menu API | feature/mvp-menu-api | 🟡 In Progress | Session 3 | - |

**Legend**: ⚪ Not Started | 🟡 In Progress | 🟢 Ready | ✅ Merged

---

## 🔄 Integration Phases

### Phase 1: Pre-Integration (각 모듈 완료 전)

#### ABTest Module (Session 1)
- [ ] All tests pass (`./gradlew test --tests "foodiepass.server.abtest.*"`)
- [ ] Test coverage ≥80%
- [ ] ABTestService.assignGroup() 50:50 배정 검증
- [ ] MenuScan entity persistence 확인
- [ ] Admin API `/api/admin/ab-test/results` 동작 확인
- [ ] Commit & Push to feature/mvp-abtest
- [ ] **Status Update**: Change to 🟢 Ready

#### Survey Module (Session 2)
- [ ] All tests pass (`./gradlew test --tests "foodiepass.server.survey.*"`)
- [ ] Test coverage ≥80%
- [ ] SurveyService.saveSurveyResponse() 동작 확인
- [ ] Analytics calculation (Yes rate, ratio) 검증
- [ ] Survey API `/api/surveys` 동작 확인
- [ ] FK constraint (scanId → MenuScan) 확인
- [ ] Commit & Push to feature/mvp-survey
- [ ] **Status Update**: Change to 🟢 Ready

#### Menu API Module (Session 3)
- [ ] All tests pass (`./gradlew test`)
- [ ] Test coverage ≥80%
- [ ] Control group: FoodInfo 제거 검증
- [ ] Treatment group: FoodInfo 포함 검증
- [ ] Processing time ≤5초 검증
- [ ] `/api/menus/scan` 엔드포인트 동작 확인
- [ ] Commit & Push to feature/mvp-menu-api
- [ ] **Status Update**: Change to 🟢 Ready

---

### Phase 2: Module Readiness Check

**Before merging, verify each module**:

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# Check ABTest
cd ../foodiePass-abtest/backend
./gradlew clean build
./gradlew test --tests "foodiepass.server.abtest.*"
./gradlew jacocoTestReport
# Check coverage: open build/reports/jacoco/test/html/index.html

# Check Survey
cd ../../foodiePass-survey/backend
./gradlew clean build
./gradlew test --tests "foodiepass.server.survey.*"
./gradlew jacocoTestReport

# Check Menu API
cd ../../foodiePass-menu-api/backend
./gradlew clean build
./gradlew test
./gradlew jacocoTestReport
```

**All checks must pass** before proceeding to Phase 3.

---

### Phase 3: Sequential Merge (순차적 병합)

#### Step 1: Merge ABTest (First)

**Why first?**: Survey와 Menu API가 ABTest에 의존

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass
git checkout develop
git pull origin develop

# Merge ABTest
git merge feature/mvp-abtest --no-ff -m "feat: integrate A/B test system (#ISSUE_NUMBER)

- Add ABTestService with 50:50 random group assignment
- Add MenuScan entity for scan session management
- Add Admin API for A/B test results
- Test coverage: XX%

Related: H2, H3 hypothesis validation"

# Check for conflicts
git status

# If conflicts, resolve them
# Then continue

# Run tests
cd backend
./gradlew clean build
./gradlew test

# If all pass, push
git push origin develop
```

**Verification**:
- [ ] Build successful
- [ ] All existing tests pass
- [ ] ABTest module tests pass
- [ ] No regression in existing features

---

#### Step 2: Merge Survey (Second)

**Why second?**: Survey depends on ABTest (MenuScan FK)

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass
git checkout develop
git pull origin develop

# Merge Survey
git merge feature/mvp-survey --no-ff -m "feat: integrate survey response system (#ISSUE_NUMBER)

- Add SurveyResponse entity with FK to MenuScan
- Add SurveyService with analytics calculation
- Add Survey API for response submission
- Test coverage: XX%

Related: H1, H3 hypothesis validation"

# Check for conflicts
git status

# Run tests
cd backend
./gradlew clean build
./gradlew test

# If all pass, push
git push origin develop
```

**Verification**:
- [ ] Build successful
- [ ] All existing tests pass
- [ ] Survey module tests pass
- [ ] ABTest + Survey integration works (FK constraint)

---

#### Step 3: Merge Menu API (Last)

**Why last?**: Menu API integrates both ABTest and Survey

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass
git checkout develop
git pull origin develop

# Merge Menu API
git merge feature/mvp-menu-api --no-ff -m "feat: integrate menu scan API with A/B test (#ISSUE_NUMBER)

- Add /api/menus/scan endpoint
- Add MenuScanService for pipeline orchestration
- Add conditional response (Control vs Treatment)
- Test coverage: XX%

Related: H1, H2, H3 hypothesis validation"

# Check for conflicts
git status

# Run tests
cd backend
./gradlew clean build
./gradlew test

# If all pass, push
git push origin develop
```

**Verification**:
- [ ] Build successful
- [ ] All tests pass (ABTest + Survey + Menu API)
- [ ] E2E flow works (scan → A/B assign → response)

---

### Phase 4: E2E Integration Testing

**Run full integration test suite**:

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass/backend

# 1. Start application
./gradlew bootRun --args='--spring.profiles.active=local'

# 2. In another terminal, run E2E tests
./gradlew test --tests "*IntegrationTest"

# 3. Manual API testing (see E2E_TEST_SCENARIOS.md)
```

**Critical E2E Scenarios**:
1. [ ] Menu scan → A/B group assignment → conditional response
2. [ ] Survey submission with valid scanId
3. [ ] Survey submission with invalid scanId (should fail)
4. [ ] Admin API: A/B test results
5. [ ] Admin API: Survey analytics
6. [ ] Processing time ≤5초 (load test)

**See**: `E2E_TEST_SCENARIOS.md` for detailed test cases

---

### Phase 5: Performance Validation

**Goal**: Verify H2 technical requirements

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# Run performance tests
node scripts/performance-test.js

# Or use existing scripts
node backend/src/test/performance/menu-scan-load-test.js
```

**Metrics to verify**:
- [ ] OCR accuracy ≥90% (sample 100 menus)
- [ ] Currency accuracy ≥95% (real-time API check)
- [ ] Food matching relevance ≥70% (manual review sample)
- [ ] Processing time ≤5초 (95th percentile)

**See**: `HYPOTHESIS_VALIDATION.md` for full criteria

---

### Phase 6: Database Migration

**Apply schema changes to production-like environment**:

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass/backend

# Run migration scripts
./gradlew flywayMigrate -Pflyway.url=jdbc:mysql://localhost:3306/foodiepass_dev

# Or manually apply
mysql -u root -p foodiepass_dev < scripts/db/V2_0__add_abtest_tables.sql
```

**Verify**:
- [ ] menu_scan table created
- [ ] survey_response table created
- [ ] FK constraints applied
- [ ] Indexes created

**See**: `scripts/db/V2_0__add_abtest_tables.sql`

---

### Phase 7: Cleanup

**Remove worktrees** (after successful merge):

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# Remove worktrees
git worktree remove ../foodiePass-abtest
git worktree remove ../foodiePass-survey
git worktree remove ../foodiePass-menu-api

# Verify
git worktree list

# Optionally delete feature branches (if no longer needed)
git branch -d feature/mvp-abtest
git branch -d feature/mvp-survey
git branch -d feature/mvp-menu-api

# Delete remote branches (if merged)
git push origin --delete feature/mvp-abtest
git push origin --delete feature/mvp-survey
git push origin --delete feature/mvp-menu-api
```

---

## 🚨 Conflict Resolution Strategy

### Potential Conflicts

1. **Build files**: `build.gradle`, `settings.gradle`
   - **Resolution**: Accept both changes, merge dependencies

2. **Application properties**: `application.yml`, `application-local.yml`
   - **Resolution**: Keep all new configurations

3. **Domain package**: `foodiepass.server.*`
   - **Resolution**: No conflicts expected (separate packages)

### Conflict Resolution Steps

```bash
# If conflicts occur
git status  # Check conflicted files

# For each conflict
git diff <file>  # Review changes

# Edit file manually or
git checkout --ours <file>   # Keep current branch
git checkout --theirs <file> # Accept incoming changes

# After resolving
git add <file>
git merge --continue
```

---

## ✅ Integration Success Criteria

### Technical (H2)
- [ ] All unit tests pass (>80% coverage)
- [ ] All integration tests pass
- [ ] Processing time ≤5초 (95th percentile)
- [ ] OCR accuracy ≥90%
- [ ] Currency accuracy ≥95%
- [ ] Food matching ≥70% relevance

### Functional (H1, H3)
- [ ] A/B group assignment works (50:50 ±5%)
- [ ] Control group: No FoodInfo in response
- [ ] Treatment group: Full FoodInfo in response
- [ ] Survey submission works
- [ ] Survey analytics calculation correct

### Quality
- [ ] No regressions in existing features
- [ ] Build successful
- [ ] No critical bugs
- [ ] Documentation updated

---

## 📝 Post-Integration Tasks

- [ ] Update API documentation
- [ ] Update README with new endpoints
- [ ] Create release notes
- [ ] Tag release: `git tag v2.0.0-mvp`
- [ ] Deploy to staging environment
- [ ] Notify team of completion

---

## 🔗 Related Documents

- [E2E_TEST_SCENARIOS.md](./E2E_TEST_SCENARIOS.md) - 통합 테스트 시나리오
- [HYPOTHESIS_VALIDATION.md](./HYPOTHESIS_VALIDATION.md) - 가설 검증 기준
- [MERGE_STRATEGY.md](./MERGE_STRATEGY.md) - 병합 전략 상세
- [DB_MIGRATION.md](./scripts/db/README.md) - DB 마이그레이션 가이드

---

## 🆘 Troubleshooting

### Build fails after merge
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Tests fail after merge
```bash
# Run specific failing test with verbose output
./gradlew test --tests "FailingTestClass" --info

# Check for H2 vs MySQL differences
# Ensure application-local.yml is used for tests
```

### Database issues
```bash
# Reset H2 database
rm -rf backend/data/

# Recreate schema
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 📞 Contact

**Integration Lead**: (Current Session - Session 4)

**Module Owners**:
- ABTest: Session 1 (foodiePass-abtest)
- Survey: Session 2 (foodiePass-survey)
- Menu API: Session 3 (foodiePass-menu-api)

---

**Last Updated**: 2025-11-04 22:30
**Next Update**: After Phase 1 completion
