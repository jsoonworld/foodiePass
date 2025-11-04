# Agent 2 (Core): Survey Module - Domain & Service Layer

## Objective
Implement survey system core logic (Domain + Repository + Service) for collecting user confidence ratings and validating hypothesis H3.

## Scope
✅ **In Scope**:
- Domain layer (SurveyResponse entity)
- Repository layer (SurveyResponseRepository)
- Service layer (SurveyService)
- Unit tests (>85% coverage)

❌ **Out of Scope**:
- Controller layer (handled by Agent 3)
- API endpoints
- Integration tests (handled by Agent 4)

## Context
- **Survey Question**: "Do you feel confident ordering from this menu?"
- **Purpose**: Measure confidence difference between CONTROL and TREATMENT groups
- **Expected Outcome**: Treatment Yes rate / Control Yes rate >= 2.0 (Hypothesis H3)

## Dependencies
**Minimal**: Only needs ABGroup enum from Agent 1
- Can start in parallel by using interface contract from `docs/INTERFACE_CONTRACT.md`
- ABGroup enum is simple (2 values: CONTROL, TREATMENT)

**Interface Contract**: See `docs/INTERFACE_CONTRACT.md` for SurveyService specification.

---

## Files to Create

### 1. Domain Layer
```
src/main/java/foodiepass/server/survey/domain/
└── SurveyResponse.java (entity)
```

### 2. Application Layer
```
src/main/java/foodiepass/server/survey/application/
├── SurveyService.java (implementation)
├── exception/
│   └── DuplicateResponseException.java (custom exception)
└── dto/
    ├── SurveyAnalytics.java (record)
    └── GroupStats.java (record)
```

### 3. Infrastructure Layer
```
src/main/java/foodiepass/server/survey/repository/
└── SurveyResponseRepository.java (Spring Data JPA)
```

### 4. Test Files
```
src/test/java/foodiepass/server/survey/
├── domain/
│   └── SurveyResponseTest.java
├── application/
│   └── SurveyServiceTest.java
└── repository/
    └── SurveyResponseRepositoryTest.java
```

---

## Implementation Details

### SurveyResponse.java (entity)

**File**: `src/main/java/foodiepass/server/survey/domain/SurveyResponse.java`

**Responsibilities**:
- Store user survey response
- Link to MenuScan via scanId (FK)
- Enable analytics by A/B group

**Fields**:
```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@Column(name = "scan_id", nullable = false, unique = true)
private UUID scanId;  // FK to MenuScan, UNIQUE constraint

@Enumerated(EnumType.STRING)
@Column(name = "ab_group", nullable = false, length = 20)
private ABGroup abGroup;  // Denormalized from MenuScan

@Column(name = "has_confidence", nullable = false)
private Boolean hasConfidence;  // true = Yes, false = No

@Column(name = "created_at", nullable = false)
private LocalDateTime createdAt;
```

**Business Rules**:
- `scanId` must not be null
- `scanId` must be unique (only one response per scan)
- `abGroup` must not be null
- `hasConfidence` must not be null (must be true or false)
- `createdAt` is set automatically on creation
- Use Builder pattern for construction
- Use factory method `create()` for validation

**Implementation Pattern**:
```java
@Entity
@Table(name = "survey_response",
       uniqueConstraints = @UniqueConstraint(columnNames = "scan_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SurveyResponse {
    // Fields...

    /**
     * Create new SurveyResponse with validation
     */
    public static SurveyResponse create(
        UUID scanId,
        ABGroup abGroup,
        Boolean hasConfidence
    ) {
        validateScanId(scanId);
        validateAbGroup(abGroup);
        validateHasConfidence(hasConfidence);

        return SurveyResponse.builder()
            .scanId(scanId)
            .abGroup(abGroup)
            .hasConfidence(hasConfidence)
            .createdAt(LocalDateTime.now())
            .build();
    }

    private static void validateScanId(UUID scanId) {
        if (scanId == null) {
            throw new IllegalArgumentException("scanId must not be null");
        }
    }

    private static void validateAbGroup(ABGroup abGroup) {
        if (abGroup == null) {
            throw new IllegalArgumentException("abGroup must not be null");
        }
    }

    private static void validateHasConfidence(Boolean hasConfidence) {
        if (hasConfidence == null) {
            throw new IllegalArgumentException("hasConfidence must not be null");
        }
    }
}
```

**Tests Required**:
- Create valid SurveyResponse
- Reject null scanId
- Reject null abGroup
- Reject null hasConfidence
- Auto-set createdAt
- Factory method validation

---

### SurveyResponseRepository.java

**File**: `src/main/java/foodiepass/server/survey/repository/SurveyResponseRepository.java`

```java
package foodiepass.server.survey.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.survey.domain.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SurveyResponse entity
 */
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {

    /**
     * Find response by scanId (unique)
     */
    Optional<SurveyResponse> findByScanId(UUID scanId);

    /**
     * Check if response exists for scanId
     */
    boolean existsByScanId(UUID scanId);

    /**
     * Count by A/B group and confidence
     */
    long countByAbGroupAndHasConfidence(ABGroup abGroup, Boolean hasConfidence);

    /**
     * Count total responses by A/B group
     */
    long countByAbGroup(ABGroup abGroup);

    /**
     * Find all responses by A/B group
     */
    List<SurveyResponse> findByAbGroup(ABGroup abGroup);
}
```

**Tests Required**:
- Save and retrieve SurveyResponse
- Find by scanId (exists)
- Find by scanId (not found)
- Exists by scanId (true/false)
- Count by abGroup and hasConfidence (Yes/No)
- Count by abGroup (total)
- Find all by abGroup
- Unique constraint on scanId (duplicate should fail)

---

### DuplicateResponseException.java

**File**: `src/main/java/foodiepass/server/survey/application/exception/DuplicateResponseException.java`

```java
package foodiepass.server.survey.application.exception;

/**
 * Exception thrown when attempting to submit duplicate survey response
 */
public class DuplicateResponseException extends RuntimeException {

    public DuplicateResponseException(String message) {
        super(message);
    }

    public DuplicateResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

### SurveyService.java

**File**: `src/main/java/foodiepass/server/survey/application/SurveyService.java`

**Responsibilities**:
- Save survey responses
- Validate scanId exists in MenuScan table
- Prevent duplicate responses
- Calculate analytics (Yes rate by group)
- Validate H3 hypothesis (ratio >= 2.0)

**Interface** (from INTERFACE_CONTRACT.md):
```java
SurveyResponse saveSurveyResponse(UUID scanId, Boolean hasConfidence);
Optional<SurveyResponse> getResponseByScanId(UUID scanId);
boolean hasResponse(UUID scanId);
SurveyAnalytics getAnalytics();
```

**Implementation**:
```java
package foodiepass.server.survey.application;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.survey.application.dto.GroupStats;
import foodiepass.server.survey.application.dto.SurveyAnalytics;
import foodiepass.server.survey.application.exception.DuplicateResponseException;
import foodiepass.server.survey.domain.SurveyResponse;
import foodiepass.server.survey.repository.SurveyResponseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyResponseRepository surveyResponseRepository;
    private final ABTestService abTestService;  // For scanId validation

    /**
     * Save survey response
     *
     * @throws EntityNotFoundException if scanId not found
     * @throws DuplicateResponseException if response already exists
     */
    @Transactional
    public SurveyResponse saveSurveyResponse(UUID scanId, Boolean hasConfidence) {
        // 1. Check for duplicate
        if (surveyResponseRepository.existsByScanId(scanId)) {
            throw new DuplicateResponseException(
                "Survey response already exists for scanId: " + scanId);
        }

        // 2. Validate scanId exists and get abGroup
        MenuScan scan = abTestService.getScanById(scanId);
        ABGroup abGroup = scan.getAbGroup();

        // 3. Create and save response
        SurveyResponse response = SurveyResponse.create(
            scanId, abGroup, hasConfidence);

        return surveyResponseRepository.save(response);
    }

    /**
     * Get response by scanId
     */
    public Optional<SurveyResponse> getResponseByScanId(UUID scanId) {
        return surveyResponseRepository.findByScanId(scanId);
    }

    /**
     * Check if response exists
     */
    public boolean hasResponse(UUID scanId) {
        return surveyResponseRepository.existsByScanId(scanId);
    }

    /**
     * Get survey analytics
     */
    public SurveyAnalytics getAnalytics() {
        // Control group stats
        long controlTotal = surveyResponseRepository.countByAbGroup(ABGroup.CONTROL);
        long controlYes = surveyResponseRepository
            .countByAbGroupAndHasConfidence(ABGroup.CONTROL, true);
        long controlNo = controlTotal - controlYes;
        double controlYesRate = controlTotal > 0 ? (double) controlYes / controlTotal : 0.0;

        GroupStats controlStats = new GroupStats(
            controlTotal, controlYes, controlNo, controlYesRate);

        // Treatment group stats
        long treatmentTotal = surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT);
        long treatmentYes = surveyResponseRepository
            .countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true);
        long treatmentNo = treatmentTotal - treatmentYes;
        double treatmentYesRate = treatmentTotal > 0 ? (double) treatmentYes / treatmentTotal : 0.0;

        GroupStats treatmentStats = new GroupStats(
            treatmentTotal, treatmentYes, treatmentNo, treatmentYesRate);

        // Calculate confidence ratio (H3 validation)
        double confidenceRatio = controlYesRate > 0
            ? treatmentYesRate / controlYesRate
            : 0.0;

        // H3 hypothesis validation
        String hypothesis = confidenceRatio >= 2.0
            ? "H3 validated (ratio >= 2.0)"
            : "H3 not validated (ratio < 2.0)";

        // Statistical significance (simplified for MVP)
        String statisticalSignificance = controlTotal >= 30 && treatmentTotal >= 30
            ? "Sample size sufficient (n >= 30)"
            : "Sample size insufficient (n < 30)";

        return new SurveyAnalytics(
            controlStats,
            treatmentStats,
            confidenceRatio,
            hypothesis,
            statisticalSignificance
        );
    }
}
```

**Tests Required**:
- Save survey response success
- Save throws EntityNotFoundException for invalid scanId
- Save throws DuplicateResponseException for duplicate scanId
- Get response by scanId (found)
- Get response by scanId (not found)
- Has response returns true/false correctly
- Get analytics with no data (returns zeros)
- Get analytics with data (calculates correctly)
- Get analytics calculates confidence ratio correctly
- Get analytics validates H3 hypothesis (ratio >= 2.0)

---

### GroupStats.java (DTO)

**File**: `src/main/java/foodiepass/server/survey/application/dto/GroupStats.java`

```java
package foodiepass.server.survey.application.dto;

/**
 * Statistics for a single A/B test group
 */
public record GroupStats(
    long totalResponses,  // Total survey responses
    long yesCount,        // "Yes" responses
    long noCount,         // "No" responses
    double yesRate        // yesCount / totalResponses
) {}
```

---

### SurveyAnalytics.java (DTO)

**File**: `src/main/java/foodiepass/server/survey/application/dto/SurveyAnalytics.java`

```java
package foodiepass.server.survey.application.dto;

/**
 * Survey analytics for A/B test evaluation
 */
public record SurveyAnalytics(
    GroupStats controlGroup,       // CONTROL group statistics
    GroupStats treatmentGroup,     // TREATMENT group statistics
    double confidenceRatio,        // Treatment Yes rate / Control Yes rate
    String hypothesis,             // H3 validation result
    String statisticalSignificance // Statistical test result
) {}
```

---

## Test Requirements

### SurveyResponseTest.java

**File**: `src/test/java/foodiepass/server/survey/domain/SurveyResponseTest.java`

**Test Cases**:
```java
@Test
@DisplayName("create() creates valid SurveyResponse")
void createValidResponse() { ... }

@Test
@DisplayName("create() throws exception for null scanId")
void rejectNullScanId() { ... }

@Test
@DisplayName("create() throws exception for null abGroup")
void rejectNullAbGroup() { ... }

@Test
@DisplayName("create() throws exception for null hasConfidence")
void rejectNullHasConfidence() { ... }

@Test
@DisplayName("createdAt is set automatically")
void autoSetCreatedAt() { ... }
```

**Coverage Target**: 100%

---

### SurveyResponseRepositoryTest.java

**File**: `src/test/java/foodiepass/server/survey/repository/SurveyResponseRepositoryTest.java`

**Setup**: Use `@DataJpaTest` with H2

**Test Cases**:
```java
@Test
@DisplayName("Save and retrieve SurveyResponse")
void saveAndRetrieve() { ... }

@Test
@DisplayName("Find by scanId (exists)")
void findByScanIdExists() { ... }

@Test
@DisplayName("Find by scanId (not found)")
void findByScanIdNotFound() { ... }

@Test
@DisplayName("Exists by scanId")
void existsByScanId() { ... }

@Test
@DisplayName("Count by abGroup and hasConfidence")
void countByGroupAndConfidence() { ... }

@Test
@DisplayName("Count by abGroup")
void countByGroup() { ... }

@Test
@DisplayName("Find all by abGroup")
void findByAbGroup() { ... }

@Test
@DisplayName("Duplicate scanId throws exception")
void uniqueConstraintOnScanId() {
    // Should throw DataIntegrityViolationException
}
```

**Coverage Target**: 100%

---

### SurveyServiceTest.java

**File**: `src/test/java/foodiepass/server/survey/application/SurveyServiceTest.java`

**Setup**: Use `@ExtendWith(MockitoExtension.class)`, mock repository and ABTestService

**Test Cases**:
```java
@Test
@DisplayName("saveSurveyResponse() saves valid response")
void saveResponseSuccess() { ... }

@Test
@DisplayName("saveSurveyResponse() throws EntityNotFoundException for invalid scanId")
void saveResponseInvalidScanId() { ... }

@Test
@DisplayName("saveSurveyResponse() throws DuplicateResponseException")
void saveResponseDuplicate() { ... }

@Test
@DisplayName("getResponseByScanId() returns existing response")
void getResponseFound() { ... }

@Test
@DisplayName("getResponseByScanId() returns empty for non-existent")
void getResponseNotFound() { ... }

@Test
@DisplayName("hasResponse() returns true for existing")
void hasResponseTrue() { ... }

@Test
@DisplayName("hasResponse() returns false for non-existent")
void hasResponseFalse() { ... }

@Test
@DisplayName("getAnalytics() returns zeros for no data")
void getAnalyticsNoData() { ... }

@Test
@DisplayName("getAnalytics() calculates stats correctly")
void getAnalyticsWithData() {
    // Mock: 100 control (45 yes, 55 no) = 45% yes rate
    // Mock: 100 treatment (90 yes, 10 no) = 90% yes rate
    // Ratio: 90/45 = 2.0
    // Expected: H3 validated
}

@Test
@DisplayName("getAnalytics() validates H3 hypothesis")
void getAnalyticsH3Validation() {
    // Test ratio >= 2.0 → H3 validated
    // Test ratio < 2.0 → H3 not validated
}
```

**Coverage Target**: >90%

---

## Success Criteria

### Implementation Checklist
- [ ] SurveyResponse entity with unique constraint on scanId
- [ ] SurveyResponseRepository with all query methods
- [ ] DuplicateResponseException custom exception
- [ ] SurveyService implements interface contract exactly
- [ ] GroupStats and SurveyAnalytics DTOs match specification
- [ ] All files follow package structure

### Testing Checklist
- [ ] All domain tests pass (SurveyResponseTest)
- [ ] All repository tests pass (SurveyResponseRepositoryTest)
- [ ] All service tests pass (SurveyServiceTest)
- [ ] Test coverage >85% overall
- [ ] No skipped or disabled tests
- [ ] Tests follow AAA pattern (Arrange-Act-Assert)
- [ ] Unique constraint violation tested

### Quality Checklist
- [ ] No code smells (long methods, god classes)
- [ ] SOLID principles followed
- [ ] Clean code (descriptive names, no magic numbers)
- [ ] No TODO comments in production code
- [ ] Javadoc for all public methods
- [ ] H3 validation logic is clear and correct

---

## Timeline

**Estimated Time**: 6 hours

**Breakdown**:
- SurveyResponse entity + tests: 2 hours
- SurveyResponseRepository + tests: 1 hour
- DuplicateResponseException: 15 minutes
- SurveyService + tests: 2.5 hours
- Integration validation: 15 minutes

---

## Deliverables

1. ✅ All source files in correct package structure
2. ✅ All test files with >85% coverage
3. ✅ No compilation errors
4. ✅ All tests passing
5. ✅ Code ready for Agent 3/4 consumption

---

## Notes

- **Focus on hypothesis validation** (H1, H3)
- **H3 ratio calculation** is critical: Treatment Yes rate / Control Yes rate
- **Denormalize abGroup** from MenuScan for query performance (avoid JOINs)
- **Unique constraint on scanId** prevents duplicate responses
- **Keep it simple** (MVP scope)
- **Follow existing patterns** (see `menu/`, `currency/` modules)
- **No controller code** (handled by Agent 3)
- **Use Lombok** for boilerplate reduction
