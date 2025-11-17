# Agent 1 (Core): ABTest Module - Domain & Service Layer

## Objective
Implement A/B testing core logic (Domain + Repository + Service) for hypothesis validation.

## Scope
✅ **In Scope**:
- Domain layer (ABGroup enum, MenuScan entity)
- Repository layer (MenuScanRepository)
- Service layer (ABTestService)
- Unit tests (>85% coverage)

❌ **Out of Scope**:
- Controller layer (handled by Agent 3)
- API endpoints
- Integration tests (handled by Agent 4)

## Context
- **Hypothesis H1**: Users gain confidence with visual menu vs text-only
- **Hypothesis H3**: Treatment group shows 2x higher confidence than Control
- **A/B Groups**:
  - CONTROL: Text + currency only
  - TREATMENT: Photos + descriptions + text + currency

## Dependencies
**None** - This agent is fully independent and can run in parallel with Agent 2.

**Interface Contract**: See `docs/INTERFACE_CONTRACT.md` for ABTestService specification.

---

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
├── ABTestService.java (implementation)
└── dto/
    └── ABTestResult.java (record)
```

### 3. Infrastructure Layer
```
src/main/java/foodiepass/server/abtest/repository/
└── MenuScanRepository.java (Spring Data JPA)
```

### 4. Test Files
```
src/test/java/foodiepass/server/abtest/
├── domain/
│   └── MenuScanTest.java
├── application/
│   └── ABTestServiceTest.java
└── repository/
    └── MenuScanRepositoryTest.java
```

---

## Implementation Details

### ABGroup.java (enum)

**File**: `src/main/java/foodiepass/server/abtest/domain/ABGroup.java`

```java
package foodiepass.server.abtest.domain;

/**
 * A/B Test Group Assignment
 *
 * Controls which version of the menu users see:
 * - CONTROL: Text translation + currency only
 * - TREATMENT: Full experience with photos and descriptions
 */
public enum ABGroup {
    /**
     * Control group: Minimal menu (text + price only)
     */
    CONTROL,

    /**
     * Treatment group: Enhanced menu (photos + descriptions + text + price)
     */
    TREATMENT
}
```

**Tests Required**:
- Enum has exactly 2 values
- CONTROL value exists
- TREATMENT value exists

---

### MenuScan.java (entity)

**File**: `src/main/java/foodiepass/server/abtest/domain/MenuScan.java`

**Responsibilities**:
- Represent a single menu scan session
- Store A/B group assignment
- Track scan metadata for analytics

**Fields**:
```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@Column(name = "user_id", nullable = false)
private String userId;  // Session ID

@Enumerated(EnumType.STRING)
@Column(name = "ab_group", nullable = false, length = 20)
private ABGroup abGroup;

@Column(name = "image_url", length = 500)
private String imageUrl;  // Optional

@Column(name = "source_language", length = 50)
private String sourceLanguage;  // Optional

@Column(name = "target_language", nullable = false, length = 50)
private String targetLanguage;

@Column(name = "source_currency", length = 10)
private String sourceCurrency;  // Optional

@Column(name = "target_currency", nullable = false, length = 10)
private String targetCurrency;

@Column(name = "created_at", nullable = false)
private LocalDateTime createdAt;
```

**Business Rules**:
- `userId` must not be null or empty
- `abGroup` must not be null
- `targetLanguage` must not be null or empty
- `targetCurrency` must not be null or empty
- `createdAt` is set automatically on creation
- Use Builder pattern for construction
- Use factory method `create()` for validation

**Implementation Pattern**:
```java
@Entity
@Table(name = "menu_scan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuScan {
    // Fields...

    /**
     * Create new MenuScan with validation
     */
    public static MenuScan create(
        String userId,
        ABGroup abGroup,
        String imageUrl,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency
    ) {
        validateUserId(userId);
        validateAbGroup(abGroup);
        validateTargetLanguage(targetLanguage);
        validateTargetCurrency(targetCurrency);

        return MenuScan.builder()
            .userId(userId)
            .abGroup(abGroup)
            .imageUrl(imageUrl)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .sourceCurrency(sourceCurrency)
            .targetCurrency(targetCurrency)
            .createdAt(LocalDateTime.now())
            .build();
    }

    private static void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId must not be null or empty");
        }
    }

    // Other validation methods...
}
```

**Tests Required**:
- Create valid MenuScan
- Reject null userId
- Reject null abGroup
- Reject null targetLanguage
- Reject null targetCurrency
- Auto-set createdAt
- Factory method validation

---

### MenuScanRepository.java

**File**: `src/main/java/foodiepass/server/abtest/repository/MenuScanRepository.java`

```java
package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MenuScan entity
 */
public interface MenuScanRepository extends JpaRepository<MenuScan, UUID> {

    /**
     * Count scans by A/B group
     */
    long countByAbGroup(ABGroup abGroup);

    /**
     * Find all scans by user ID
     */
    List<MenuScan> findByUserId(String userId);

    /**
     * Find scans by user ID and A/B group
     */
    List<MenuScan> findByUserIdAndAbGroup(String userId, ABGroup abGroup);
}
```

**Tests Required**:
- Save and retrieve MenuScan
- Count by CONTROL group
- Count by TREATMENT group
- Find by userId
- Find by userId and abGroup
- Return empty list for non-existent userId

---

### ABTestService.java

**File**: `src/main/java/foodiepass/server/abtest/application/ABTestService.java`

**Responsibilities**:
- Assign users to A/B groups (50:50 random)
- Create and persist MenuScan records
- Retrieve scan records
- Provide analytics for admin

**Interface** (from INTERFACE_CONTRACT.md):
```java
ABGroup assignGroup(String userId);
MenuScan createScan(String userId, ABGroup group, ...);
MenuScan getScanById(UUID scanId);
ABTestResult getResults();
```

**Implementation**:
```java
package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.ABTestResult;
import foodiepass.server.abtest.repository.MenuScanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ABTestService {

    private final MenuScanRepository menuScanRepository;

    /**
     * Assign user to A/B group (50:50 random split)
     *
     * @param userId Session ID or user identifier
     * @return CONTROL or TREATMENT (50% each)
     * @throws IllegalArgumentException if userId is null/empty
     */
    public ABGroup assignGroup(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId must not be null or empty");
        }

        return randomAssign();
    }

    /**
     * Create MenuScan record
     *
     * @return Persisted MenuScan with generated ID
     */
    @Transactional
    public MenuScan createScan(
        String userId,
        ABGroup abGroup,
        String imageUrl,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency
    ) {
        MenuScan scan = MenuScan.create(
            userId, abGroup, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency
        );

        return menuScanRepository.save(scan);
    }

    /**
     * Get MenuScan by ID
     *
     * @throws EntityNotFoundException if scan not found
     */
    public MenuScan getScanById(UUID scanId) {
        return menuScanRepository.findById(scanId)
            .orElseThrow(() -> new EntityNotFoundException(
                "MenuScan not found: " + scanId));
    }

    /**
     * Get A/B test results (admin analytics)
     */
    public ABTestResult getResults() {
        long controlCount = menuScanRepository.countByAbGroup(ABGroup.CONTROL);
        long treatmentCount = menuScanRepository.countByAbGroup(ABGroup.TREATMENT);

        return new ABTestResult(controlCount, treatmentCount);
    }

    /**
     * Random 50:50 assignment
     */
    private ABGroup randomAssign() {
        return ThreadLocalRandom.current().nextBoolean()
            ? ABGroup.CONTROL
            : ABGroup.TREATMENT;
    }
}
```

**Tests Required**:
- Assign group returns CONTROL or TREATMENT
- Assign group throws exception for null userId
- Create scan success
- Create scan validates all required fields
- Get scan by ID success
- Get scan by ID throws EntityNotFoundException
- Get results with no data returns zeros
- Get results with data returns correct counts
- Random assignment is ~50:50 (statistical test with 1000 samples)

---

### ABTestResult.java (DTO)

**File**: `src/main/java/foodiepass/server/abtest/dto/ABTestResult.java`

```java
package foodiepass.server.abtest.dto;

/**
 * A/B Test Analytics Result
 */
public record ABTestResult(
    long controlCount,
    long treatmentCount,
    long totalScans
) {
    public ABTestResult(long controlCount, long treatmentCount) {
        this(controlCount, treatmentCount, controlCount + treatmentCount);
    }
}
```

---

## Test Requirements

### MenuScanTest.java

**File**: `src/test/java/foodiepass/server/abtest/domain/MenuScanTest.java`

**Test Cases**:
```java
@Test
@DisplayName("create() creates valid MenuScan")
void createValidMenuScan() { ... }

@Test
@DisplayName("create() throws exception for null userId")
void rejectNullUserId() { ... }

@Test
@DisplayName("create() throws exception for null abGroup")
void rejectNullAbGroup() { ... }

@Test
@DisplayName("create() throws exception for null targetLanguage")
void rejectNullTargetLanguage() { ... }

@Test
@DisplayName("create() throws exception for null targetCurrency")
void rejectNullTargetCurrency() { ... }

@Test
@DisplayName("createdAt is set automatically")
void autoSetCreatedAt() { ... }
```

**Coverage Target**: 100%

---

### MenuScanRepositoryTest.java

**File**: `src/test/java/foodiepass/server/abtest/repository/MenuScanRepositoryTest.java`

**Setup**: Use `@DataJpaTest` with H2

**Test Cases**:
```java
@Test
@DisplayName("Save and retrieve MenuScan")
void saveAndRetrieve() { ... }

@Test
@DisplayName("Count by CONTROL group")
void countByControl() { ... }

@Test
@DisplayName("Count by TREATMENT group")
void countByTreatment() { ... }

@Test
@DisplayName("Find by userId")
void findByUserId() { ... }

@Test
@DisplayName("Find by userId and abGroup")
void findByUserIdAndAbGroup() { ... }

@Test
@DisplayName("Return empty for non-existent userId")
void emptyForNonExistentUser() { ... }
```

**Coverage Target**: 100%

---

### ABTestServiceTest.java

**File**: `src/test/java/foodiepass/server/abtest/application/ABTestServiceTest.java`

**Setup**: Use `@ExtendWith(MockitoExtension.class)`, mock repository

**Test Cases**:
```java
@Test
@DisplayName("assignGroup() returns CONTROL or TREATMENT")
void assignGroupReturnsValidGroup() { ... }

@Test
@DisplayName("assignGroup() throws exception for null userId")
void assignGroupRejectsNullUserId() { ... }

@Test
@DisplayName("assignGroup() distributes ~50:50 over 1000 samples")
void assignGroupDistribution() {
    // Run 1000 times, verify ratio is 40-60%
}

@Test
@DisplayName("createScan() saves and returns scan")
void createScanSuccess() { ... }

@Test
@DisplayName("createScan() validates required fields")
void createScanValidation() { ... }

@Test
@DisplayName("getScanById() returns existing scan")
void getScanByIdSuccess() { ... }

@Test
@DisplayName("getScanById() throws EntityNotFoundException")
void getScanByIdNotFound() { ... }

@Test
@DisplayName("getResults() returns correct counts")
void getResultsWithData() { ... }

@Test
@DisplayName("getResults() returns zeros for no data")
void getResultsNoData() { ... }
```

**Coverage Target**: >90%

---

## Success Criteria

### Implementation Checklist
- [ ] ABGroup enum created with CONTROL and TREATMENT
- [ ] MenuScan entity with all required fields and validation
- [ ] MenuScanRepository with query methods
- [ ] ABTestService implements interface contract exactly
- [ ] ABTestResult DTO matches specification
- [ ] All files follow package structure

### Testing Checklist
- [ ] All domain tests pass (MenuScanTest)
- [ ] All repository tests pass (MenuScanRepositoryTest)
- [ ] All service tests pass (ABTestServiceTest)
- [ ] Test coverage >85% overall
- [ ] No skipped or disabled tests
- [ ] Tests follow AAA pattern (Arrange-Act-Assert)

### Quality Checklist
- [ ] No code smells (long methods, god classes)
- [ ] SOLID principles followed
- [ ] Clean code (descriptive names, no magic numbers)
- [ ] No TODO comments in production code
- [ ] Javadoc for all public methods
- [ ] Validation logic is clear and tested

---

## Timeline

**Estimated Time**: 6 hours

**Breakdown**:
- ABGroup enum + tests: 30 minutes
- MenuScan entity + tests: 2 hours
- MenuScanRepository + tests: 1 hour
- ABTestService + tests: 2 hours
- Integration validation: 30 minutes

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
- **Keep it simple** (MVP scope)
- **Follow existing patterns** (see `menu/`, `currency/` modules)
- **No controller code** (handled by Agent 3)
- **Use Lombok** for boilerplate reduction (@Getter, @Builder, etc.)
- **ThreadLocalRandom** for thread-safe random assignment
