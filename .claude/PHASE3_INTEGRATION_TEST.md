# Phase 3-4: Integration Test Plan

> **목적**: Phase 3 구현이 완료된 상태에서 Frontend-Backend 통합 테스트 및 가설 검증 준비 확인

---

## 테스트 환경

### Backend Server
- **URL**: http://localhost:8080
- **Profile**: local
- **Database**: H2 in-memory
- **APIs**:
  - Spoonacular API (음식 데이터)
  - Google Cloud Translation API (번역)
  - Google Cloud Vision API (OCR)
  - Currency API (환율)

### Frontend Server
- **URL**: http://localhost:3000
- **Framework**: React + Vite
- **Proxy**: Vite proxy to backend (localhost:8080)

---

## Phase 3-4 통합 테스트 체크리스트

### ✅ Phase 3-1: Backend 도메인 & 서비스 레이어
- [x] ABTest 모듈 구현
  - [x] `ABGroup` enum (CONTROL, TREATMENT)
  - [x] `MenuScan` entity (A/B 그룹 정보 저장)
  - [x] `ABTestService` (그룹 배정 로직)
  - [x] `MenuScanRepository`
- [x] Survey 모듈 구현
  - [x] `SurveyResponse` entity
  - [x] `SurveyService` (설문 제출 로직)
  - [x] `SurveyResponseRepository`
- [x] 단위 테스트 작성 및 통과
  - [x] `ABTestServiceTest`
  - [x] `SurveyServiceTest`

### ✅ Phase 3-2: Backend API 레이어
- [x] API 컨트롤러 구현
  - [x] `ABTestController`
  - [x] `SurveyController`
- [x] DTO 구현
  - [x] `MenuScanRequest`
  - [x] `MenuScanResponse`
  - [x] `SurveyRequest`
  - [x] `SurveyResponse`
- [x] 전역 예외 처리
  - [x] `GlobalExceptionHandler`
- [x] API 통합 테스트
  - [x] All backend tests passing

### ✅ Phase 3-3: Frontend 기본 구조
- [x] React + Vite 프로젝트 초기화
- [x] API 클라이언트 (`lib/api.ts`)
- [x] TypeScript 타입 정의 (`lib/types.ts`)
- [x] Custom Hooks
  - [x] `useMenuScan.ts`
  - [x] `useSurvey.ts`
- [x] 주요 컴포넌트
  - [x] `MenuUploader` - 이미지 업로드 + 선택
  - [x] `ControlMenu` - Control 그룹 UI
  - [x] `TreatmentMenu` - Treatment 그룹 UI
  - [x] `SurveyModal` - 설문 모달 (5초 딜레이)
  - [x] `LanguageSelector`, `CurrencySelector`
- [x] Pages
  - [x] `HomePage` - 업로더 페이지
  - [x] `MenuResultPage` - 결과 페이지 (A/B 분기)
- [x] React Router 설정
- [x] 환경 설정 (`.env.local`)

### 🔲 Phase 3-4: 통합 테스트 (Current)
- [ ] Backend-Frontend 통합 확인
- [ ] End-to-End 사용자 플로우 테스트
- [ ] A/B 테스트 로직 검증
- [ ] 설문 제출 검증
- [ ] 에러 핸들링 검증
- [ ] 성능 검증 (≤ 5초 목표)

---

## 통합 테스트 시나리오

### Test 1: Control Group Flow (텍스트 전용)

**목표**: Control 그룹 사용자가 텍스트 + 환율만 표시되는 UI를 경험

**Steps**:
1. ✅ Frontend 접속 (http://localhost:3000)
2. ✅ 메뉴판 이미지 업로드
   - 형식: JPG, PNG, HEIC
   - 크기: < 10MB
   - 내용: 실제 음식 메뉴 (일본어, 한국어, 영어 등)
3. ✅ 언어 선택: Korean
4. ✅ 화폐 선택: South Korean won (KRW)
5. ✅ "메뉴 스캔하기" 버튼 클릭
6. ⏳ 처리 중... (LoadingSpinner 표시)
7. ✅ 결과 페이지 리다이렉트 (`/menu/{scanId}`)
8. ✅ **Control UI 확인**:
   - [x] 메뉴 아이템 이름 (원본 + 번역)
   - [x] 가격 (원본 + 환율 변환)
   - [ ] 음식 사진 **없음** ❌
   - [ ] 음식 설명 **없음** ❌
9. ⏳ 5초 대기
10. ✅ 설문 모달 자동 표시
    - 질문: "이 정보만으로 확신을 갖고 주문할 수 있습니까?"
    - 버튼: "Yes (확신 있음)" / "No (여전히 불안함)"
11. ✅ Yes 또는 No 클릭
12. ✅ 설문 제출 성공 확인

**Expected Results**:
- `abGroup: "CONTROL"` 반환
- 텍스트 + 환율만 표시
- 사진/설명 없음
- 설문 제출 성공

**Hypothesis Validation**:
- H1 검증 준비: Control 그룹 사용자 경험 확인
- H3 검증 준비: Control 그룹 확신도 측정

---

### Test 2: Treatment Group Flow (시각적 메뉴)

**목표**: Treatment 그룹 사용자가 사진 + 설명 + 텍스트 + 환율을 경험

**Steps**:
1-6. (Control Group과 동일)
7. ✅ 결과 페이지 리다이렉트 (`/menu/{scanId}`)
8. ✅ **Treatment UI 확인**:
   - [x] 메뉴 아이템 이름 (원본 + 번역)
   - [x] 가격 (원본 + 환율 변환)
   - [x] 음식 사진 ✅ (TasteAtlas API)
   - [x] 음식 설명 ✅
9-12. (Control Group과 동일)

**Expected Results**:
- `abGroup: "TREATMENT"` 반환
- 텍스트 + 환율 + 사진 + 설명 표시
- 설문 제출 성공

**Hypothesis Validation**:
- H1 검증 준비: Treatment 그룹 사용자 경험 확인
- H2 검증 준비: 음식 매칭 연관성 확인
- H3 검증 준비: Treatment 그룹 확신도 측정

---

### Test 3: A/B Group Randomization

**목표**: A/B 그룹 배정이 랜덤하게 작동하는지 확인

**Steps**:
1. ✅ "메뉴 스캔하기" 5회 반복
2. ✅ 각 결과에서 `abGroup` 값 확인
3. ✅ Control과 Treatment가 섞여서 나타나는지 확인

**Expected Results**:
- 5회 중 Control과 Treatment가 모두 나타남
- 대략 50:50 비율 (완벽하지 않아도 됨)

**Hypothesis Validation**:
- H3 검증 준비: 무작위 A/B 배정 확인

---

### Test 4: Survey Submission

**목표**: 설문 데이터가 정상적으로 저장되는지 확인

**Steps**:
1. ✅ Test 1 또는 Test 2 완료 후 설문 제출
2. ✅ Backend 로그 확인:
   ```bash
   tail -f /tmp/backend-debug.log | grep -i survey
   ```
3. ✅ Database 확인 (H2 Console):
   - URL: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Query: `SELECT * FROM survey_response;`

**Expected Results**:
- `survey_response` 테이블에 데이터 저장됨
- `scan_id`, `has_confidence`, `submitted_at` 필드 정상

**Hypothesis Validation**:
- H3 검증 준비: 설문 데이터 수집 확인

---

### Test 5: Error Handling

**목표**: 에러 상황에서 적절한 처리가 되는지 확인

**Steps**:
1. ✅ 잘못된 파일 업로드 (TXT 파일)
2. ✅ 빈 이미지 업로드
3. ✅ 네트워크 오류 시뮬레이션 (Backend 중지 후 요청)
4. ✅ API 오류 시뮬레이션 (잘못된 API 키)

**Expected Results**:
- 사용자 친화적인 에러 메시지 표시
- Frontend가 크래시되지 않음
- Retry 옵션 제공

---

### Test 6: Performance

**목표**: 처리 시간이 5초 이내인지 확인 (H2 검증)

**Steps**:
1. ✅ 다양한 메뉴판 이미지 5개 준비
2. ✅ 각각 스캔하여 처리 시간 측정
3. ✅ Frontend에 표시된 `processingTime` 확인

**Expected Results**:
- 평균 처리 시간 ≤ 5초
- 95 percentile ≤ 7초

**Hypothesis Validation**:
- H2 검증: 처리 시간 목표 달성

---

## 테스트 실행 방법

### 1. 서버 시작

```bash
# Terminal 1: Backend
cd backend
export SPOONACULAR_API_KEY="your_key"
./gradlew bootRun --args='--spring.profiles.active=local'

# Terminal 2: Frontend
cd frontend
npm run dev
```

### 2. 브라우저 접속

```
http://localhost:3000
```

### 3. 테스트 이미지 준비

**권장**:
- 실제 식당 메뉴판 사진 (JPG, PNG)
- 텍스트가 명확한 이미지
- 일본어, 한국어, 영어 메뉴 다양하게 준비

**예시**:
- 일본 라멘 가게 메뉴
- 한국 카페 메뉴
- 미국 레스토랑 메뉴

### 4. 테스트 체크리스트 작성

각 테스트 시나리오를 실행하며 체크리스트 작성:
- ✅ 성공
- ❌ 실패 (이유 기록)
- ⚠️ 부분 성공 (개선 필요 사항 기록)

---

## 성공 기준

### Phase 3-4 완료 조건

- [x] Backend-Frontend 통합 정상 작동
- [ ] Control Group Flow 정상 작동
- [ ] Treatment Group Flow 정상 작동
- [ ] A/B 그룹 배정 랜덤 확인
- [ ] 설문 제출 및 저장 확인
- [ ] 에러 핸들링 정상 작동
- [ ] 처리 시간 ≤ 5초 (평균)

### 가설 검증 준비 완료 조건

- [ ] H1 검증 가능: Control vs Treatment UI 차별화 확인
- [ ] H2 검증 가능: 기술 정확도 측정 가능
- [ ] H3 검증 가능: 설문 데이터 수집 가능

---

## 다음 단계: Phase 3-5

Phase 3-4가 완료되면:
- [ ] Phase 3-5: 배포 준비
  - Docker 컨테이너화
  - 환경변수 관리
  - Production 설정
  - 배포 문서 작성

---

## 참고 문서

- [Backend API Spec](develop:backend/docs/API_SPEC.md)
- [Frontend Component Design](frontend/docs/COMPONENT_DESIGN.md)
- [1-Pager](docs/1-PAGER.md) - 핵심 가설
- [PRD](docs/PRD.md) - 가설 검증 계획서
