# Agent 3: Integration Tests & MenuController Modifications

## Objective
Integrate A/B testing into MenuController and create comprehensive integration tests for the entire menu scanning pipeline.

## Context
- **New Endpoint**: `POST /api/menus/scan` (replaces or extends existing menu upload)
- **A/B Logic**: Assign user to group → Process menu → Return results based on group
- **CONTROL Group**: Returns text translation + currency only (no FoodInfo)
- **TREATMENT Group**: Returns full response (text + FoodInfo with photos/descriptions)

## Files to Modify

### 1. MenuController.java
```text
src/main/java/foodiepass/server/menu/api/MenuController.java
```

**Changes**:
- Add new endpoint: `POST /api/menus/scan`
- Integrate `ABTestService` to assign group and create scan
- Conditionally include/exclude `FoodInfo` based on A/B group
- Return `scanId` in response for survey linking

## Files to Create

### 1. DTO Layer
```text
src/main/java/foodiepass/server/menu/api/dto/
├── request/
│   └── MenuScanRequest.java
└── response/
    ├── MenuScanResponse.java
    └── MenuItemResponse.java
```

### 2. Integration Tests
```text
src/test/java/foodiepass/server/menu/
├── integration/
│   ├── MenuScanIntegrationTest.java
│   ├── ControlGroupIntegrationTest.java
│   └── TreatmentGroupIntegrationTest.java
└── api/
    └── MenuControllerTest.java (new tests for /scan endpoint)
```

### 3. E2E Tests
```text
src/test/java/foodiepass/server/e2e/
└── MenuScanE2ETest.java
```

## Implementation Details

### MenuController Modifications

**New Endpoint**: `POST /api/menus/scan`

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

**Response (CONTROL group)**:
```json
{
  "scanId": "uuid",
  "abGroup": "CONTROL",
  "items": [
    {
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "priceInfo": {
        "original": "$15.00",
        "converted": "₩20,000"
      }
    }
  ],
  "processingTime": 4.2
}
```

**Response (TREATMENT group)**:
```json
{
  "scanId": "uuid",
  "abGroup": "TREATMENT",
  "items": [
    {
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "description": "Classic pizza with tomato sauce, mozzarella cheese, and fresh basil",
      "imageUrl": "https://tasteatlas.com/.../margherita.jpg",
      "previewImageUrl": "https://tasteatlas.com/.../margherita-thumb.jpg",
      "priceInfo": {
        "original": "$15.00",
        "converted": "₩20,000"
      }
    }
  ],
  "processingTime": 4.2
}
```

**Processing Steps**:
```java
@PostMapping("/scan")
public Mono<MenuScanResponse> scanMenu(@RequestBody @Valid MenuScanRequest request) {
    // 1. Get or create userId (from session or generate new UUID)
    String userId = sessionService.getUserId();  // or generate UUID

    // 2. Assign A/B group
    ABGroup abGroup = abTestService.assignGroup(userId);

    // 3. Create MenuScan record
    MenuScan scan = abTestService.createScan(userId, abGroup,
                                              request.image(),
                                              request.sourceLanguage(),
                                              request.targetLanguage(),
                                              request.sourceCurrency(),
                                              request.targetCurrency());

    // 4. Process menu (existing MenuService.reconfigure)
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
                ? MenuItemResponse.forControlGroup(item)  // Strip FoodInfo
                : MenuItemResponse.forTreatmentGroup(item))  // Full info
            .toList();

        // 6. Return response with scanId
        return MenuScanResponse.of(scan.getId(), abGroup, items, processingTime);
    });
}
```

**Key Logic**:
- **Session Management**: Use Redis or in-memory session to track `userId`
- **A/B Assignment**: Delegate to `ABTestService.assignGroup(userId)`
- **Conditional Response**:
  - CONTROL: Strip `description`, `imageUrl`, `previewImageUrl` from response
  - TREATMENT: Include full `FoodInfo`
- **Performance Tracking**: Measure processing time (should be <5s)

### DTOs

#### MenuScanRequest.java
```java
public record MenuScanRequest(
    @NotBlank String image,          // base64 encoded
    @NotBlank String sourceLanguage, // "en", "auto", etc.
    @NotBlank String targetLanguage, // "ko", "en", etc.
    @NotBlank String sourceCurrency, // "USD", "EUR", etc.
    @NotBlank String targetCurrency  // "KRW", "USD", etc.
) {}
```

#### MenuItemResponse.java
```java
public record MenuItemResponse(
    String originalName,
    String translatedName,
    String description,       // null for CONTROL
    String imageUrl,          // null for CONTROL
    String previewImageUrl,   // null for CONTROL
    PriceInfo priceInfo
) {
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

#### MenuScanResponse.java
```java
public record MenuScanResponse(
    UUID scanId,
    ABGroup abGroup,
    List<MenuItemResponse> items,
    double processingTime  // in seconds
) {
    public static MenuScanResponse of(UUID scanId, ABGroup abGroup,
                                      List<MenuItemResponse> items,
                                      double processingTime) {
        return new MenuScanResponse(scanId, abGroup, items, processingTime);
    }
}
```

## Test Requirements

### MenuControllerTest.java (Unit Tests)
**New Test Cases for /scan endpoint**:

- `testScanMenu_Success()`: Returns 200 with valid response
- `testScanMenu_ControlGroup()`: Response excludes FoodInfo fields
- `testScanMenu_TreatmentGroup()`: Response includes FoodInfo fields
- `testScanMenu_InvalidImage()`: Returns 400 with validation error
- `testScanMenu_MissingFields()`: Returns 400 with validation error
- `testScanMenu_InvalidLanguage()`: Returns 400 with error
- `testScanMenu_InvalidCurrency()`: Returns 400 with error
- `testScanMenu_ProcessingTime()`: Completes within 5 seconds

**Mocking**:
- Mock `ABTestService` (assignGroup, createScan)
- Mock `MenuService` (reconfigure)
- Use `@WebMvcTest(MenuController.class)`
- Use `MockMvc` for HTTP requests

**Coverage Target**: >85%

### MenuScanIntegrationTest.java (Integration Tests)
**Purpose**: Test full pipeline from request to response (no mocks)

**Setup**:
- Use `@SpringBootTest`
- Use real services (MenuService, ABTestService, etc.)
- Use H2 in-memory database
- Mock external APIs only (OCR, Translation, TasteAtlas, Currency)

**Test Cases**:
- `testFullPipeline_ControlGroup()`: End-to-end for CONTROL
- `testFullPipeline_TreatmentGroup()`: End-to-end for TREATMENT
- `testABGroupAssignment()`: Verify random 50:50 distribution
- `testScanRecordCreated()`: MenuScan persisted to database
- `testOcrIntegration()`: OCR extracts menu items correctly
- `testTranslationIntegration()`: Translation applied correctly
- `testFoodScrapingIntegration()`: Food info scraped correctly
- `testCurrencyConversionIntegration()`: Currency converted correctly
- `testProcessingTime()`: Pipeline completes in <5 seconds

**Mock External APIs**:
```java
@MockBean
private OcrReader ocrReader;

@MockBean
private TranslationClient translationClient;

@MockBean
private FoodScrapper foodScrapper;

@MockBean
private ExchangeRateProvider exchangeRateProvider;
```

**Coverage Target**: >80%

### ControlGroupIntegrationTest.java
**Purpose**: Specific tests for CONTROL group behavior

**Test Cases**:
- `testControlGroupResponse_NoFoodInfo()`: FoodInfo fields are null
- `testControlGroupResponse_HasPriceInfo()`: PriceInfo is included
- `testControlGroupResponse_HasTranslation()`: Translation is included
- `testControlGroup_NoScrapingCalls()`: FoodScrapper is NOT called (performance)

**Coverage Target**: 100% of CONTROL path

### TreatmentGroupIntegrationTest.java
**Purpose**: Specific tests for TREATMENT group behavior

**Test Cases**:
- `testTreatmentGroupResponse_HasFoodInfo()`: FoodInfo fields are included
- `testTreatmentGroupResponse_HasPriceInfo()`: PriceInfo is included
- `testTreatmentGroupResponse_HasTranslation()`: Translation is included
- `testTreatmentGroup_ScrapingCalled()`: FoodScrapper is called
- `testTreatmentGroup_ImageUrlValid()`: imageUrl is valid URL
- `testTreatmentGroup_DescriptionNotEmpty()`: description is not empty

**Coverage Target**: 100% of TREATMENT path

### MenuScanE2ETest.java (End-to-End Tests)
**Purpose**: Full E2E test with real external APIs (optional, for manual testing)

**Setup**:
- Use `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`
- Use real external APIs (requires API keys)
- Use TestRestTemplate for HTTP requests

**Test Cases**:
- `testE2E_RealMenuImage()`: Upload real menu image, verify response
- `testE2E_ControlGroup()`: Verify CONTROL group response
- `testE2E_TreatmentGroup()`: Verify TREATMENT group response
- `testE2E_ProcessingTime()`: Measure actual processing time
- `testE2E_SurveySubmission()`: Submit survey after menu scan

**Note**: These tests are expensive (API calls), run manually or in CI with limited frequency

**Coverage Target**: N/A (E2E validation, not coverage)

## Performance Testing

### Processing Time Test
```java
@Test
void testProcessingTime_UnderFiveSeconds() {
    long startTime = System.currentTimeMillis();

    MenuScanResponse response = menuController.scanMenu(validRequest).block();

    long endTime = System.currentTimeMillis();
    double processingTime = (endTime - startTime) / 1000.0;

    assertThat(processingTime).isLessThan(5.0);
    assertThat(response.processingTime()).isCloseTo(processingTime, within(0.5));
}
```

### Load Test (Optional)
- Use JMeter or Gatling
- Simulate 100 concurrent requests
- Verify all complete within 5 seconds
- Monitor CPU, memory, DB connections

## Code Style Guidelines

### Naming Conventions
- Class names: PascalCase
- Method names: camelCase
- Test methods: `test{What}_{Condition}()` or `test{What}()` for simple cases

### Annotations
- Use `@WebMvcTest` for controller unit tests
- Use `@SpringBootTest` for integration tests
- Use `@DataJpaTest` for repository tests
- Use `@MockBean` for mocking beans in Spring context

### Documentation
- Add Javadoc for public methods
- Explain test scenarios in comments
- Document expected behavior for CONTROL vs TREATMENT

### Error Handling
- Test all error cases (invalid inputs, missing data)
- Verify error responses are user-friendly
- Log errors appropriately

## Success Criteria

### Implementation
- ✅ New `/scan` endpoint implemented
- ✅ A/B group assignment integrated
- ✅ Conditional response logic (CONTROL vs TREATMENT)
- ✅ MenuScan record created for each scan
- ✅ Processing time tracked and returned

### Testing
- ✅ Unit tests for MenuController >85% coverage
- ✅ Integration tests for full pipeline >80% coverage
- ✅ Separate tests for CONTROL and TREATMENT paths
- ✅ All tests pass
- ✅ No skipped or disabled tests

### Quality
- ✅ No code smells
- ✅ Clean separation of concerns (Controller → Service → Repository)
- ✅ Error handling is comprehensive
- ✅ Performance requirement met (<5s processing time)

## Integration Points

### With ABTest Module
- Call `ABTestService.assignGroup(userId)` before processing
- Call `ABTestService.createScan(...)` to persist scan record
- Return `scanId` in response for survey linking

### With Survey Module
- Frontend uses `scanId` to submit survey response
- Survey links back to MenuScan for analytics

### With Menu Module
- Reuse existing `MenuService.reconfigure()` method
- Conditionally map `MenuItem` to `MenuItemResponse` based on A/B group
- No changes to existing menu processing logic

## Performance Requirements
- Processing time: <5 seconds (H2 requirement)
- A/B group assignment: <10ms
- Scan record creation: <50ms
- Pipeline (OCR + Translation + Scraping + Currency): <4.5s

## Security Considerations
- Input validation for all fields (image, languages, currencies)
- Sanitize base64 image input (prevent XSS)
- Rate limiting for scan endpoint (prevent abuse)
- Session management for userId (prevent session hijacking)

## Timeline
**Estimated Time**: 1 day (Day 8)

**Breakdown**:
- MenuController modifications: 3 hours
- DTOs: 1 hour
- Unit tests: 2 hours
- Integration tests: 4 hours
- Performance validation: 1 hour
- E2E manual testing: 1 hour

## Deliverables
1. Modified MenuController with `/scan` endpoint
2. New DTOs (MenuScanRequest, MenuScanResponse, MenuItemResponse)
3. Unit tests for MenuController >85% coverage
4. Integration tests for full pipeline >80% coverage
5. All tests passing
6. Performance validated (<5s processing time)
7. Code ready for code review

## Notes
- Focus on hypothesis validation (H1, H2, H3)
- Keep existing menu endpoints intact (backward compatibility)
- Use reactive programming (Mono, Flux) for consistency
- Follow existing project patterns
- Document CONTROL vs TREATMENT logic clearly
- Measure and log processing time for H2 validation
