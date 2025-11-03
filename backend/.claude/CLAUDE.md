# FoodiePass Backend - Context for Claude

## Current Task: MVP v2 Backend Implementation

**Phase**: Phase 2 (상세 설계 완료) → Phase 3 (구현 시작 준비)

**Goal**: 기존 v1 코드를 최대한 재사용하면서, A/B 테스트와 설문 기능만 추가하여 가설 검증을 위한 MVP 구축

---

## Quick Links

### 프로젝트 전체 문서
- [Project CLAUDE.md](../../.claude/CLAUDE.md) - 전체 프로젝트 컨텍스트
- [1-Pager](../../docs/1-PAGER.md) - 가설 및 MVP 실험 설계
- [PRD](../../docs/PRD.md) - 가설 검증 요구사항 명세

### 백엔드 전용 문서
- [HYPOTHESES.md](../docs/HYPOTHESES.md) - 백엔드 개발을 위한 핵심 가설 요약
- [ARCHITECTURE.md](../docs/ARCHITECTURE.md) - 백엔드 아키텍처 및 기술 스택
- [API_SPEC.md](../docs/API_SPEC.md) - REST API 엔드포인트 상세 명세
- [DATABASE_SCHEMA.md](../docs/DATABASE_SCHEMA.md) - MySQL 데이터베이스 스키마
- [CODE_REUSE_GUIDE.md](../docs/CODE_REUSE_GUIDE.md) - v1 코드 재사용 가이드
- [IMPLEMENTATION_PLAN.md](../docs/IMPLEMENTATION_PLAN.md) - 백엔드 구현 단계별 계획

---

## Core Hypotheses (Reminder)

### H1: 핵심 가치 가설
> "여행객은 [텍스트 번역만]으로는 여전히 불안하지만, [사진/설명/환율]이 포함된 시각적 메뉴가 제공될 경우 '주문 확신'을 갖게 된다."

### H2: 기술 실현 가설
> "우리는 OCR, 환율 API, 음식 매칭 시스템을 통해 70% 이상 연관성의 사진/설명과 95% 이상 정확도의 환율을 제공할 수 있다."

### H3: 사용자 행동/인지 가설
> "[시각적 메뉴] 사용 집단은 [텍스트 번역만] 집단 대비 '확신도'가 2배 이상 높다."

---

## Backend Architecture Overview

### Tech Stack
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Build**: Gradle 8.x
- **Database**: H2 (dev), MySQL (prod)
- **Cache**: Redis (세션, 환율)
- **Reactive**: Project Reactor (WebFlux)

### External APIs
- **Google Vertex AI**: OCR (메뉴판 텍스트 추출)
- **Google Translation**: 번역
- **TasteAtlas**: 음식 이미지/설명 매칭
- **Currency API**: 실시간 환율

---

## Code Structure (Existing v1)

### ✅ 재사용 가능한 모듈

#### 1. Menu Module (`menu/`)
**역할**: 메뉴판 OCR, 번역, 음식 정보 스크래핑, 환율 변환 파이프라인

**핵심 클래스**:
- `MenuService`: 파이프라인 조율 (OCR → Enrichment)
- `MenuItemEnricher`: 번역 + 음식 스크래핑 + 환율 변환 통합
- `MenuItem`: 도메인 객체 (name, price, foodInfo)
- `FoodInfo`: 음식 정보 (name, description, image, previewImage)

**Ports (인터페이스)**:
- `OcrReader`: OCR 텍스트 추출 (Gemini 구현체)
- `FoodScrapper`: 음식 정보 스크래핑 (TasteAtlas 구현체)
- `TranslationClient`: 번역 (Google Translation 구현체)

**파일 위치**:
```
menu/
├── application/
│   ├── MenuService.java                    # 파이프라인 조율
│   ├── MenuItemEnricher.java               # 번역+스크래핑+환율 통합
│   └── port/
│       └── out/
│           ├── OcrReader.java              # OCR 포트
│           ├── FoodScrapper.java           # 스크래핑 포트
│           └── TranslationClient.java      # 번역 포트
├── domain/
│   ├── MenuItem.java                        # 메뉴 아이템 도메인
│   └── FoodInfo.java                        # 음식 정보 도메인
├── infra/                                   # 구현체들
└── api/
    └── MenuController.java                  # REST API
```

**v2 변경사항**:
- MenuController에 새 엔드포인트 추가: `POST /api/menus/scan`
- A/B 그룹에 따라 FoodInfo 포함 여부 결정

---

#### 2. Language Module (`language/`)
**역할**: 언어 목록 제공 및 번역

**핵심 클래스**:
- `LanguageService`: 지원 언어 목록 반환
- `Language` enum: 지원 언어 정의

**v2 변경사항**: 없음 (그대로 재사용)

---

#### 3. Currency Module (`currency/`)
**역할**: 환율 변환 및 포맷팅

**핵심 클래스**:
- `CurrencyService`: 환율 변환 로직
- `ExchangeRateCache`: 환율 캐싱
- `Currency` enum: 지원 통화 정의

**v2 변경사항**: 없음 (그대로 재사용)

---

### ⚠️ MVP 제외 모듈

#### 4. Order Module (`order/`)
**이유**: H1, H2, H3 검증에 불필요 (장바구니는 MVP 범위 밖)

#### 5. Script Module (`script/`)
**이유**: 주문서 생성은 MVP 범위 밖

---

## New Modules for v2

### ➕ 1. ABTest Module (`abtest/`)
**역할**: A/B 테스트 그룹 배정 및 관리

**필요한 클래스**:
```java
// Enum
public enum ABGroup {
    CONTROL,    // 텍스트 + 환율만
    TREATMENT   // 사진 + 설명 + 텍스트 + 환율
}

// Domain
@Entity
public class MenuScan {
    @Id
    private UUID id;
    private String userId;      // 세션 ID
    private ABGroup abGroup;
    private String imageUrl;
    private String sourceLanguage;
    private String targetLanguage;
    private String sourceCurrency;
    private String targetCurrency;
    private LocalDateTime createdAt;
}

// Service
@Service
public class ABTestService {
    public ABGroup assignGroup(String userId);
    public MenuScan createScan(String userId, ABGroup group, ...);
    public ABTestResult getResults();  // 관리자용
}

// Controller
@RestController
@RequestMapping("/api/admin/ab-test")
public class ABTestController {
    @GetMapping("/results")
    public ABTestResult getResults();
}
```

**패키지 구조**:
```
abtest/
├── domain/
│   ├── ABGroup.java (enum)
│   └── MenuScan.java
├── application/
│   └── ABTestService.java
├── api/
│   └── ABTestController.java
└── repository/
    └── MenuScanRepository.java
```

---

### ➕ 2. Survey Module (`survey/`)
**역할**: 확신도 설문 응답 수집 및 분석

**필요한 클래스**:
```java
// Domain
@Entity
public class SurveyResponse {
    @Id
    private UUID id;
    private UUID scanId;        // FK to MenuScan
    private ABGroup abGroup;
    private Boolean hasConfidence;  // true = Yes, false = No
    private LocalDateTime createdAt;
}

// Service
@Service
public class SurveyService {
    public void saveSurveyResponse(UUID scanId, Boolean hasConfidence);
    public SurveyAnalytics getAnalytics();  // 그룹별 Yes 응답률
}

// Controller
@RestController
@RequestMapping("/api/surveys")
public class SurveyController {
    @PostMapping
    public void submitSurvey(@RequestBody SurveyRequest request);
}
```

**패키지 구조**:
```
survey/
├── domain/
│   └── SurveyResponse.java
├── application/
│   └── SurveyService.java
├── api/
│   └── SurveyController.java
├── dto/
│   ├── request/
│   │   └── SurveyRequest.java
│   └── response/
│       └── SurveyAnalytics.java
└── repository/
    └── SurveyResponseRepository.java
```

---

## Key API Changes for v2

### ✅ New Endpoint: POST /api/menus/scan

**Purpose**: 메뉴판 업로드 → A/B 그룹 배정 → 파이프라인 실행 → 결과 반환

**Request**:
```json
{
  "image": "base64 encoded string",
  "sourceLanguage": "auto",
  "targetLanguage": "ko",
  "sourceCurrency": "USD",
  "targetCurrency": "KRW"
}
```

**Response**:
```json
{
  "scanId": "uuid",
  "abGroup": "TREATMENT",  // or "CONTROL"
  "items": [
    {
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "description": "토마토 소스, 모짜렐라 치즈, 바질",  // Treatment만
      "imageUrl": "https://...",                      // Treatment만
      "priceInfo": {
        "original": "$15.00",
        "converted": "₩20,000"
      }
    }
  ],
  "processingTime": 4.2
}
```

**처리 과정**:
1. 세션 ID 생성 또는 가져오기
2. A/B 그룹 배정 (랜덤 50:50)
3. MenuScan 레코드 생성
4. OCR 실행 (기존 MenuService.reconfigure 재사용)
5. **조건부 처리**:
   - **CONTROL**: FoodInfo 필드 제거 (translatedName, priceInfo만)
   - **TREATMENT**: 전체 FoodInfo 포함
6. 결과 반환

**구현 위치**: `MenuController` 또는 새 `MenuScanController`

---

### ✅ New Endpoint: POST /api/surveys

**Purpose**: 확신도 설문 응답 수집

**Request**:
```json
{
  "scanId": "uuid",
  "hasConfidence": true
}
```

**Response**:
```json
{
  "success": true,
  "message": "Response recorded"
}
```

**구현 위치**: `SurveyController`

---

## Database Schema Changes

### New Tables

#### 1. menu_scan
```sql
CREATE TABLE menu_scan (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    ab_group VARCHAR(20) NOT NULL,  -- 'CONTROL' or 'TREATMENT'
    image_url VARCHAR(500),
    source_language VARCHAR(50),
    target_language VARCHAR(50),
    source_currency VARCHAR(10),
    target_currency VARCHAR(10),
    created_at TIMESTAMP NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_ab_group (ab_group)
);
```

#### 2. survey_response
```sql
CREATE TABLE survey_response (
    id UUID PRIMARY KEY,
    scan_id UUID NOT NULL,
    ab_group VARCHAR(20) NOT NULL,
    has_confidence BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (scan_id) REFERENCES menu_scan(id),
    INDEX idx_scan_id (scan_id),
    INDEX idx_ab_group (ab_group)
);
```

---

## Development Workflow

### 1. Before You Start
```bash
cd backend
git checkout develop
git pull origin develop
git checkout -b feature/YOUR_FEATURE_NAME
```

### 2. Run Backend Locally
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. Run Tests
```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests MenuServiceTest
```

### 4. Check Code Coverage
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## Implementation Principles

### 1. Code Reuse First
- 기존 v1 코드를 최대한 재사용
- MenuService, MenuItemEnricher, CurrencyService 등 그대로 활용
- 새 기능만 추가 (ABTest, Survey)

### 2. Hypothesis-Driven
- 모든 기능은 H1, H2, H3 검증을 위해 존재
- 가설 검증과 무관한 기능은 제외

### 3. Clean Architecture
- **Domain**: 비즈니스 로직 (MenuItem, MenuScan, SurveyResponse)
- **Application**: 유스케이스 (Services)
- **API**: REST 컨트롤러
- **Infra**: 외부 시스템 연동 (OCR, Translation, Scraping)

### 4. Reactive Programming
- 기존 코드가 Reactor 사용 (Mono, Flux)
- 비동기 처리로 성능 최적화
- 5초 이내 처리 목표 달성

### 5. Test Coverage
- 목표: >80% coverage
- 단위 테스트: Service 레이어 비즈니스 로직
- 통합 테스트: API 엔드포인트, 파이프라인 전체 플로우

---

## DO

✅ 기존 코드 재사용 (MenuService, MenuItemEnricher, CurrencyService 등)
✅ A/B 테스트 로직 구현 (50:50 랜덤 배정)
✅ Control vs Treatment 조건부 처리 (FoodInfo 포함 여부)
✅ 설문 시스템 구현 (scanId, hasConfidence)
✅ 테스트 작성 (>80% coverage)
✅ 처리 시간 5초 이내 최적화
✅ Redis를 활용한 세션 관리 고려
✅ 가설 검증에 집중 (H1, H2, H3)

---

## DO NOT

❌ 가설 검증과 무관한 기능 추가 (장바구니, 로그인 등)
❌ 기존 v1 코드 불필요하게 수정
❌ Order, Script 모듈 건드리기 (MVP 제외)
❌ 데이터베이스 스키마 임의 변경 (새 테이블만 추가)
❌ Over-engineering (MVP에 맞지 않는 복잡한 구조)
❌ 성능 목표 무시 (5초 제한 엄수)

---

## Key Risks & Mitigation

### Technical Risk (H2)
**위험**: OCR/매칭 정확도가 목표(90%, 70%)를 달성하지 못할 위험

**완화**:
- Phase 3 종료 시 내부 기술 테스트 (100개 메뉴판)
- 실패 시 사용자 테스트 중단, R&D 피봇

### Integration Risk
**위험**: 기존 코드와 새 코드 통합 시 버그 발생

**완화**:
- 기존 API 엔드포인트 유지 (하위 호환성)
- 새 엔드포인트만 추가 (/api/menus/scan)
- 통합 테스트 필수

### Performance Risk
**위험**: 파이프라인 처리 시간이 5초를 초과할 위험

**완화**:
- Reactive 비동기 처리 유지
- 외부 API 호출 병렬화 (Mono.zip)
- 성능 테스트 자동화

---

## Next Steps

### Phase 3: 구현 (Day 6-9)
1. **Day 6**: ABTest 모듈 구현 (ABTestService, MenuScan)
2. **Day 7**: Survey 모듈 구현 (SurveyService, SurveyResponse)
3. **Day 8**: MenuController 수정 (/api/menus/scan 엔드포인트)
4. **Day 9**: 통합 테스트 및 내부 기술 검증 (H2)

### Phase 4: E2E 테스트 및 배포 (Day 10)
- E2E 테스트
- 성능 테스트 (5초 제한)
- 배포 준비

---

## Quick Command Reference

```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun --args='--spring.profiles.active=local'

# Test
./gradlew test
./gradlew test --tests MenuServiceTest

# Coverage
./gradlew test jacocoTestReport

# Run performance tests
node currency-api-performance-test.js
node reconfigure-menu-load-test.js
```

---

## Notes for Claude

- 이 문서는 **backend 구현에 특화**된 컨텍스트입니다
- 전체 프로젝트 컨텍스트는 `../../.claude/CLAUDE.md`를 참조하세요
- 기술 명세는 `../../docs/TECH_SPEC.md`를 참조하세요
- 구현 계획은 `../docs/IMPLEMENTATION_PLAN.md`를 참조하세요 ← **백엔드 구현 시작 시 필수**
- 기존 코드를 **최대한 재사용**하고, 새 기능(ABTest, Survey)만 추가합니다
- 모든 기능은 **가설 검증(H1, H2, H3)**을 위해 존재합니다
- **10일 타임라인**이 매우 타이트하므로 효율성이 중요합니다
