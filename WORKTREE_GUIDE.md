# Worktree: Survey Module Implementation

**Branch**: `feature/mvp-survey`

**Focus**: 확신도 설문 응답 수집 및 분석 시스템 구현

---

## 🎯 Goal

사용자의 "주문 확신도" 설문 응답을 수집하고, Control vs Treatment 그룹별 Yes 응답률을 분석하는 시스템 구축

---

## 📁 Working Directory

**Primary**: `backend/src/main/java/foodiepass/server/survey/`

**Test**: `backend/src/test/java/foodiepass/server/survey/`

---

## 🚀 Tasks

### 1. Domain Layer
- [ ] `SurveyResponse.java` entity
  - `id` (UUID)
  - `scanId` (UUID, FK to MenuScan)
  - `abGroup` (ABGroup enum)
  - `hasConfidence` (Boolean - Yes/No)
  - `createdAt` (LocalDateTime)

### 2. Repository Layer
- [ ] `SurveyResponseRepository.java` (JpaRepository)
  - Custom query: `countByAbGroupAndHasConfidence(ABGroup, Boolean)`

### 3. Service Layer
- [ ] `SurveyService.java`
  - `saveSurveyResponse(UUID scanId, Boolean hasConfidence)`: 응답 저장
  - `getAnalytics()`: 그룹별 Yes/No 응답률 집계
  - `validateScanExists(UUID scanId)`: scanId 유효성 검증

### 4. DTO Layer
- [ ] `dto/request/SurveyRequest.java`
  - `scanId` (UUID)
  - `hasConfidence` (Boolean)
- [ ] `dto/response/SurveyAnalytics.java`
  - Control 그룹: total, yesCount, yesRate
  - Treatment 그룹: total, yesCount, yesRate
  - Ratio: treatment_yes_rate / control_yes_rate

### 5. API Layer
- [ ] `api/SurveyController.java`
  - `POST /api/surveys`: 설문 응답 제출
  - `GET /api/admin/surveys/analytics`: 분석 결과 조회 (Admin용)

### 6. Tests
- [ ] `SurveyResponseRepositoryTest.java`
- [ ] `SurveyServiceTest.java`
- [ ] `SurveyControllerTest.java`

---

## 📋 Acceptance Criteria

### H1, H3 검증을 위한 요구사항:
- ✅ scanId 유효성 검증 (존재하지 않는 scanId 거부)
- ✅ 중복 응답 방지 (1 scan = 1 response)
- ✅ 그룹별 Yes 응답률 계산
- ✅ Treatment/Control 비율 계산
- ✅ >80% 테스트 커버리지

### Success Metrics:
- 응답 저장: <50ms
- 분석 조회: <200ms
- 데이터 무결성: FK 제약조건 유지
- Target (H3 검증): Treatment Yes Rate / Control Yes Rate ≥ 2.0

---

## 🔗 Dependencies

**다른 모듈과의 관계**:
- `MenuScan` (ABTest 모듈): FK 참조
- Menu API에서 설문 링크 제공

**External**:
- Spring Data JPA
- H2/MySQL
- UUID (java.util)
- ABGroup enum (from abtest module)

---

## 🧪 How to Run

```bash
cd backend

# Run tests
./gradlew test --tests "foodiepass.server.survey.*"

# Run app (local profile)
./gradlew bootRun --args='--spring.profiles.active=local'

# Check coverage
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## 📚 Documentation References

- [IMPLEMENTATION_PLAN.md](backend/docs/IMPLEMENTATION_PLAN.md) - Agent 2 섹션 참조
- [DATABASE_SCHEMA.md](backend/docs/DATABASE_SCHEMA.md) - survey_response 테이블 스키마
- [API_SPEC.md](backend/docs/API_SPEC.md) - Survey API 명세
- [Agent 2 Spec](backend/.claude/agents/agent-2-survey-spec.md) - 상세 구현 스펙

---

## 🧮 Analytics Calculation Example

**Given**:
- Control: 100 responses, 30 Yes → 30%
- Treatment: 100 responses, 70 Yes → 70%

**Result**:
```json
{
  "control": {
    "total": 100,
    "yesCount": 30,
    "yesRate": 0.30
  },
  "treatment": {
    "total": 100,
    "yesCount": 70,
    "yesRate": 0.70
  },
  "ratio": 2.33  // 70% / 30% = 2.33
}
```

**Hypothesis Validation**: ratio ≥ 2.0 → H3 성공 ✅

---

## ✅ When You're Done

1. 모든 테스트 통과 확인
2. Coverage ≥80% 확인
3. Commit with message: `feat(survey): implement survey response collection and analytics`
4. Push to `feature/mvp-survey`
5. 다른 worktree의 작업이 끝나면 통합 테스트 진행

---

## 🚨 DO NOT

- ❌ ABTest 모듈 수정하지 말 것 (다른 worktree의 책임)
- ❌ Menu API 수정하지 말 것 (다른 worktree의 책임)
- ❌ develop 브랜치에 직접 커밋하지 말 것
- ❌ 중복 응답 허용하지 말 것 (1 scan = 1 response)

---

## 💡 Tips

- `scanId` FK 제약조건 설정 필수
- 중복 응답 방지: DB unique constraint 또는 service layer validation
- 분석 쿼리 성능: index on (ab_group, has_confidence)
- Yes 응답률 계산: (yesCount / total) * 100
- Ratio 계산 시 division by zero 처리 (control total = 0인 경우)
