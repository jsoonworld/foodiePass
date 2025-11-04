# Interface Contract for Agent Independence

> **Purpose**: Define clear interface contracts so agents can work in parallel without waiting for implementation.

## Overview

This document specifies the interfaces that agents depend on, enabling parallel development:

- **Agent 1 (ABTest Core)**: Provides ABTestService interface
- **Agent 2 (Survey Core)**: Provides SurveyService interface
- **Agent 3 (API Layer)**: Consumes both services, provides REST APIs
- **Agent 4 (Integration)**: Consumes all services, integrates with MenuController

---

## ABTestService Interface

**Provider**: Agent 1 (ABTest Core)
**Consumers**: Agent 3 (API Layer), Agent 4 (Integration)

### Contract

```java
package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.ABTestResult;

import java.util.UUID;

/**
 * A/B Testing Service Interface
 *
 * Responsibilities:
 * - Assign users to A/B groups (50:50 random split)
 * - Create and persist MenuScan records
 * - Provide analytics for hypothesis validation
 */
public interface ABTestService {

    /**
     * Assign user to A/B group (CONTROL or TREATMENT)
     *
     * @param userId Session ID or user identifier
     * @return Assigned A/B group (50% CONTROL, 50% TREATMENT)
     * @throws IllegalArgumentException if userId is null or empty
     */
    ABGroup assignGroup(String userId);

    /**
     * Create MenuScan record for tracking
     *
     * @param userId User/session identifier
     * @param abGroup Assigned A/B group
     * @param imageUrl Menu image URL (optional, can be null)
     * @param sourceLanguage Source language code (e.g., "en", "ja")
     * @param targetLanguage Target language code (e.g., "ko")
     * @param sourceCurrency Source currency code (e.g., "USD")
     * @param targetCurrency Target currency code (e.g., "KRW")
     * @return Created MenuScan with generated ID
     * @throws IllegalArgumentException if required fields are null/empty
     */
    MenuScan createScan(
        String userId,
        ABGroup abGroup,
        String imageUrl,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency
    );

    /**
     * Retrieve MenuScan by ID
     *
     * @param scanId MenuScan UUID
     * @return MenuScan entity
     * @throws EntityNotFoundException if scanId not found
     */
    MenuScan getScanById(UUID scanId);

    /**
     * Get A/B test analytics (admin endpoint)
     *
     * @return Analytics with group counts and conversion rates
     */
    ABTestResult getResults();
}
```

### Data Contracts

#### ABGroup (enum)
```java
public enum ABGroup {
    CONTROL,    // Text + currency only
    TREATMENT   // Photos + descriptions + text + currency
}
```

#### MenuScan (entity)
```java
public class MenuScan {
    private UUID id;              // PK, auto-generated
    private String userId;        // Required, session ID
    private ABGroup abGroup;      // Required, CONTROL or TREATMENT
    private String imageUrl;      // Optional
    private String sourceLanguage;   // Optional
    private String targetLanguage;   // Required
    private String sourceCurrency;   // Optional
    private String targetCurrency;   // Required
    private LocalDateTime createdAt; // Auto-set on creation

    // Getters only (immutable after creation)
}
```

#### ABTestResult (DTO)
```java
public record ABTestResult(
    long controlCount,      // Total CONTROL group scans
    long treatmentCount,    // Total TREATMENT group scans
    long totalScans         // controlCount + treatmentCount
) {}
```

---

## SurveyService Interface

**Provider**: Agent 2 (Survey Core)
**Consumers**: Agent 3 (API Layer), Agent 4 (Integration)

### Contract

```java
package foodiepass.server.survey.application;

import foodiepass.server.survey.domain.SurveyResponse;
import foodiepass.server.survey.dto.SurveyAnalytics;

import java.util.Optional;
import java.util.UUID;

/**
 * Survey Service Interface
 *
 * Responsibilities:
 * - Collect user confidence survey responses
 * - Validate hypothesis H3 (Treatment/Control ratio >= 2.0)
 * - Provide analytics for A/B test evaluation
 */
public interface SurveyService {

    /**
     * Save survey response for a menu scan
     *
     * @param scanId MenuScan UUID (must exist)
     * @param hasConfidence User confidence (true = Yes, false = No)
     * @return Saved SurveyResponse
     * @throws EntityNotFoundException if scanId not found
     * @throws DuplicateResponseException if response already exists for scanId
     */
    SurveyResponse saveSurveyResponse(UUID scanId, Boolean hasConfidence);

    /**
     * Get survey response by scanId
     *
     * @param scanId MenuScan UUID
     * @return Optional containing SurveyResponse, or empty if not found
     */
    Optional<SurveyResponse> getResponseByScanId(UUID scanId);

    /**
     * Check if survey response exists for scanId
     *
     * @param scanId MenuScan UUID
     * @return true if response exists, false otherwise
     */
    boolean hasResponse(UUID scanId);

    /**
     * Get survey analytics (admin endpoint)
     *
     * @return Analytics with Yes rates by group and confidence ratio
     */
    SurveyAnalytics getAnalytics();
}
```

### Data Contracts

#### SurveyResponse (entity)
```java
public class SurveyResponse {
    private UUID id;                 // PK, auto-generated
    private UUID scanId;             // FK to MenuScan, unique constraint
    private ABGroup abGroup;         // Denormalized from MenuScan
    private Boolean hasConfidence;   // Required, true = Yes, false = No
    private LocalDateTime createdAt; // Auto-set on creation

    // Getters only (immutable after creation)
}
```

#### SurveyAnalytics (DTO)
```java
public record SurveyAnalytics(
    GroupStats controlGroup,      // CONTROL group statistics
    GroupStats treatmentGroup,    // TREATMENT group statistics
    double confidenceRatio,       // Treatment Yes rate / Control Yes rate
    String hypothesis,            // "H3 validated" or "H3 not validated"
    String statisticalSignificance  // e.g., "p < 0.05"
) {}
```

#### GroupStats (DTO)
```java
public record GroupStats(
    long totalResponses,  // Total survey responses
    long yesCount,        // "Yes" responses
    long noCount,         // "No" responses
    double yesRate        // yesCount / totalResponses
) {}
```

---

## MenuService Interface (Existing, Reused)

**Provider**: Existing Menu Module
**Consumers**: Agent 4 (Integration)

### Contract

```java
package foodiepass.server.menu.application;

import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.language.domain.Language;
import foodiepass.server.currency.domain.Currency;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Menu Service Interface (Existing v1 code)
 *
 * Responsibilities:
 * - OCR menu image
 * - Translate menu items
 * - Scrape food information (photos, descriptions)
 * - Convert currency
 */
public interface MenuService {

    /**
     * Process menu image through full pipeline
     *
     * @param base64Image Base64 encoded menu image
     * @param sourceLanguage Source language (can be Language.AUTO)
     * @param targetLanguage Target language for translation
     * @param sourceCurrency Source currency
     * @param targetCurrency Target currency for conversion
     * @return Mono containing list of processed MenuItems
     */
    Mono<List<MenuItem>> reconfigure(
        String base64Image,
        Language sourceLanguage,
        Language targetLanguage,
        Currency sourceCurrency,
        Currency targetCurrency
    );
}
```

### Data Contracts

#### MenuItem (domain)
```java
public class MenuItem {
    private MenuItemName name;      // original + translated
    private PriceInfo priceInfo;    // original + converted
    private FoodInfo foodInfo;      // photos + description (nullable)

    // Getters only
}
```

#### FoodInfo (domain)
```java
public class FoodInfo {
    private String name;            // Food name
    private String description;     // Food description
    private String imageUrl;        // Full-size image URL
    private String previewImageUrl; // Thumbnail URL

    // Getters only
}
```

---

## REST API Contracts

### ABTest API

**Provider**: Agent 3 (API Layer)
**Consumers**: Frontend

#### GET /api/admin/ab-test/results

**Response**:
```json
{
  "controlCount": 150,
  "treatmentCount": 148,
  "totalScans": 298
}
```

**Status Codes**:
- 200: Success
- 500: Server error

---

### Survey API

**Provider**: Agent 3 (API Layer)
**Consumers**: Frontend

#### POST /api/surveys

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

**Status Codes**:
- 200: Success
- 400: Invalid scanId or duplicate response
- 500: Server error

#### GET /api/admin/surveys/analytics

**Response**:
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
  "statisticalSignificance": "p < 0.05"
}
```

**Status Codes**:
- 200: Success
- 500: Server error

---

### MenuScan API

**Provider**: Agent 4 (Integration)
**Consumers**: Frontend

#### POST /api/menus/scan

**Request**:
```json
{
  "image": "base64EncodedString",
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
      "description": null,
      "imageUrl": null,
      "previewImageUrl": null,
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
      "description": "Classic pizza with tomato sauce, mozzarella, and basil",
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

**Status Codes**:
- 200: Success
- 400: Invalid request (missing fields, invalid base64, etc.)
- 500: Server error

---

## Agent Dependencies

### Agent 1 (ABTest Core)
**Dependencies**: None
**Provides**: ABTestService

### Agent 2 (Survey Core)
**Dependencies**: ABGroup enum (from Agent 1)
**Provides**: SurveyService

**Note**: Agent 2 can start in parallel with Agent 1 by using ABGroup interface contract above.

### Agent 3 (API Layer)
**Dependencies**: ABTestService, SurveyService
**Provides**: REST API endpoints

**Note**: Can start development using interface contracts before Agent 1/2 complete.

### Agent 4 (Integration)
**Dependencies**: ABTestService, SurveyService, MenuService
**Provides**: Integrated MenuScan endpoint, E2E tests

**Note**: Must wait for Agent 1/2/3 service implementations to complete.

---

## Mocking Strategy for Parallel Development

### Agent 3 (API Layer)
Mock ABTestService and SurveyService in unit tests:

```java
@WebMvcTest(ABTestController.class)
class ABTestControllerTest {

    @MockBean
    private ABTestService abTestService;

    @Test
    void testGetResults() {
        // Mock service response
        when(abTestService.getResults())
            .thenReturn(new ABTestResult(50, 50, 100));

        // Test controller
        mockMvc.perform(get("/api/admin/ab-test/results"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.controlCount").value(50));
    }
}
```

### Agent 4 (Integration)
Use @MockBean for external APIs only, use real services:

```java
@SpringBootTest
class MenuScanIntegrationTest {

    @Autowired
    private ABTestService abTestService;  // Real service

    @Autowired
    private SurveyService surveyService;  // Real service

    @MockBean
    private OcrReader ocrReader;  // Mock external API

    @Test
    void testFullPipeline() {
        // Test with real services, mocked external APIs
    }
}
```

---

## Validation Checklist

Before merging agents:

### Agent 1 Checklist
- [ ] ABTestService interface matches contract exactly
- [ ] ABGroup enum has CONTROL and TREATMENT values
- [ ] MenuScan entity has all required fields
- [ ] All methods throw documented exceptions
- [ ] Unit tests achieve >85% coverage

### Agent 2 Checklist
- [ ] SurveyService interface matches contract exactly
- [ ] SurveyResponse has unique constraint on scanId
- [ ] abGroup is denormalized from MenuScan
- [ ] All methods throw documented exceptions
- [ ] Unit tests achieve >85% coverage

### Agent 3 Checklist
- [ ] REST API endpoints match contract exactly
- [ ] Request/Response DTOs match JSON schemas
- [ ] Status codes match documentation
- [ ] Error responses are user-friendly
- [ ] Controller tests achieve >85% coverage

### Agent 4 Checklist
- [ ] MenuScan API integrates all services correctly
- [ ] CONTROL group strips FoodInfo fields
- [ ] TREATMENT group includes FoodInfo fields
- [ ] Processing time is measured and returned
- [ ] E2E tests cover both CONTROL and TREATMENT paths
- [ ] Integration tests achieve >80% coverage

---

## Notes for Agents

- **Use this document as source of truth** for interface contracts
- **Do not modify interfaces** without updating this document first
- **Mock dependencies** using these contracts during parallel development
- **Validate against contracts** before marking agent work complete
- **All exceptions must be documented** in interface Javadocs
