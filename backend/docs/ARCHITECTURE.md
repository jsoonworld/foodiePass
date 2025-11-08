# FoodiePass Backend - Architecture

> **목적**: 백엔드 시스템 아키텍처 및 기술 스택 명세

---

## Tech Stack

| 레이어 | 기술 | 버전 |
|---|---|---|
| **Framework** | Spring Boot | 3.5.3 |
| **Language** | Java | 21 |
| **Build** | Gradle | 8.x |
| **Database** | MySQL (prod), H2 (dev) | 8.0 / 2.x |
| **Cache** | Redis | 7.x |
| **Reactive** | Project Reactor | (WebFlux) |

---

## High-Level Architecture

```text
┌─────────────┐
│   Frontend  │ (React/Next.js)
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
│  │  - MenuItem                   │  │
│  │  - MenuScan (NEW)             │  │
│  │  - SurveyResponse (NEW)       │  │
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
```bash

---

## 도메인 모델

### 기존 도메인 (v1 재사용)

#### MenuItem (Value Object)
```java
/**
 * MenuItem은 JPA Entity가 아닌 Value Object입니다.
 * 데이터베이스에 별도 테이블로 저장되지 않으며,
 * MenuScan의 menu_items_json 컬럼에 JSON 배열로 직렬화됩니다.
 */
public class MenuItem {
    private final String name;        // 메뉴명
    private final Price price;        // 가격 (Value Object)
    private final FoodInfo foodInfo;  // 음식 정보 (Value Object)

    public MenuItem(String name, Price price, FoodInfo foodInfo) {
        this.name = name;
        this.price = price;
        this.foodInfo = foodInfo;
    }
}
```bash

**참고**:
- MenuItem은 **불변(immutable) Value Object**입니다.
- 데이터베이스 테이블이 없으며, ID도 없습니다.
- MenuScan에 JSON으로 저장되어 함께 관리됩니다.

---

### 새 도메인 (v2 추가)

#### MenuScan (Entity)
```java
@Entity
@Table(name = "menu_scans")
public class MenuScan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // A/B 테스트 정보
    @Column(name = "session_id", nullable = false)
    private String sessionId;         // 사용자 세션 ID (Spring Session ID)

    @Enumerated(EnumType.STRING)
    @Column(name = "ab_group", nullable = false)
    private ABGroup abGroup;          // A/B 그룹 (CONTROL | TREATMENT)

    // 메뉴판 정보
    @Column(name = "image_url")
    private String imageUrl;          // S3 업로드 이미지 URL

    // 언어/화폐 정보
    @Column(name = "source_language")
    private String sourceLanguage;    // 소스 언어 (auto-detect)

    @Column(name = "target_language", nullable = false)
    private String targetLanguage;    // 타겟 언어 (사용자 선택)

    @Column(name = "source_currency")
    private String sourceCurrency;    // 소스 화폐

    @Column(name = "target_currency", nullable = false)
    private String targetCurrency;    // 타겟 화폐

    // 메뉴 아이템 (JSON 저장)
    @Column(name = "menu_items_json", columnDefinition = "TEXT")
    private String menuItemsJson;     // MenuItem 리스트를 JSON으로 직렬화

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Transient helper method (데이터베이스에 저장되지 않음)
    @Transient
    public List<MenuItem> getMenuItems(ObjectMapper objectMapper) throws JsonProcessingException {
        if (menuItemsJson == null || menuItemsJson.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMapper.readValue(menuItemsJson, new TypeReference<List<MenuItem>>() {});
    }

    @Transient
    public void setMenuItems(List<MenuItem> items, ObjectMapper objectMapper) throws JsonProcessingException {
        this.menuItemsJson = objectMapper.writeValueAsString(items);
    }
}
```java

**참고**:
- MenuItem은 별도 테이블이 아닌 JSON으로 저장됩니다.
- `menuItemsJson` 필드는 TEXT 타입으로, JSON 배열 문자열을 저장합니다.
- Helper 메서드를 통해 JSON ↔ List<MenuItem> 변환을 수행합니다.

#### ABGroup (Enum)
```java
public enum ABGroup {
    CONTROL,    // 텍스트 + 환율만
    TREATMENT   // 사진 + 설명 + 텍스트 + 환율
}
```java

#### SurveyResponse
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
```bash

---

## 패키지 구조

```text
src/main/java/foodiepass/server/
├── menu/                            # 메뉴 도메인
│   ├── domain/
│   │   ├── MenuItem.java            # 메뉴 아이템 엔티티
│   │   └── FoodInfo.java            # 음식 정보 VO
│   ├── application/
│   │   ├── MenuService.java         # 파이프라인 조율
│   │   ├── MenuItemEnricher.java    # 번역+스크래핑+환율 통합
│   │   └── port/out/
│   │       ├── OcrReader.java       # OCR 포트
│   │       ├── FoodScrapper.java    # 스크래핑 포트
│   │       └── TranslationClient.java # 번역 포트
│   ├── infra/                       # 구현체들
│   │   ├── GeminiOcrReader.java
│   │   └── TasteAtlasFoodScrapper.java
│   └── api/
│       └── MenuController.java      # REST API
│
├── language/                        # 언어/번역 도메인
│   ├── domain/
│   │   └── Language.java            # 언어 enum
│   ├── application/
│   │   └── LanguageService.java
│   └── infra/
│       └── GoogleTranslationClient.java
│
├── currency/                        # 환율 도메인
│   ├── domain/
│   │   └── Currency.java            # 통화 enum
│   ├── application/
│   │   ├── CurrencyService.java
│   │   └── ExchangeRateCache.java
│   └── infra/
│       └── GoogleFinanceRateProvider.java
│
├── abtest/                          # ➕ A/B 테스트 도메인 (NEW)
│   ├── domain/
│   │   ├── ABGroup.java             # A/B 그룹 enum
│   │   └── MenuScan.java            # 메뉴 스캔 엔티티
│   ├── application/
│   │   └── ABTestService.java       # A/B 그룹 배정
│   ├── api/
│   │   └── ABTestController.java    # 관리자 API
│   └── repository/
│       └── MenuScanRepository.java
│
└── survey/                          # ➕ 설문 도메인 (NEW)
    ├── domain/
    │   └── SurveyResponse.java      # 설문 응답 엔티티
    ├── application/
    │   └── SurveyService.java       # 설문 로직
    ├── api/
    │   └── SurveyController.java    # REST API
    ├── dto/
    │   ├── request/
    │   │   └── SurveyRequest.java
    │   └── response/
    │       └── SurveyAnalytics.java
    └── repository/
        └── SurveyResponseRepository.java
```bash

---

## 서비스 레이어 설계

### 기존 서비스 (재사용)

#### MenuService
**역할**: 메뉴 파이프라인 조율 (OCR → 번역 → 매칭 → 환율)

**기존 메서드**:
```java
public ReconfigureResponse reconfigure(ReconfigureRequest request) {
    // OCR → 번역 → 매칭 → 환율 파이프라인
}
```java

**새 메서드** (추가):
```java
public MenuScanResponse scanMenu(MenuScanRequest request, String sessionId) {
    // 1. A/B 그룹 배정 (ABTestService 호출)
    ABGroup group = abTestService.getOrAssignGroup(sessionId);

    // 2. 기존 reconfigure 로직 호출
    List<MenuItem> items = reconfigure(...);

    // 3. Treatment 그룹만 음식 매칭 실행
    if (group == ABGroup.TREATMENT) {
        items = foodScraperService.enrich(items);
    }

    // 4. MenuScan 저장
    MenuScan scan = menuScanRepository.save(new MenuScan(...));

    // 5. 응답 반환 (abGroup 포함)
    return new MenuScanResponse(scan, group, items);
}
```java

---

#### CurrencyService
**역할**: 환율 변환 및 포맷팅

```java
@Service
public class CurrencyService {
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        // 기존 로직 재사용
        // Redis 캐싱 적용 (24시간)
    }
}
```java

---

#### LanguageService
**역할**: 언어 목록 제공 및 번역

```java
@Service
public class LanguageService {
    public String translate(String text, String sourceLang, String targetLang) {
        // 기존 로직 재사용
    }
}
```bash

---

### 새 서비스 (추가)

#### ABTestService
**역할**: A/B 그룹 배정 및 관리

```java
@Service
public class ABTestService {
    private final MenuScanRepository menuScanRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final Random random = new Random();

    /**
     * 세션 ID를 기반으로 A/B 그룹 배정 (50:50)
     */
    public ABGroup assignGroup(String sessionId) {
        // 랜덤 배정
        return random.nextBoolean() ? ABGroup.CONTROL : ABGroup.TREATMENT;
    }

    /**
     * 이미 배정된 그룹 조회 (세션 재사용 시)
     */
    public ABGroup getOrAssignGroup(String sessionId) {
        // Redis에서 조회
        String cached = redisTemplate.opsForValue().get("ab:group:" + sessionId);
        if (cached != null) {
            return ABGroup.valueOf(cached);
        }

        // 없으면 새로 배정하고 캐싱
        ABGroup group = assignGroup(sessionId);
        redisTemplate.opsForValue().set("ab:group:" + sessionId, group.name(),
            Duration.ofMinutes(30));

        return group;
    }

    /**
     * A/B 테스트 결과 분석 (관리자용)
     */
    public ABTestResult getResults() {
        // Control vs Treatment 그룹 응답률 계산
    }
}
```java

---

#### SurveyService
**역할**: 설문 응답 수집 및 분석

```java
@Service
public class SurveyService {
    private final SurveyResponseRepository surveyRepository;
    private final MenuScanRepository menuScanRepository;

    /**
     * 설문 응답 저장
     */
    public void recordResponse(UUID scanId, Boolean hasConfidence) {
        MenuScan scan = menuScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Scan not found"));

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
    public SurveyAnalytics getAnalytics() {
        // Control vs Treatment 그룹별 Yes 응답률 계산
        long controlTotal = surveyRepository.countByAbGroup(ABGroup.CONTROL);
        long controlYes = surveyRepository.countByAbGroupAndHasConfidence(
            ABGroup.CONTROL, true);

        long treatmentTotal = surveyRepository.countByAbGroup(ABGroup.TREATMENT);
        long treatmentYes = surveyRepository.countByAbGroupAndHasConfidence(
            ABGroup.TREATMENT, true);

        double controlRate = (double) controlYes / controlTotal;
        double treatmentRate = (double) treatmentYes / treatmentTotal;
        double ratio = treatmentRate / controlRate;

        return new SurveyAnalytics(
            controlTotal, controlYes, controlRate,
            treatmentTotal, treatmentYes, treatmentRate,
            ratio
        );
    }
}
```bash

---

## 인프라 레이어

### 외부 API 연동

#### 1. Google Vertex AI (OCR)
**구현체**: `GeminiOcrReader`

**역할**: 메뉴판 이미지 → 메뉴명/가격 추출

**재사용**: ✅ 기존 v1 코드 그대로 사용

---

#### 2. Google Cloud Translation
**구현체**: `GoogleTranslationClient`

**역할**: 메뉴명 번역

**재사용**: ✅ 기존 v1 코드 그대로 사용

---

#### 3. TasteAtlas API (음식 매칭)
**구현체**: `TasteAtlasFoodScrapper`

**역할**: 음식 이름 → 사진 URL + 설명

**재사용**: ✅ 기존 v1 코드 그대로 사용

**개선**: 매칭 신뢰도(confidence) 계산 추가 고려

---

#### 4. Currency API (환율)
**구현체**: `GoogleFinanceRateProvider`

**역할**: 실시간 환율 조회

**재사용**: ✅ 기존 v1 코드 그대로 사용

**개선**: Redis 캐싱 추가 (24시간 TTL)

---

## Redis 캐싱 전략

### 캐싱 대상

| 키 패턴 | 값 | TTL | 용도 |
|---|---|---|---|
| `session:{sessionId}` | Session 객체 | 30분 | Spring Session 저장 |
| `ab:group:{sessionId}` | `CONTROL` or `TREATMENT` | 30분 | A/B 그룹 캐싱 |
| `currency:rate:{from}:{to}` | 환율 | 24시간 | 환율 캐싱 |

### Spring Session 설정

```java
@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    // Redis를 세션 저장소로 사용
}
```java

---

## 성능 최적화

### 1. 비동기 처리
```java
@Async
public CompletableFuture<FoodInfo> enrichFoodInfo(String foodName) {
    // 음식 매칭을 비동기로 처리 (Treatment 그룹만)
}
```java

### 2. 병렬 API 호출
```java
// Reactor를 활용한 병렬 처리
Mono<List<MenuItem>> items = Mono.zip(
    ocrService.extract(image),
    languageService.translate(...),
    currencyService.convert(...)
).map(tuple -> combine(tuple.getT1(), tuple.getT2(), tuple.getT3()));
```bash

### 3. 캐싱
- 환율: Redis 24시간 캐싱
- A/B 그룹: Redis 30분 캐싱
- 음식 이미지: CDN (CloudFront) 7일 캐싱

---

## 보안

### 1. 이미지 업로드 보안
- 파일 크기 제한: 10MB
- 허용 포맷: JPG, PNG, HEIC
- 바이러스 스캔: ClamAV (선택)

### 2. API 보안
- HTTPS 강제
- CORS 설정 (프론트엔드 도메인만 허용)
- Rate Limiting (IP당 10req/min)

---

## 모니터링 및 로깅

### 메트릭

| 메트릭 | 측정 대상 | 목표 |
|---|---|---|
| **처리 시간** | OCR → 응답 시간 | ≤ 5초 |
| **OCR 정확도** | 추출 성공률 | ≥ 90% |
| **매칭 연관성** | 수동 평가 | ≥ 70% |
| **A/B 응답률** | Yes 응답률 비율 | ≥ 2.0 |

### 로깅 예시

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
```bash

---

## 참고 문서

- [HYPOTHESES.md](./HYPOTHESES.md) - 핵심 가설 및 검증 목표
- [API_SPEC.md](./API_SPEC.md) - API 엔드포인트 상세
- [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - DB 스키마
- [CODE_REUSE_GUIDE.md](./CODE_REUSE_GUIDE.md) - v1 코드 재사용 가이드
- [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) - 구현 계획
