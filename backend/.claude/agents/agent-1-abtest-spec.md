# Agent 1: ABTest Module Implementation Specification

## Objective
Implement A/B testing system for FoodiePass MVP v2 to validate hypothesis H1 and H3.

## Context
- **Hypothesis H1**: Users gain confidence with visual menu (photos + descriptions) vs text-only translation
- **Hypothesis H3**: Treatment group shows 2x higher confidence rate than Control group
- **A/B Groups**:
  - CONTROL: Text translation + currency conversion only
  - TREATMENT: Photos + descriptions + text translation + currency conversion

## Files to Create

### 1. Domain Layer
```
src/main/java/foodiepass/server/abtest/domain/
├── ABGroup.java (enum)
└── MenuScan.java (entity)
```

### 2. Application Layer
```
src/main/java/foodiepass/server/abtest/application/
├── ABTestService.java
└── dto/
    ├── ABTestResult.java
    └── GroupAssignment.java
```

### 3. API Layer
```
src/main/java/foodiepass/server/abtest/api/
├── ABTestController.java
└── dto/
    ├── ABTestResultResponse.java
    └── GroupAssignmentResponse.java
```

### 4. Infrastructure Layer
```
src/main/java/foodiepass/server/abtest/repository/
└── MenuScanRepository.java (Spring Data JPA)
```

### 5. Test Files
```
src/test/java/foodiepass/server/abtest/
├── domain/
│   └── MenuScanTest.java
├── application/
│   └── ABTestServiceTest.java
├── api/
│   └── ABTestControllerTest.java
└── repository/
    └── MenuScanRepositoryTest.java
```

## Implementation Details

### ABGroup.java (enum)
```java
package foodiepass.server.abtest.domain;

public enum ABGroup {
    CONTROL,    // Text + currency only
    TREATMENT   // Photos + descriptions + text + currency
}
```

### MenuScan.java (entity)
**Responsibilities**:
- Represent a single menu scan session
- Store A/B group assignment
- Track user session and scan metadata

**Fields**:
- `UUID id` (PK)
- `String userId` (session ID)
- `ABGroup abGroup`
- `String imageUrl` (optional, for audit)
- `String sourceLanguage`
- `String targetLanguage`
- `String sourceCurrency`
- `String targetCurrency`
- `LocalDateTime createdAt`

**Business Rules**:
- `userId` cannot be null or empty
- `abGroup` must be assigned (not null)
- `createdAt` is set automatically on creation
- Language/currency codes must follow ISO standards (validation in service layer)

### ABTestService.java
**Responsibilities**:
- Assign users to A/B groups (50:50 random split)
- Create and persist MenuScan records
- Provide A/B test analytics (admin endpoint)

**Methods**:
```java
// Assign user to A/B group (50:50 random)
public ABGroup assignGroup(String userId);

// Create MenuScan record
public MenuScan createScan(String userId, ABGroup group, String imageUrl,
                          String sourceLanguage, String targetLanguage,
                          String sourceCurrency, String targetCurrency);

// Get MenuScan by ID
public MenuScan getScanById(UUID scanId);

// Get A/B test results (admin analytics)
public ABTestResult getResults();
```

**Business Rules**:
- Group assignment is random 50:50 (use `Random` or `Math.random()`)
- Same `userId` can have multiple scans (different sessions)
- `imageUrl` is optional (can be null)
- All other fields are required

### ABTestController.java
**Endpoints**:

**GET /api/admin/ab-test/results**
- Returns A/B test analytics
- Response: `ABTestResultResponse` with group counts, conversion rates

**Response Example**:
```json
{
  "controlGroup": {
    "totalScans": 150,
    "surveyResponses": 100,
    "yesResponses": 45,
    "conversionRate": 0.45
  },
  "treatmentGroup": {
    "totalScans": 148,
    "surveyResponses": 102,
    "yesResponses": 92,
    "conversionRate": 0.90
  },
  "confidenceRatio": 2.0,
  "hypothesis": "H3 validated (ratio >= 2.0)"
}
```

### MenuScanRepository.java
**Interface**: Extends `JpaRepository<MenuScan, UUID>`

**Custom Queries**:
```java
// Count scans by A/B group
long countByAbGroup(ABGroup abGroup);

// Find scans by userId
List<MenuScan> findByUserId(String userId);

// Find scans by userId and abGroup
List<MenuScan> findByUserIdAndAbGroup(String userId, ABGroup abGroup);
```

## Test Requirements

### MenuScanTest.java
**Test Cases**:
- `testCreateMenuScan()`: Valid entity creation
- `testMenuScanWithNullUserId()`: Validation fails
- `testMenuScanWithNullABGroup()`: Validation fails
- `testCreatedAtAutoSet()`: Timestamp is set automatically

**Coverage Target**: 100% (simple domain object)

### ABTestServiceTest.java
**Test Cases**:
- `testAssignGroup_ReturnsValidGroup()`: Returns CONTROL or TREATMENT
- `testAssignGroup_Distribution()`: ~50% split after 1000 assignments
- `testCreateScan_Success()`: Creates and persists MenuScan
- `testCreateScan_WithNullUserId()`: Throws exception
- `testCreateScan_WithNullABGroup()`: Throws exception
- `testGetScanById_Found()`: Returns existing scan
- `testGetScanById_NotFound()`: Throws exception
- `testGetResults_WithNoData()`: Returns empty result
- `testGetResults_WithData()`: Returns correct analytics

**Mocking**:
- Mock `MenuScanRepository`
- Use `@Mock` and `@InjectMocks` with Mockito

**Coverage Target**: >90%

### ABTestControllerTest.java
**Test Cases**:
- `testGetResults_Success()`: Returns 200 with valid analytics
- `testGetResults_NoData()`: Returns 200 with empty analytics

**Testing Approach**:
- Use `@WebMvcTest(ABTestController.class)`
- Mock `ABTestService`
- Use `MockMvc` for HTTP requests

**Coverage Target**: >85%

### MenuScanRepositoryTest.java
**Test Cases**:
- `testSave()`: Save and retrieve MenuScan
- `testCountByAbGroup()`: Count by CONTROL and TREATMENT
- `testFindByUserId()`: Find scans by user
- `testFindByUserIdAndAbGroup()`: Find scans by user and group

**Testing Approach**:
- Use `@DataJpaTest`
- Use H2 in-memory database
- Test actual database operations

**Coverage Target**: 100% (repository layer)

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
- Use `@Test` for test methods

### Documentation
- Add Javadoc for public methods
- Include `@param`, `@return`, `@throws` tags
- Explain business logic in comments

### Error Handling
- Throw `IllegalArgumentException` for invalid inputs
- Throw `EntityNotFoundException` for missing entities
- Use custom exceptions if needed (e.g., `InvalidABGroupException`)

## Success Criteria

### Implementation
- ✅ All classes created with correct package structure
- ✅ Domain model follows JPA best practices
- ✅ Service layer contains business logic (no DB queries)
- ✅ Controller layer is thin (delegates to service)
- ✅ Repository uses Spring Data JPA

### Testing
- ✅ Test coverage >85% overall
- ✅ All test cases pass
- ✅ No skipped or disabled tests
- ✅ Tests follow AAA pattern (Arrange-Act-Assert)

### Quality
- ✅ No code smells (long methods, god classes)
- ✅ SOLID principles followed
- ✅ Clean code (descriptive names, no magic numbers)
- ✅ No TODO comments in production code

## Integration Points

### With Survey Module
- `MenuScan.id` is referenced in `SurveyResponse.scanId`
- A/B group is copied to SurveyResponse for analytics

### With Menu Module
- `MenuController` will use `ABTestService.assignGroup()` and `createScan()`
- Response format depends on A/B group (CONTROL vs TREATMENT)

## Performance Requirements
- Group assignment: <10ms
- Scan creation: <50ms
- Analytics query: <500ms

## Security Considerations
- Admin endpoints (`/api/admin/*`) should be protected (future: add authentication)
- Input validation for all user inputs
- SQL injection prevention (use parameterized queries)

## Timeline
**Estimated Time**: 1 day (Day 6)

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
5. Code ready for code review

## Notes
- Focus on hypothesis validation (H1, H3)
- Keep it simple (MVP scope)
- Follow existing project patterns (see `menu/`, `currency/` modules)
- Use reactive programming (Mono, Flux) if needed for consistency
