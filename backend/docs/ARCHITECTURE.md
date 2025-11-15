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
│   │       ├── FoodScraper.java    # 스크래핑 포트
│   │       └── TranslationClient.java # 번역 포트
│   ├── infra/                       # 구현체들
│   │   ├── GeminiOcrReader.java
│   │   └── TasteAtlasFoodScraper.java
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

### MVP 파이프라인 설계

#### 전체 처리 흐름

```text
POST /api/menus/scan (userId, MenuScanRequest)
    │
    ↓
┌───────────────────────────────────────────────────────────┐
│  MenuController.scanMenu()                                │
└───────────────────────────────────────────────────────────┘
    │
    ↓
┌───────────────────────────────────────────────────────────┐
│  MenuService.scanMenu()                                   │
│                                                             │
│  ① ABTestService.assignAndCreateScan()                    │
│     → A/B 그룹 배정 (50:50) + MenuScan 생성              │
│     → 동일 userId 재방문 시 기존 그룹 유지                │
│                                                             │
│  ② OcrReader.read(base64Image)                            │
│     → MenuItem List 추출 (name, price)                    │
│                                                             │
│  ③ 파이프라인 분기:                                       │
│     if (abGroup == TREATMENT):                            │
│       MenuItemEnricher.enrichBatchAsync()                 │
│       → 번역 (source → target 언어)                       │
│       → 음식 매칭 (TasteAtlas: 사진 + 설명)              │
│       → 환율 변환 (source → target 화폐)                  │
│     else (CONTROL):                                       │
│       MenuItemEnricher.enrichWithoutFoodScrapingAsync()   │
│       → 번역 (source → target 언어)                       │
│       → 환율 변환 (source → target 화폐)                  │
│       → 음식 매칭 제외 ⚠️                                 │
│                                                             │
│  ④ MenuScan.setMenuItems(enrichedItems, objectMapper)    │
│     → MenuItem 리스트를 JSON으로 직렬화                    │
│                                                             │
│  ⑤ MenuScanRepository.save(menuScan)                      │
│     → DB 저장 (menu_items_json 컬럼에 JSON 저장)         │
└───────────────────────────────────────────────────────────┘
    │
    ↓
MenuScanResponse (scanId, abGroup, items, processingTime)
```

#### Control vs Treatment 파이프라인 비교

| 처리 단계 | Control 그룹 | Treatment 그룹 |
|---------|------------|--------------|
| **OCR** | ✅ 실행 | ✅ 실행 |
| **번역** | ✅ 실행 (source → target) | ✅ 실행 (source → target) |
| **환율 변환** | ✅ 실행 (source → target) | ✅ 실행 (source → target) |
| **음식 매칭** | ❌ 건너뜀 | ✅ 실행 (TasteAtlas) |
| **응답 필드** | name, translatedName, price | name, translatedName, description, imageUrl, price |

**처리 시간 목표**: ≤ 5초 (전체 파이프라인)

---

### 기존 서비스 (재사용)

#### MenuService

**역할**: 메뉴 스캔 파이프라인 전체 조율

**기존 메서드** (v1):
```java
public Mono<ReconfigureResponse> reconfigure(ReconfigureRequest request) {
    // OCR → 번역 → 매칭 → 환율 파이프라인 (v1 용도)
    // MVP에서는 사용하지 않음 (레거시)
}
```

**새 메서드** (MVP 전용):
```java
@Service
@RequiredArgsConstructor
public class MenuService {
    private final OcrReader ocrReader;
    private final MenuItemEnricher menuItemEnricher;
    private final ABTestService abTestService;
    private final MenuScanRepository menuScanRepository;
    private final ObjectMapper objectMapper;

    /**
     * 메뉴 스캔 파이프라인 실행
     *
     * @param request 메뉴 스캔 요청 (이미지, 언어, 화폐)
     * @param userId 사용자 세션 ID
     * @return MenuScanResponse (scanId, abGroup, items, processingTime)
     */
    public Mono<MenuScanResponse> scanMenu(MenuScanRequest request, String userId) {
        long startTime = System.currentTimeMillis();

        // Step 1: A/B 그룹 배정 + MenuScan 생성
        MenuScan scan = abTestService.assignAndCreateScan(
            userId,
            null, // imageUrl (S3 업로드는 Phase 4에서 구현)
            request.sourceLanguage(),
            request.targetLanguage(),
            request.sourceCurrency(),
            request.targetCurrency()
        );

        // Step 2: OCR 실행
        List<MenuItem> rawItems = ocrReader.read(request.base64EncodedImage());

        Language sourceLanguage = Language.fromLanguageName(request.sourceLanguage());
        Language targetLanguage = Language.fromLanguageName(request.targetLanguage());
        Currency sourceCurrency = Currency.fromCurrencyName(request.sourceCurrency());
        Currency targetCurrency = Currency.fromCurrencyName(request.targetCurrency());

        // Step 3: 파이프라인 분기 (Control vs Treatment)
        Mono<List<MenuItemDto>> enrichedItemsMono;
        if (scan.getAbGroup() == ABGroup.TREATMENT) {
            // Treatment: 번역 + 음식 매칭 + 환율
            enrichedItemsMono = menuItemEnricher.enrichBatchAsync(
                rawItems, sourceLanguage, targetLanguage, sourceCurrency, targetCurrency
            ).map(this::toMenuItemDtos);
        } else {
            // Control: 번역 + 환율만 (음식 매칭 제외)
            enrichedItemsMono = menuItemEnricher.enrichWithoutFoodScrapingAsync(
                rawItems, sourceLanguage, targetLanguage, sourceCurrency, targetCurrency
            ).map(this::toMenuItemDtos);
        }

        return enrichedItemsMono.flatMap(items -> {
            try {
                // Step 4: MenuItem 리스트를 JSON으로 직렬화
                scan.setMenuItems(rawItems, objectMapper);

                // Step 5: DB 저장
                MenuScan savedScan = menuScanRepository.save(scan);

                long processingTime = System.currentTimeMillis() - startTime;
                log.info("Menu scan completed: scanId={}, abGroup={}, items={}, time={}ms",
                    savedScan.getId(), savedScan.getAbGroup(), items.size(), processingTime);

                return Mono.just(new MenuScanResponse(
                    savedScan.getId(),
                    savedScan.getAbGroup(),
                    items,
                    processingTime
                ));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize menu items", e);
                return Mono.error(new RuntimeException("Failed to process menu scan", e));
            }
        });
    }

    private List<MenuItemDto> toMenuItemDtos(List<FoodItemResponse> responses) {
        // FoodItemResponse → MenuItemDto 변환
        // @JsonInclude(NON_NULL)에 의해 Control 그룹은 description, imageUrl 제외
        return responses.stream()
            .map(response -> new MenuItemDto(
                response.originalName(),
                response.translatedName(),
                response.translatedDescription(), // Control: null
                response.imageUrl(),              // Control: null
                null,                             // matchConfidence (추후 추가)
                response.priceInfo()
            ))
            .toList();
    }
}
```

**핵심 설계 포인트**:
- ✅ 동일 userId 재방문 시 기존 A/B 그룹 유지 (ABTestService 내부 처리)
- ✅ Treatment/Control 파이프라인 분기 (MenuItemEnricher 메서드 분리)
- ✅ MenuItem을 JSON으로 직렬화하여 `menu_items_json` 컬럼에 저장
- ✅ 처리 시간 측정 및 로깅

---

#### MenuItemEnricher

**역할**: 메뉴 아이템 번역 + 음식 매칭 + 환율 변환

**기존 메서드** (Treatment 그룹용):
```java
/**
 * 배치 번역 + 음식 매칭 + 환율 변환 (Treatment 그룹)
 */
public Mono<List<FoodItemResponse>> enrichBatchAsync(
    List<MenuItem> menuItems,
    Language originLanguage,
    Language userLanguage,
    Currency originCurrency,
    Currency userCurrency
) {
    // Step 1: 메뉴 이름 영문 번역 (음식 매칭용)
    // Step 2: 메뉴 이름 사용자 언어 번역
    // Step 3: 음식 매칭 (TasteAtlas 스크래핑)
    // Step 4: 음식 설명 번역
    // Step 5: 환율 변환
    // → 모든 필드 포함 (description, imageUrl, matchConfidence)
}
```

**새 메서드** (Control 그룹용):
```java
/**
 * 배치 번역 + 환율 변환만 (Control 그룹 - 음식 매칭 제외)
 */
public Mono<List<FoodItemResponse>> enrichWithoutFoodScrapingAsync(
    List<MenuItem> menuItems,
    Language originLanguage,
    Language userLanguage,
    Currency originCurrency,
    Currency userCurrency
) {
    // Step 1: 메뉴 이름 사용자 언어 번역
    // Step 2: 환율 변환
    // Step 3: FoodItemResponse 생성 (description=null, imageUrl=null)
    // → 음식 매칭 단계 제외

    List<String> menuNames = menuItems.stream()
        .map(MenuItem::getName)
        .toList();

    // 배치 번역
    Mono<List<String>> translatedNamesMono = translationClient
        .translateAsync(originLanguage, userLanguage, menuNames)
        .collectList()
        .onErrorResume(e -> {
            log.warn("메뉴 이름 번역 실패. 원본을 사용합니다.", e);
            return Mono.just(menuNames);
        });

    return translatedNamesMono.flatMap(translatedNames -> {
        // 환율 변환 (병렬 처리)
        List<Mono<FoodItemResponse>> responseMonos = new ArrayList<>();
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem menuItem = menuItems.get(i);
            String translatedName = i < translatedNames.size()
                ? translatedNames.get(i)
                : menuItem.getName();

            Mono<FoodItemResponse> responseMono = currencyService
                .convertAndFormatAsync(menuItem.getPrice(), userCurrency)
                .map(priceInfo -> new FoodItemResponse(
                    menuItem.getName(),
                    translatedName,
                    null,  // ⚠️ description = null (Control 그룹)
                    null,  // ⚠️ imageUrl = null (Control 그룹)
                    priceInfo
                ))
                .onErrorResume(e -> Mono.just(new FoodItemResponse(
                    menuItem.getName(),
                    translatedName,
                    null,
                    null,
                    new PriceInfoResponse("N/A", "N/A")
                )));

            responseMonos.add(responseMono);
        }

        // 병렬 실행
        return Mono.zip(responseMonos, objects -> {
            List<FoodItemResponse> responses = new ArrayList<>();
            for (Object obj : objects) {
                responses.add((FoodItemResponse) obj);
            }
            return responses;
        });
    });
}
```

**성능 최적화**:
- ✅ 배치 번역 API 호출 (단건 N번 → 배치 1번)
- ✅ 환율 변환 병렬 처리 (Mono.zip)
- ✅ Control 그룹은 음식 매칭 제외 → **~2초 단축**

---

#### CurrencyService

**역할**: 환율 변환 및 포맷팅

```java
@Service
public class CurrencyService {
    /**
     * 환율 변환 및 포맷팅 (비동기)
     */
    public Mono<PriceInfoResponse> convertAndFormatAsync(
        Price price,
        Currency targetCurrency
    ) {
        // 기존 로직 재사용
        // Redis 캐싱 적용 (24시간 TTL)
    }
}
```

**캐싱 전략**:
- Redis 키: `currency:rate:{sourceCurrency}:{targetCurrency}`
- TTL: 24시간
- 캐시 미스 시 Currency API 호출

---

#### LanguageService

**역할**: 언어 목록 제공 및 번역

```java
@Service
public class LanguageService {
    /**
     * 배치 번역 (비동기)
     */
    public Flux<String> translateAsync(
        Language source,
        Language target,
        List<String> texts
    ) {
        // 기존 로직 재사용 (Google Translation API)
        // 배치 API 호출로 효율성 향상
    }
}
```

---

### 새 서비스 (추가)

#### ABTestService

**역할**: A/B 그룹 배정 및 관리

**구현 상태**: ✅ 이미 구현됨 (backend/src/main/java/foodiepass/server/abtest/application/ABTestService.java:1-150)

**핵심 메서드**:
```java
@Service
@Transactional(readOnly = true)
public class ABTestService {
    /**
     * A/B 그룹 배정 + MenuScan 생성 (원자적 처리)
     *
     * - 신규 userId: 랜덤 배정 (50:50)
     * - 기존 userId: 이전 그룹 유지
     *
     * @return 저장된 MenuScan (abGroup 포함)
     */
    @Transactional
    public MenuScan assignAndCreateScan(
        String userId,
        String imageUrl,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency
    ) {
        // 기존 스캔 조회 (동일 userId)
        Optional<MenuScan> existingScan = menuScanRepository
            .findFirstByUserIdOrderByCreatedAtDesc(userId);

        ABGroup abGroup;
        if (existingScan.isPresent()) {
            abGroup = existingScan.get().getAbGroup();
        } else {
            abGroup = randomAssign(); // 50:50 랜덤
        }

        MenuScan scan = MenuScan.create(
            userId, abGroup, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency
        );

        return menuScanRepository.save(scan);
    }

    /**
     * A/B 테스트 결과 분석 (관리자용)
     */
    public ABTestResult getResults() {
        List<Object[]> groupCounts = menuScanRepository.countGroupByAbGroup();

        long controlCount = 0;
        long treatmentCount = 0;

        for (Object[] row : groupCounts) {
            ABGroup group = (ABGroup) row[0];
            Long count = (Long) row[1];

            if (group == ABGroup.CONTROL) {
                controlCount = count;
            } else if (group == ABGroup.TREATMENT) {
                treatmentCount = count;
            }
        }

        return new ABTestResult(controlCount, treatmentCount);
    }

    private ABGroup randomAssign() {
        return ThreadLocalRandom.current().nextBoolean()
            ? ABGroup.CONTROL
            : ABGroup.TREATMENT;
    }
}
```

**설계 특징**:
- ✅ **원자적 그룹 배정**: 트랜잭션 내에서 조회 + 생성
- ✅ **일관성 보장**: 동일 userId는 항상 같은 그룹
- ✅ **Race condition 방지**: `@Transactional`로 동시 요청 처리

---

#### SurveyService

**역할**: 설문 응답 수집 및 분석

**구현 상태**: ✅ 이미 구현됨 (backend/src/main/java/foodiepass/server/survey/application/SurveyService.java:1-90)

**핵심 메서드**:
```java
@Service
@Transactional(readOnly = true)
public class SurveyService {
    /**
     * 설문 응답 저장
     *
     * @throws IllegalArgumentException scanId 존재하지 않음
     * @throws IllegalStateException 중복 응답
     */
    @Transactional
    public void saveSurveyResponse(UUID scanId, Boolean hasConfidence) {
        // 1. MenuScan 조회 (검증)
        MenuScan menuScan = menuScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException(
                "MenuScan not found with id: " + scanId));

        // 2. 중복 응답 방지
        if (surveyResponseRepository.existsByScanId(scanId)) {
            throw new IllegalStateException(
                "Survey response already exists for scan: " + scanId);
        }

        // 3. 응답 저장
        SurveyResponse response = new SurveyResponse(
            scanId,
            menuScan.getAbGroup(),
            hasConfidence
        );

        surveyResponseRepository.save(response);
    }

    /**
     * A/B 테스트 결과 분석 (H3 가설 검증)
     *
     * @return SurveyAnalytics (Control/Treatment 응답률 + Ratio)
     */
    public SurveyAnalytics getAnalytics() {
        // Control 그룹 집계
        long controlTotal = surveyResponseRepository.countByAbGroup(ABGroup.CONTROL);
        long controlYes = surveyResponseRepository
            .countByAbGroupAndHasConfidence(ABGroup.CONTROL, true);

        // Treatment 그룹 집계
        long treatmentTotal = surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT);
        long treatmentYes = surveyResponseRepository
            .countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true);

        // H3 검증 메트릭 계산
        return SurveyAnalytics.of(
            controlTotal, controlYes,
            treatmentTotal, treatmentYes
        );
    }
}
```

**H3 가설 검증**:
- **목표**: Treatment Yes Rate / Control Yes Rate ≥ 2.0
- **데이터**: SurveyAnalytics의 `ratio` 필드
- **예시**:
  - Control: 30% Yes (30/100)
  - Treatment: 80% Yes (80/100)
  - Ratio: 2.67 ✅ (H3 검증 성공)

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
**구현체**: `TasteAtlasFoodScraper`

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

## 성능 최적화

### 1. 파이프라인 분기 최적화

**Control 그룹 최적화**:
- ❌ 음식 매칭 단계 제외 → **~2초 단축**
- ✅ 번역 + 환율 변환만 실행
- ✅ API 호출 횟수 감소 (TasteAtlas 스크래핑 제외)

**예상 처리 시간**:
- Control 그룹: ~2-3초 (OCR + 번역 + 환율)
- Treatment 그룹: ~4-5초 (OCR + 번역 + 음식 매칭 + 환율)

---

### 2. 배치 API 호출

**번역 API 배치 처리**:
```java
// ❌ 단건 호출 (N번)
for (String name : menuNames) {
    translationClient.translate(name); // N번 API 호출
}

// ✅ 배치 호출 (1번)
translationClient.translateAsync(originLanguage, userLanguage, menuNames)
    .collectList(); // 1번 API 호출
```

**환율 변환 병렬 처리**:
```java
// Reactor를 활용한 병렬 환율 변환
List<Mono<FoodItemResponse>> responseMonos = menuItems.stream()
    .map(item -> currencyService.convertAndFormatAsync(item.getPrice(), targetCurrency))
    .toList();

Mono.zip(responseMonos, objects -> Arrays.asList(objects)); // 병렬 실행
```

---

### 3. Redis 캐싱 전략

#### 캐싱 대상

| 키 패턴 | 값 | TTL | 용도 | 효과 |
|---|---|---|---|---|
| `currency:rate:{from}:{to}` | 환율 (Double) | 24시간 | 환율 API 캐싱 | API 호출 ~90% 감소 |
| ~~`ab:group:{sessionId}`~~ | ~~A/B 그룹~~ | ~~30분~~ | ~~(미사용)~~ | ~~DB 기반으로 변경~~ |

**A/B 그룹 배정 방식**:
- ❌ Redis 캐싱 (초기 설계) → Race condition 위험
- ✅ **DB 기반** (최종 설계) → `MenuScan` 테이블에 저장
- ✅ `findFirstByUserIdOrderByCreatedAtDesc()` 쿼리로 기존 그룹 조회
- ✅ 트랜잭션 내에서 원자적 처리 (`@Transactional`)

**환율 캐싱 로직**:
```java
@Service
public class CurrencyService {
    private final RedisTemplate<String, Double> redisTemplate;
    private final CurrencyApiClient currencyApiClient;

    public Mono<Double> getExchangeRate(String from, String to) {
        String cacheKey = "currency:rate:" + from + ":" + to;

        // 1. Redis 캐시 조회
        Double cachedRate = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRate != null) {
            log.debug("Cache hit: {}", cacheKey);
            return Mono.just(cachedRate);
        }

        // 2. Cache miss → API 호출 + 캐싱
        return currencyApiClient.fetchRate(from, to)
            .doOnNext(rate -> {
                redisTemplate.opsForValue().set(cacheKey, rate, Duration.ofHours(24));
                log.debug("Cache updated: {}", cacheKey);
            });
    }
}
```

---

### 4. DB 인덱스 최적화

**A/B 테스트 성능 최적화**:
```sql
-- userId로 최신 스캔 조회 (ABTestService.assignAndCreateScan)
CREATE INDEX idx_menu_scans_user_id_created_at
    ON menu_scans (user_id, created_at DESC);

-- A/B 그룹별 집계 (ABTestService.getResults)
CREATE INDEX idx_menu_scans_ab_group
    ON menu_scans (ab_group);

-- scanId로 설문 응답 조회 (SurveyService.saveSurveyResponse)
CREATE INDEX idx_survey_responses_scan_id
    ON survey_responses (scan_id);

-- A/B 그룹별 설문 집계 (SurveyService.getAnalytics)
CREATE INDEX idx_survey_responses_ab_group_confidence
    ON survey_responses (ab_group, has_confidence);
```

**쿼리 성능 목표**:
- `findFirstByUserIdOrderByCreatedAtDesc`: < 10ms
- `countGroupByAbGroup`: < 50ms
- `countByAbGroupAndHasConfidence`: < 50ms

---

### 5. 비동기 처리 (Reactor)

**Reactive 스택 활용**:
```java
// 번역, 음식 매칭, 환율 변환을 비동기로 처리
return Mono.zip(
    translationClient.translateAsync(...),  // 비동기 번역
    foodScraper.scrapAsync(...),            // 비동기 스크래핑
    currencyService.convertAndFormatAsync(...) // 비동기 환율 변환
).map(tuple -> combine(tuple.getT1(), tuple.getT2(), tuple.getT3()));
```

**처리 시간 개선**:
- 순차 처리: 1s (번역) + 2s (매칭) + 0.5s (환율) = **3.5s**
- 병렬 처리: max(1s, 2s, 0.5s) = **2s** ✅

---

### 6. 캐싱 및 CDN

**이미지 캐싱** (Phase 4):
- TasteAtlas 이미지: CloudFront CDN (7일 TTL)
- 메뉴판 업로드: S3 + CloudFront (30일 TTL)

**API 응답 캐싱** (선택):
- 동일 이미지 재스캔 방지 (이미지 해시 기반 캐싱)
- 캐시 키: `menu:scan:{imageHash}`
- TTL: 1시간

---

### 성능 모니터링 메트릭

| 메트릭 | 측정 지점 | 목표 | 현재 상태 |
|-------|---------|-----|----------|
| **처리 시간 (Control)** | MenuService.scanMenu() | ≤ 3초 | TBD |
| **처리 시간 (Treatment)** | MenuService.scanMenu() | ≤ 5초 | TBD |
| **OCR 정확도** | OcrReader.read() | ≥ 90% | TBD |
| **음식 매칭 연관성** | FoodScraper.scrapAsync() | ≥ 70% | TBD |
| **환율 정확도** | CurrencyService.convert() | ≥ 95% | TBD |
| **캐시 적중률 (환율)** | Redis | ≥ 80% | TBD |
| **DB 쿼리 시간** | Repository 메서드 | < 50ms | TBD |

**로깅 예시**:
```java
@Slf4j
public class MenuService {
    public Mono<MenuScanResponse> scanMenu(MenuScanRequest request, String userId) {
        log.info("Menu scan started: userId={}, sourceLanguage={}, targetLanguage={}",
            userId, request.sourceLanguage(), request.targetLanguage());

        long startTime = System.currentTimeMillis();

        return /* ... 파이프라인 처리 ... */
            .doOnSuccess(response -> {
                long duration = System.currentTimeMillis() - startTime;
                log.info("Menu scan completed: scanId={}, abGroup={}, items={}, duration={}ms",
                    response.scanId(), response.abGroup(), response.items().size(), duration);

                // 성능 목표 검증
                if (duration > 5000) {
                    log.warn("Processing time exceeded target: {}ms > 5000ms", duration);
                }
            })
            .doOnError(e -> {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Menu scan failed: userId={}, duration={}ms, error={}",
                    userId, duration, e.getMessage(), e);
            });
    }
}
```

---

## 컨트롤러 레이어 설계

### 1. MenuController

**역할**: 메뉴 스캔 API 엔드포인트 제공

**엔드포인트**: `POST /api/menus/scan`

#### 구현 설계

```java
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Slf4j
public class MenuController {
    private final MenuService menuService;

    /**
     * 메뉴 스캔 API
     *
     * @param request 메뉴 스캔 요청 (이미지, 언어, 화폐)
     * @param request Spring Session에서 자동 생성된 세션 ID
     * @return MenuScanResponse (scanId, abGroup, items, processingTime)
     */
    @PostMapping("/scan")
    public Mono<ResponseEntity<MenuScanResponse>> scanMenu(
        @Valid @RequestBody MenuScanRequest request,
        HttpSession session
    ) {
        String sessionId = session.getId();
        log.info("Menu scan request received: sessionId={}, targetLanguage={}, targetCurrency={}",
            sessionId, request.userLanguageName(), request.userCurrencyName());

        return menuService.scanMenu(request, sessionId)
            .map(response -> {
                log.info("Menu scan completed: scanId={}, abGroup={}, items={}",
                    response.scanId(), response.abGroup(), response.items().size());
                return ResponseEntity.ok(response);
            })
            .onErrorResume(this::handleError);
    }

    /**
     * 에러 처리
     */
    private Mono<ResponseEntity<MenuScanResponse>> handleError(Throwable error) {
        log.error("Menu scan failed: {}", error.getMessage(), error);

        // GlobalExceptionHandler로 위임
        return Mono.error(error);
    }
}
```

#### 입력 검증

**MenuScanRequest DTO**:
```java
public record MenuScanRequest(
    @NotBlank(message = "base64EncodedImage must not be blank")
    String base64EncodedImage,

    String originLanguageName,  // Optional, default "auto"

    @NotBlank(message = "userLanguageName must not be blank")
    String userLanguageName,

    String originCurrencyName,  // Optional, default "auto"

    @NotBlank(message = "userCurrencyName must not be blank")
    String userCurrencyName
) {
    /**
     * Canonical constructor with validation
     */
    public MenuScanRequest {
        // 선택 필드 기본값 설정
        if (originLanguageName == null || originLanguageName.isBlank()) {
            originLanguageName = "auto";
        }
        if (originCurrencyName == null || originCurrencyName.isBlank()) {
            originCurrencyName = "auto";
        }
    }
}
```

**검증 규칙**:
- `@Valid` 어노테이션으로 자동 검증 활성화
- `@NotBlank`: null, 빈 문자열, 공백만 있는 문자열 거부
- Canonical constructor에서 선택 필드 기본값 설정

---

### 2. SurveyController

**역할**: 설문 제출 및 분석 API 제공

**엔드포인트**:
- `POST /api/surveys` - 설문 응답 제출
- `GET /api/admin/surveys/analytics` - 설문 분석 결과 (관리자용)

#### 구현 설계

```java
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Slf4j
public class SurveyController {
    private final SurveyService surveyService;

    /**
     * 설문 응답 제출
     *
     * @param request 설문 응답 (scanId, hasConfidence)
     * @return SurveySubmitResponse (success, message)
     */
    @PostMapping
    public ResponseEntity<SurveySubmitResponse> submitSurvey(
        @Valid @RequestBody SurveyRequest request
    ) {
        log.info("Survey submission received: scanId={}, hasConfidence={}",
            request.scanId(), request.hasConfidence());

        surveyService.saveSurveyResponse(request.scanId(), request.hasConfidence());

        log.info("Survey submitted successfully: scanId={}", request.scanId());
        return ResponseEntity.ok(new SurveySubmitResponse(
            true,
            "Survey response recorded successfully."
        ));
    }

    /**
     * 설문 분석 결과 조회 (관리자용)
     *
     * @return SurveyAnalytics (Control/Treatment 응답률 + Ratio)
     */
    @GetMapping("/admin/analytics")
    public ResponseEntity<SurveyAnalytics> getAnalytics() {
        log.info("Survey analytics requested (admin)");

        SurveyAnalytics analytics = surveyService.getAnalytics();

        log.info("Survey analytics retrieved: controlYesRate={}, treatmentYesRate={}, ratio={}",
            analytics.getControl().getYesRate(),
            analytics.getTreatment().getYesRate(),
            analytics.getRatio());

        return ResponseEntity.ok(analytics);
    }
}
```

#### 입력 검증

**SurveyRequest DTO**:
```java
public record SurveyRequest(
    @NotNull(message = "scanId must not be null")
    UUID scanId,

    @NotNull(message = "hasConfidence must not be null")
    Boolean hasConfidence
) {}
```

**검증 규칙**:
- `@NotNull`: null 값 거부 (UUID, Boolean 타입)
- Spring Validation이 컨트롤러 진입 전 자동 검증

---

### 3. ABTestController

**역할**: A/B 테스트 결과 조회 (관리자용)

**엔드포인트**: `GET /api/admin/ab-test/results`

#### 구현 설계

```java
@RestController
@RequestMapping("/api/admin/ab-test")
@RequiredArgsConstructor
@Slf4j
public class ABTestController {
    private final ABTestService abTestService;

    /**
     * A/B 테스트 결과 조회 (관리자용)
     *
     * @return ABTestResult (controlCount, treatmentCount, totalScans)
     */
    @GetMapping("/results")
    public ResponseEntity<ABTestResult> getResults() {
        log.info("A/B test results requested (admin)");

        ABTestResult result = abTestService.getResults();

        log.info("A/B test results retrieved: control={}, treatment={}, total={}",
            result.controlCount(), result.treatmentCount(), result.totalScans());

        return ResponseEntity.ok(result);
    }
}
```

**참고**: MVP에서는 인증 없이 구현, Phase 4에서 Spring Security 추가 가능

---

## 예외 처리 전략

### GlobalExceptionHandler

**역할**: 전역 예외 처리 및 에러 응답 표준화

#### 구현 설계

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Validation 실패 (입력 검증 오류)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", message);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                "BAD_REQUEST",
                message,
                Instant.now()
            ));
    }

    /**
     * IllegalArgumentException (잘못된 인자)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                "BAD_REQUEST",
                ex.getMessage(),
                Instant.now()
            ));
    }

    /**
     * IllegalStateException (잘못된 상태)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
        IllegalStateException ex
    ) {
        log.warn("Illegal state: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                "BAD_REQUEST",
                ex.getMessage(),
                Instant.now()
            ));
    }

    /**
     * EntityNotFoundException (리소스 없음)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
        EntityNotFoundException ex
    ) {
        log.warn("Entity not found: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                "NOT_FOUND",
                ex.getMessage(),
                Instant.now()
            ));
    }

    /**
     * PayloadTooLargeException (요청 크기 초과)
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
        MaxUploadSizeExceededException ex
    ) {
        log.warn("Payload too large: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(new ErrorResponse(
                "PAYLOAD_TOO_LARGE",
                "Image size exceeds 10MB limit.",
                Instant.now()
            ));
    }

    /**
     * 외부 API 연동 실패
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientException(
        WebClientResponseException ex
    ) {
        log.error("External API call failed: status={}, message={}",
            ex.getStatusCode(), ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse(
                "SERVICE_UNAVAILABLE",
                "External service temporarily unavailable. Please try again later.",
                Instant.now()
            ));
    }

    /**
     * 일반 예외 (서버 내부 오류)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
        Exception ex
    ) {
        log.error("Unexpected error occurred", ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again.",
                Instant.now()
            ));
    }
}
```

#### ErrorResponse DTO

```java
public record ErrorResponse(
    String error,        // Error code (e.g., "BAD_REQUEST")
    String message,      // Human-readable error message
    Instant timestamp    // Error timestamp
) {}
```

---

### Custom Exceptions

**새로 추가할 Custom Exception**:

```java
/**
 * 리소스를 찾을 수 없을 때 발생
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
```

**사용 예시**:
```java
@Service
public class SurveyService {
    public void saveSurveyResponse(UUID scanId, Boolean hasConfidence) {
        MenuScan menuScan = menuScanRepository.findById(scanId)
            .orElseThrow(() -> new EntityNotFoundException(
                "MenuScan not found with id: " + scanId));

        // ...
    }
}
```

---

### 에러 응답 표준화

#### HTTP Status Code 매핑

| Exception | HTTP Status | Error Code |
|-----------|-------------|------------|
| `MethodArgumentNotValidException` | 400 | `BAD_REQUEST` |
| `IllegalArgumentException` | 400 | `BAD_REQUEST` |
| `IllegalStateException` | 400 | `BAD_REQUEST` |
| `EntityNotFoundException` | 404 | `NOT_FOUND` |
| `MaxUploadSizeExceededException` | 413 | `PAYLOAD_TOO_LARGE` |
| `WebClientResponseException` | 503 | `SERVICE_UNAVAILABLE` |
| `Exception` (general) | 500 | `INTERNAL_SERVER_ERROR` |

#### 에러 응답 예시

**400 Bad Request (Validation 실패)**:
```json
{
  "error": "BAD_REQUEST",
  "message": "base64EncodedImage: must not be blank",
  "timestamp": "2025-11-03T12:34:56Z"
}
```

**404 Not Found (scanId 없음)**:
```json
{
  "error": "NOT_FOUND",
  "message": "MenuScan not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-11-03T12:34:56Z"
}
```

**500 Internal Server Error**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred. Please try again.",
  "timestamp": "2025-11-03T12:34:56Z"
}
```

---

## 입력 검증 (Validation)

### Validation 전략

#### 1. Request DTO 검증

**Spring Validation 어노테이션 활용**:
- `@Valid`: 컨트롤러 메서드 파라미터에 지정
- `@NotNull`: null 값 거부
- `@NotBlank`: null, 빈 문자열, 공백 거부
- `@Size`: 문자열 길이 제한
- `@Pattern`: 정규식 검증

**적용 위치**:
```java
@PostMapping("/scan")
public Mono<ResponseEntity<MenuScanResponse>> scanMenu(
    @Valid @RequestBody MenuScanRequest request,  // ← @Valid 활성화
    HttpSession session
) { ... }
```

#### 2. 이미지 크기 검증

**application.yml 설정**:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

**자동 검증**:
- Spring Boot가 자동으로 크기 초과 시 `MaxUploadSizeExceededException` 발생
- GlobalExceptionHandler에서 413 응답 반환

#### 3. 비즈니스 로직 검증

**Service 레이어에서 검증**:
```java
@Service
public class SurveyService {
    @Transactional
    public void saveSurveyResponse(UUID scanId, Boolean hasConfidence) {
        // 1. scanId 검증
        MenuScan menuScan = menuScanRepository.findById(scanId)
            .orElseThrow(() -> new EntityNotFoundException(
                "MenuScan not found with id: " + scanId));

        // 2. 중복 응답 방지
        if (surveyResponseRepository.existsByScanId(scanId)) {
            throw new IllegalStateException(
                "Survey response already exists for scan: " + scanId);
        }

        // 3. 저장
        surveyResponseRepository.save(new SurveyResponse(scanId, menuScan.getAbGroup(), hasConfidence));
    }
}
```

---

### Validation 테스트

**단위 테스트 예시**:
```java
@Test
void scanMenu_WithInvalidRequest_ShouldReturn400() throws Exception {
    MenuScanRequest invalidRequest = new MenuScanRequest(
        "",  // ← 빈 문자열 (검증 실패)
        "auto",
        "Korean",
        "auto",
        "KRW Won"
    );

    mockMvc.perform(post("/api/menus/scan")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value(containsString("base64EncodedImage")));
}
```

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
