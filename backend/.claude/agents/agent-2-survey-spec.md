# Agent 2: Survey Module Implementation Specification

## Objective
Implement survey system to collect user confidence ratings and validate hypothesis H1 and H3.

## Context
- **Survey Question**: "Do you feel confident ordering from this menu?"
- **Purpose**: Measure user confidence difference between Control and Treatment groups
- **Expected Outcome**: Treatment group Yes rate / Control group Yes rate >= 2.0

## Files to Create

### 1. Domain Layer
```
src/main/java/foodiepass/server/survey/domain/
└── SurveyResponse.java (entity)
```

### 2. Application Layer
```
src/main/java/foodiepass/server/survey/application/
├── SurveyService.java
└── dto/
    ├── SurveyAnalytics.java
    └── GroupStats.java
```

### 3. API Layer
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

### 4. Infrastructure Layer
```
src/main/java/foodiepass/server/survey/repository/
└── SurveyResponseRepository.java (Spring Data JPA)
```

### 5. Test Files
```
src/test/java/foodiepass/server/survey/
├── domain/
│   └── SurveyResponseTest.java
├── application/
│   └── SurveyServiceTest.java
├── api/
│   └── SurveyControllerTest.java
└── repository/
    └── SurveyResponseRepositoryTest.java
```

## Implementation Details

### SurveyResponse.java (entity)
**Responsibilities**:
- Store user survey response
- Link to MenuScan (FK)
- Enable analytics by A/B group

**Fields**:
- `UUID id` (PK)
- `UUID scanId` (FK to MenuScan)
- `ABGroup abGroup` (denormalized for analytics performance)
- `Boolean hasConfidence` (true = Yes, false = No)
- `LocalDateTime createdAt`

**Business Rules**:
- `scanId` cannot be null (must reference existing MenuScan)
- `abGroup` cannot be null
- `hasConfidence` cannot be null (must be true or false)
- `createdAt` is set automatically on creation
- One scan can have only one survey response (unique constraint on `scanId`)

**Relationships**:
- `@ManyToOne` with `MenuScan` (optional, can be lazy-loaded)
- No cascade operations (MenuScan is independent)

### SurveyService.java
**Responsibilities**:
- Save survey responses
- Validate scanId exists
- Calculate analytics (Yes rate by group)
- Validate H3 hypothesis (ratio >= 2.0)

**Methods**:
```java
// Save survey response
public SurveyResponse saveSurveyResponse(UUID scanId, Boolean hasConfidence);

// Get survey response by scanId
public Optional<SurveyResponse> getResponseByScanId(UUID scanId);

// Check if survey already submitted for scanId
public boolean hasResponse(UUID scanId);

// Get analytics (admin endpoint)
public SurveyAnalytics getAnalytics();
```

**Business Rules**:
- Validate `scanId` exists in MenuScan table before saving
- Prevent duplicate responses for same `scanId`
- Denormalize `abGroup` from MenuScan to SurveyResponse for query performance
- Calculate confidence ratio: Treatment Yes rate / Control Yes rate

### SurveyController.java
**Endpoints**:

**POST /api/surveys**
- Submit survey response
- Request: `SurveyRequest`
- Response: `SurveySubmitResponse`

**Request Example**:
```json
{
  "scanId": "uuid",
  "hasConfidence": true
}
```

**Response Example**:
```json
{
  "success": true,
  "message": "Response recorded"
}
```

**Error Cases**:
- 400: Invalid scanId (not found)
- 400: Duplicate response for scanId
- 400: Missing required fields

**GET /api/admin/surveys/analytics**
- Get survey analytics (admin only)
- Response: `SurveyAnalyticsResponse`

**Response Example**:
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

### SurveyResponseRepository.java
**Interface**: Extends `JpaRepository<SurveyResponse, UUID>`

**Custom Queries**:
```java
// Find response by scanId
Optional<SurveyResponse> findByScanId(UUID scanId);

// Check if response exists for scanId
boolean existsByScanId(UUID scanId);

// Count by A/B group and confidence
long countByAbGroupAndHasConfidence(ABGroup abGroup, Boolean hasConfidence);

// Count by A/B group (total responses)
long countByAbGroup(ABGroup abGroup);

// Get all responses by A/B group (for detailed analysis)
List<SurveyResponse> findByAbGroup(ABGroup abGroup);
```

## Test Requirements

### SurveyResponseTest.java
**Test Cases**:
- `testCreateSurveyResponse()`: Valid entity creation
- `testSurveyResponseWithNullScanId()`: Validation fails
- `testSurveyResponseWithNullABGroup()`: Validation fails
- `testSurveyResponseWithNullHasConfidence()`: Validation fails
- `testCreatedAtAutoSet()`: Timestamp is set automatically

**Coverage Target**: 100% (simple domain object)

### SurveyServiceTest.java
**Test Cases**:
- `testSaveSurveyResponse_Success()`: Saves valid response
- `testSaveSurveyResponse_InvalidScanId()`: Throws exception
- `testSaveSurveyResponse_DuplicateScanId()`: Throws exception
- `testGetResponseByScanId_Found()`: Returns existing response
- `testGetResponseByScanId_NotFound()`: Returns empty Optional
- `testHasResponse_True()`: Returns true for existing response
- `testHasResponse_False()`: Returns false for non-existing response
- `testGetAnalytics_NoData()`: Returns analytics with zero counts
- `testGetAnalytics_WithData()`: Returns correct analytics
- `testGetAnalytics_ConfidenceRatio()`: Calculates ratio correctly (Treatment/Control)
- `testGetAnalytics_H3Validation()`: Validates H3 hypothesis (ratio >= 2.0)

**Mocking**:
- Mock `SurveyResponseRepository`
- Mock `MenuScanRepository` (for scanId validation)
- Use `@Mock` and `@InjectMocks` with Mockito

**Coverage Target**: >90%

### SurveyControllerTest.java
**Test Cases**:
- `testSubmitSurvey_Success()`: Returns 200 with success message
- `testSubmitSurvey_InvalidScanId()`: Returns 400 with error
- `testSubmitSurvey_DuplicateResponse()`: Returns 400 with error
- `testSubmitSurvey_MissingFields()`: Returns 400 with validation error
- `testGetAnalytics_Success()`: Returns 200 with valid analytics

**Testing Approach**:
- Use `@WebMvcTest(SurveyController.class)`
- Mock `SurveyService`
- Use `MockMvc` for HTTP requests
- Test request validation (`@Valid` on `SurveyRequest`)

**Coverage Target**: >85%

### SurveyResponseRepositoryTest.java
**Test Cases**:
- `testSave()`: Save and retrieve SurveyResponse
- `testFindByScanId_Found()`: Find response by scanId
- `testFindByScanId_NotFound()`: Return empty Optional
- `testExistsByScanId()`: Check existence by scanId
- `testCountByAbGroupAndHasConfidence()`: Count by group and confidence
- `testCountByAbGroup()`: Count total responses by group
- `testFindByAbGroup()`: Find all responses by group
- `testUniqueScanIdConstraint()`: Duplicate scanId throws exception

**Testing Approach**:
- Use `@DataJpaTest`
- Use H2 in-memory database
- Test actual database operations
- Test unique constraint on `scanId`

**Coverage Target**: 100% (repository layer)

## DTO Specifications

### SurveyRequest.java
```java
public record SurveyRequest(
    @NotNull UUID scanId,
    @NotNull Boolean hasConfidence
) {}
```

**Validation**:
- `scanId`: Required, must be valid UUID
- `hasConfidence`: Required, must be true or false

### SurveySubmitResponse.java
```java
public record SurveySubmitResponse(
    boolean success,
    String message
) {}
```

### GroupStats.java
```java
public record GroupStats(
    long totalResponses,
    long yesCount,
    long noCount,
    double yesRate
) {}
```

### SurveyAnalytics.java
```java
public record SurveyAnalytics(
    GroupStats controlGroup,
    GroupStats treatmentGroup,
    double confidenceRatio,  // Treatment Yes rate / Control Yes rate
    String hypothesis,       // "H3 validated" or "H3 not validated"
    String statisticalSignificance  // Optional: p-value
) {}
```

## Code Style Guidelines

### Naming Conventions
- Class names: PascalCase
- Method names: camelCase
- Constants: UPPER_SNAKE_CASE
- Package names: lowercase

### Annotations
- Use `@Service`, `@RestController`, `@Repository`
- Use `@Transactional` for service methods that modify data
- Use `@Valid` for request validation
- Use `@NotNull` for required fields
- Use `@Column(unique = true)` for scanId in SurveyResponse

### Documentation
- Add Javadoc for public methods
- Include `@param`, `@return`, `@throws` tags
- Explain business logic in comments
- Document H3 hypothesis validation logic

### Error Handling
- Throw `IllegalArgumentException` for invalid inputs
- Throw `EntityNotFoundException` for invalid scanId
- Throw `DuplicateResponseException` for duplicate scanId (custom exception)
- Return meaningful error messages in API responses

## Success Criteria

### Implementation
- ✅ All classes created with correct package structure
- ✅ Domain model follows JPA best practices
- ✅ Unique constraint on `scanId` enforced
- ✅ Service layer validates scanId before saving
- ✅ Analytics calculation is accurate
- ✅ H3 hypothesis validation logic implemented

### Testing
- ✅ Test coverage >85% overall
- ✅ All test cases pass
- ✅ No skipped or disabled tests
- ✅ Tests follow AAA pattern (Arrange-Act-Assert)
- ✅ Edge cases covered (duplicate responses, invalid scanId)

### Quality
- ✅ No code smells (long methods, god classes)
- ✅ SOLID principles followed
- ✅ Clean code (descriptive names, no magic numbers)
- ✅ No TODO comments in production code

## Integration Points

### With ABTest Module
- `SurveyResponse.scanId` references `MenuScan.id` (FK)
- `SurveyResponse.abGroup` is denormalized from `MenuScan.abGroup`
- Validate scanId exists before saving survey response

### With Menu Module
- Frontend calls `POST /api/surveys` after displaying menu
- Survey button appears only after menu is fully loaded

## Analytics Calculations

### Yes Rate
```
yesRate = yesCount / totalResponses
```

### Confidence Ratio (H3 validation)
```
confidenceRatio = treatmentYesRate / controlYesRate
```

**H3 Success**: `confidenceRatio >= 2.0`

### Statistical Significance (Optional)
- Use Chi-square test for proportions
- Calculate p-value
- Report if `p < 0.05` (statistically significant)

## Performance Requirements
- Survey submission: <100ms
- Analytics query: <500ms
- Handle 1000+ survey responses efficiently

## Security Considerations
- Admin endpoints (`/api/admin/*`) should be protected (future: add authentication)
- Input validation for all user inputs
- Prevent SQL injection (use parameterized queries)
- Rate limiting for survey submission (prevent spam)

## Timeline
**Estimated Time**: 1 day (Day 7)

**Breakdown**:
- Domain + Repository: 2 hours
- Service layer: 3 hours
- Controller layer: 2 hours
- Testing: 4 hours
- Integration validation: 1 hour

## Deliverables
1. All source files in correct package structure
2. All test files with >85% coverage
3. No compilation errors
4. All tests passing
5. Analytics logic validated with sample data
6. Code ready for code review

## Notes
- Focus on hypothesis validation (H1, H3)
- Keep analytics calculation simple and accurate
- Follow existing project patterns (see `menu/`, `currency/` modules)
- Use Java records for DTOs (immutable, concise)
- Denormalize `abGroup` for query performance (avoid JOIN in analytics)
