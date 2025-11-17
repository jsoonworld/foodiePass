# FoodiePass v2 - Phase Review & Improvement Plan

**작성일**: 2025-11-17
**목적**: Kent Beck의 TDD 원칙에 따라 Phase별 코드 리뷰 및 개선점 식별

---

## Executive Summary

### 🎯 전체 진행 상황
- **Phase 1**: AI 분석 및 전략 수립 ✅ 완료
- **Phase 2**: 상세 설계 ✅ 완료
- **Phase 3-1~3-4**: Backend/Frontend 구현 ✅ 완료
- **Phase 3-5**: E2E 통합 테스트 🔄 진행 중

### 🚨 발견된 주요 문제

#### 1. **백엔드 테스트 실패 (최우선 해결)**
- **심각도**: 🔴 CRITICAL
- **상태**: 229개 테스트 중 **10개 실패**
- **Kent Beck 원칙 위반**: "모든 테스트는 항상 통과해야 한다"

**실패한 테스트**:
1. `ServerApplicationTests` (1개)
2. `CurrencyControllerIntegrationTest` (8개)
3. `GoogleFinanceRateProviderCircuitBreakerTest` (1개)
4. 스킵된 테스트 7개 (통합 테스트)

**영향**:
- 코드 신뢰성 저하
- 배포 불가능
- TDD 사이클 위반

#### 2. **프론트엔드 단위 테스트 부재**
- **심각도**: 🟡 IMPORTANT
- **상태**: E2E 테스트만 존재, 단위 테스트 0개
- **Kent Beck 원칙 위반**: "각 모듈은 독립적인 단위 테스트를 가져야 한다"

**영향**:
- 컴포넌트 신뢰성 검증 불가
- 리팩토링 안전망 부재
- 버그 조기 발견 불가

#### 3. **테스트 커버리지 미확인**
- **심각도**: 🟡 IMPORTANT
- **상태**: Jacoco 리포트 미확인
- **목표**: >80% 커버리지

---

## Phase별 상세 리뷰

### Phase 1: AI 분석 및 전략 수립 ✅

#### 완료된 작업
- ✅ 1-Pager 작성 (가설 H1, H2, H3)
- ✅ PRD 작성 (가설 검증 중심)
- ✅ Architecture 설계
- ✅ Implementation Plan 작성

#### 평가
| 항목 | 켄트 백 원칙 | 평가 | 개선 필요 |
|------|-------------|------|----------|
| 문서화 | - | ✅ Excellent | ❌ |
| 가설 명확성 | - | ✅ Clear | ❌ |
| MVP 범위 정의 | YAGNI | ✅ Well-scoped | ❌ |

#### 개선점
- **없음** - Phase 1은 잘 완료됨

---

### Phase 2: 상세 설계 ✅

#### 완료된 작업
- ✅ API 설계 (API_SPEC.md)
- ✅ Database Schema (DATABASE_SCHEMA.md)
- ✅ Hexagonal Architecture 설계

#### 평가
| 항목 | 켄트 백 원칙 | 평가 | 개선 필요 |
|------|-------------|------|----------|
| API 설계 | Simple Design | ✅ Clear | ❌ |
| DB 스키마 | - | ✅ Normalized | ❌ |
| 아키텍처 | - | ✅ Hexagonal | ❌ |

#### 개선점
- **없음** - Phase 2는 잘 완료됨

---

### Phase 3-1 & 3-2: Backend 구현 ⚠️

#### 완료된 작업
- ✅ ABTest 모듈 (ABGroup, MenuScan, ABTestService)
- ✅ Survey 모듈 (SurveyResponse, SurveyService)
- ✅ MenuScan API
- ✅ 기존 MenuService 통합

#### 평가
| 항목 | 켄트 백 원칙 | 평가 | 개선 필요 |
|------|-------------|------|-----------|
| TDD 준수 | **Test-First** | ⚠️ **Partial** | ✅ **Yes** |
| 테스트 통과 | **All Green** | ❌ **10 Failed** | ✅ **Yes** |
| 코드 품질 | Simple Design | ⚠️ Unknown | ✅ Yes |
| 리팩토링 | Refactoring | ⚠️ Unknown | ✅ Yes |

#### 🔴 Critical Issues

##### Issue 1: 테스트 실패
```
FAILURE: 229 tests, 10 failed, 34 skipped
- ServerApplicationTests: 1 failed
- CurrencyControllerIntegrationTest: 8 failed
- GoogleFinanceRateProviderCircuitBreakerTest: 1 failed
```

**원인 분석 필요**:
1. Currency API 연동 문제?
2. Redis 의존성 문제?
3. 환경 설정 문제?

**해결 방안**:
- [ ] 각 실패 테스트의 에러 메시지 확인
- [ ] Currency 관련 모듈 디버깅
- [ ] 통합 테스트 환경 설정 검증

##### Issue 2: 스킵된 테스트 (34개)
```
Skipped: 34 tests
- GoogleFinanceRateProviderIntegrationTest (7 skipped)
```

**원인**: @Disabled 또는 조건부 실행
**해결 방안**:
- [ ] 스킵 이유 확인
- [ ] 필요 시 활성화 또는 제거

#### 개선점
1. **모든 테스트 수정** (최우선)
2. 테스트 커버리지 >80% 검증
3. Code Smell 제거 (SonarQube)

---

### Phase 3-3 & 3-4: Frontend 구현 ⚠️

#### 완료된 작업
- ✅ MenuUploader 컴포넌트
- ✅ ControlMenu 컴포넌트
- ✅ TreatmentMenu 컴포넌트
- ✅ SurveyModal 컴포넌트
- ✅ LoadingSpinner 컴포넌트
- ✅ API 클라이언트 (api.ts)
- ✅ Custom Hooks (useMenuScan, useSurvey)

#### 평가
| 항목 | 켄트 백 원칙 | 평가 | 개선 필요 |
|------|-------------|------|-----------|
| TDD 준수 | **Test-First** | ❌ **No** | ✅ **Yes** |
| 단위 테스트 | Testing | ❌ **0 tests** | ✅ **Yes** |
| 컴포넌트 구조 | Simple Design | ✅ Clear | ❌ |
| 타입 안정성 | - | ✅ TypeScript | ❌ |

#### 🟡 Important Issues

##### Issue 1: 단위 테스트 부재
**현상**: 프론트엔드 단위 테스트 0개

**문제점**:
- 컴포넌트 로직 검증 불가
- 리팩토링 시 안전망 부재
- 버그 조기 발견 불가

**해결 방안**:
- [ ] Vitest 설정
- [ ] React Testing Library 설정
- [ ] 각 컴포넌트 단위 테스트 작성
  - [ ] MenuUploader.test.tsx
  - [ ] ControlMenu.test.tsx
  - [ ] TreatmentMenu.test.tsx
  - [ ] SurveyModal.test.tsx
  - [ ] LoadingSpinner.test.tsx
- [ ] Custom Hooks 테스트
  - [ ] useMenuScan.test.ts
  - [ ] useSurvey.test.ts

##### Issue 2: E2E 테스트만 존재
**현상**: Playwright E2E 테스트만 있음

**문제점**:
- 테스트 실행 시간 느림
- 테스트 격리 어려움
- 디버깅 어려움

**해결 방안**:
- [ ] 단위 테스트 추가 (70%)
- [ ] 통합 테스트 추가 (20%)
- [ ] E2E 테스트 유지 (10%)

#### 개선점
1. **단위 테스트 작성** (중요)
2. 테스트 피라미드 구축
3. 테스트 커버리지 >80%

---

### Phase 3-5: E2E 통합 테스트 🔄

#### 진행 중 작업
- 🔄 Playwright E2E 테스트 작성
- 🔄 data-testid 속성 추가 (최근 완료)

#### 평가
| 항목 | 켄트 백 원칙 | 평가 | 개선 필요 |
|------|-------------|------|-----------|
| E2E 커버리지 | Testing | ⚠️ Partial | ✅ Yes |
| 테스트 안정성 | - | ⚠️ Unknown | ✅ Yes |

#### 개선점
1. E2E 시나리오 완성
2. 실제 메뉴 이미지로 테스트
3. 성능 검증 (≤ 5초)

---

## Kent Beck 원칙 준수 평가

### ✅ 잘 지켜진 원칙

1. **Simple Design**
   - 컴포넌트 구조가 명확함
   - API 설계가 간결함
   - 도메인 모델이 명확함

2. **YAGNI (You Aren't Gonna Need It)**
   - MVP 범위를 엄격히 준수
   - 불필요한 기능 제외

3. **Incremental Development**
   - Phase별로 단계적 구현
   - 작은 단위의 커밋

### ❌ 개선이 필요한 원칙

1. **Test-First Development** ⚠️
   - 백엔드: 일부 준수, 하지만 10개 테스트 실패
   - 프론트엔드: 단위 테스트 부재

2. **All Tests Must Pass** ❌
   - 백엔드: 229개 중 10개 실패 (4.4% 실패율)
   - 이것은 **가장 심각한 문제**

3. **Refactoring** ⚠️
   - 리팩토링 여부 미확인
   - 코드 중복 검사 필요
   - Code Smell 분석 필요

4. **Continuous Integration** ⚠️
   - CI/CD 파이프라인 미설정
   - 자동화된 테스트 실행 없음

---

## 개선 계획 (우선순위별)

### 🔴 Priority 1: Critical (즉시 해결)

#### 1. 백엔드 테스트 수정
```bash
# 목표: 모든 테스트 통과 (0 failures)
# 기간: 1-2시간
# 담당: Backend 개발자
```

**액션 플랜**:
1. [ ] ServerApplicationTests 실패 원인 분석 및 수정
2. [ ] CurrencyControllerIntegrationTest 8개 실패 원인 분석 및 수정
3. [ ] GoogleFinanceRateProviderCircuitBreakerTest 수정
4. [ ] 모든 테스트 재실행 → 100% 통과 확인
5. [ ] 커밋: `fix: Resolve all failing tests (229 tests, 0 failures)`

**성공 기준**:
```
BUILD SUCCESSFUL
229 tests completed, 0 failed, 0 skipped
```

---

### 🟡 Priority 2: Important (Phase 완료 전 필수)

#### 2. 프론트엔드 단위 테스트 작성
```bash
# 목표: 주요 컴포넌트 단위 테스트 작성
# 기간: 2-3시간
# 담당: Frontend 개발자
```

**액션 플랜**:
1. [ ] Vitest + React Testing Library 설정
2. [ ] 테스트 유틸리티 작성 (test-utils.tsx)
3. [ ] 컴포넌트 단위 테스트 작성:
   - [ ] MenuUploader.test.tsx (업로드 로직)
   - [ ] ControlMenu.test.tsx (텍스트 전용 UI)
   - [ ] TreatmentMenu.test.tsx (사진 포함 UI)
   - [ ] SurveyModal.test.tsx (설문 제출)
4. [ ] Custom Hooks 테스트:
   - [ ] useMenuScan.test.ts
   - [ ] useSurvey.test.ts
5. [ ] 커버리지 >70% 달성
6. [ ] 커밋: `test: Add unit tests for frontend components`

**성공 기준**:
```
Test Files: 7 passed (7)
Tests: 30+ passed (30+)
Coverage: >70%
```

#### 3. 백엔드 테스트 커버리지 검증
```bash
# 목표: 테스트 커버리지 >80% 확인
# 기간: 30분
# 담당: Backend 개발자
```

**액션 플랜**:
1. [ ] Jacoco 리포트 생성
2. [ ] 커버리지 확인:
   - Domain Layer: >90%
   - Service Layer: >85%
   - Controller Layer: >80%
3. [ ] 부족한 영역 테스트 추가
4. [ ] 커밋: `test: Improve test coverage to >80%`

**성공 기준**:
```
Overall Coverage: >80%
Domain Layer: >90%
Service Layer: >85%
Controller Layer: >80%
```

---

### 🟢 Priority 3: Nice-to-Have (시간 여유 시)

#### 4. 코드 품질 개선
```bash
# 목표: Code Smell 제거, 리팩토링
# 기간: 1-2시간
# 담당: Backend/Frontend 개발자
```

**액션 플랜**:
1. [ ] 백엔드 코드 리뷰:
   - [ ] 중복 코드 제거
   - [ ] 복잡도 측정 (Cyclomatic Complexity)
   - [ ] 네이밍 일관성 확인
2. [ ] 프론트엔드 코드 리뷰:
   - [ ] Props drilling 개선
   - [ ] 상태 관리 최적화
   - [ ] 불필요한 re-render 제거
3. [ ] 커밋: `refactor: Improve code quality`

#### 5. 문서화 개선
```bash
# 목표: 코드 문서화 및 주석 추가
# 기간: 1시간
# 담당: Backend/Frontend 개발자
```

**액션 플랜**:
1. [ ] 백엔드 JavaDoc 추가
2. [ ] 프론트엔드 TSDoc 추가
3. [ ] README 업데이트
4. [ ] 커밋: `docs: Add code documentation`

#### 6. CI/CD 설정
```bash
# 목표: GitHub Actions 자동화
# 기간: 1-2시간
# 담당: DevOps
```

**액션 플랜**:
1. [ ] .github/workflows/ci.yml 작성
2. [ ] PR마다 자동 테스트 실행
3. [ ] 테스트 실패 시 Merge 차단
4. [ ] 커밋: `ci: Add GitHub Actions workflow`

---

## 다음 세션 실행 계획

### Session 1: 백엔드 테스트 수정 (최우선)
```bash
# 목표: 모든 테스트 통과
# 기간: 1-2시간
# 브랜치: fix/backend-failing-tests
```

**체크리스트**:
- [ ] `git checkout -b fix/backend-failing-tests`
- [ ] ServerApplicationTests 수정
- [ ] CurrencyControllerIntegrationTest 수정
- [ ] GoogleFinanceRateProviderCircuitBreakerTest 수정
- [ ] `./gradlew test` → 100% 통과
- [ ] Git commit
- [ ] PR 생성

### Session 2: 프론트엔드 단위 테스트 (중요)
```bash
# 목표: 주요 컴포넌트 테스트 작성
# 기간: 2-3시간
# 브랜치: test/frontend-unit-tests
```

**체크리스트**:
- [ ] `git checkout -b test/frontend-unit-tests`
- [ ] Vitest 설정
- [ ] 컴포넌트 테스트 작성
- [ ] Custom Hooks 테스트 작성
- [ ] `npm test` → 통과
- [ ] 커버리지 >70%
- [ ] Git commit
- [ ] PR 생성

### Session 3: 통합 테스트 완성
```bash
# 목표: E2E 플로우 검증
# 기간: 2-3시간
# 브랜치: test/e2e-integration
```

**체크리스트**:
- [ ] 실제 메뉴 이미지로 테스트
- [ ] Control 그룹 플로우 검증
- [ ] Treatment 그룹 플로우 검증
- [ ] 성능 검증 (≤ 5초)
- [ ] Git commit
- [ ] PR 생성

---

## 성공 지표

### Phase 3 완료 기준
- ✅ 백엔드 테스트: **229 tests, 0 failed**
- ✅ 프론트엔드 단위 테스트: **30+ tests, 0 failed**
- ✅ E2E 테스트: **10+ scenarios, 100% pass**
- ✅ 테스트 커버리지: **>80%**
- ✅ 코드 품질: **No critical issues**

### Kent Beck 원칙 준수 목표
- ✅ **Test-First**: 모든 코드가 테스트 먼저
- ✅ **All Green**: 모든 테스트 100% 통과
- ✅ **Simple Design**: 코드 복잡도 낮음
- ✅ **Refactoring**: 중복 코드 제거
- ✅ **CI/CD**: 자동화된 테스트 실행

---

## 결론

### 강점
1. ✅ 명확한 가설 기반 MVP
2. ✅ 잘 설계된 아키텍처 (Hexagonal)
3. ✅ 체계적인 문서화
4. ✅ 작은 단위의 커밋

### 개선 필요
1. ❌ 백엔드 테스트 실패 (최우선 해결)
2. ❌ 프론트엔드 단위 테스트 부재
3. ⚠️ 테스트 커버리지 미확인
4. ⚠️ 코드 품질 검증 필요

### 권장 사항
1. **즉시**: 백엔드 테스트 수정 (Priority 1)
2. **Phase 3 완료 전**: 프론트엔드 단위 테스트 작성 (Priority 2)
3. **Phase 4 시작 전**: 모든 테스트 100% 통과 확인

---

**Last Updated**: 2025-11-17
**Reviewed By**: Claude (AI Pair Programmer)
**Next Review**: Phase 4 시작 전
