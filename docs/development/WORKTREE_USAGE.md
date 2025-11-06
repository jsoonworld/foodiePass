# FoodiePass MVP v2 - Worktree 사용 가이드

## 🌳 Worktree 구조

MVP v2 구현을 위해 3개의 독립적인 worktree를 생성했습니다:

```
projects/
├── foodiePass/          # 원본 (develop 브랜치)
├── foodiePass-abtest/   # feature/mvp-abtest
├── foodiePass-survey/   # feature/mvp-survey
└── foodiePass-menu-api/ # feature/mvp-menu-api
```

---

## 🎯 각 Worktree의 역할

### 1. foodiePass-abtest (feature/mvp-abtest)
**목적**: A/B 테스트 시스템 구현

**작업 내용**:
- ABGroup enum, MenuScan entity
- ABTestService (그룹 배정, 스캔 생성)
- MenuScanRepository
- Admin API (결과 조회)

**시작 방법**:
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass-abtest/backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**가이드**: [WORKTREE_GUIDE.md](../foodiePass-abtest/WORKTREE_GUIDE.md)

---

### 2. foodiePass-survey (feature/mvp-survey)
**목적**: 확신도 설문 시스템 구현

**작업 내용**:
- SurveyResponse entity
- SurveyService (응답 저장, 분석)
- SurveyResponseRepository
- Survey API (응답 제출, 분석 조회)

**시작 방법**:
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass-survey/backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**가이드**: [WORKTREE_GUIDE.md](../foodiePass-survey/WORKTREE_GUIDE.md)

---

### 3. foodiePass-menu-api (feature/mvp-menu-api)
**목적**: Menu API와 A/B 테스트 통합

**작업 내용**:
- MenuScanController (새 엔드포인트)
- MenuScanService (파이프라인 조율)
- DTO 레이어 (Request/Response)
- 조건부 응답 (Control vs Treatment)

**시작 방법**:
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass-menu-api/backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**가이드**: [WORKTREE_GUIDE.md](../foodiePass-menu-api/WORKTREE_GUIDE.md)

---

## 🚀 권장 작업 순서

### Phase 1: 독립 모듈 개발 (병렬 가능)
1. **ABTest 모듈** (Day 1-2)
   - `cd foodiePass-abtest/backend`
   - ABTestService, Repository, Admin API 구현
   - 테스트 작성 (>80% coverage)

2. **Survey 모듈** (Day 1-2)
   - `cd foodiePass-survey/backend`
   - SurveyService, Repository, Survey API 구현
   - 테스트 작성 (>80% coverage)

### Phase 2: 통합 (순차적)
3. **Menu API Integration** (Day 3-4)
   - `cd foodiePass-menu-api/backend`
   - ABTest + Survey 모듈 의존성 추가
   - MenuScanController, MenuScanService 구현
   - E2E 통합 테스트

---

## 🔄 Worktree 기본 명령어

### Worktree 목록 확인
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass
git worktree list
```

### 브랜치 상태 확인
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass-abtest
git status
git log --oneline -5
```

### 변경사항 커밋 및 푸시
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass-abtest
git add .
git commit -m "feat(abtest): implement A/B test system"
git push origin feature/mvp-abtest
```

### Worktree 제거 (작업 완료 후)
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass
git worktree remove ../foodiePass-abtest
git branch -d feature/mvp-abtest  # 로컬 브랜치 삭제 (선택)
```

---

## 🧪 테스트 실행

각 worktree에서 독립적으로 테스트 실행:

```bash
# ABTest 모듈 테스트
cd /Users/harperkwon/Desktop/github/projects/foodiePass-abtest/backend
./gradlew test --tests "foodiepass.server.abtest.*"

# Survey 모듈 테스트
cd /Users/harperkwon/Desktop/github/projects/foodiePass-survey/backend
./gradlew test --tests "foodiepass.server.survey.*"

# Menu API 통합 테스트
cd /Users/harperkwon/Desktop/github/projects/foodiePass-menu-api/backend
./gradlew test
```

---

## 📊 Progress Tracking

| Module | Branch | Status | Coverage | Owner |
|--------|--------|--------|----------|-------|
| ABTest | feature/mvp-abtest | ⚪ Not Started | - | TBD |
| Survey | feature/mvp-survey | ⚪ Not Started | - | TBD |
| Menu API | feature/mvp-menu-api | ⚪ Not Started | - | TBD |

**Legend**:
- ⚪ Not Started
- 🟡 In Progress
- 🟢 Completed
- 🔴 Blocked

---

## 🔗 통합 전략

### Step 1: 각 모듈 독립 개발
- 각 worktree에서 독립적으로 구현
- 테스트 커버리지 >80% 확보
- feature 브랜치에 커밋/푸시

### Step 2: develop에 순차적 병합
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# 1. ABTest 먼저 병합
git checkout develop
git merge feature/mvp-abtest --no-ff
git push origin develop

# 2. Survey 병합
git merge feature/mvp-survey --no-ff
git push origin develop

# 3. Menu API 마지막 병합
git merge feature/mvp-menu-api --no-ff
git push origin develop
```

### Step 3: E2E 통합 테스트
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass/backend
./gradlew clean build
./gradlew test
./gradlew bootRun --args='--spring.profiles.active=local'

# API 테스트
curl -X POST http://localhost:8080/api/menus/scan ...
curl -X POST http://localhost:8080/api/surveys ...
curl -X GET http://localhost:8080/api/admin/ab-test/results
```

---

## 🚨 주의사항

### DO
✅ 각 worktree에서 독립적으로 작업
✅ 자주 커밋 (작은 단위)
✅ feature 브랜치에 푸시
✅ 각 가이드(WORKTREE_GUIDE.md) 참조
✅ 테스트 커버리지 >80% 유지

### DO NOT
❌ 다른 worktree의 책임 영역 수정하지 말 것
❌ develop 브랜치에 직접 커밋하지 말 것
❌ worktree 간 파일 복사하지 말 것 (git merge 사용)
❌ 가설 검증과 무관한 기능 추가하지 말 것

---

## 💡 Tips

### Session 분리
- **ABTest 세션**: `cd foodiePass-abtest/backend` → 오직 A/B 테스트만
- **Survey 세션**: `cd foodiePass-survey/backend` → 오직 설문만
- **Menu API 세션**: `cd foodiePass-menu-api/backend` → 통합 작업만

### 컴팩트한 세션 유지
- 한 세션에서는 하나의 worktree만 다루기
- 여러 작업을 동시에 하지 말고, 하나씩 집중
- 작업 완료 후 다음 worktree로 전환

### Conflict 방지
- 각 모듈은 독립적인 패키지 구조
- 겹치는 파일이 거의 없음
- 통합 시점에 conflict 최소화

---

## 📚 Documentation

- [Project CLAUDE.md](../.claude/CLAUDE.md) - 전체 프로젝트 컨텍스트
- [Backend CLAUDE.md](../backend/.claude/CLAUDE.md) - 백엔드 컨텍스트
- [IMPLEMENTATION_PLAN.md](../backend/docs/IMPLEMENTATION_PLAN.md) - 상세 구현 계획
- [Agent Specs](../backend/.claude/agents/) - 각 모듈별 에이전트 스펙

---

## 🤝 Collaboration

각 worktree는 독립적이지만, 최종적으로 하나의 시스템으로 통합됩니다.

**Communication**:
- 인터페이스 변경 시 다른 팀원에게 알리기
- 공통 의존성 변경 시 조율
- 통합 시점 전에 상호 리뷰

**Integration Points**:
- `ABTestService` ↔ `MenuScanController`
- `MenuScan` (ABTest) ← `SurveyResponse` (Survey)
- `MenuService` (기존) ↔ `MenuScanService` (신규)

---

## ✅ Checklist Before Integration

각 모듈 완료 전 체크리스트:

### ABTest Module
- [ ] ABTestService 구현 완료
- [ ] MenuScanRepository 테스트 >80%
- [ ] Admin API 동작 확인
- [ ] 50:50 배정 검증

### Survey Module
- [ ] SurveyService 구현 완료
- [ ] SurveyResponseRepository 테스트 >80%
- [ ] Survey API 동작 확인
- [ ] Analytics 계산 검증

### Menu API
- [ ] MenuScanController 구현 완료
- [ ] Control/Treatment 조건부 응답 검증
- [ ] 처리 시간 ≤5초 검증
- [ ] E2E 통합 테스트 통과

---

## 🎉 Success Criteria

**Technical (H2)**:
- ✅ OCR 정확도 ≥90%
- ✅ 환율 정확도 ≥95%
- ✅ 음식 매칭 연관성 ≥70%
- ✅ 처리 시간 ≤5초

**User (H1, H3)**:
- ✅ Control vs Treatment 그룹 배정 동작
- ✅ 설문 응답 수집 동작
- ✅ 그룹별 Yes 응답률 분석 가능

**Quality**:
- ✅ 전체 테스트 커버리지 >80%
- ✅ 모든 통합 테스트 통과
- ✅ API 문서 업데이트 완료
