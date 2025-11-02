# FoodiePass: Technical Specification

> **문서 목적**: 기존 v1 코드베이스를 v2 MVP 요구사항에 맞게 개선하기 위한 기술 아키텍처 명세

---

## 1. 문서 개요

| 항목 | 내용 |
|---|---|
| **프로젝트** | FoodiePass v2 MVP |
| **버전** | 2.0 |
| **작성일** | 2025-11-02 |
| **목표** | 가설 검증을 위한 기술 아키텍처 설계 |

---

## 2. 시스템 아키텍처

### 2.1 High-Level Architecture

```
┌─────────────┐
│   Frontend  │ (React/Next.js - TBD)
│  (S3/Vercel)│
└──────┬──────┘
       │ HTTPS/REST
       ↓
┌─────────────────────────────────────┐
│       Spring Boot Backend           │
│  ┌───────────────────────────────┐  │
│  │    API Layer (Controllers)    │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │  Application Layer (Services) │  │
│  │  - MenuService                │  │
│  │  - LanguageService            │  │
│  │  - CurrencyService            │  │
│  │  - ABTestService (NEW)        │  │
│  │  - SurveyService (NEW)        │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │   Domain Layer (Entities)     │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │  Infrastructure Layer         │  │
│  │  - OCR Client (Gemini)        │  │
│  │  - Translation Client (GCP)   │  │
│  │  - Food Scraper (TasteAtlas)  │  │
│  │  - Currency Client (API)      │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
       │                    │
       ↓                    ↓
┌─────────────┐      ┌─────────────┐
│   MySQL     │      │   Redis     │
│   (RDS)     │      │  (Session)  │
└─────────────┘      └─────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│      External APIs                   │
│  - Google Vertex AI (OCR)            │
│  - Google Translation                │
│  - TasteAtlas (Food Images)          │
│  - Currency API (Exchange Rates)     │
└──────────────────────────────────────┘
```

---

## 3. 기존 코드베이스 분석

### 3.1 현재 도메인 구조 (v1)

| 도메인 | 역할 | 주요 클래스 | 상태 |
|---|---|---|---|
| **menu** | 메뉴 OCR, 번역, enrichment | `MenuService`, `MenuItemEnricher`, `MenuItem` | ✅ 재사용 가능 |
| **language** | 번역 서비스 | `LanguageService`, `GoogleTranslationClient` | ✅ 재사용 가능 |
| **currency** | 환율 변환 | `CurrencyService`, `GoogleFinanceRateProvider` | ✅ 재사용 가능 |
| **order** | 주문 도메인 | `Order`, `OrderItem` | ⚠️ MVP 제외 |
| **script** | 주문서 생성 | `ScriptService`, `ScriptFactory` | ⚠️ MVP 제외 |

### 3.2 기존 인프라 컴포넌트

| 컴포넌트 | 구현체 | 역할 | v2 사용 여부 |
|---|---|---|---|
| **OCR** | `GeminiOcrReader` | 메뉴판 텍스트 추출 | ✅ 재사용 |
| **Translation** | `GoogleTranslationClient` | 메뉴명 번역 | ✅ 재사용 |
| **Food Scraping** | `TasteAtlasFoodScrapper`, `GeminiFoodScrapper` | 음식 사진/설명 검색 | ✅ 재사용 |
| **Currency** | `GoogleFinanceRateProvider` | 환율 조회 | ✅ 재사용 |
| **Database** | H2 (dev) | 데이터 저장 | ⚠️ MySQL로 변경 |

### 3.3 재사용 가능한 기능

**✅ 그대로 사용 가능**:
- OCR 파이프라인 (`GeminiOcrReader`)
- 번역 서비스 (`GoogleTranslationClient`)
- 환율 변환 (`CurrencyService`)
- 음식 매칭 (`TasteAtlasFoodScrapper`)

**🔧 수정 필요**:
- `MenuService`: A/B 테스트 로직 추가
- API 엔드포인트: 새 응답 형식 (abGroup 포함)

**➕ 새로 개발**:
- A/B 테스트 시스템
- 설문 시스템
- 세션 관리 (Redis)

---

## 4. v2 MVP 요구사항 매핑

### 4.1 기능별 기술 매핑

| 기능 ID | 기능명 | 기존 코드 | 새 개발 | 수정 |
|---|---|---|---|---|
| **F-01** | 웹 업로더 | - | Frontend 개발 | - |
| **F-02** | OCR 파이프라인 | `GeminiOcrReader` | - | API 응답 형식 |
| **F-03** | 번역 파이프라인 | `GoogleTranslationClient` | - | - |
| **F-04** | 음식 매칭 파이프라인 | `TasteAtlasFoodScrapper` | - | - |
| **F-05** | 환율 변환 | `CurrencyService` | - | - |
| **F-06** | 시각적 메뉴 UI | - | Frontend 개발 | - |
| **F-07** | 텍스트 전용 UI | - | Frontend 개발 | - |
| **F-08** | A/B 그룹 배정 | - | `ABTestService` | - |
| **F-09** | 확신도 설문 | - | `SurveyService` | - |

---

## 5. 백엔드 아키텍처 상세

### 5.1 도메인 모델 (Domain Model)

#### 5.1.1 기존 도메인 재사용

**MenuItem** (기존 유지):
```java
@Entity
public class MenuItem {
    @Id
    private UUID id;
    private String originalName;      // OCR 추출 원어
    private String translatedName;    // 번역된 이름
    private BigDecimal originalPrice; // 원래 가격
    private BigDecimal convertedPrice; // 변환된 가격

    // Food enrichment
    private String foodImageUrl;      // 음식 사진 URL
    private String foodDescription;   // 음식 설명
    private Float matchConfidence;    // 매칭 신뢰도 (0-1)
}
```

#### 5.1.2 새 도메인 추가

**MenuScan** (새로 추가):
```java
@Entity
public class MenuScan {
    @Id
    private UUID id;

    private String sessionId;         // 사용자 세션 ID
    private ABGroup abGroup;          // A/B 그룹 (CONTROL | TREATMENT)

    private String imageUrl;          // S3 업로드 이미지 URL
    private String sourceLanguage;    // 소스 언어 (auto-detect)
    private String targetLanguage;    // 타겟 언어 (사용자 선택)
    private String sourceCurrency;    // 소스 화폐
    private String targetCurrency;    // 타겟 화폐

    @OneToMany(mappedBy = "scan")
    private List<MenuItem> items;     // 메뉴 아이템 리스트

    private LocalDateTime createdAt;
}

public enum ABGroup {
    CONTROL,    // 텍스트 + 환율만
    TREATMENT   // 사진 + 설명 + 텍스트 + 환율
}
```

**SurveyResponse** (새로 추가):
```java
@Entity
public class SurveyResponse {
    @Id
    private UUID id;

    @ManyToOne
    private MenuScan scan;            // FK to MenuScan

    private ABGroup abGroup;          // 그룹 정보 (분석용)
    private Boolean hasConfidence;    // 확신 여부 (Yes=true, No=false)

    private LocalDateTime createdAt;
}
```

---

### 5.2 서비스 레이어 (Service Layer)

#### 5.2.1 기존 서비스 재사용

**MenuService** (수정):
```java
@Service
public class MenuService {
    // 기존 메서드
    public ReconfigureResponse reconfigure(ReconfigureRequest request) {
        // OCR → 번역 → 매칭 → 환율 파이프라인
    }

    // 새 메서드 추가
    public MenuScanResponse scanMenu(MenuScanRequest request) {
        // 1. A/B 그룹 배정 (ABTestService 호출)
        // 2. 기존 reconfigure 로직 호출
        // 3. Treatment 그룹만 음식 매칭 실행
        // 4. MenuScan 저장
        // 5. 응답 반환 (abGroup 포함)
    }
}
```

**CurrencyService** (그대로 사용):
```java
@Service
public class CurrencyService {
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        // 기존 로직 재사용
    }
}
```

**LanguageService** (그대로 사용):
```java
@Service
public class LanguageService {
    public String translate(String text, String sourceLang, String targetLang) {
        // 기존 로직 재사용
    }
}
```

#### 5.2.2 새 서비스 추가

**ABTestService** (새로 추가):
```java
@Service
public class ABTestService {
    private final Random random = new Random();

    /**
     * 세션 ID를 기반으로 A/B 그룹 랜덤 배정 (50:50)
     */
    public ABGroup assignGroup(String sessionId) {
        // 랜덤 또는 세션 ID 해시 기반 배정
        return random.nextBoolean() ? ABGroup.CONTROL : ABGroup.TREATMENT;
    }

    /**
     * 이미 배정된 그룹 조회 (세션 재사용 시)
     */
    public ABGroup getGroup(String sessionId) {
        // Redis에서 조회, 없으면 새로 배정
    }
}
```

**SurveyService** (새로 추가):
```java
@Service
public class SurveyService {
    private final SurveyResponseRepository surveyRepository;

    /**
     * 설문 응답 저장
     */
    public void recordResponse(UUID scanId, Boolean hasConfidence) {
        MenuScan scan = menuScanRepository.findById(scanId)
            .orElseThrow();

        SurveyResponse response = new SurveyResponse();
        response.setScan(scan);
        response.setAbGroup(scan.getAbGroup());
        response.setHasConfidence(hasConfidence);
        response.setCreatedAt(LocalDateTime.now());

        surveyRepository.save(response);
    }

    /**
     * A/B 테스트 결과 분석
     */
    public ABTestResult analyzeResults() {
        // Control vs Treatment 그룹 Yes 응답률 계산
    }
}
```

---

### 5.3 API 설계

#### 5.3.1 메뉴 스캔 API

**POST /api/menus/scan**

**Request**:
```json
{
  "image": "base64...",
  "targetLanguage": "ko",
  "targetCurrency": "KRW"
}
```

**Response** (Treatment 그룹):
```json
{
  "scanId": "uuid",
  "abGroup": "TREATMENT",
  "items": [
    {
      "id": "uuid",
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "originalPrice": 15.00,
      "convertedPrice": 20000,
      "foodImageUrl": "https://...",
      "foodDescription": "토마토 소스, 모짜렐라, 바질",
      "matchConfidence": 0.85
    }
  ],
  "processingTime": 4.2
}
```

**Response** (Control 그룹):
```json
{
  "scanId": "uuid",
  "abGroup": "CONTROL",
  "items": [
    {
      "id": "uuid",
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "originalPrice": 15.00,
      "convertedPrice": 20000
      // foodImageUrl, foodDescription 없음
    }
  ],
  "processingTime": 3.1
}
```

**처리 로직**:
```java
@PostMapping("/scan")
public ResponseEntity<MenuScanResponse> scanMenu(
    @RequestBody MenuScanRequest request,
    HttpSession session
) {
    String sessionId = session.getId();

    // 1. A/B 그룹 배정
    ABGroup group = abTestService.getOrAssignGroup(sessionId);

    // 2. OCR 실행
    List<MenuItem> items = ocrService.extract(request.getImage());

    // 3. 번역 실행
    items = languageService.translate(items, request.getTargetLanguage());

    // 4. 환율 변환
    items = currencyService.convert(items, request.getTargetCurrency());

    // 5. 음식 매칭 (Treatment 그룹만)
    if (group == ABGroup.TREATMENT) {
        items = foodScraperService.enrich(items);
    }

    // 6. MenuScan 저장
    MenuScan scan = menuScanRepository.save(new MenuScan(...));

    // 7. 응답 생성
    return ResponseEntity.ok(new MenuScanResponse(scan, group, items));
}
```

---

#### 5.3.2 설문 API

**POST /api/surveys**

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

**처리 로직**:
```java
@PostMapping
public ResponseEntity<SurveyResponseDto> recordSurvey(
    @RequestBody SurveyRequest request
) {
    surveyService.recordResponse(
        request.getScanId(),
        request.getHasConfidence()
    );

    return ResponseEntity.ok(new SurveyResponseDto(true, "Response recorded"));
}
```

---

#### 5.3.3 A/B 테스트 결과 API (관리자용)

**GET /api/admin/ab-test/results**

**Response**:
```json
{
  "control": {
    "totalResponses": 10,
    "yesCount": 3,
    "yesRate": 0.30
  },
  "treatment": {
    "totalResponses": 10,
    "yesCount": 8,
    "yesRate": 0.80
  },
  "ratio": 2.67,
  "hypothesisValidated": true
}
```

---

### 5.4 데이터베이스 설계

#### 5.4.1 스키마

**menu_scans** (새 테이블):
```sql
CREATE TABLE menu_scans (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    ab_group VARCHAR(20) NOT NULL, -- 'CONTROL' or 'TREATMENT'
    image_url VARCHAR(512),
    source_language VARCHAR(10),
    target_language VARCHAR(10) NOT NULL,
    source_currency VARCHAR(3),
    target_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_ab_group (ab_group)
);
```

**menu_items** (기존 테이블, FK 추가):
```sql
CREATE TABLE menu_items (
    id VARCHAR(36) PRIMARY KEY,
    scan_id VARCHAR(36) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    translated_name VARCHAR(255),
    original_price DECIMAL(10, 2),
    converted_price DECIMAL(10, 2),
    food_image_url VARCHAR(512),
    food_description TEXT,
    match_confidence FLOAT,
    FOREIGN KEY (scan_id) REFERENCES menu_scans(id)
);
```

**survey_responses** (새 테이블):
```sql
CREATE TABLE survey_responses (
    id VARCHAR(36) PRIMARY KEY,
    scan_id VARCHAR(36) NOT NULL,
    ab_group VARCHAR(20) NOT NULL,
    has_confidence BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES menu_scans(id),
    INDEX idx_ab_group (ab_group)
);
```

---

### 5.5 세션 관리 (Redis)

#### 5.5.1 Redis 용도

| 키 | 값 | TTL | 용도 |
|---|---|---|---|
| `session:{sessionId}` | Session 객체 | 30분 | Spring Session 저장 |
| `ab:group:{sessionId}` | `CONTROL` or `TREATMENT` | 30분 | A/B 그룹 캐싱 |
| `currency:rate:{from}:{to}` | 환율 | 24시간 | 환율 캐싱 |

#### 5.5.2 Spring Session 설정

```java
@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    // Redis를 세션 저장소로 사용
}
```

---

## 6. 프론트엔드 아키텍처

### 6.1 페이지 구조

```
/
├── / (홈)
│   └─ 메뉴판 업로드 + 언어/화폐 선택
├── /menu/:scanId (메뉴 표시)
│   ├─ Control 그룹: 텍스트 전용 UI
│   └─ Treatment 그룹: 시각적 메뉴 UI
└── /survey/:scanId (설문)
    └─ 확신도 질문 (Yes/No)
```

### 6.2 컴포넌트 구조

```
src/
├── components/
│   ├── MenuUploader/          # F-01: 업로더
│   ├── MenuList/
│   │   ├── TextOnlyMenu/      # F-07: Control UI
│   │   └── VisualMenu/        # F-06: Treatment UI
│   ├── SurveyModal/           # F-09: 설문
│   └── LoadingSpinner/
├── services/
│   ├── api.ts                 # API 클라이언트
│   └── abTest.ts              # A/B 테스트 로직
├── hooks/
│   ├── useMenuScan.ts
│   └── useSurvey.ts
└── pages/
    ├── index.tsx              # 홈
    ├── menu/[scanId].tsx      # 메뉴 표시
    └── survey/[scanId].tsx    # 설문
```

### 6.3 A/B 테스트 로직 (프론트엔드)

```typescript
// services/abTest.ts
export function renderMenuUI(abGroup: 'CONTROL' | 'TREATMENT', items: MenuItem[]) {
  if (abGroup === 'CONTROL') {
    return <TextOnlyMenu items={items} />;
  } else {
    return <VisualMenu items={items} />;
  }
}
```

---

## 7. 외부 API 통합

### 7.1 Google Cloud Vertex AI (OCR)

**기존 구현**: `GeminiOcrReader`

**역할**: 메뉴판 이미지 → 메뉴명/가격 추출

**재사용**: ✅ 그대로 사용

---

### 7.2 Google Cloud Translation

**기존 구현**: `GoogleTranslationClient`

**역할**: 메뉴명 번역

**재사용**: ✅ 그대로 사용

---

### 7.3 TasteAtlas API (음식 매칭)

**기존 구현**: `TasteAtlasFoodScrapper`

**역할**: 음식 이름 → 사진 URL + 설명

**재사용**: ✅ 그대로 사용

**개선**: 매칭 신뢰도(confidence) 계산 추가

---

### 7.4 Currency API (환율)

**기존 구현**: `GoogleFinanceRateProvider`

**역할**: 실시간 환율 조회

**재사용**: ✅ 그대로 사용

**개선**: Redis 캐싱 추가 (24시간)

---

## 8. 성능 최적화

### 8.1 캐싱 전략

| 대상 | 캐싱 위치 | TTL | 이유 |
|---|---|---|---|
| 환율 | Redis | 24시간 | API 호출 비용 절감 |
| A/B 그룹 | Redis | 30분 | 세션 일관성 유지 |
| 음식 사진 | CDN (CloudFront) | 7일 | 이미지 로딩 속도 향상 |

### 8.2 비동기 처리

```java
@Async
public CompletableFuture<FoodInfo> enrichFoodInfo(String foodName) {
    // 음식 매칭을 비동기로 처리 (Treatment 그룹만)
}
```

---

## 9. 보안

### 9.1 이미지 업로드 보안

- 파일 크기 제한: 10MB
- 허용 포맷: JPG, PNG, HEIC
- 바이러스 스캔: ClamAV (선택)

### 9.2 API 보안

- HTTPS 강제
- CORS 설정 (프론트엔드 도메인만 허용)
- Rate Limiting (IP당 10req/min)

---

## 10. 모니터링 및 로깅

### 10.1 메트릭

| 메트릭 | 측정 대상 | 목표 |
|---|---|---|
| **처리 시간** | OCR → 응답 시간 | ≤ 5초 |
| **OCR 정확도** | 추출 성공률 | ≥ 90% |
| **매칭 연관성** | 수동 평가 | ≥ 70% |
| **A/B 응답률** | Yes 응답률 비율 | ≥ 2.0 |

### 10.2 로깅

```java
@Slf4j
public class MenuService {
    public MenuScanResponse scanMenu(MenuScanRequest request) {
        log.info("Menu scan started: sessionId={}", sessionId);
        long startTime = System.currentTimeMillis();

        // ... 처리 로직 ...

        long duration = System.currentTimeMillis() - startTime;
        log.info("Menu scan completed: scanId={}, duration={}ms, abGroup={}",
            scanId, duration, abGroup);
    }
}
```

---

## 11. 배포 아키텍처

### 11.1 AWS 인프라

```
┌───────────────┐
│  CloudFront   │ (CDN)
│  (Frontend)   │
└───────┬───────┘
        │
┌───────▼───────┐
│      S3       │ (Static Hosting)
│  (React App)  │
└───────────────┘

┌───────────────┐
│      ALB      │ (Load Balancer)
└───────┬───────┘
        │
┌───────▼───────┐
│      ECS      │ (Container)
│  (Spring Boot)│
└───┬───────┬───┘
    │       │
    ↓       ↓
┌────────┐ ┌────────┐
│  RDS   │ │ Redis  │
│(MySQL) │ │ (ElastiCache)
└────────┘ └────────┘
```

---

## 12. 개발 환경 구성

### 12.1 로컬 개발 환경

**Backend**:
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Frontend**:
```bash
cd frontend
npm run dev
```

**Database**:
- H2 (로컬): `jdbc:h2:mem:testdb`
- MySQL (Docker):
  ```bash
  docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8
  ```

**Redis** (Docker):
```bash
docker run -p 6379:6379 redis:7
```

---

## 13. 마이그레이션 계획

### 13.1 v1 → v2 마이그레이션

| 항목 | v1 | v2 | 작업 |
|---|---|---|---|
| **Database** | H2 | MySQL | 스키마 생성 + 마이그레이션 스크립트 |
| **Session** | In-memory | Redis | Spring Session 설정 |
| **도메인** | Order, Script | MenuScan, Survey | 새 도메인 추가 |
| **API** | /api/menus/reconfigure | /api/menus/scan | 새 엔드포인트 |

### 13.2 데이터베이스 마이그레이션 (Flyway)

**V2__create_menu_scan_tables.sql**:
```sql
CREATE TABLE menu_scans (...);
CREATE TABLE survey_responses (...);
ALTER TABLE menu_items ADD COLUMN scan_id VARCHAR(36);
```

---

## 14. 테스트 전략

### 14.1 단위 테스트

- Service 레이어: 비즈니스 로직 검증
- Domain 레이어: 도메인 로직 검증

### 14.2 통합 테스트

- API 엔드포인트: `/api/menus/scan`, `/api/surveys`
- A/B 그룹 배정 로직
- 파이프라인 전체 플로우

### 14.3 성능 테스트

- 목표: 처리 시간 ≤ 5초
- 도구: JMeter 또는 Gatling

---

## 15. 부록

### A. 기존 코드 재사용 체크리스트

- [x] `GeminiOcrReader`: OCR
- [x] `GoogleTranslationClient`: 번역
- [x] `TasteAtlasFoodScrapper`: 음식 매칭
- [x] `CurrencyService`: 환율 변환
- [ ] `MenuService`: A/B 로직 추가 필요
- [ ] A/B 테스트 시스템: 새로 개발
- [ ] 설문 시스템: 새로 개발

### B. 새로 개발할 컴포넌트

**Backend**:
- `ABTestService`: A/B 그룹 배정
- `SurveyService`: 설문 응답 수집
- `MenuScanRepository`, `SurveyResponseRepository`

**Frontend**:
- 전체 React/Next.js 앱
- Control/Treatment UI 분기
- 설문 모달

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|---|---|---|
| v2.0 | 2025-11-02 | 초안 작성 - 기존 코드 분석 및 v2 아키텍처 설계 |
