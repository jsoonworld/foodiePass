# Next Session Prompt

**다음 세션 시작 시 Claude에게 이 프롬프트를 그대로 전달하세요.**

---

## 🎯 Quick Start

안녕하세요! 이전 세션에서 Phase 3-4 Integration Testing 준비를 완료했습니다.

### 현재 상황
- **브랜치**: `feature/phase3-mvp-implementation`
- **상태**: Phase 3-4 문서화 완료, 수동 테스트 실행 대기
- **서버**: Backend (8080) + Frontend (3000) 실행 필요
- **작업**: 수동 통합 테스트 실행 → 결과 문서화 → PR 생성

### Phase 3 진행 상황
```
✅ Phase 3-1: Backend Domain & Service (develop에 머지됨 - PR #53)
✅ Phase 3-2: Backend API Layer (develop에 머지됨 - PR #54, #55)
✅ Phase 3-3: Frontend Setup (develop에 있음)
📝 Phase 3-4: Integration Testing (문서화 완료, 테스트 실행 필요) ← 현재
⬜ Phase 3-5: Deployment Preparation (다음 단계)
```

### 즉시 실행할 작업 (1-1.5시간)

**Priority 1: 서버 실행 및 수동 통합 테스트**

1. **서버 시작** (2개 터미널 필요):
   ```bash
   # Terminal 1: Backend
   cd backend
   export SPOONACULAR_API_KEY="your-api-key-here"
   ./gradlew bootRun --args='--spring.profiles.active=local'

   # Terminal 2: Frontend
   cd frontend
   npm run dev

   # Terminal 3: Health Check
   curl http://localhost:8080/api/language | jq .status  # Expected: 200
   open http://localhost:3000
   ```

2. **테스트 이미지 준비**:
   - 테스트용 일본어 메뉴: `backend/src/test/resources/images/일본메뉴판.png` 사용 가능
   - 또는 실제 메뉴판 사진 3-5개 준비 (JPG/PNG, <10MB)

3. **수동 테스트 실행** (30-40분):
   - **가이드**: `.claude/PHASE3-4_TEST_CHECKLIST.md` 참조
   - **브라우저**: http://localhost:3000

   **필수 테스트 시나리오**:
   - [ ] Test 1: Control Group Flow (텍스트 전용)
   - [ ] Test 2: Treatment Group Flow (시각적 메뉴)
   - [ ] Test 3: A/B Randomization (5회 반복)
   - [ ] Test 4: Survey Submission (H2 Console 확인)
   - [ ] Test 5: Performance (처리 시간 측정)

4. **결과 문서화**:
   - 체크리스트에 결과 기록 (☐ → ☑)
   - 처리 시간, 메뉴 개수, 평가 기록
   - 문제점 및 개선 사항 메모

**Priority 2: Kent Beck Small Commit**

테스트 완료 후:
```bash
git add .claude/PHASE3-4_TEST_CHECKLIST.md
git commit -m "test(phase3-4): Complete manual integration testing

Test Results:
- Control Group: ✅ Text + price only (photos/descriptions hidden)
- Treatment Group: ✅ Photos + descriptions displayed
- A/B Randomization: ✅ Control __회 / Treatment __회 (5회 테스트)
- Survey Submission: ✅ Data saved in H2 database
- Performance: Avg ____ sec (target: ≤5 sec, actual: ______)

Hypothesis Validation Status:
- H1 (Value): Control vs Treatment UI clearly differentiated
- H2 (Tech): OCR __%, Currency __%, Food matching ___% accuracy
- H3 (Behavior): Survey data collection system working

Phase: Phase 3-4 Complete
Next: Create PR and prepare Phase 3-5"

git push origin feature/phase3-mvp-implementation
```

**Priority 3: PR 생성**

GitHub에서 PR 생성:
- **Title**: `[Phase 3-4] Integration Test Documentation and Manual Testing Results`
- **Base**: `develop`
- **Description**:
  ```markdown
  ## Phase 3-4: Integration Testing

  ### 목표
  Frontend-Backend 통합 테스트 및 가설 검증 준비 확인

  ### 완료된 작업
  - ✅ 통합 테스트 계획서 작성 (PHASE3_INTEGRATION_TEST.md)
  - ✅ 수동 테스트 체크리스트 작성 (PHASE3-4_TEST_CHECKLIST.md)
  - ✅ 다음 세션 핸드오프 문서 작성 (NEXT_SESSION.md)
  - ✅ 테스트용 일본어 메뉴 이미지 추가
  - ✅ 수동 통합 테스트 실행 완료

  ### 테스트 결과
  **Control Group (텍스트 전용)**:
  - 메뉴 표시: ✅
  - 텍스트 + 환율만: ✅
  - 사진/설명 없음: ✅

  **Treatment Group (시각적 메뉴)**:
  - 메뉴 표시: ✅
  - 사진 + 설명 + 텍스트 + 환율: ✅

  **A/B Randomization**: Control __회 / Treatment __회 (5회)
  **Survey**: H2 데이터베이스 저장 확인 ✅
  **Performance**: 평균 ____ 초 (목표: ≤5초)

  ### 가설 검증 준비 상태
  - [x] H1: Control vs Treatment UI 차별화 명확
  - [x] H2: 기술 정확도 측정 가능
  - [x] H3: 설문 데이터 수집 가능

  ### 다음 단계
  - Phase 3-5: Deployment Preparation (별도 브랜치 + PR)

  ### Related Documents
  - [Integration Test Plan](.claude/PHASE3_INTEGRATION_TEST.md)
  - [Test Checklist](.claude/PHASE3-4_TEST_CHECKLIST.md)
  - [Next Session Guide](.claude/NEXT_SESSION.md)
  ```

**Priority 4: Phase 3-5 준비**

PR 머지 후:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/phase3-5-deployment

# Phase 3-5 작업 시작
# - Docker containerization
# - Environment variable management
# - Production configuration
# - Deployment documentation
```

---

## 🎯 성공 기준

Phase 3-4 완료 조건:
- [ ] 수동 테스트 6개 시나리오 모두 실행
- [ ] 체크리스트에 결과 기록 완료
- [ ] Control vs Treatment UI 차이 확인
- [ ] A/B 랜덤 배정 작동 확인
- [ ] 설문 데이터 H2 저장 확인
- [ ] 평균 처리 시간 측정 (목표: ≤5초)
- [ ] 테스트 결과 커밋 (Kent Beck Small Commit)
- [ ] PR 생성 완료

---

## 📚 주요 문서 위치

- **테스트 체크리스트**: `.claude/PHASE3-4_TEST_CHECKLIST.md`
- **통합 테스트 계획**: `.claude/PHASE3_INTEGRATION_TEST.md`
- **다음 세션 가이드**: `.claude/NEXT_SESSION.md`
- **Kent Beck 원칙**: `git show 59c32ae:.claude/PRINCIPLES.md`

---

## ⚠️ 주의사항

1. **Small Commits 원칙 준수**:
   - 테스트 결과 커밋 1개만 생성
   - 명확한 커밋 메시지 작성
   - 모든 테스트 결과 포함

2. **Kent Beck 방법론**:
   - 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ COMMIT
   - Structural ≠ Behavioral (섞지 않기)
   - 각 커밋은 독립적으로 빌드 가능

3. **Phase 3-5부터 새 전략**:
   - 세부 단계마다 별도 브랜치 + PR
   - Small PRs = 빠른 리뷰
   - 명확한 진행 상황 추적

---

## 🚀 Quick Commands

```bash
# 1. 브랜치 확인
git checkout feature/phase3-mvp-implementation
git status

# 2. 최근 커밋 확인
git log --oneline -5

# 3. 서버 시작
cd backend && export SPOONACULAR_API_KEY="your-api-key-here" && ./gradlew bootRun --args='--spring.profiles.active=local' &
cd frontend && npm run dev &

# 4. 테스트 시작
open http://localhost:3000
open .claude/PHASE3-4_TEST_CHECKLIST.md

# 5. 테스트 완료 후 커밋
git add .claude/PHASE3-4_TEST_CHECKLIST.md
git commit -m "test(phase3-4): Complete manual integration testing"
git push origin feature/phase3-mvp-implementation

# 6. PR 생성
gh pr create --base develop --title "[Phase 3-4] Integration Test Documentation and Manual Testing Results"
```

---

**예상 소요 시간**: 1-1.5시간
**작업 목표**: Phase 3-4 완료 + PR 생성 + Phase 3-5 준비

이 프롬프트를 다음 세션 시작 시 Claude에게 전달하면, 바로 작업을 시작할 수 있습니다!
