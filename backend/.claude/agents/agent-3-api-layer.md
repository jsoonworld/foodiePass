# Agent 3 (API Layer): REST Controllers for ABTest & Survey

## Objective
Implement REST API controllers for ABTest and Survey modules, providing admin analytics and survey submission endpoints.

## Scope
✅ **In Scope**:
- ABTest Controller (admin analytics endpoint)
- Survey Controller (submission + analytics endpoints)
- Request/Response DTOs
- Controller unit tests (>85% coverage)

❌ **Out of Scope**:
- Service layer implementation (handled by Agent 1/2)
- Integration tests (handled by Agent 4)
- MenuController modifications (handled by Agent 4)

## Dependencies
**Required**: Agent 1 (ABTestService) and Agent 2 (SurveyService)

**Strategy**: Can start development using interface contracts from `docs/INTERFACE_CONTRACT.md` and mock services in tests. Integrate with real services once Agent 1/2 complete.

---

## Files to Create

### 1. ABTest API Layer
```
src/main/java/foodiepass/server/abtest/api/
├── ABTestController.java
└── dto/
    └── response/
        └── ABTestResultResponse.java
```

### 2. Survey API Layer
```
src/main/java/foodiepass/server/survey/api/
├── SurveyController.java
└── dto/
    ├── request/
    │   └── SurveyRequest.java
    └── response/
        ├── SurveySubmitResponse.java
        └── SurveyAnalyticsResponse.java
```

### 3. Test Files
```
src/test/java/foodiepass/server/
├── abtest/
│   └── api/
│       └── ABTestControllerTest.java
└── survey/
    └── api/
        └── SurveyControllerTest.java
```

---

## Implementation Details

### ABTestController.java

**File**: `src/main/java/foodiepass/server/abtest/api/ABTestController.java`

**Endpoint**: `GET /api/admin/ab-test/results`

**Responsibility**: Provide A/B test analytics for admin dashboard

**Implementation**:
```java
package foodiepass.server.abtest.api;

import foodiepass.server.abtest.api.dto.response.ABTestResultResponse;
import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.dto.ABTestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for A/B Test Administration
 */
@RestController
@RequestMapping("/api/admin/ab-test")
@RequiredArgsConstructor
public class ABTestController {

    private final ABTestService abTestService;

    /**
     * Get A/B test results (admin only)
     *
     * @return A/B test analytics with group counts
     */
    @GetMapping("/results")
    public ResponseEntity<ABTestResultResponse> getResults() {
        ABTestResult result = abTestService.getResults();
        ABTestResultResponse response = ABTestResultResponse.from(result);
        return ResponseEntity.ok(response);
    }
}
```

**Tests Required**:
- GET /results returns 200 with valid data
- GET /results returns correct JSON structure
- Service exception is handled gracefully

---

### ABTestResultResponse.java

**File**: `src/main/java/foodiepass/server/abtest/api/dto/response/ABTestResultResponse.java`

```java
package foodiepass.server.abtest.api.dto.response;

import foodiepass.server.abtest.dto.ABTestResult;

/**
 * API response for A/B test results
 */
public record ABTestResultResponse(
    long controlCount,
    long treatmentCount,
    long totalScans
) {
    public static ABTestResultResponse from(ABTestResult result) {
        return new ABTestResultResponse(
            result.controlCount(),
            result.treatmentCount(),
            result.totalScans()
        );
    }
}
```

**JSON Example**:
```json
{
  "controlCount": 150,
  "treatmentCount": 148,
  "totalScans": 298
}
```

---

### SurveyController.java

**File**: `src/main/java/foodiepass/server/survey/api/SurveyController.java`

**Endpoints**:
- `POST /api/surveys` - Submit survey response
- `GET /api/admin/surveys/analytics` - Get analytics (admin)

**Implementation**:
```java
package foodiepass.server.survey.api;

import foodiepass.server.survey.api.dto.request.SurveyRequest;
import foodiepass.server.survey.api.dto.response.SurveyAnalyticsResponse;
import foodiepass.server.survey.api.dto.response.SurveySubmitResponse;
import foodiepass.server.survey.application.SurveyService;
import foodiepass.server.survey.application.dto.SurveyAnalytics;
import foodiepass.server.survey.application.exception.DuplicateResponseException;
import foodiepass.server.survey.domain.SurveyResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Survey Operations
 */
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * Submit survey response
     *
     * @param request Survey submission request
     * @return Success response
     */
    @PostMapping
    public ResponseEntity<SurveySubmitResponse> submitSurvey(
        @RequestBody @Valid SurveyRequest request
    ) {
        try {
            surveyService.saveSurveyResponse(
                request.scanId(),
                request.hasConfidence()
            );

            SurveySubmitResponse response = new SurveySubmitResponse(
                true,
                "Response recorded"
            );

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            SurveySubmitResponse response = new SurveySubmitResponse(
                false,
                "Invalid scanId: " + e.getMessage()
            );
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);

        } catch (DuplicateResponseException e) {
            SurveySubmitResponse response = new SurveySubmitResponse(
                false,
                "Duplicate response: " + e.getMessage()
            );
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
        }
    }

    /**
     * Get survey analytics (admin only)
     *
     * @return Analytics with Yes rates and H3 validation
     */
    @GetMapping("/admin/analytics")
    public ResponseEntity<SurveyAnalyticsResponse> getAnalytics() {
        SurveyAnalytics analytics = surveyService.getAnalytics();
        SurveyAnalyticsResponse response = SurveyAnalyticsResponse.from(analytics);
        return ResponseEntity.ok(response);
    }
}
```

**Tests Required**:
- POST /surveys with valid request returns 200
- POST /surveys with invalid scanId returns 400
- POST /surveys with duplicate scanId returns 400
- POST /surveys validates request fields (@Valid)
- GET /admin/analytics returns 200 with valid data
- GET /admin/analytics returns correct JSON structure

---

### SurveyRequest.java

**File**: `src/main/java/foodiepass/server/survey/api/dto/request/SurveyRequest.java`

```java
package foodiepass.server.survey.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Survey submission request
 */
public record SurveyRequest(
    @NotNull(message = "scanId is required")
    UUID scanId,

    @NotNull(message = "hasConfidence is required")
    Boolean hasConfidence
) {}
```

**JSON Example**:
```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "hasConfidence": true
}
```

---

### SurveySubmitResponse.java

**File**: `src/main/java/foodiepass/server/survey/api/dto/response/SurveySubmitResponse.java`

```java
package foodiepass.server.survey.api.dto.response;

/**
 * Survey submission response
 */
public record SurveySubmitResponse(
    boolean success,
    String message
) {}
```

**JSON Example (Success)**:
```json
{
  "success": true,
  "message": "Response recorded"
}
```

**JSON Example (Error)**:
```json
{
  "success": false,
  "message": "Invalid scanId: MenuScan not found: uuid"
}
```

---

### SurveyAnalyticsResponse.java

**File**: `src/main/java/foodiepass/server/survey/api/dto/response/SurveyAnalyticsResponse.java`

```java
package foodiepass.server.survey.api.dto.response;

import foodiepass.server.survey.application.dto.GroupStats;
import foodiepass.server.survey.application.dto.SurveyAnalytics;

/**
 * Survey analytics API response
 */
public record SurveyAnalyticsResponse(
    GroupStatsResponse controlGroup,
    GroupStatsResponse treatmentGroup,
    double confidenceRatio,
    String hypothesis,
    String statisticalSignificance
) {
    public static SurveyAnalyticsResponse from(SurveyAnalytics analytics) {
        return new SurveyAnalyticsResponse(
            GroupStatsResponse.from(analytics.controlGroup()),
            GroupStatsResponse.from(analytics.treatmentGroup()),
            analytics.confidenceRatio(),
            analytics.hypothesis(),
            analytics.statisticalSignificance()
        );
    }
}
```

**Nested DTO**:
```java
public record GroupStatsResponse(
    long totalResponses,
    long yesCount,
    long noCount,
    double yesRate
) {
    public static GroupStatsResponse from(GroupStats stats) {
        return new GroupStatsResponse(
            stats.totalResponses(),
            stats.yesCount(),
            stats.noCount(),
            stats.yesRate()
        );
    }
}
```

**JSON Example**:
```json
{
  "controlGroup": {
    "totalResponses": 100,
    "yesCount": 45,
    "noCount": 55,
    "yesRate": 0.45
  },
  "treatmentGroup": {
    "totalResponses": 102,
    "yesCount": 92,
    "noCount": 10,
    "yesRate": 0.90
  },
  "confidenceRatio": 2.0,
  "hypothesis": "H3 validated (ratio >= 2.0)",
  "statisticalSignificance": "Sample size sufficient (n >= 30)"
}
```

---

## Test Requirements

### ABTestControllerTest.java

**File**: `src/test/java/foodiepass/server/abtest/api/ABTestControllerTest.java`

**Setup**: Use `@WebMvcTest(ABTestController.class)`, mock ABTestService

**Test Cases**:
```java
@WebMvcTest(ABTestController.class)
class ABTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ABTestService abTestService;

    @Test
    @DisplayName("GET /api/admin/ab-test/results returns 200 with valid data")
    void getResults_Success() throws Exception {
        // Given
        ABTestResult mockResult = new ABTestResult(50, 50, 100);
        when(abTestService.getResults()).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/admin/ab-test/results"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.controlCount").value(50))
            .andExpect(jsonPath("$.treatmentCount").value(50))
            .andExpect(jsonPath("$.totalScans").value(100));
    }

    @Test
    @DisplayName("GET /api/admin/ab-test/results returns empty result")
    void getResults_EmptyData() throws Exception {
        // Given
        ABTestResult mockResult = new ABTestResult(0, 0, 0);
        when(abTestService.getResults()).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/admin/ab-test/results"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalScans").value(0));
    }
}
```

**Coverage Target**: >85%

---

### SurveyControllerTest.java

**File**: `src/test/java/foodiepass/server/survey/api/SurveyControllerTest.java`

**Setup**: Use `@WebMvcTest(SurveyController.class)`, mock SurveyService

**Test Cases**:
```java
@WebMvcTest(SurveyController.class)
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurveyService surveyService;

    @Test
    @DisplayName("POST /api/surveys with valid request returns 200")
    void submitSurvey_Success() throws Exception {
        // Given
        UUID scanId = UUID.randomUUID();
        String requestJson = String.format(
            "{\"scanId\":\"%s\",\"hasConfidence\":true}", scanId);

        SurveyResponse mockResponse = SurveyResponse.create(
            scanId, ABGroup.TREATMENT, true);
        when(surveyService.saveSurveyResponse(scanId, true))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Response recorded"));
    }

    @Test
    @DisplayName("POST /api/surveys with invalid scanId returns 400")
    void submitSurvey_InvalidScanId() throws Exception {
        // Given
        UUID scanId = UUID.randomUUID();
        String requestJson = String.format(
            "{\"scanId\":\"%s\",\"hasConfidence\":true}", scanId);

        when(surveyService.saveSurveyResponse(scanId, true))
            .thenThrow(new EntityNotFoundException("MenuScan not found"));

        // When & Then
        mockMvc.perform(post("/api/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/surveys with duplicate scanId returns 400")
    void submitSurvey_DuplicateResponse() throws Exception {
        // Given
        UUID scanId = UUID.randomUUID();
        String requestJson = String.format(
            "{\"scanId\":\"%s\",\"hasConfidence\":true}", scanId);

        when(surveyService.saveSurveyResponse(scanId, true))
            .thenThrow(new DuplicateResponseException("Already exists"));

        // When & Then
        mockMvc.perform(post("/api/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/surveys with missing fields returns 400")
    void submitSurvey_MissingFields() throws Exception {
        // Given
        String requestJson = "{\"hasConfidence\":true}";  // Missing scanId

        // When & Then
        mockMvc.perform(post("/api/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/surveys/admin/analytics returns 200")
    void getAnalytics_Success() throws Exception {
        // Given
        GroupStats controlStats = new GroupStats(100, 45, 55, 0.45);
        GroupStats treatmentStats = new GroupStats(102, 92, 10, 0.90);
        SurveyAnalytics mockAnalytics = new SurveyAnalytics(
            controlStats, treatmentStats, 2.0,
            "H3 validated", "p < 0.05");

        when(surveyService.getAnalytics()).thenReturn(mockAnalytics);

        // When & Then
        mockMvc.perform(get("/api/surveys/admin/analytics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confidenceRatio").value(2.0))
            .andExpect(jsonPath("$.hypothesis").value("H3 validated"))
            .andExpect(jsonPath("$.controlGroup.yesRate").value(0.45))
            .andExpect(jsonPath("$.treatmentGroup.yesRate").value(0.90));
    }
}
```

**Coverage Target**: >85%

---

## Success Criteria

### Implementation Checklist
- [ ] ABTestController with GET /results endpoint
- [ ] SurveyController with POST /surveys and GET /admin/analytics
- [ ] All request/response DTOs created
- [ ] Request validation (@Valid, @NotNull) implemented
- [ ] Error handling for EntityNotFoundException and DuplicateResponseException
- [ ] Clean separation: Controller → Service (thin controllers)

### Testing Checklist
- [ ] ABTestControllerTest passes (>85% coverage)
- [ ] SurveyControllerTest passes (>85% coverage)
- [ ] All HTTP status codes tested (200, 400)
- [ ] JSON response structure validated with JsonPath
- [ ] Request validation tested
- [ ] Error responses tested
- [ ] MockMvc used for HTTP testing
- [ ] Services properly mocked

### Quality Checklist
- [ ] Controllers are thin (no business logic)
- [ ] DTOs use Java records (immutable, concise)
- [ ] Error messages are user-friendly
- [ ] Javadoc for all public methods
- [ ] Clean code (no magic strings, descriptive names)

---

## Timeline

**Estimated Time**: 4 hours

**Breakdown**:
- ABTestController + DTOs + tests: 1.5 hours
- SurveyController + DTOs + tests: 2.5 hours

**Note**: Can start immediately using interface contracts, integrate with real services once Agent 1/2 complete.

---

## Deliverables

1. ✅ All controller classes with endpoints
2. ✅ All request/response DTOs
3. ✅ All test files with >85% coverage
4. ✅ No compilation errors
5. ✅ All tests passing (with mocked services)
6. ✅ Code ready for Agent 4 integration

---

## Notes

- **Thin controllers**: Delegate all logic to services
- **Error handling**: Return user-friendly messages
- **Validation**: Use @Valid and Bean Validation annotations
- **JSON structure**: Must match specs in INTERFACE_CONTRACT.md
- **Testing**: Use @WebMvcTest for controller isolation
- **No integration tests**: Agent 4 handles full pipeline tests
