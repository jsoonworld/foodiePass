# Phase 2: 상세 설계 (Blueprint) 세션별 계획

> **목적**: Phase 2를 4개의 세션으로 나눠서 각 세션마다 구현 가능한 산출물을 생성합니다.

---

## 📋 Phase 2 개요

| 항목 | 내용 |
|---|---|
| **기간** | Day 3-5 (3일) |
| **목표** | 구현 전 상세 설계 완료 (API, DB, UI) |
| **세션 수** | 4개 세션 |
| **산출물** | API 명세, DB 스키마, UI 목업, 검증 계획 |

---

## 🗓️ 세션 구조

```
Phase 2: 상세 설계 (Day 3-5)
├─ Session 2.1: 백엔드 도메인 및 DB 스키마 설계 (Day 3 오전)
├─ Session 2.2: API 엔드포인트 및 DTO 설계 (Day 3 오후)
├─ Session 2.3: 프론트엔드 UI/UX 설계 (Day 4)
└─ Session 2.4: 통합 플로우 및 검증 계획 수립 (Day 5)
```

---

## 📦 Session 2.1: 백엔드 도메인 및 DB 스키마 설계

**소요 시간**: 3-4시간 (Day 3 오전)

**목표**: 백엔드 도메인 모델과 데이터베이스 스키마를 설계하여 구현 준비 완료

### ✅ 체크리스트

#### 2.1.1 도메인 모델 설계
- [ ] ABGroup enum 정의
  - [ ] CONTROL, TREATMENT 값 정의
  - [ ] 각 값의 의미 주석 추가
- [ ] MenuScan entity 설계
  - [ ] 필드 정의 (id, userId, abGroup, imageUrl, 언어/화폐 정보)
  - [ ] 생성 시각 필드 추가
  - [ ] 검증 로직 설계 (필수 필드)
- [ ] SurveyResponse entity 설계
  - [ ] 필드 정의 (id, scanId, abGroup, hasConfidence)
  - [ ] FK 관계 정의 (MenuScan 참조)
  - [ ] 생성 시각 필드 추가

#### 2.1.2 Repository 인터페이스 설계
- [ ] MenuScanRepository 메서드 정의
  - [ ] `save(MenuScan)` - 저장
  - [ ] `findById(UUID)` - 조회
  - [ ] `findFirstByUserIdOrderByCreatedAtDesc(String)` - 최근 스캔 조회
  - [ ] `countByAbGroup(ABGroup)` - 그룹별 개수
- [ ] SurveyResponseRepository 메서드 정의
  - [ ] `save(SurveyResponse)` - 저장
  - [ ] `countByAbGroup(ABGroup)` - 그룹별 개수
  - [ ] `countByAbGroupAndHasConfidence(ABGroup, Boolean)` - 그룹별 Yes/No 개수

#### 2.1.3 DB 스키마 설계
- [ ] `menu_scan` 테이블 DDL 작성
  ```sql
  CREATE TABLE menu_scan (
    id CHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    ab_group VARCHAR(20) NOT NULL,
    image_url VARCHAR(500),
    source_language VARCHAR(50),
    target_language VARCHAR(50) NOT NULL,
    source_currency VARCHAR(10),
    target_currency VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_ab_group (ab_group)
  );
  ```
- [ ] `survey_response` 테이블 DDL 작성
  ```sql
  CREATE TABLE survey_response (
    id CHAR(36) PRIMARY KEY,
    scan_id CHAR(36) NOT NULL,
    ab_group VARCHAR(20) NOT NULL,
    has_confidence BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (scan_id) REFERENCES menu_scan(id),
    INDEX idx_scan_id (scan_id),
    INDEX idx_ab_group (ab_group)
  );
  ```
- [ ] 기존 `menu_item` 테이블 재사용 검토
  - [ ] scanId FK 추가 필요 여부 확인
  - [ ] 스키마 마이그레이션 계획 수립

#### 2.1.4 Service 레이어 설계
- [ ] ABTestService 메서드 정의
  - [ ] `assignGroup(String userId): ABGroup` - 그룹 배정
  - [ ] `createScan(...): MenuScan` - 스캔 생성
  - [ ] `getResults(): ABTestResult` - 결과 조회
- [ ] SurveyService 메서드 정의
  - [ ] `saveSurveyResponse(...): void` - 응답 저장
  - [ ] `getAnalytics(): SurveyAnalytics` - 분석 결과 조회

### 📄 산출물
- [ ] `backend/docs/DATABASE_SCHEMA.md` 생성
- [ ] 도메인 모델 다이어그램 (선택)
- [ ] ERD (Entity Relationship Diagram)

---

## 🔌 Session 2.2: API 엔드포인트 및 DTO 설계

**소요 시간**: 3-4시간 (Day 3 오후)

**목표**: Frontend-Backend API 계약 명세 완료

### ✅ 체크리스트

#### 2.2.1 MenuScan API 설계

**POST /api/menus/scan**
- [ ] Request DTO 설계
  ```json
  {
    "base64EncodedImage": "string",
    "sourceLanguage": "auto",
    "targetLanguage": "ko",
    "sourceCurrency": "JPY",
    "targetCurrency": "KRW"
  }
  ```
- [ ] Response DTO 설계 (Control vs Treatment 분기)
  ```json
  {
    "scanId": "uuid",
    "abGroup": "treatment",
    "items": [
      {
        "id": "uuid",
        "originalName": "Pizza Margherita",
        "translatedName": "마르게리타 피자",
        "originalPrice": 15.00,
        "convertedPrice": 20000,
        "foodImageUrl": "https://..." (Treatment만),
        "foodDescription": "토마토 소스..." (Treatment만),
        "matchConfidence": 0.85 (Treatment만)
      }
    ],
    "processingTime": 4.2
  }
  ```
- [ ] 에러 응답 설계
  - [ ] 400 Bad Request (이미지 없음, 필수 파라미터 누락)
  - [ ] 500 Internal Server Error (파이프라인 실패)
- [ ] 처리 플로우 정의
  1. A/B 그룹 배정 (`ABTestService.assignGroup`)
  2. OCR 실행 (기존 `MenuService`)
  3. 번역 실행 (기존 `LanguageService`)
  4. 음식 매칭 실행 (Treatment만)
  5. 환율 변환 (기존 `CurrencyService`)
  6. MenuScan 저장
  7. 응답 반환

#### 2.2.2 Survey API 설계

**POST /api/surveys**
- [ ] Request DTO 설계
  ```json
  {
    "scanId": "uuid",
    "hasConfidence": true
  }
  ```
- [ ] Response DTO 설계
  ```json
  {
    "success": true,
    "message": "Response recorded"
  }
  ```
- [ ] 검증 로직
  - [ ] scanId 존재 여부 확인
  - [ ] 중복 응답 방지 (같은 scanId에 여러 응답 불가)

#### 2.2.3 Admin API 설계

**GET /api/admin/ab-test/results**
- [ ] Response DTO 설계
  ```json
  {
    "controlCount": 50,
    "treatmentCount": 50,
    "totalScans": 100
  }
  ```

**GET /api/admin/surveys/analytics**
- [ ] Response DTO 설계
  ```json
  {
    "control": {
      "totalResponses": 45,
      "yesCount": 15,
      "noCount": 30,
      "yesRate": 0.33
    },
    "treatment": {
      "totalResponses": 48,
      "yesCount": 35,
      "noCount": 13,
      "yesRate": 0.73
    },
    "ratio": 2.21,
    "hypothesisValidated": true
  }
  ```

#### 2.2.4 DTO 클래스 설계
- [ ] Request DTOs
  - [ ] `MenuScanRequest`
  - [ ] `SurveyRequest`
- [ ] Response DTOs
  - [ ] `MenuScanResponse`
  - [ ] `MenuItemResponse`
  - [ ] `SurveyResponse`
  - [ ] `ABTestResult`
  - [ ] `SurveyAnalytics`
  - [ ] `GroupAnalytics`

### 📄 산출물
- [ ] `backend/docs/API_SPEC.md` 생성 또는 업데이트
- [ ] Postman Collection (선택)

---

## 🎨 Session 2.3: 프론트엔드 UI/UX 설계

**소요 시간**: 6-8시간 (Day 4 전체)

**목표**: Control vs Treatment UI 와이어프레임 및 컴포넌트 구조 설계

### ✅ 체크리스트

#### 2.3.1 사용자 플로우 설계
- [ ] 전체 플로우 다이어그램 작성
  ```
  [시작] → [업로드] → [처리 중] → [메뉴 표시] → [설문] → [종료]
  ```
- [ ] Control 그룹 플로우
- [ ] Treatment 그룹 플로우
- [ ] 에러 처리 플로우

#### 2.3.2 화면 설계

**Screen 1: 업로드 화면**
- [ ] 와이어프레임 작성
  - [ ] 이미지 업로드 영역 (드래그 앤 드롭)
  - [ ] 언어 선택 드롭다운 (소스/타겟)
  - [ ] 화폐 선택 드롭다운 (소스/타겟)
  - [ ] "분석 시작" 버튼
- [ ] UI 요구사항
  - [ ] 이미지 미리보기
  - [ ] 파일 크기 제한 안내 (최대 5MB)
  - [ ] 지원 포맷 안내 (JPG, PNG, HEIC)

**Screen 2: 로딩 화면**
- [ ] 와이어프레임 작성
  - [ ] 로딩 스피너
  - [ ] 진행 상태 메시지 ("메뉴판 분석 중...", "번역 중...", "음식 정보 찾는 중...")
- [ ] 예상 소요 시간 표시 (약 5초)

**Screen 3: 메뉴 표시 화면 (Control 그룹)**
- [ ] 와이어프레임 작성
  - [ ] 메뉴 리스트 (텍스트 전용)
  - [ ] 각 항목: 메뉴명 (원어 + 번역) + 가격 (현지 + 변환)
  - [ ] 간결한 레이아웃
- [ ] UI 요구사항
  - [ ] 모바일 최적화
  - [ ] 읽기 쉬운 폰트 크기

**Screen 4: 메뉴 표시 화면 (Treatment 그룹)**
- [ ] 와이어프레임 작성
  - [ ] 메뉴 카드 그리드
  - [ ] 각 카드: 음식 사진 + 메뉴명 + 번역 + 설명 + 가격
  - [ ] 시각적으로 풍부한 레이아웃
- [ ] UI 요구사항
  - [ ] 카드 디자인 (그림자, 라운드 코너)
  - [ ] 이미지 로딩 실패 시 placeholder
  - [ ] 반응형 그리드 (모바일: 1열, 태블릿: 2열, 데스크톱: 3열)

**Screen 5: 설문 화면**
- [ ] 와이어프레임 작성
  - [ ] 질문: "이 정보만으로, 실패에 대한 걱정 없이 확신을 갖고 메뉴를 주문할 수 있습니까?"
  - [ ] Yes/No 버튼 (크게, 명확하게)
  - [ ] 제출 후 감사 메시지
- [ ] UI 요구사항
  - [ ] 버튼 크기 충분 (터치하기 쉽게)
  - [ ] 명확한 시각적 피드백

#### 2.3.3 컴포넌트 구조 설계
- [ ] 컴포넌트 트리 작성
  ```
  App
  ├─ UploadPage
  │  ├─ ImageUploader
  │  ├─ LanguageSelector
  │  └─ CurrencySelector
  ├─ LoadingPage
  │  └─ ProgressIndicator
  ├─ MenuPage
  │  ├─ MenuList (Control)
  │  │  └─ MenuItemText
  │  └─ MenuGrid (Treatment)
  │     └─ MenuItemCard
  └─ SurveyPage
     └─ SurveyForm
  ```
- [ ] 공통 컴포넌트 정의
  - [ ] Button
  - [ ] Dropdown
  - [ ] Card
  - [ ] Spinner

#### 2.3.4 상태 관리 설계
- [ ] 상태 정의
  - [ ] `uploadState`: {image, sourceLanguage, targetLanguage, sourceCurrency, targetCurrency}
  - [ ] `menuState`: {scanId, abGroup, items, isLoading, error}
  - [ ] `surveyState`: {submitted, hasConfidence}
- [ ] API 호출 로직
  - [ ] `POST /api/menus/scan` 호출
  - [ ] `POST /api/surveys` 호출
  - [ ] 에러 핸들링

#### 2.3.5 스타일링 가이드
- [ ] 색상 팔레트 정의
  - [ ] Primary: #3B82F6 (파란색)
  - [ ] Secondary: #10B981 (초록색)
  - [ ] Error: #EF4444 (빨간색)
  - [ ] Background: #F9FAFB
- [ ] 타이포그래피
  - [ ] 제목: 24px, 700
  - [ ] 본문: 16px, 400
  - [ ] 작은 텍스트: 14px, 400
- [ ] 간격
  - [ ] 기본 마진: 16px
  - [ ] 큰 마진: 32px

### 📄 산출물
- [ ] `frontend/docs/UI_REQUIREMENTS.md` 생성
- [ ] `frontend/docs/USER_FLOW.md` 생성
- [ ] `frontend/docs/COMPONENT_DESIGN.md` 생성
- [ ] 와이어프레임 이미지 (Figma, Excalidraw 등)

---

## 🔗 Session 2.4: 통합 플로우 및 검증 계획 수립

**소요 시간**: 4-6시간 (Day 5)

**목표**: 전체 시스템 통합 검증 계획 수립 및 위험 요소 점검

### ✅ 체크리스트

#### 2.4.1 E2E 플로우 검증
- [ ] Control 그룹 시나리오 작성
  ```
  1. 사용자가 메뉴판 이미지 업로드
  2. 언어/화폐 선택 (ja → ko, JPY → KRW)
  3. 분석 시작
  4. A/B 그룹 배정 (Control)
  5. 메뉴 표시 (텍스트 + 환율만)
  6. 설문 응답 (No 예상)
  7. 응답 저장 확인
  ```
- [ ] Treatment 그룹 시나리오 작성
  ```
  1. 사용자가 메뉴판 이미지 업로드
  2. 언어/화폐 선택 (ja → ko, JPY → KRW)
  3. 분석 시작
  4. A/B 그룹 배정 (Treatment)
  5. 메뉴 표시 (사진 + 설명 + 텍스트 + 환율)
  6. 설문 응답 (Yes 예상)
  7. 응답 저장 확인
  ```
- [ ] 에러 시나리오 작성
  - [ ] 이미지 업로드 실패
  - [ ] OCR 실패
  - [ ] 음식 매칭 실패
  - [ ] API 타임아웃

#### 2.4.2 성능 검증 계획
- [ ] 처리 시간 측정 포인트 정의
  - [ ] OCR 시간
  - [ ] 번역 시간
  - [ ] 음식 매칭 시간
  - [ ] 환율 변환 시간
  - [ ] 전체 파이프라인 시간
- [ ] 성능 목표 재확인
  - [ ] 전체 처리 시간 ≤ 5초
  - [ ] OCR 정확도 ≥ 90%
  - [ ] 음식 매칭 연관성 ≥ 70%
  - [ ] 환율 정확도 ≥ 95%

#### 2.4.3 A/B 테스트 검증 계획
- [ ] 그룹 배정 로직 검증
  - [ ] 50:50 비율 확인 (1000명 시뮬레이션)
  - [ ] 기존 사용자 그룹 유지 확인
- [ ] 조건부 응답 검증
  - [ ] Control 그룹에 FoodInfo 없음 확인
  - [ ] Treatment 그룹에 FoodInfo 포함 확인
- [ ] 설문 데이터 수집 검증
  - [ ] scanId와 응답 연결 확인
  - [ ] 중복 응답 방지 확인

#### 2.4.4 가설 검증 계획 재확인
- [ ] H2 (기술 가설) 검증 방법
  - [ ] 내부 테스트 데이터 100개 준비
  - [ ] 자동화 검증 스크립트 작성 계획
  - [ ] Ground Truth 라벨링 방법
- [ ] H1, H3 (가치/행동 가설) 검증 방법
  - [ ] 참가자 모집 계획 (20명)
  - [ ] 테스트 환경 준비 (동일한 메뉴판)
  - [ ] 데이터 수집 및 분석 방법

#### 2.4.5 위험 요소 체크
- [ ] 기술 위험
  - [ ] OCR 정확도 목표 미달 시 대응 계획
  - [ ] 음식 매칭 연관성 목표 미달 시 대응 계획
  - [ ] 처리 시간 초과 시 최적화 계획
- [ ] 사용자 위험
  - [ ] 샘플 크기 부족 시 대응 계획
  - [ ] 설문 응답률 저조 시 대응 계획
- [ ] 운영 위험
  - [ ] API 장애 시 Fallback 계획
  - [ ] 배포 실패 시 롤백 계획

#### 2.4.6 Phase 3 진입 준비
- [ ] 구현 우선순위 확정
  1. ABTest 모듈 (Step 1-5)
  2. Survey 모듈 (Step 6-9)
  3. MenuScan API (Step 10-12)
- [ ] 개발 환경 준비
  - [ ] 로컬 DB 설정
  - [ ] Redis 설정
  - [ ] API 키 확인 (OCR, Translation, TasteAtlas, Currency)
- [ ] TDD 사이클 재확인
  - [ ] 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ VERIFY

### 📄 산출물
- [ ] `docs/INTEGRATION_TEST_PLAN.md` 생성
- [ ] `docs/RISK_MITIGATION.md` 생성 또는 업데이트
- [ ] Phase 3 킥오프 준비 완료

---

## ✅ Phase 2 완료 체크리스트

### Session 2.1 완료
- [ ] 도메인 모델 설계 완료
- [ ] DB 스키마 DDL 작성 완료
- [ ] Repository 인터페이스 정의 완료
- [ ] `backend/docs/DATABASE_SCHEMA.md` 생성

### Session 2.2 완료
- [ ] API 엔드포인트 명세 완료
- [ ] Request/Response DTO 설계 완료
- [ ] 에러 처리 정의 완료
- [ ] `backend/docs/API_SPEC.md` 생성

### Session 2.3 완료
- [ ] Control/Treatment UI 와이어프레임 완료
- [ ] 컴포넌트 구조 설계 완료
- [ ] 상태 관리 설계 완료
- [ ] `frontend/docs/UI_REQUIREMENTS.md` 생성
- [ ] `frontend/docs/USER_FLOW.md` 생성
- [ ] `frontend/docs/COMPONENT_DESIGN.md` 생성

### Session 2.4 완료
- [ ] E2E 시나리오 작성 완료
- [ ] 성능 검증 계획 수립 완료
- [ ] 위험 요소 체크 완료
- [ ] Phase 3 진입 준비 완료
- [ ] `docs/INTEGRATION_TEST_PLAN.md` 생성

### 전체 Phase 2 산출물
- [ ] 7개 문서 생성 완료
- [ ] 백엔드 설계 검토 완료
- [ ] 프론트엔드 설계 검토 완료
- [ ] 통합 검증 계획 수립 완료
- [ ] Git commit: `docs: Complete Phase 2 detailed design`

---

## 🎯 Phase 2 성공 기준

1. **완전성**: 모든 API, DB, UI 요구사항이 문서화되었는가?
2. **명확성**: 개발자가 문서만 보고 구현을 시작할 수 있는가?
3. **검증 가능성**: 각 가설을 어떻게 검증할지 명확한가?
4. **위험 관리**: 주요 위험 요소를 파악하고 대응 계획이 있는가?

---

## 🚀 Phase 3 시작 준비

Phase 2 완료 후 다음과 같이 Phase 3로 진입합니다:

1. **킥오프 미팅**: Phase 2 산출물 리뷰
2. **개발 환경 준비**: DB, Redis, API 키 설정
3. **TDD 사이클 시작**: `backend/docs/IMPLEMENTATION_PLAN.md` 참조
4. **첫 Step 착수**: Step 1 (ABGroup enum 구현)

---

## 📝 Notes

- 각 세션은 독립적으로 진행 가능
- 세션 간 순서는 권장 사항 (병렬 진행 가능)
- Phase 2는 코드 작성 없음 (설계만)
- 모든 산출물은 Markdown 문서로 작성
- Git에 commit하여 버전 관리
