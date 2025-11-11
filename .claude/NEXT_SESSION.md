# Next Session: Phase 3-4 Completion

> **세션 시작 시간**: TBD
> **현재 브랜치**: `feature/phase3-mvp-implementation`
> **현재 Phase**: Phase 3-4 - Integration Testing

---

## 📊 Current Status Summary

### ✅ Completed in This Session

#### Phase 3-1: Backend Domain & Service Layer
- [x] ABTest module (ABGroup, MenuScan, ABTestService, Repository)
- [x] Survey module (SurveyResponse, SurveyService, Repository)
- [x] Unit tests passing (ABTestServiceTest, SurveyServiceTest)

#### Phase 3-2: Backend API Layer
- [x] ABTestController, SurveyController
- [x] DTO classes (MenuScanRequest/Response, SurveyRequest/Response)
- [x] GlobalExceptionHandler
- [x] Backend integration tests passing

#### Phase 3-3: Frontend Setup
- [x] React + Vite project initialization
- [x] API client (`lib/api.ts`, axios, 150s timeout)
- [x] TypeScript types (`lib/types.ts`)
- [x] Custom hooks (`useMenuScan.ts`, `useSurvey.ts`)
- [x] Components:
  - MenuUploader, ControlMenu, TreatmentMenu
  - SurveyModal (5초 자동 표시)
  - LanguageSelector, CurrencySelector
  - LoadingSpinner, ErrorMessage, ErrorBoundary
- [x] Pages: HomePage, MenuResultPage (A/B 분기)
- [x] React Router setup
- [x] Environment configuration (`.env.local`)

#### Phase 3-4: Integration Test (In Progress)
- [x] Integration test plan created (`.claude/PHASE3_INTEGRATION_TEST.md`)
- [x] Manual test checklist created (`.claude/PHASE3-4_TEST_CHECKLIST.md`)
- [x] Backend server running (http://localhost:8080)
- [x] Frontend server running (http://localhost:3000)
- [ ] **Manual testing NOT YET executed** ← 다음 세션 작업

### 📝 Recent Commits (Kent Beck Small Commits)

```
266a944 docs(phase3-4): Add comprehensive manual test checklist
b85e611 docs(phase3-4): Add integration test plan and checklist
ad94b46 docs: Add Phase 3 execution plan (token-efficient sessions)
```

---

## 🎯 Next Session Tasks

### Priority 1: Execute Manual Integration Tests

**목표**: Phase 3-4 완료 - 통합 테스트 실행 및 결과 문서화

**작업 순서**:

1. **서버 실행 확인** (5분)
   ```bash
   # Terminal 1: Backend
   cd backend
   export SPOONACULAR_API_KEY="1fe91ac5a2614fe985481f65a21ce6f6"
   ./gradlew bootRun --args='--spring.profiles.active=local'

   # Terminal 2: Frontend
   cd frontend
   npm run dev

   # Terminal 3: Health check
   curl http://localhost:8080/api/language | jq .status
   curl -s http://localhost:3000 | head -1
   ```

2. **테스트 이미지 준비** (10분)
   - 실제 메뉴판 사진 3-5개 준비 (JPG/PNG, <10MB)
   - 일본어, 한국어, 영어 메뉴 다양하게
   - 텍스트가 명확한 이미지 선택

3. **Manual Test 실행** (30-40분)
   - 문서: `.claude/PHASE3-4_TEST_CHECKLIST.md` 참조
   - 브라우저: http://localhost:3000

   **Test 시나리오**:
   - Test 1: Control Group Flow (텍스트 전용)
   - Test 2: Treatment Group Flow (시각적 메뉴)
   - Test 3: A/B Randomization (5회 반복)
   - Test 4: Survey Data Persistence (H2 Console 확인)
   - Test 5: Error Handling (잘못된 파일, 빈 이미지)
   - Test 6: Performance (처리 시간 측정)

4. **테스트 결과 문서화** (15분)
   - 체크리스트에 결과 기록
   - 문제점 및 개선 사항 정리
   - 스크린샷 캡처 (Control UI vs Treatment UI)

5. **Phase 3-4 완료 커밋** (5분)
   ```bash
   # Kent Beck Small Commit
   git add .claude/PHASE3-4_TEST_CHECKLIST.md  # 결과 기록된 체크리스트
   git commit -m "test(phase3-4): Complete manual integration testing

   - Execute all 6 test scenarios (Control/Treatment/AB/Survey/Error/Perf)
   - Validate Control vs Treatment UI differentiation
   - Confirm A/B group randomization working correctly
   - Verify survey submission and data persistence
   - Measure processing time (avg: ____ sec, target: ≤5 sec)
   - Document test results and identified issues

   Phase: Phase 3-4 - Integration Testing Complete
   Status: ☐ All tests passed ☐ Some issues found
   Next: Create Phase 3-4 PR and prepare Phase 3-5"
   ```

6. **Push and Create PR** (10분)
   ```bash
   git push origin feature/phase3-mvp-implementation
   ```

   **PR Title**: `[Phase 3-4] Integration Testing - Frontend-Backend Integration Complete`

   **PR Description Template**:
   ```markdown
   ## Phase 3-4: Integration Testing

   ### 목표
   Frontend-Backend 통합 테스트 및 가설 검증 준비 확인

   ### 완료된 작업
   - ✅ Backend-Frontend 통합 정상 작동
   - ✅ Control Group Flow 검증
   - ✅ Treatment Group Flow 검증
   - ✅ A/B 그룹 배정 랜덤 확인
   - ✅ 설문 제출 및 저장 확인
   - ✅ 에러 핸들링 검증
   - ✅ 성능 검증 (평균 처리 시간: ____ 초)

   ### 테스트 결과
   **Control Group**:
   - 메뉴 아이템 표시: ✅
   - 텍스트 + 환율만 표시: ✅
   - 사진/설명 없음: ✅

   **Treatment Group**:
   - 메뉴 아이템 표시: ✅
   - 텍스트 + 환율 + 사진 + 설명: ✅

   **A/B Randomization**:
   - Control/Treatment 비율: __:__ (5회 테스트)

   **Survey Submission**:
   - 설문 데이터 저장: ✅
   - H2 Console 확인: ✅

   **Performance**:
   - 평균 처리 시간: ____ 초 (목표: ≤5초)
   - 목표 달성: ☐ Yes ☐ No

   ### 문제점 및 개선 사항
   ```
   (여기에 발견된 문제 기록)
   ```

   ### 가설 검증 준비 상태
   - [x] H1 검증 가능: Control vs Treatment UI 차별화 명확
   - [x] H2 검증 가능: 기술 정확도 측정 가능
   - [x] H3 검증 가능: 설문 데이터 수집 가능

   ### Next Steps
   - Phase 3-5: Deployment Preparation
   - Docker containerization
   - Environment variable management
   - Production configuration

   ### Related Documents
   - [Integration Test Plan](.claude/PHASE3_INTEGRATION_TEST.md)
   - [Manual Test Checklist](.claude/PHASE3-4_TEST_CHECKLIST.md)

   ### Checklist
   - [ ] All tests passed
   - [ ] Test results documented
   - [ ] Issues logged (if any)
   - [ ] Ready for Phase 3-5
   ```

---

### Priority 2: Prepare Phase 3-5 Branch

**새로운 브랜치 전략 적용** (Kent Beck 방법론 준수):

```bash
# Phase 3-4 PR 머지 후
git checkout develop
git pull origin develop

# Phase 3-5 브랜치 생성
git checkout -b feature/phase3-5-deployment
```

**Phase 3-5 작업 내용** (다음 세션):
- Docker containerization (backend, frontend)
- Docker Compose setup
- Environment variable management (.env templates)
- Production configuration (application-prod.yml)
- Deployment documentation

---

## 🔧 서버 실행 명령어 (Quick Reference)

### Backend
```bash
cd backend
export SPOONACULAR_API_KEY="1fe91ac5a2614fe985481f65a21ce6f6"
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 background로 실행
cd backend && export SPOONACULAR_API_KEY="1fe91ac5a2614fe985481f65a21ce6f6" && ./gradlew bootRun --args='--spring.profiles.active=local' > /tmp/backend-run.log 2>&1 &
```

### Frontend
```bash
cd frontend
npm run dev

# 또는 background로 실행
cd frontend && npm run dev > /tmp/frontend-run.log 2>&1 &
```

### Health Check
```bash
# Backend
curl http://localhost:8080/api/language | jq .status  # Expected: 200

# Frontend
curl -s http://localhost:3000 | head -1  # Expected: <!doctype html>

# Open browser
open http://localhost:3000
```

---

## 📚 Important Documents

### Phase 3 문서
- **Integration Test Plan**: `.claude/PHASE3_INTEGRATION_TEST.md`
- **Manual Test Checklist**: `.claude/PHASE3-4_TEST_CHECKLIST.md`
- **Phase 3 Execution Plan**: Previous commit `ad94b46`

### 프로젝트 문서
- **1-Pager**: `docs/1-PAGER.md` (핵심 가설 H1, H2, H3)
- **PRD**: `docs/PRD.md` (가설 검증 계획서)
- **Backend API Spec**: `develop:backend/docs/API_SPEC.md`
- **Frontend Component Design**: `frontend/docs/COMPONENT_DESIGN.md`

### Kent Beck 방법론 문서
- **Principles**: `git show 59c32ae:.claude/PRINCIPLES.md`
- **TDD**: Red → Green → Refactor
- **Tidy First**: Structural ≠ Behavioral commits
- **Small Commits**: 각 커밋은 독립적으로 빌드 가능

---

## ⚠️ Known Issues & Considerations

### 1. E2E 자동화 테스트 미완료
- **상태**: Playwright E2E 테스트 스크립트 존재하나 실행 안 함
- **이유**: 테스트 fixture (sample-menu.jpg) 없음
- **해결**: 수동 테스트로 대체 (Phase 3-4), E2E 자동화는 선택 사항

### 2. Phase 3-1, 3-2, 3-3 PR 미생성
- **상태**: 코드는 구현되었으나 개별 PR 없음
- **이유**: 한 브랜치에 모든 작업이 진행됨
- **해결**: Phase 3-5부터 세부 단계별 PR 전략 적용

### 3. 환경변수 관리
- **상태**: `.env.local` 생성됨 (.gitignore에 의해 무시됨)
- **주의**: Spoonacular API 키는 Backend 실행 시 export 필요
- **개선**: Phase 3-5에서 환경변수 관리 체계화

---

## 🎯 Success Criteria for Phase 3-4

Phase 3-4가 완료되려면 다음 조건을 만족해야 합니다:

### 필수 조건
- [x] Backend-Frontend 통합 정상 작동
- [ ] Control Group Flow 정상 작동
- [ ] Treatment Group Flow 정상 작동
- [ ] A/B 그룹 배정 랜덤 확인
- [ ] 설문 제출 및 저장 확인
- [ ] 에러 핸들링 정상 작동
- [ ] 처리 시간 ≤ 5초 (평균)

### 가설 검증 준비
- [ ] H1 검증 가능: Control vs Treatment UI 차별화 확인
- [ ] H2 검증 가능: 기술 정확도 측정 가능
- [ ] H3 검증 가능: 설문 데이터 수집 가능

---

## 📝 Kent Beck Principles Reminder

### 다음 세션에서 엄격히 준수할 원칙:

1. **Small Commits**
   - 각 커밋은 하나의 명확한 목적
   - 커밋 메시지는 "what + why" 포함
   - 모든 커밋은 빌드 가능해야 함

2. **Tidy First**
   - Structural changes (refactor:) ≠ Behavioral changes (feat:, fix:)
   - 절대 섞지 않기
   - Refactor → Test → Feature → Test 순서

3. **TDD Cycle**
   - 🔴 RED: Write failing test
   - 🟢 GREEN: Minimal code to pass
   - 🔵 REFACTOR: Improve structure
   - ✅ VERIFY: All tests green → Commit

4. **Sub-Phase별 PR**
   - Phase 3-5부터 적용
   - 각 PR은 독립적으로 리뷰 가능
   - 작은 PR = 빠른 리뷰 = 빠른 머지

---

## 🚀 Quick Start for Next Session

```bash
# 1. Checkout current branch
git checkout feature/phase3-mvp-implementation
git status

# 2. Start servers (2 terminals)
# Terminal 1: Backend
cd backend && export SPOONACULAR_API_KEY="1fe91ac5a2614fe985481f65a21ce6f6" && ./gradlew bootRun --args='--spring.profiles.active=local'

# Terminal 2: Frontend
cd frontend && npm run dev

# 3. Open test checklist
open .claude/PHASE3-4_TEST_CHECKLIST.md

# 4. Open browser and start testing
open http://localhost:3000

# 5. Execute tests and record results
# (Follow .claude/PHASE3-4_TEST_CHECKLIST.md)

# 6. Commit results
git add .claude/PHASE3-4_TEST_CHECKLIST.md
git commit -m "test(phase3-4): Complete manual integration testing"

# 7. Push and create PR
git push origin feature/phase3-mvp-implementation
# Create PR on GitHub
```

---

## 📞 Questions to Answer in Next Session

1. **처리 시간 목표 달성?**
   - 평균 처리 시간이 5초 이내인가?
   - 실패 시 어떤 부분이 느린가? (OCR? Translation? Food matching?)

2. **A/B 테스트 정상 작동?**
   - Control과 Treatment가 섞여서 나타나는가?
   - 비율이 대략 50:50인가?

3. **UI 차별화 명확?**
   - Control 그룹은 텍스트만 보이는가?
   - Treatment 그룹은 사진+설명이 모두 보이는가?

4. **설문 제출 정상?**
   - 5초 후 모달이 자동으로 표시되는가?
   - 데이터가 H2 데이터베이스에 저장되는가?

5. **에러 핸들링 적절?**
   - 잘못된 파일 업로드 시 사용자 친화적 메시지가 표시되는가?
   - 앱이 크래시되지 않는가?

---

**Next Session 시작 시**: 이 문서를 먼저 읽고 시작하세요!

**Estimated Duration**: 1-1.5 hours

**Goal**: Phase 3-4 Complete → Create PR → Prepare Phase 3-5
