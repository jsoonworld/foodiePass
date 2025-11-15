# Agent 4 (Integration): MenuController Integration & E2E Tests

## Objective
Integrate A/B testing into MenuController and create comprehensive end-to-end tests for the entire menu scanning pipeline with Control/Treatment group differentiation.

## Scope
✅ **In Scope**:
- Modify MenuController to add `/scan` endpoint
- A/B group assignment integration
- Conditional response logic (CONTROL vs TREATMENT)
- Full pipeline integration tests
- E2E tests for both groups
- Performance validation (<5 seconds)

❌ **Out of Scope**:
- Service layer implementation (Agent 1/2 already completed)
- Controller-only unit tests (Agent 3 already completed)

## Dependencies
**Required**: Agent 1 (ABTestService), Agent 2 (SurveyService), Agent 3 (Controllers), existing MenuService

**Execution Order**: Must wait for Agent 1/2/3 to complete before starting

---

## Files to Modify

### MenuController.java
```
src/main/java/foodiepass/server/menu/api/MenuController.java
```

**Changes**:
- Add new endpoint: `POST /api/menus/scan`
- Integrate ABTestService for group assignment
- Integrate MenuService for processing
- Conditional response based on A/B group

---

## Files to Create

### 1. DTOs
```
src/main/java/foodiepass/server/menu/api/dto/
├── request/
│   └── MenuScanRequest.java
└── response/
    ├── MenuScanResponse.java
    └── MenuItemResponse.java
```

### 2. Integration Tests
```
src/test/java/foodiepass/server/menu/integration/
├── MenuScanIntegrationTest.java
├── ControlGroupIntegrationTest.java
└── TreatmentGroupIntegrationTest.java
```

### 3. E2E Tests
```
src/test/java/foodiepass/server/e2e/
└── MenuScanE2ETest.java
```

---

## Implementation Details

### MenuScanRequest.java

**File**: `src/main/java/foodiepass/server/menu/api/dto/request/MenuScanRequest.java`

```java
package foodiepass.server.menu.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Menu scan request
 */
public record MenuScanRequest(
    @NotBlank(message = "image is required")
    String image,  // base64 encoded

    @NotBlank(message = "sourceLanguage is required")
    String sourceLanguage,  // "en", "auto", etc.

    @NotBlank(message = "targetLanguage is required")
    String targetLanguage,  // "ko", "en", etc.

    @NotBlank(message = "sourceCurrency is required")
    String sourceCurrency,  // "USD", "EUR", etc.

    @NotBlank(message = "targetCurrency is required")
    String targetCurrency  // "KRW", "USD", etc.
) {}
```

---

### MenuItemResponse.java

**File**: `src/main/java/foodiepass/server/menu/api/dto/response/MenuItemResponse.java`

```java
package foodiepass.server.menu.api.dto.response;

import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.PriceInfo;

/**
 * Menu item response (Control or Treatment)
 */
public record MenuItemResponse(
    String originalName,
    String translatedName,
    String description,       // null for CONTROL
    String imageUrl,          // null for CONTROL
    String previewImageUrl,   // null for CONTROL
    PriceInfo priceInfo
) {
    /**
     * Create response for CONTROL group (no FoodInfo)
     */
    public static MenuItemResponse forControlGroup(MenuItem item) {
        return new MenuItemResponse(
            item.name().original(),
            item.name().translated(),
            null,  // No description
            null,  // No imageUrl
            null,  // No previewImageUrl
            item.priceInfo()
        );
    }

    /**
     * Create response for TREATMENT group (full FoodInfo)
     */
    public static MenuItemResponse forTreatmentGroup(MenuItem item) {
        FoodInfo food = item.foodInfo();
        return new MenuItemResponse(
            item.name().original(),
            item.name().translated(),
            food != null ? food.description() : null,
            food != null ? food.imageUrl() : null,
            food != null ? food.previewImageUrl() : null,
            item.priceInfo()
        );
    }
}
```

---

### MenuScanResponse.java

**File**: `src/main/java/foodiepass/server/menu/api/dto/response/MenuScanResponse.java`

```java
package foodiepass.server.menu.api.dto.response;

import foodiepass.server.abtest.domain.ABGroup;

import java.util.List;
import java.util.UUID;

/**
 * Menu scan response with A/B group info
 */
public record MenuScanResponse(
    UUID scanId,
    ABGroup abGroup,
    List<MenuItemResponse> items,
    double processingTime  // in seconds
) {}
```

---

### MenuController Modification

**File**: `src/main/java/foodiepass/server/menu/api/MenuController.java`

**New Endpoint**: `POST /api/menus/scan`

```java
@PostMapping("/scan")
public Mono<MenuScanResponse> scanMenu(
    @RequestBody @Valid MenuScanRequest request
) {
    long startTime = System.currentTimeMillis();

    // 1. Get or create userId (from session or generate)
    String userId = UUID.randomUUID().toString();  // Simplified for MVP

    // 2. Assign A/B group
    ABGroup abGroup = abTestService.assignGroup(userId);

    // 3. Create MenuScan record
    MenuScan scan = abTestService.createScan(
        userId,
        abGroup,
        null,  // imageUrl (not stored for MVP)
        request.sourceLanguage(),
        request.targetLanguage(),
        request.sourceCurrency(),
        request.targetCurrency()
    );

    // 4. Process menu through existing pipeline
    return menuService.reconfigure(
        request.image(),
        Language.of(request.sourceLanguage()),
        Language.of(request.targetLanguage()),
        Currency.of(request.sourceCurrency()),
        Currency.of(request.targetCurrency())
    )
    .map(menuItems -> {
        // 5. Conditionally filter FoodInfo based on A/B group
        List<MenuItemResponse> items = menuItems.stream()
            .map(item -> abGroup == ABGroup.CONTROL
                ? MenuItemResponse.forControlGroup(item)
                : MenuItemResponse.forTreatmentGroup(item))
            .toList();

        // 6. Calculate processing time
        long endTime = System.currentTimeMillis();
        double processingTime = (endTime - startTime) / 1000.0;

        // 7. Return response with scanId
        return new MenuScanResponse(
            scan.getId(),
            abGroup,
            items,
            processingTime
        );
    });
}
```

**Key Logic**:
- CONTROL group: Strip FoodInfo fields (description, imageUrl, previewImageUrl)
- TREATMENT group: Include full FoodInfo
- Processing time must be <5 seconds (H2 requirement)

---

## Test Requirements

### MenuScanIntegrationTest.java

**File**: `src/test/java/foodiepass/server/menu/integration/MenuScanIntegrationTest.java`

**Purpose**: Test full pipeline with real services, mock external APIs only

**Setup**:
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class MenuScanIntegrationTest {

    @Autowired
    private MenuController menuController;

    @Autowired
    private ABTestService abTestService;

    @Autowired
    private SurveyService surveyService;

    @MockBean
    private OcrReader ocrReader;

    @MockBean
    private TranslationClient translationClient;

    @MockBean
    private FoodScraper foodScrapper;

    @MockBean
    private ExchangeRateProvider exchangeRateProvider;

    @BeforeEach
    void setUp() {
        // Mock external API responses
    }
}
```

**Test Cases**:
```java
@Test
@DisplayName("Full pipeline - CONTROL group strips FoodInfo")
void fullPipeline_ControlGroup() {
    // Given: Mock external APIs
    // When: Call /scan endpoint
    // Then: Response has no FoodInfo fields
}

@Test
@DisplayName("Full pipeline - TREATMENT group includes FoodInfo")
void fullPipeline_TreatmentGroup() {
    // Given: Mock external APIs
    // When: Call /scan endpoint
    // Then: Response has FoodInfo fields
}

@Test
@DisplayName("MenuScan record is created in database")
void scanRecordPersisted() {
    // Verify MenuScan entity is saved with correct abGroup
}

@Test
@DisplayName("Processing time is under 5 seconds")
void processingTimeUnderFiveSeconds() {
    // Measure actual processing time
    // Assert < 5.0 seconds
}
```

**Coverage Target**: >80%

---

### ControlGroupIntegrationTest.java

**File**: `src/main/java/foodiepass/server/menu/integration/ControlGroupIntegrationTest.java`

**Purpose**: Specific tests for CONTROL group behavior

**Test Cases**:
```java
@Test
@DisplayName("CONTROL response has no description")
void controlNoDescription() {
    // Assert description field is null
}

@Test
@DisplayName("CONTROL response has no imageUrl")
void controlNoImageUrl() {
    // Assert imageUrl field is null
}

@Test
@DisplayName("CONTROL response has no previewImageUrl")
void controlNoPreviewImageUrl() {
    // Assert previewImageUrl field is null
}

@Test
@DisplayName("CONTROL response has priceInfo")
void controlHasPriceInfo() {
    // Assert priceInfo exists with original + converted
}

@Test
@DisplayName("CONTROL response has translation")
void controlHasTranslation() {
    // Assert translatedName exists
}
```

**Coverage Target**: 100% of CONTROL path

---

### TreatmentGroupIntegrationTest.java

**File**: `src/test/java/foodiepass/server/menu/integration/TreatmentGroupIntegrationTest.java`

**Purpose**: Specific tests for TREATMENT group behavior

**Test Cases**:
```java
@Test
@DisplayName("TREATMENT response has description")
void treatmentHasDescription() {
    // Assert description is not null
}

@Test
@DisplayName("TREATMENT response has imageUrl")
void treatmentHasImageUrl() {
    // Assert imageUrl is valid URL
}

@Test
@DisplayName("TREATMENT response has previewImageUrl")
void treatmentHasPreviewImageUrl() {
    // Assert previewImageUrl is valid URL
}

@Test
@DisplayName("TREATMENT response has priceInfo")
void treatmentHasPriceInfo() {
    // Assert priceInfo exists
}

@Test
@DisplayName("TREATMENT response has translation")
void treatmentHasTranslation() {
    // Assert translatedName exists
}

@Test
@DisplayName("FoodScraper is called for TREATMENT group")
void treatmentCallsScrapper() {
    // Verify foodScrapper.scrape() was called
}
```

**Coverage Target**: 100% of TREATMENT path

---

### MenuScanE2ETest.java

**File**: `src/test/java/foodiepass/server/e2e/MenuScanE2ETest.java`

**Purpose**: End-to-end validation with real external APIs (optional, expensive)

**Setup**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")  // Uses real API keys
class MenuScanE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("E2E - Upload real menu image")
    void e2eRealMenu() {
        // Upload real menu image
        // Verify response with real OCR, translation, scraping
        // This test is expensive, run manually or in nightly CI
    }
}
```

**Coverage Target**: N/A (validation, not coverage)

---

## Performance Testing

### Processing Time Test
```java
@Test
@DisplayName("Processing time is under 5 seconds")
void processingTimeRequirement() {
    long startTime = System.currentTimeMillis();

    MenuScanResponse response = menuController
        .scanMenu(validRequest)
        .block();

    long endTime = System.currentTimeMillis();
    double actualTime = (endTime - startTime) / 1000.0;

    // H2 requirement: <5 seconds
    assertThat(actualTime).isLessThan(5.0);

    // Response should report similar time
    assertThat(response.processingTime())
        .isCloseTo(actualTime, within(0.5));
}
```

---

## Success Criteria

### Implementation Checklist
- [ ] New `/scan` endpoint added to MenuController
- [ ] ABTestService integrated for group assignment
- [ ] MenuService integrated for processing
- [ ] Conditional response logic (CONTROL vs TREATMENT)
- [ ] Processing time tracked and returned
- [ ] MenuScan record created for each scan
- [ ] DTOs match specification (MenuScanRequest/Response/MenuItemResponse)

### Testing Checklist
- [ ] Integration tests pass (MenuScanIntegrationTest)
- [ ] CONTROL group tests pass (all FoodInfo fields null)
- [ ] TREATMENT group tests pass (all FoodInfo fields present)
- [ ] Processing time <5 seconds validated
- [ ] A/B group distribution is ~50:50
- [ ] Test coverage >80% for integration layer

### Quality Checklist
- [ ] No business logic in controller (delegates to services)
- [ ] Clean separation of concerns
- [ ] Error handling is comprehensive
- [ ] Performance requirement met (<5s)
- [ ] Integration tests use real services (only mock external APIs)

---

## Integration Points

### With ABTest Module (Agent 1)
- Call `ABTestService.assignGroup(userId)` before processing
- Call `ABTestService.createScan(...)` to persist scan record
- Return `scanId` in response for survey linking

### With Survey Module (Agent 2)
- Frontend uses `scanId` from response to submit survey
- Survey links back to MenuScan for analytics

### With Menu Module (Existing v1)
- Reuse existing `MenuService.reconfigure()` method
- No changes to existing menu processing logic
- Conditionally map `MenuItem` to `MenuItemResponse` based on A/B group

---

## Timeline

**Estimated Time**: 8 hours

**Breakdown**:
- DTOs: 1 hour
- MenuController modifications: 2 hours
- Integration tests: 3 hours
- CONTROL/TREATMENT path tests: 1.5 hours
- Performance validation: 30 minutes

**Prerequisite**: Agent 1/2/3 must be completed

---

## Deliverables

1. ✅ Modified MenuController with `/scan` endpoint
2. ✅ New DTOs (MenuScanRequest, MenuScanResponse, MenuItemResponse)
3. ✅ Integration tests with >80% coverage
4. ✅ Separate CONTROL/TREATMENT path tests
5. ✅ All tests passing
6. ✅ Performance validated (<5s processing time)
7. ✅ Code ready for deployment

---

## Notes

- **Focus on hypothesis validation** (H1, H2, H3)
- **Keep existing endpoints intact** (backward compatibility)
- **Use reactive programming** (Mono, Flux) for consistency
- **Document CONTROL vs TREATMENT logic clearly**
- **Measure and log processing time** for H2 validation
- **Real services in integration tests**, only mock external APIs
- **No E2E tests with real APIs** in standard CI (too expensive)
