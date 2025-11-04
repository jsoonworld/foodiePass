# Worktree: ABTest Module Implementation

**Branch**: `feature/mvp-abtest`

**Focus**: A/B 테스트 시스템 구현 (Control vs Treatment 그룹 배정)

---

## 🎯 Goal

사용자를 랜덤하게 Control/Treatment 그룹으로 배정하고, 메뉴 스캔 세션을 관리하는 A/B 테스트 시스템 구축

---

## 📁 Working Directory

**Primary**: `backend/src/main/java/foodiepass/server/abtest/`

**Test**: `backend/src/test/java/foodiepass/server/abtest/`

---

## 🚀 Tasks

### 1. Domain Layer
- [x] `ABGroup.java` enum (이미 존재 - 확인 필요)
- [x] `MenuScan.java` entity (이미 존재 - 확인 필요)

### 2. Repository Layer
- [ ] `MenuScanRepository.java` (JpaRepository)

### 3. Service Layer
- [ ] `ABTestService.java`
  - `assignGroup(String userId)`: 랜덤 50:50 배정
  - `createScan(...)`: MenuScan 생성
  - `getScanById(UUID scanId)`: 스캔 조회
  - `getResults()`: A/B 테스트 결과 집계

### 4. DTO Layer
- [ ] `dto/request/MenuScanRequest.java`
- [ ] `dto/response/MenuScanResponse.java`
- [ ] `dto/response/ABTestResult.java`

### 5. API Layer (Admin용)
- [ ] `api/ABTestController.java`
  - `GET /api/admin/ab-test/results`: 테스트 결과 조회

### 6. Tests
- [ ] `MenuScanRepositoryTest.java`
- [ ] `ABTestServiceTest.java`
- [ ] `ABTestControllerTest.java`

---

## 📋 Acceptance Criteria

### H2 검증을 위한 기술 요구사항:
- ✅ 50:50 랜덤 그룹 배정 (±5% 허용)
- ✅ MenuScan 엔티티 영속성 (UUID, userId, abGroup, timestamps)
- ✅ 그룹별 스캔 수 집계 기능
- ✅ >80% 테스트 커버리지

### Success Metrics:
- 배정 비율: Control 50% (±5%), Treatment 50% (±5%)
- 응답 시간: <100ms (배정 + 생성)
- 데이터 무결성: scanId 유니크, FK 제약조건 유지

---

## 🔗 Dependencies

**다른 모듈과의 관계**:
- `MenuScan`은 Survey 모듈에서 FK로 참조됨
- Menu API에서 `ABTestService.assignGroup()`과 `createScan()` 호출

**External**:
- Spring Data JPA
- H2/MySQL
- UUID (java.util)

---

## 🧪 How to Run

```bash
cd backend

# Run tests
./gradlew test --tests "foodiepass.server.abtest.*"

# Run app (local profile)
./gradlew bootRun --args='--spring.profiles.active=local'

# Check coverage
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## 📚 Documentation References

- [IMPLEMENTATION_PLAN.md](backend/docs/IMPLEMENTATION_PLAN.md) - Agent 1 섹션 참조
- [DATABASE_SCHEMA.md](backend/docs/DATABASE_SCHEMA.md) - menu_scan 테이블 스키마
- [API_SPEC.md](backend/docs/API_SPEC.md) - Admin API 명세
- [Agent 1 Spec](backend/.claude/agents/agent-1-abtest-spec.md) - 상세 구현 스펙

---

## ✅ When You're Done

1. 모든 테스트 통과 확인
2. Coverage ≥80% 확인
3. Commit with message: `feat(abtest): implement A/B test system`
4. Push to `feature/mvp-abtest`
5. 다른 worktree의 작업이 끝나면 통합 테스트 진행

---

## 🚨 DO NOT

- ❌ Menu API 수정하지 말 것 (다른 worktree의 책임)
- ❌ Survey 모듈 건드리지 말 것
- ❌ develop 브랜치에 직접 커밋하지 말 것
- ❌ 가설 검증과 무관한 기능 추가하지 말 것

---

## 💡 Tips

- 기존 도메인 객체(ABGroup, MenuScan) 먼저 확인
- Repository → Service → Controller 순서로 구현 (TDD)
- 각 레이어 완성 후 즉시 테스트 작성
- 50:50 배정은 `Random` 또는 `UUID.hashCode() % 2` 활용
