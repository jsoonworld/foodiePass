# FoodiePass Backend: TDD Implementation Plan

> **문서 목적**: TDD 방식으로 백엔드를 단계별로 구현하기 위한 상세 체크리스트

---

## 📋 전체 구현 단계

```text
Phase 1: ABTest 모듈 구현
  ├─ Step 1: ABGroup enum 구현
  ├─ Step 2: MenuScan entity 구현
  ├─ Step 3: MenuScanRepository 구현
  ├─ Step 4: ABTestService 구현
  └─ Step 5: ABTestController 구현

Phase 2: Survey 모듈 구현
  ├─ Step 6: SurveyResponse entity 구현
  ├─ Step 7: SurveyResponseRepository 구현
  ├─ Step 8: SurveyService 구현
  └─ Step 9: SurveyController 구현

Phase 3: MenuScan API 구현
  ├─ Step 9.5: 공통 인프라 (GlobalExceptionHandler, ErrorResponse)
  ├─ Step 10: MenuScanRequest/Response DTO 구현
  ├─ Step 11: MenuScanController 구현
  └─ Step 12: 통합 테스트

Phase 4: 기술 검증 (H2)
  └─ Step 13: 내부 기술 검증 테스트
```bash

---

## 🎯 TDD 사이클

각 단계는 다음 TDD 사이클을 따릅니다:

```text
🔴 RED   → 실패하는 테스트 작성
🟢 GREEN → 최소한의 코드로 테스트 통과
🔵 REFACTOR → 코드 개선 및 리팩토링
✅ VERIFY → 모든 테스트 재실행 및 검증
```bash

### TDD 준수 원칙

각 사이클마다 다음 원칙을 확인합니다:

| 단계 | 핵심 원칙 | 검증 질문 |
|------|----------|-----------|
| 🔴 RED | 테스트 우선 작성 | "프로덕션 코드를 작성하기 전에 테스트를 먼저 작성했는가?" |
| 🟢 GREEN | 최소 구현 | "테스트를 통과시키기 위한 최소한의 코드만 작성했는가?" |
| 🔵 REFACTOR | 기능 보존 | "리팩토링 전후로 테스트가 모두 통과하는가?" |
| ✅ VERIFY | 전체 검증 | "다른 테스트에 영향을 주지 않았는가?" |

---

# Phase 1: ABTest 모듈 구현

## Step 1: ABGroup enum 구현

### 1.1 요구사항 정의
- [ ] ABGroup enum은 CONTROL과 TREATMENT 두 가지 값을 가진다
- [ ] CONTROL: 텍스트 + 환율만 표시
- [ ] TREATMENT: 사진 + 설명 + 텍스트 + 환율 표시

### 1.2 🔴 RED - 테스트 작성

#### 파일 생성
- [ ] `backend/src/test/java/foodiepass/server/abtest/domain/ABGroupTest.java` 생성

#### 테스트 케이스 작성
```java
package foodiepass.server.abtest.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ABGroupTest {

    @Test
    @DisplayName("CONTROL 값이 존재한다")
    void hasControlValue() {
        // Given & When
        ABGroup control = ABGroup.CONTROL;

        // Then
        assertNotNull(control);
        assertEquals("CONTROL", control.name());
    }

    @Test
    @DisplayName("TREATMENT 값이 존재한다")
    void hasTreatmentValue() {
        // Given & When
        ABGroup treatment = ABGroup.TREATMENT;

        // Then
        assertNotNull(treatment);
        assertEquals("TREATMENT", treatment.name());
    }

    @Test
    @DisplayName("enum은 정확히 2개의 값을 가진다")
    void hasTwoValues() {
        // Given & When
        ABGroup[] values = ABGroup.values();

        // Then
        assertEquals(2, values.length);
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests ABGroupTest` 실행
- [ ] 테스트 실패 확인 (클래스가 없으므로 컴파일 에러)
- [ ] 실패 메시지 확인: "Cannot resolve symbol 'ABGroup'"

#### 🔍 TDD 준수 검증 (RED 단계)
- [ ] **테스트 우선**: 프로덕션 코드(`ABGroup.java`)를 작성하기 전에 테스트를 먼저 작성했는가?
- [ ] **실패 확인**: 테스트가 정말로 실패하는 것을 확인했는가?
- [ ] **실패 이유**: 실패 이유가 명확한가? (예: "Cannot resolve symbol 'ABGroup'")
- [ ] **테스트 품질**:
  - 모든 enum 값(CONTROL, TREATMENT)을 테스트하는가?
  - enum 개수(2개)를 검증하는가?
  - 의미 있는 @DisplayName을 사용하는가?
- [ ] **엣지 케이스**: enum의 모든 요구사항을 테스트 케이스로 작성했는가?

### 1.3 🟢 GREEN - 최소 구현

#### 파일 생성
- [ ] `backend/src/main/java/foodiepass/server/abtest/domain/ABGroup.java` 생성

#### 구현
```java
package foodiepass.server.abtest.domain;

public enum ABGroup {
    CONTROL,    // 텍스트 + 환율만
    TREATMENT   // 사진 + 설명 + 텍스트 + 환율
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests ABGroupTest` 실행
- [ ] 모든 테스트 통과 확인 ✅
- [ ] 테스트 커버리지 확인: 100%

#### 🔍 TDD 준수 검증 (GREEN 단계)
- [ ] **최소 구현**: 테스트를 통과시키기 위한 최소한의 코드만 작성했는가?
  - 불필요한 메서드나 필드를 추가하지 않았는가?
  - CONTROL, TREATMENT 두 값만 정의했는가?
- [ ] **테스트 통과**: 모든 테스트가 통과하는가?
- [ ] **프로덕션 코드 수정**: 테스트를 통과시키기 위해 **프로덕션 코드**를 수정했는가? (테스트 코드 수정 ❌)
- [ ] **기능 완성도**: 요구사항(CONTROL, TREATMENT enum)을 모두 만족하는가?
- [ ] **코드 품질**: 컴파일 에러가 없고, 문법적으로 올바른가?

### 1.4 🔵 REFACTOR - 리팩토링

#### 개선 사항 검토
- [ ] enum에 설명 추가 필요?
- [ ] 추가 메서드 필요? (현재는 필요 없음)

#### 최종 코드
```java
package foodiepass.server.abtest.domain;

/**
 * A/B 테스트 그룹
 */
public enum ABGroup {
    /**
     * 대조군: 텍스트 번역 + 환율 변환만 제공
     */
    CONTROL,

    /**
     * 실험군: 음식 사진 + 설명 + 텍스트 번역 + 환율 변환 제공
     */
    TREATMENT
}
```bash

#### 테스트 재실행
- [ ] `./gradlew test --tests ABGroupTest` 실행
- [ ] 모든 테스트 여전히 통과 ✅

#### 🔍 TDD 준수 검증 (REFACTOR 단계)
- [ ] **리팩토링 전 상태**: 리팩토링 시작 전에 모든 테스트가 통과하는 상태였는가? (GREEN 단계 완료)
- [ ] **기능 보존**: 리팩토링 후에도 모든 테스트가 통과하는가?
- [ ] **코드 개선**:
  - JavaDoc 주석으로 가독성이 개선되었는가?
  - 각 enum 값의 의미가 명확한가?
- [ ] **불필요한 변경 방지**: 기능 변경 없이 코드 구조만 개선했는가?
- [ ] **리팩토링 범위**: 과도한 리팩토링을 하지 않았는가? (단순 주석 추가 수준)

### 1.5 ✅ VERIFY - 검증

#### 테스트 검증
- [ ] `./gradlew test --tests ABGroupTest` 실행 → 통과
- [ ] `./gradlew test` 전체 테스트 실행 → 통과

#### 커버리지 검증
- [ ] `./gradlew test jacocoTestReport` 실행
- [ ] `open build/reports/jacoco/test/html/index.html` → ABGroup 커버리지 100% 확인

#### Git 커밋
- [ ] Git commit: `git commit -m "feat: Add ABGroup enum for A/B testing"`

#### 🔍 TDD 준수 검증 (VERIFY 단계)
- [ ] **격리 테스트**: 이 Step의 변경이 다른 테스트에 영향을 주지 않았는가?
- [ ] **전체 테스트**: 전체 테스트 스위트가 통과하는가?
- [ ] **커버리지 목표**: 테스트 커버리지 >80% 달성했는가? (이 경우 100%)
- [ ] **Commit 메시지**:
  - 명확한 prefix를 사용했는가? (`feat`, `fix`, `refactor` 등)
  - 무엇을 구현했는지 명확한가?
  - A/B 테스트 도메인을 언급하는가?
- [ ] **코드 리뷰 준비**: 다른 개발자가 이해할 수 있는 코드인가?

### 1.6 🎯 TDD 사이클 회고

#### 이 Step에서 TDD를 제대로 따랐는가?
- [ ] **🔴 RED 단계를 거쳤는가?**: 테스트 먼저 작성 → 실패 확인
- [ ] **🟢 GREEN 단계를 거쳤는가?**: 최소 구현 → 테스트 통과
- [ ] **🔵 REFACTOR 단계를 거쳤는가?**: 리팩토링 → 테스트 여전히 통과
- [ ] **✅ VERIFY 단계를 거쳤는가?**: 전체 검증 → 커밋

#### 개선점
- [ ] 놓친 테스트 케이스가 있는가?
- [ ] 불필요하게 복잡한 코드를 작성했는가?
- [ ] 테스트 없이 구현한 부분이 있는가?

---

## Step 2: MenuScan entity 구현

### 2.1 요구사항 정의
- [ ] MenuScan은 메뉴 스캔 세션을 나타내는 엔티티
- [ ] UUID id를 가진다
- [ ] userId (세션 ID)를 가진다
- [ ] ABGroup (A/B 그룹)을 가진다
- [ ] 이미지 URL, 언어/화폐 정보를 가진다
- [ ] 생성 시각을 가진다

### 2.2 🔴 RED - 테스트 작성

#### 파일 생성
- [ ] `backend/src/test/java/foodiepass/server/abtest/domain/MenuScanTest.java` 생성

#### 테스트 케이스 작성
```java
package foodiepass.server.abtest.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MenuScanTest {

    @Test
    @DisplayName("MenuScan 객체를 생성할 수 있다")
    void createMenuScan() {
        // Given
        UUID id = UUID.randomUUID();
        String userId = "test-user-123";
        ABGroup abGroup = ABGroup.CONTROL;
        String imageUrl = "https://s3.amazonaws.com/menu.jpg";
        String sourceLanguage = "ja";
        String targetLanguage = "ko";
        String sourceCurrency = "JPY";
        String targetCurrency = "KRW";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        MenuScan menuScan = new MenuScan(
            id, userId, abGroup, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency,
            createdAt
        );

        // Then
        assertNotNull(menuScan);
        assertEquals(id, menuScan.getId());
        assertEquals(userId, menuScan.getUserId());
        assertEquals(abGroup, menuScan.getAbGroup());
        assertEquals(imageUrl, menuScan.getImageUrl());
        assertEquals(sourceLanguage, menuScan.getSourceLanguage());
        assertEquals(targetLanguage, menuScan.getTargetLanguage());
        assertEquals(sourceCurrency, menuScan.getSourceCurrency());
        assertEquals(targetCurrency, menuScan.getTargetCurrency());
        assertEquals(createdAt, menuScan.getCreatedAt());
    }

    @Test
    @DisplayName("userId가 null이면 예외를 던진다")
    void throwExceptionWhenUserIdIsNull() {
        // Given
        UUID id = UUID.randomUUID();
        String userId = null;
        ABGroup abGroup = ABGroup.CONTROL;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new MenuScan(
                id, userId, abGroup, null,
                "ja", "ko", "JPY", "KRW",
                LocalDateTime.now()
            );
        });
    }

    @Test
    @DisplayName("abGroup이 null이면 예외를 던진다")
    void throwExceptionWhenAbGroupIsNull() {
        // Given
        UUID id = UUID.randomUUID();
        String userId = "test-user";
        ABGroup abGroup = null;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new MenuScan(
                id, userId, abGroup, null,
                "ja", "ko", "JPY", "KRW",
                LocalDateTime.now()
            );
        });
    }

    @Test
    @DisplayName("targetLanguage가 null이면 예외를 던진다")
    void throwExceptionWhenTargetLanguageIsNull() {
        // Given
        UUID id = UUID.randomUUID();
        String userId = "test-user";
        ABGroup abGroup = ABGroup.CONTROL;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new MenuScan(
                id, userId, abGroup, null,
                "ja", null, "JPY", "KRW",
                LocalDateTime.now()
            );
        });
    }

    @Test
    @DisplayName("targetCurrency가 null이면 예외를 던진다")
    void throwExceptionWhenTargetCurrencyIsNull() {
        // Given
        UUID id = UUID.randomUUID();
        String userId = "test-user";
        ABGroup abGroup = ABGroup.CONTROL;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new MenuScan(
                id, userId, abGroup, null,
                "ja", "ko", "JPY", null,
                LocalDateTime.now()
            );
        });
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests MenuScanTest` 실행
- [ ] 테스트 실패 확인 (클래스가 없으므로 컴파일 에러)

### 2.3 🟢 GREEN - 최소 구현

#### 파일 생성
- [ ] `backend/src/main/java/foodiepass/server/abtest/domain/MenuScan.java` 생성

#### 구현
```java
package foodiepass.server.abtest.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "menu_scan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MenuScan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ab_group", nullable = false, length = 20)
    private ABGroup abGroup;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "source_language", length = 50)
    private String sourceLanguage;

    @Column(name = "target_language", nullable = false, length = 50)
    private String targetLanguage;

    @Column(name = "source_currency", length = 10)
    private String sourceCurrency;

    @Column(name = "target_currency", nullable = false, length = 10)
    private String targetCurrency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public MenuScan(
        UUID id,
        String userId,
        ABGroup abGroup,
        String imageUrl,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency,
        LocalDateTime createdAt
    ) {
        validateUserId(userId);
        validateAbGroup(abGroup);
        validateTargetLanguage(targetLanguage);
        validateTargetCurrency(targetCurrency);

        this.id = id;
        this.userId = userId;
        this.abGroup = abGroup;
        this.imageUrl = imageUrl;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.createdAt = createdAt;
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId must not be null or empty");
        }
    }

    private void validateAbGroup(ABGroup abGroup) {
        if (abGroup == null) {
            throw new IllegalArgumentException("abGroup must not be null");
        }
    }

    private void validateTargetLanguage(String targetLanguage) {
        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            throw new IllegalArgumentException("targetLanguage must not be null or empty");
        }
    }

    private void validateTargetCurrency(String targetCurrency) {
        if (targetCurrency == null || targetCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("targetCurrency must not be null or empty");
        }
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests MenuScanTest` 실행
- [ ] 모든 테스트 통과 확인 ✅

### 2.4 🔵 REFACTOR - 리팩토링

#### 개선 사항 검토
- [ ] Builder 패턴 적용 검토
- [ ] 생성 시각 자동 설정 검토

#### 개선된 코드
```java
package foodiepass.server.abtest.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "menu_scan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuScan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ab_group", nullable = false, length = 20)
    private ABGroup abGroup;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "source_language", length = 50)
    private String sourceLanguage;

    @Column(name = "target_language", nullable = false, length = 50)
    private String targetLanguage;

    @Column(name = "source_currency", length = 10)
    private String sourceCurrency;

    @Column(name = "target_currency", nullable = false, length = 10)
    private String targetCurrency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 새 MenuScan 생성 (Factory method)
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

    private static void validateAbGroup(ABGroup abGroup) {
        if (abGroup == null) {
            throw new IllegalArgumentException("abGroup must not be null");
        }
    }

    private static void validateTargetLanguage(String targetLanguage) {
        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            throw new IllegalArgumentException("targetLanguage must not be null or empty");
        }
    }

    private static void validateTargetCurrency(String targetCurrency) {
        if (targetCurrency == null || targetCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("targetCurrency must not be null or empty");
        }
    }
}
```java

#### Factory method 테스트 추가
```java
@Test
@DisplayName("create() 메서드로 MenuScan을 생성할 수 있다")
void createMenuScanUsingFactoryMethod() {
    // Given
    String userId = "test-user";
    ABGroup abGroup = ABGroup.TREATMENT;

    // When
    MenuScan menuScan = MenuScan.create(
        userId, abGroup, null,
        "ja", "ko", "JPY", "KRW"
    );

    // Then
    assertNotNull(menuScan);
    assertEquals(userId, menuScan.getUserId());
    assertEquals(abGroup, menuScan.getAbGroup());
    assertNotNull(menuScan.getCreatedAt());
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests MenuScanTest` 실행
- [ ] 모든 테스트 통과 확인 ✅

### 2.5 ✅ VERIFY - 검증

- [ ] `./gradlew test --tests MenuScanTest` 실행 → 통과
- [ ] `./gradlew test` 전체 테스트 실행 → 통과
- [ ] 테스트 커버리지 확인: >80%
- [ ] Git commit: `git commit -m "feat: Add MenuScan entity with validation"`

#### 🔍 TDD 준수 검증
- [ ] 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ VERIFY 사이클을 완료했는가?
- [ ] 모든 검증 로직(userId, abGroup, targetLanguage, targetCurrency)에 대한 테스트가 있는가?
- [ ] Factory method(`create`)에 대한 테스트가 있는가?
- [ ] 전체 테스트가 통과하고 커버리지 >80%인가?

---

## Step 3: MenuScanRepository 구현

### 3.1 요구사항 정의
- [ ] MenuScan을 저장하고 조회할 수 있는 Repository
- [ ] userId로 가장 최근 스캔 조회 기능
- [ ] ABGroup별 카운트 기능

### 3.2 🔴 RED - 테스트 작성

#### 파일 생성
- [ ] `backend/src/test/java/foodiepass/server/abtest/repository/MenuScanRepositoryTest.java` 생성

#### 테스트 케이스 작성
```java
package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MenuScanRepositoryTest {

    @Autowired
    private MenuScanRepository menuScanRepository;

    @BeforeEach
    void setUp() {
        menuScanRepository.deleteAll();
    }

    @Test
    @DisplayName("MenuScan을 저장할 수 있다")
    void saveMenuScan() {
        // Given
        MenuScan menuScan = MenuScan.create(
            "user-123", ABGroup.CONTROL, null,
            "ja", "ko", "JPY", "KRW"
        );

        // When
        MenuScan saved = menuScanRepository.save(menuScan);

        // Then
        assertNotNull(saved.getId());
        assertEquals("user-123", saved.getUserId());
    }

    @Test
    @DisplayName("userId로 가장 최근 스캔을 조회할 수 있다")
    void findFirstByUserIdOrderByCreatedAtDesc() throws InterruptedException {
        // Given
        String userId = "user-123";
        MenuScan scan1 = MenuScan.create(userId, ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");
        menuScanRepository.save(scan1);

        Thread.sleep(10); // 시간 차이 보장

        MenuScan scan2 = MenuScan.create(userId, ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW");
        menuScanRepository.save(scan2);

        // When
        Optional<MenuScan> result = menuScanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(scan2.getId(), result.get().getId());
        assertEquals(ABGroup.TREATMENT, result.get().getAbGroup());
    }

    @Test
    @DisplayName("존재하지 않는 userId로 조회하면 Optional.empty를 반환한다")
    void findFirstByUserIdOrderByCreatedAtDesc_notFound() {
        // Given
        String userId = "non-existent-user";

        // When
        Optional<MenuScan> result = menuScanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("ABGroup별로 개수를 셀 수 있다")
    void countByAbGroup() {
        // Given
        menuScanRepository.save(MenuScan.create("user1", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW"));
        menuScanRepository.save(MenuScan.create("user2", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW"));
        menuScanRepository.save(MenuScan.create("user3", ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW"));

        // When
        long controlCount = menuScanRepository.countByAbGroup(ABGroup.CONTROL);
        long treatmentCount = menuScanRepository.countByAbGroup(ABGroup.TREATMENT);

        // Then
        assertEquals(2, controlCount);
        assertEquals(1, treatmentCount);
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests MenuScanRepositoryTest` 실행
- [ ] 테스트 실패 확인 (인터페이스가 없으므로 컴파일 에러)

### 3.3 🟢 GREEN - 최소 구현

#### 파일 생성
- [ ] `backend/src/main/java/foodiepass/server/abtest/repository/MenuScanRepository.java` 생성

#### 구현
```java
package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuScanRepository extends JpaRepository<MenuScan, UUID> {

    /**
     * 사용자의 가장 최근 스캔 조회
     */
    Optional<MenuScan> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * A/B 그룹별 스캔 개수
     */
    long countByAbGroup(ABGroup abGroup);
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests MenuScanRepositoryTest` 실행
- [ ] 모든 테스트 통과 확인 ✅

### 3.4 🔵 REFACTOR - 리팩토링

#### 개선 사항 검토
- [ ] 추가 쿼리 메서드 필요? (현재는 충분함)
- [ ] 인덱스 최적화? (필요 시 추가)

#### 현재 코드 유지
- 리팩토링 불필요 (심플한 인터페이스)

### 3.5 ✅ VERIFY - 검증

- [ ] `./gradlew test --tests MenuScanRepositoryTest` 실행 → 통과
- [ ] `./gradlew test` 전체 테스트 실행 → 통과
- [ ] Git commit: `git commit -m "feat: Add MenuScanRepository with query methods"`

#### 🔍 TDD 준수 검증
- [ ] 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ VERIFY 사이클을 완료했는가?
- [ ] 모든 쿼리 메서드(`findFirstByUserIdOrderByCreatedAtDesc`, `countByAbGroup`)에 대한 테스트가 있는가?
- [ ] 엣지 케이스(존재하지 않는 userId) 테스트가 있는가?
- [ ] @DataJpaTest를 사용하여 Repository만 격리 테스트했는가?

---

## Step 4: ABTestService 구현

### 4.1 요구사항 정의
- [ ] 사용자를 A/B 그룹에 랜덤 배정 (50:50)
- [ ] 기존 사용자는 이전 그룹 유지
- [ ] MenuScan 생성 기능
- [ ] A/B 테스트 결과 조회 기능 (관리자용)

### 4.2 🔴 RED - 테스트 작성

#### 파일 생성
- [ ] `backend/src/test/java/foodiepass/server/abtest/application/ABTestServiceTest.java` 생성

#### 테스트 케이스 작성
```java
package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.response.ABTestResult;
import foodiepass.server.abtest.repository.MenuScanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ABTestServiceTest {

    @Autowired
    private ABTestService abTestService;

    @Autowired
    private MenuScanRepository menuScanRepository;

    @BeforeEach
    void setUp() {
        menuScanRepository.deleteAll();
    }

    @Test
    @DisplayName("신규 사용자는 A/B 그룹에 배정된다")
    void assignGroup_newUser() {
        // Given
        String userId = "new-user-123";

        // When
        ABGroup group = abTestService.assignGroup(userId);

        // Then
        assertNotNull(group);
        assertTrue(group == ABGroup.CONTROL || group == ABGroup.TREATMENT);
    }

    @Test
    @DisplayName("기존 사용자는 이전 그룹을 유지한다")
    void assignGroup_existingUser_maintainsSameGroup() {
        // Given
        String userId = "existing-user-456";
        MenuScan existingScan = MenuScan.create(
            userId, ABGroup.CONTROL, null,
            "ja", "ko", "JPY", "KRW"
        );
        menuScanRepository.save(existingScan);

        // When
        ABGroup assignedGroup = abTestService.assignGroup(userId);

        // Then
        assertEquals(ABGroup.CONTROL, assignedGroup);
    }

    @Test
    @DisplayName("여러 사용자를 배정하면 대략 50:50 비율이 된다")
    void assignGroup_multipleUsers_balancedRatio() {
        // Given
        int totalUsers = 1000;

        // When
        int controlCount = 0;
        for (int i = 0; i < totalUsers; i++) {
            ABGroup group = abTestService.assignGroup("user-" + i);
            if (group == ABGroup.CONTROL) controlCount++;
        }

        // Then
        double controlRatio = (double) controlCount / totalUsers * 100;
        assertTrue(controlRatio >= 40.0 && controlRatio <= 60.0,
            "Control 비율: " + controlRatio + "% (40-60% 범위 내여야 함)");
    }

    @Test
    @DisplayName("MenuScan을 생성할 수 있다")
    void createScan() {
        // Given
        String userId = "user-123";
        ABGroup abGroup = ABGroup.TREATMENT;

        // When
        MenuScan scan = abTestService.createScan(
            userId, abGroup, "https://s3.../menu.jpg",
            "ja", "ko", "JPY", "KRW"
        );

        // Then
        assertNotNull(scan);
        assertNotNull(scan.getId());
        assertEquals(userId, scan.getUserId());
        assertEquals(abGroup, scan.getAbGroup());
        assertEquals("ko", scan.getTargetLanguage());
    }

    @Test
    @DisplayName("A/B 테스트 결과를 조회할 수 있다")
    void getResults() {
        // Given
        abTestService.createScan("user1", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");
        abTestService.createScan("user2", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");
        abTestService.createScan("user3", ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW");

        // When
        ABTestResult result = abTestService.getResults();

        // Then
        assertEquals(2, result.controlCount());
        assertEquals(1, result.treatmentCount());
        assertEquals(3, result.totalScans());
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests ABTestServiceTest` 실행
- [ ] 테스트 실패 확인 (클래스가 없으므로 컴파일 에러)

### 4.3 🟢 GREEN - 최소 구현

#### DTO 파일 생성
- [ ] `backend/src/main/java/foodiepass/server/abtest/dto/response/ABTestResult.java` 생성

```java
package foodiepass.server.abtest.dto.response;

public record ABTestResult(
    long controlCount,
    long treatmentCount,
    long totalScans
) {
    public ABTestResult(long controlCount, long treatmentCount) {
        this(controlCount, treatmentCount, controlCount + treatmentCount);
    }
}
```java

#### Service 파일 생성
- [ ] `backend/src/main/java/foodiepass/server/abtest/application/ABTestService.java` 생성

#### 구현
```java
package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.response.ABTestResult;
import foodiepass.server.abtest.repository.MenuScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ABTestService {

    private final MenuScanRepository menuScanRepository;
    private final Random random = new Random();

    /**
     * 사용자를 A/B 그룹에 배정
     * - 신규 사용자: 랜덤 배정 (50:50)
     * - 기존 사용자: 이전 그룹 유지
     */
    public ABGroup assignGroup(String userId) {
        Optional<MenuScan> existingScan = menuScanRepository
            .findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (existingScan.isPresent()) {
            return existingScan.get().getAbGroup();
        }

        return random.nextBoolean() ? ABGroup.CONTROL : ABGroup.TREATMENT;
    }

    /**
     * MenuScan 생성
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
     * A/B 테스트 결과 조회 (관리자용)
     */
    public ABTestResult getResults() {
        long controlCount = menuScanRepository.countByAbGroup(ABGroup.CONTROL);
        long treatmentCount = menuScanRepository.countByAbGroup(ABGroup.TREATMENT);

        return new ABTestResult(controlCount, treatmentCount);
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests ABTestServiceTest` 실행
- [ ] 모든 테스트 통과 확인 ✅

### 4.4 🔵 REFACTOR - 리팩토링

#### 개선 사항 검토
- [ ] Random을 ThreadLocalRandom으로 변경?
- [ ] 랜덤 로직을 별도 클래스로 분리?

#### 개선된 코드
```java
package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.response.ABTestResult;
import foodiepass.server.abtest.repository.MenuScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ABTestService {

    private final MenuScanRepository menuScanRepository;

    /**
     * 사용자를 A/B 그룹에 배정
     * - 신규 사용자: 랜덤 배정 (50:50)
     * - 기존 사용자: 이전 그룹 유지
     */
    public ABGroup assignGroup(String userId) {
        Optional<MenuScan> existingScan = menuScanRepository
            .findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (existingScan.isPresent()) {
            return existingScan.get().getAbGroup();
        }

        return randomAssign();
    }

    /**
     * MenuScan 생성 및 저장
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
     * A/B 테스트 결과 조회 (관리자용)
     */
    public ABTestResult getResults() {
        long controlCount = menuScanRepository.countByAbGroup(ABGroup.CONTROL);
        long treatmentCount = menuScanRepository.countByAbGroup(ABGroup.TREATMENT);

        return new ABTestResult(controlCount, treatmentCount);
    }

    /**
     * 랜덤 그룹 배정 (50:50)
     */
    private ABGroup randomAssign() {
        return ThreadLocalRandom.current().nextBoolean()
            ? ABGroup.CONTROL
            : ABGroup.TREATMENT;
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests ABTestServiceTest` 실행
- [ ] 모든 테스트 통과 확인 ✅

### 4.5 ✅ VERIFY - 검증

- [ ] `./gradlew test --tests ABTestServiceTest` 실행 → 통과
- [ ] `./gradlew test` 전체 테스트 실행 → 통과
- [ ] 테스트 커버리지 확인: >80%
- [ ] Git commit: `git commit -m "feat: Add ABTestService with group assignment logic"`

#### 🔍 TDD 준수 검증
- [ ] 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ VERIFY 사이클을 완료했는가?
- [ ] 핵심 비즈니스 로직 테스트:
  - 신규 사용자 랜덤 배정 테스트?
  - 기존 사용자 그룹 유지 테스트?
  - 50:50 비율 테스트? (통계적 검증)
- [ ] Service 레이어만 격리 테스트했는가? (@SpringBootTest 사용)
- [ ] ThreadLocalRandom으로 리팩토링 후 테스트가 여전히 통과하는가?

---

## Step 5: ABTestController 구현

### 5.1 요구사항 정의
- [ ] GET /api/admin/ab-test/results 엔드포인트
- [ ] A/B 테스트 결과를 JSON으로 반환

### 5.2 🔴 RED - 테스트 작성

#### 파일 생성
- [ ] `backend/src/test/java/foodiepass/server/abtest/api/ABTestControllerTest.java` 생성

#### 테스트 케이스 작성
```java
package foodiepass.server.abtest.api;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.dto.response.ABTestResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ABTestController.class)
class ABTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ABTestService abTestService;

    @Test
    @DisplayName("GET /api/admin/ab-test/results - A/B 테스트 결과를 반환한다")
    void getResults() throws Exception {
        // Given
        ABTestResult mockResult = new ABTestResult(50, 50);
        when(abTestService.getResults()).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/admin/ab-test/results"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.controlCount").value(50))
            .andExpect(jsonPath("$.treatmentCount").value(50))
            .andExpect(jsonPath("$.totalScans").value(100));
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests ABTestControllerTest` 실행
- [ ] 테스트 실패 확인 (컨트롤러가 없으므로 에러)

### 5.3 🟢 GREEN - 최소 구현

#### 파일 생성
- [ ] `backend/src/main/java/foodiepass/server/abtest/api/ABTestController.java` 생성

#### 구현
```java
package foodiepass.server.abtest.api;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.dto.response.ABTestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ab-test")
@RequiredArgsConstructor
public class ABTestController {

    private final ABTestService abTestService;

    /**
     * A/B 테스트 결과 조회 (관리자용)
     */
    @GetMapping("/results")
    public ResponseEntity<ABTestResult> getResults() {
        ABTestResult result = abTestService.getResults();
        return ResponseEntity.ok(result);
    }
}
```bash

#### 테스트 실행
- [ ] `./gradlew test --tests ABTestControllerTest` 실행
- [ ] 모든 테스트 통과 확인 ✅

### 5.4 🔵 REFACTOR - 리팩토링

#### 개선 사항 검토
- [ ] API 응답에 타임스탬프 추가?
- [ ] 관리자 인증 추가? (v2에서는 생략)

#### 현재 코드 유지
- 리팩토링 불필요 (심플한 컨트롤러)

### 5.5 ✅ VERIFY - 검증

- [ ] `./gradlew test --tests ABTestControllerTest` 실행 → 통과
- [ ] `./gradlew test` 전체 테스트 실행 → 통과
- [ ] Postman으로 수동 테스트
  - [ ] `GET http://localhost:8080/api/admin/ab-test/results` 호출
  - [ ] 응답 확인
- [ ] Git commit: `git commit -m "feat: Add ABTestController for admin results"`

#### 🔍 TDD 준수 검증
- [ ] 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ VERIFY 사이클을 완료했는가?
- [ ] Controller 레이어 테스트:
  - @WebMvcTest로 Controller만 격리 테스트했는가?
  - MockBean으로 Service를 모킹했는가?
  - JSON 응답 형식을 검증했는가? (controlCount, treatmentCount, totalScans)
- [ ] 수동 테스트(Postman)도 성공했는가?
- [ ] HTTP 상태 코드(200 OK)를 검증했는가?

---

## ✅ Phase 1 완료 체크리스트

### 기능 완료
- [ ] ABGroup enum 구현 및 테스트 (Step 1)
- [ ] MenuScan entity 구현 및 테스트 (Step 2)
- [ ] MenuScanRepository 구현 및 테스트 (Step 3)
- [ ] ABTestService 구현 및 테스트 (Step 4)
- [ ] ABTestController 구현 및 테스트 (Step 5)

### 품질 검증
- [ ] 전체 테스트 실행 (`./gradlew test`) → 통과
- [ ] 테스트 커버리지 확인 (`./gradlew test jacocoTestReport`) → >80%
- [ ] 각 레이어별 커버리지 확인:
  - [ ] Domain Layer (ABGroup, MenuScan): >90%
  - [ ] Repository Layer: >85%
  - [ ] Service Layer (ABTestService): >85%
  - [ ] Controller Layer (ABTestController): >80%

### Git 관리
- [ ] 각 Step마다 commit 완료 (총 5개 commit)
- [ ] Commit 메시지가 명확하고 일관성 있는가?
- [ ] Git branch merge: `git checkout develop && git merge feature/abtest-module`

### 🔍 Phase 1 TDD 회고

#### 전체 TDD 사이클 준수
- [ ] 모든 Step에서 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ VERIFY 사이클을 따랐는가?
- [ ] 테스트 없이 구현한 코드가 있는가? (있으면 ❌)
- [ ] 테스트를 통과시키기 위해 테스트를 수정한 적이 있는가? (있으면 ❌)

#### 테스트 품질
- [ ] 모든 테스트가 의미 있는 @DisplayName을 가지는가?
- [ ] 테스트가 Given-When-Then 구조를 따르는가?
- [ ] 엣지 케이스와 예외 상황을 테스트하는가?
- [ ] 테스트가 격리되어 있는가? (서로 영향을 주지 않는가?)

#### 코드 품질
- [ ] 도메인 로직이 명확하게 분리되어 있는가?
- [ ] 불필요한 복잡성이 없는가?
- [ ] 코드가 읽기 쉽고 이해하기 쉬운가?
- [ ] SOLID 원칙을 따르는가?

#### 개선점 기록
```text
Phase 1에서 배운 점:
-
-
-

Phase 2에서 개선할 점:
-
-
-
```bash

---

# Phase 2: Survey 모듈 구현

> Phase 1과 동일한 TDD 사이클 적용

## 🔍 Phase 2 TDD 적용 가이드

**각 Step마다 다음을 확인하세요:**

### 🔴 RED 단계
- [ ] 테스트를 먼저 작성했는가?
- [ ] 테스트가 실패하는 것을 확인했는가?
- [ ] 실패 이유가 명확한가?

### 🟢 GREEN 단계
- [ ] 최소한의 코드로 구현했는가?
- [ ] 모든 테스트가 통과하는가?
- [ ] 프로덕션 코드를 수정했는가? (테스트 수정 ❌)

### 🔵 REFACTOR 단계
- [ ] 리팩토링 전에 테스트가 통과했는가?
- [ ] 리팩토링 후에도 테스트가 통과하는가?

### ✅ VERIFY 단계
- [ ] 전체 테스트가 통과하는가?
- [ ] 커버리지 >80% 달성했는가?
- [ ] Git commit 완료했는가?

---

## Step 6: SurveyResponse entity 구현

### 6.1 요구사항 정의
- [ ] 설문 응답을 저장하는 엔티티
- [ ] scanId (FK to MenuScan), abGroup, hasConfidence, createdAt

### 6.2 🔴 RED - 테스트 작성
- [ ] `SurveyResponseTest.java` 생성
- [ ] 객체 생성 테스트
- [ ] 필드 검증 테스트
- [ ] 테스트 실행 → 실패 확인

### 6.3 🟢 GREEN - 최소 구현
- [ ] `SurveyResponse.java` 생성
- [ ] JPA 어노테이션 추가
- [ ] 검증 로직 추가
- [ ] 테스트 실행 → 통과 확인

### 6.4 🔵 REFACTOR - 리팩토링
- [ ] Builder 패턴 적용
- [ ] Factory method 추가
- [ ] 테스트 실행 → 통과 확인

### 6.5 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] Git commit

---

## Step 7: SurveyResponseRepository 구현

### 7.1 요구사항 정의
- [ ] ABGroup별 개수 조회
- [ ] ABGroup + hasConfidence별 개수 조회

### 7.2 🔴 RED - 테스트 작성
- [ ] `SurveyResponseRepositoryTest.java` 생성
- [ ] save 테스트
- [ ] countByAbGroup 테스트
- [ ] countByAbGroupAndHasConfidence 테스트
- [ ] 테스트 실행 → 실패 확인

### 7.3 🟢 GREEN - 최소 구현
- [ ] `SurveyResponseRepository.java` 생성
- [ ] 쿼리 메서드 정의
- [ ] 테스트 실행 → 통과 확인

### 7.4 🔵 REFACTOR - 리팩토링
- [ ] 필요 시 커스텀 쿼리 추가
- [ ] 테스트 실행 → 통과 확인

### 7.5 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] Git commit

---

## Step 8: SurveyService 구현

### 8.1 요구사항 정의
- [ ] 설문 응답 저장 기능
- [ ] A/B 테스트 분석 기능 (그룹별 Yes 응답률)
- [ ] H3 가설 검증 로직 (ratio >= 2.0)

### 8.2 🔴 RED - 테스트 작성
- [ ] `SurveyServiceTest.java` 생성
- [ ] saveSurveyResponse 테스트
- [ ] getAnalytics 테스트
- [ ] 가설 검증 로직 테스트
- [ ] 예외 처리 테스트
- [ ] 테스트 실행 → 실패 확인

### 8.3 🟢 GREEN - 최소 구현
- [ ] `SurveyAnalytics.java` DTO 생성
- [ ] `GroupAnalytics.java` DTO 생성
- [ ] `SurveyService.java` 생성
- [ ] saveSurveyResponse 구현
- [ ] getAnalytics 구현
- [ ] 테스트 실행 → 통과 확인

### 8.4 🔵 REFACTOR - 리팩토링
- [ ] 분석 로직 메서드 분리
- [ ] 가설 검증 로직 명확화
- [ ] 테스트 실행 → 통과 확인

### 8.5 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] 테스트 커버리지 >80%
- [ ] Git commit

---

## Step 9: SurveyController 구현

### 9.1 요구사항 정의
- [ ] POST /api/surveys - 설문 응답 제출
- [ ] GET /api/surveys/analytics - 분석 결과 조회

### 9.2 🔴 RED - 테스트 작성
- [ ] `SurveyControllerTest.java` 생성
- [ ] POST /api/surveys 테스트
- [ ] GET /api/surveys/analytics 테스트
- [ ] 입력 검증 테스트
- [ ] 테스트 실행 → 실패 확인

### 9.3 🟢 GREEN - 최소 구현
- [ ] `SurveyRequest.java` DTO 생성
- [ ] `SurveyController.java` 생성
- [ ] POST, GET 엔드포인트 구현
- [ ] 테스트 실행 → 통과 확인

### 9.4 🔵 REFACTOR - 리팩토링
- [ ] @Valid 검증 추가
- [ ] 에러 핸들링 개선
- [ ] 테스트 실행 → 통과 확인

### 9.5 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] Postman 수동 테스트
- [ ] Git commit

---

## ✅ Phase 2 완료 체크리스트

### 기능 완료
- [ ] SurveyResponse entity 구현 및 테스트 (Step 6)
- [ ] SurveyResponseRepository 구현 및 테스트 (Step 7)
- [ ] SurveyService 구현 및 테스트 (Step 8)
- [ ] SurveyController 구현 및 테스트 (Step 9)

### 품질 검증
- [ ] 전체 테스트 실행 → 통과
- [ ] 테스트 커버리지 >80%
- [ ] 각 레이어별 커버리지 확인

### Git 관리
- [ ] 각 Step마다 commit 완료 (총 4개 commit)
- [ ] Git branch merge

### 🔍 Phase 2 TDD 회고
- [ ] 모든 Step에서 TDD 사이클을 따랐는가?
- [ ] H3 가설 검증 로직(ratio >= 2.0)에 대한 테스트가 있는가?
- [ ] 설문 응답 중복 방지 로직을 테스트했는가?
- [ ] Phase 1에서 배운 개선점을 적용했는가?

---

# Phase 3: MenuScan API 구현

## 🔍 Phase 3 TDD 적용 가이드

**Phase 1-2와 동일한 TDD 사이클 적용**

각 Step마다 🔴 RED → 🟢 GREEN → 🔵 REFACTOR → ✅ VERIFY 사이클을 준수하세요.

**Phase 3 특별 고려사항:**
- [ ] Control vs Treatment 조건부 로직을 테스트하는가?
- [ ] A/B 그룹 배정이 올바르게 동작하는가?
- [ ] 기존 MenuService와의 통합이 잘 되는가?
- [ ] 처리 시간 측정 로직이 정확한가?

---

## Step 9.5: 공통 인프라 구현 (GlobalExceptionHandler, ErrorResponse)

### 9.5.1 요구사항 정의
- [ ] GlobalExceptionHandler: 모든 컨트롤러의 예외를 중앙 집중 처리
- [ ] ErrorResponse DTO: 일관된 에러 응답 형식
- [ ] Custom Exception: EntityNotFoundException 등

### 9.5.2 🔴 RED - 테스트 작성
- [ ] `GlobalExceptionHandlerTest.java` 생성
- [ ] Validation 예외 처리 테스트 (MethodArgumentNotValidException)
- [ ] IllegalArgumentException 처리 테스트
- [ ] EntityNotFoundException 처리 테스트
- [ ] 일반 예외 처리 테스트
- [ ] 테스트 실행 → 실패 확인

### 9.5.3 🟢 GREEN - 최소 구현
- [ ] `ErrorResponse.java` record 생성
- [ ] `EntityNotFoundException.java` 생성
- [ ] `GlobalExceptionHandler.java` 생성
- [ ] @RestControllerAdvice 구현
- [ ] 테스트 실행 → 통과 확인

### 9.5.4 🔵 REFACTOR - 리팩토링
- [ ] HTTP Status Code 매핑 확인
- [ ] 에러 메시지 명확성 개선
- [ ] 테스트 실행 → 통과 확인

### 9.5.5 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] Git commit

**참고**:
- ARCHITECTURE.md 섹션 "예외 처리 전략"에 상세 설계 참조
- 모든 컨트롤러에서 공통으로 사용되는 인프라이므로 Phase 3 시작 전에 구현

---

## Step 10: MenuScanRequest/Response DTO 구현

### 10.1 요구사항 정의
- [ ] MenuScanRequest: base64EncodedImage, 언어/화폐 정보
- [ ] MenuScanResponse: scanId, abGroup, items[], processingTime

### 10.2 🔴 RED - 테스트 작성
- [ ] `MenuScanRequestTest.java` 생성
- [ ] 검증 로직 테스트
- [ ] null 체크 테스트
- [ ] 테스트 실행 → 실패 확인

### 10.3 🟢 GREEN - 최소 구현
- [ ] `MenuScanRequest.java` record 생성
- [ ] `MenuScanResponse.java` record 생성
- [ ] @NotNull 검증 추가
- [ ] 테스트 실행 → 통과 확인

### 10.4 🔵 REFACTOR - 리팩토링
- [ ] Compact constructor로 검증 추가
- [ ] JavaDoc 추가
- [ ] 테스트 실행 → 통과 확인

### 10.5 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] Git commit

---

## Step 11: MenuScanController 구현

### 11.1 요구사항 정의
- [ ] POST /api/menus/scan 엔드포인트
- [ ] A/B 그룹 배정
- [ ] MenuService 재사용
- [ ] Control vs Treatment 조건부 처리

### 11.2 🔴 RED - 테스트 작성
- [ ] `MenuScanControllerTest.java` 생성
- [ ] Control 그룹 테스트 (FoodInfo 제거)
- [ ] Treatment 그룹 테스트 (FoodInfo 포함)
- [ ] MenuScan 레코드 생성 테스트
- [ ] 처리 시간 측정 테스트
- [ ] 테스트 실행 → 실패 확인

### 11.3 🟢 GREEN - 최소 구현
- [ ] `MenuScanController.java` 생성
- [ ] POST /api/menus/scan 구현
- [ ] A/B 그룹 배정 로직 추가
- [ ] 조건부 응답 처리
- [ ] 테스트 실행 → 통과 확인

### 11.4 🔵 REFACTOR - 리팩토링
- [ ] 조건부 로직 메서드 분리
- [ ] 로깅 추가
- [ ] 처리 시간 측정 개선
- [ ] 테스트 실행 → 통과 확인

### 11.5 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] Postman 수동 테스트 (Control)
- [ ] Postman 수동 테스트 (Treatment)
- [ ] Git commit

---

## Step 12: 통합 테스트

### 12.1 E2E 테스트 시나리오
- [ ] 메뉴 스캔 → A/B 그룹 배정 → 응답 확인
- [ ] Control 그룹 플로우
- [ ] Treatment 그룹 플로우
- [ ] 설문 제출 플로우

### 12.2 🔴 RED - 통합 테스트 작성
- [ ] `MenuScanIntegrationTest.java` 생성
- [ ] 전체 플로우 테스트
- [ ] 테스트 실행 → 실패 확인

### 12.3 🟢 GREEN - 구현 완료
- [ ] 통합 테스트 통과
- [ ] 모든 모듈 연동 확인

### 12.4 ✅ VERIFY - 검증
- [ ] 전체 테스트 실행
- [ ] E2E Postman Collection 작성
- [ ] Git commit

---

## ✅ Phase 3 완료 체크리스트

### 기능 완료
- [ ] MenuScanRequest/Response DTO 구현 (Step 10)
- [ ] MenuScanController 구현 (Step 11)
- [ ] 통합 테스트 (Step 12)

### 품질 검증
- [ ] 전체 테스트 실행 → 통과
- [ ] E2E 수동 테스트 완료
- [ ] Control 그룹 시나리오 테스트
- [ ] Treatment 그룹 시나리오 테스트

### Git 관리
- [ ] 각 Step마다 commit 완료
- [ ] Git branch merge

### 🔍 Phase 3 TDD 회고
- [ ] 통합 테스트도 TDD 방식으로 작성했는가?
- [ ] A/B 테스트 로직이 올바르게 동작하는가?
- [ ] 기존 코드(MenuService)와의 통합이 매끄러운가?
- [ ] 전체 Phase 1-3의 TDD 원칙 준수도는?

---

# Phase 4: 기술 검증 (H2)

## Step 13: 내부 기술 검증 테스트

### 13.1 테스트 데이터 준비
- [ ] 메뉴판 이미지 100개 수집
- [ ] Ground Truth 라벨링
- [ ] CSV 파일 작성

### 13.2 자동화 검증 스크립트
- [ ] OCR 정확도 테스트 (>= 90%)
- [ ] 환율 정확도 테스트 (>= 95%)
- [ ] 음식 매칭 연관성 테스트 (>= 70%)
- [ ] 처리 시간 테스트 (<= 5초)

### 13.3 검증 실행
- [ ] 테스트 실행
- [ ] 결과 분석
- [ ] 검증 리포트 작성

### 13.4 성공 기준 확인
- [ ] H2 검증 통과 여부 결정
- [ ] 실패 시 R&D 피봇 계획

---

## 🎯 전체 완료 체크리스트

### Phase 1: ABTest 모듈
- [ ] Step 1-5 모두 완료
- [ ] 전체 테스트 통과
- [ ] Git merge 완료

### Phase 2: Survey 모듈
- [ ] Step 6-9 모두 완료
- [ ] 전체 테스트 통과
- [ ] Git merge 완료

### Phase 3: MenuScan API
- [ ] Step 10-12 모두 완료
- [ ] 통합 테스트 통과
- [ ] Git merge 완료

### Phase 4: 기술 검증
- [ ] Step 13 완료
- [ ] H2 가설 검증 완료

---

## 📊 품질 지표

### 테스트 커버리지
- [ ] Domain Layer: >90%
- [ ] Service Layer: >85%
- [ ] Controller Layer: >80%
- [ ] 전체: >80%

### 코드 품질
- [ ] 모든 테스트 통과
- [ ] Checkstyle 위반 없음
- [ ] SonarQube 분석 통과

---

## 🚀 Quick Commands

```bash
# 특정 테스트 실행
./gradlew test --tests ABGroupTest

# 전체 테스트 실행
./gradlew test

# 커버리지 리포트
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html

# 특정 모듈만 테스트
./gradlew test --tests "foodiepass.server.abtest.*"

# 통합 테스트만 실행
./gradlew test --tests "*IntegrationTest"
```bash

---

## 📝 Notes

- 각 Step은 독립적으로 완료 가능
- TDD 사이클을 엄격히 준수
- 테스트 먼저, 구현은 나중
- 리팩토링은 테스트 통과 후에만
- 각 Step 완료 후 Git commit

---

## 🎓 TDD 베스트 프랙티스 요약

### ✅ DO (반드시 해야 할 것)

1. **🔴 RED 단계**
   - ✅ 프로덕션 코드 작성 전에 테스트를 먼저 작성
   - ✅ 테스트가 실패하는 것을 확인
   - ✅ 실패 이유가 명확한지 확인
   - ✅ 모든 요구사항을 테스트로 표현

2. **🟢 GREEN 단계**
   - ✅ 테스트를 통과시키기 위한 최소한의 코드만 작성
   - ✅ 빠르게 GREEN 상태로 전환
   - ✅ 프로덕션 코드를 수정 (테스트 수정 ❌)

3. **🔵 REFACTOR 단계**
   - ✅ GREEN 상태에서만 리팩토링 시작
   - ✅ 리팩토링 후 테스트가 여전히 통과하는지 확인
   - ✅ 중복 제거, 가독성 개선
   - ✅ 작은 단위로 자주 리팩토링

4. **✅ VERIFY 단계**
   - ✅ 전체 테스트 스위트 실행
   - ✅ 커버리지 목표 달성 확인
   - ✅ 의미 있는 commit 메시지 작성

### ❌ DON'T (절대 하지 말아야 할 것)

1. **RED 단계에서**
   - ❌ 테스트 없이 프로덕션 코드 작성
   - ❌ 실패를 확인하지 않고 넘어가기
   - ❌ 모호한 테스트 작성

2. **GREEN 단계에서**
   - ❌ 테스트를 통과시키기 위해 테스트 수정
   - ❌ 필요 이상으로 복잡한 구현
   - ❌ 리팩토링과 구현을 동시에 진행

3. **REFACTOR 단계에서**
   - ❌ 테스트가 실패하는 상태에서 리팩토링
   - ❌ 기능 변경과 리팩토링을 동시에
   - ❌ 과도한 리팩토링으로 시간 낭비

4. **전체 프로세스에서**
   - ❌ TDD 사이클을 건너뛰기
   - ❌ 테스트 커버리지만을 위한 의미 없는 테스트 작성
   - ❌ 통합 테스트 없이 단위 테스트만 작성

### 🎯 TDD의 핵심 가치

1. **빠른 피드백**: 코드 변경 시 즉시 문제 발견
2. **안전한 리팩토링**: 테스트가 안전망 역할
3. **설계 개선**: 테스트하기 쉬운 코드 = 좋은 설계
4. **문서화**: 테스트가 코드의 사용법을 보여줌
5. **자신감**: 변경에 대한 두려움 제거

### 📊 품질 지표

#### 테스트 품질
- [ ] 모든 테스트가 독립적으로 실행 가능
- [ ] Given-When-Then 구조를 따름
- [ ] 의미 있는 @DisplayName 사용
- [ ] 빠른 실행 속도 (<1초/테스트)
- [ ] 안정적 (Flaky Test 없음)

#### 코드 품질
- [ ] 높은 응집도, 낮은 결합도
- [ ] SOLID 원칙 준수
- [ ] 명확한 책임 분리
- [ ] 읽기 쉬운 코드
- [ ] 불필요한 복잡성 제거

#### 프로세스 품질
- [ ] 모든 코드가 TDD 사이클을 거침
- [ ] 커버리지 >80% 유지
- [ ] 각 단계마다 commit
- [ ] 전체 테스트 항상 통과

---

## 🚨 TDD 위반 시 체크리스트

혹시 TDD 원칙을 위반했다면:

1. **테스트 없이 코드를 작성했는가?**
   - → 즉시 멈추고 해당 코드에 대한 테스트 작성
   - → 테스트가 실패하는지 확인
   - → 이미 작성한 코드로 테스트 통과시키기

2. **테스트를 통과시키기 위해 테스트를 수정했는가?**
   - → 테스트를 원래대로 되돌리기
   - → 프로덕션 코드를 수정하여 테스트 통과시키기

3. **리팩토링 중 테스트가 실패했는가?**
   - → 리팩토링 되돌리기
   - → GREEN 상태로 돌아가기
   - → 더 작은 단위로 리팩토링 다시 시작

4. **커버리지가 목표에 미달했는가?**
   - → 누락된 테스트 케이스 파악
   - → RED 단계부터 다시 시작
   - → 테스트 추가 후 커버리지 재확인

---

## 📚 참고 자료

### TDD 관련 추천 도서
- "Test Driven Development: By Example" - Kent Beck
- "Growing Object-Oriented Software, Guided by Tests" - Steve Freeman, Nat Pryce
- "Effective Unit Testing" - Lasse Koskela

### TDD 원칙
- **Three Laws of TDD** (Robert C. Martin):
  1. 실패하는 단위 테스트를 작성할 때까지 프로덕션 코드를 작성하지 않는다
  2. 컴파일은 실패하지 않으면서 실행이 실패하는 정도로만 단위 테스트를 작성한다
  3. 현재 실패하는 테스트를 통과할 정도로만 실제 코드를 작성한다

---

# 🚀 Phase 3: Execution Plan (Token-Efficient Sessions)

> **목적**: Phase 1-4의 구현을 토큰 효율적으로 5개 세션으로 나누어 진행
>
> 각 세션은 독립적으로 완료 가능하며, 세션 종료 시 커밋 체크포인트 생성

---

## Phase 3-1: 백엔드 도메인 & 서비스 레이어 (~2-3시간)

### 📋 구현 범위
- ABGroup enum
- MenuScan entity (도메인 모델)
- SurveyResponse entity
- MenuScanRepository
- SurveyResponseRepository
- ABTestService (그룹 배정, 결과 분석)
- SurveyService (응답 저장, 통계 조회)
- 단위 테스트 (>80% 커버리지)

### ✅ 완료 기준
- [ ] 모든 도메인 모델 구현 및 테스트
- [ ] Repository 계층 구현 및 @DataJpaTest 통과
- [ ] Service 계층 구현 및 @SpringBootTest 통과
- [ ] 테스트 커버리지 >80%
- [ ] `./gradlew test` 전체 통과
- [ ] Git commit: "feat: Implement ABTest and Survey domain & service layers"

### 🔑 핵심 비즈니스 로직
- A/B 그룹 배정: 신규 사용자 50:50 랜덤, 기존 사용자는 이전 그룹 유지
- 설문 분석: Control vs Treatment 그룹별 Yes 응답률 계산
- H3 가설 검증: Treatment / Control 비율 >= 2.0 확인

---

## Phase 3-2: 백엔드 API 레이어 (~2-3시간)

### 📋 구현 범위
- GlobalExceptionHandler (공통 예외 처리)
- ErrorResponse DTO
- MenuScanController (POST /api/menus/scan)
- SurveyController (POST /api/surveys)
- ABTestController (GET /api/admin/ab-test/results)
- Request/Response DTO
- 기존 MenuService 수정 (Treatment 그룹만 FoodInfo 포함)
- API 통합 테스트

### ✅ 완료 기준
- [ ] GlobalExceptionHandler 구현 및 테스트
- [ ] 모든 Controller 구현 및 @WebMvcTest 통과
- [ ] Request/Response DTO 검증 추가
- [ ] 기존 MenuService A/B 로직 통합
- [ ] API 통합 테스트 (@SpringBootTest) 통과
- [ ] Postman Collection 작성 및 수동 테스트
- [ ] API 문서 업데이트
- [ ] Git commit: "feat: Implement ABTest and Survey API controllers"

### 🔑 핵심 API 로직
- POST /api/menus/scan: A/B 그룹 배정 → MenuService 호출 → 조건부 응답
- Control 그룹: FoodInfo 제거
- Treatment 그룹: FoodInfo 포함

---

## Phase 3-3: 프론트엔드 기본 구조 (~2시간)

### 📋 구현 범위
- React/Next.js 프로젝트 초기화
- 프로젝트 구조 설정
- API 클라이언트 구현
- 공통 컴포넌트 (Button, Input, Layout)
- 라우팅 설정
- 환경 변수 설정

### ✅ 완료 기준
- [ ] Next.js 프로젝트 초기화 완료
- [ ] API 클라이언트 구현 (Axios/Fetch wrapper)
- [ ] 공통 컴포넌트 4개 구현
- [ ] 라우팅 설정 (/, /menu/[scanId], /survey/[scanId])
- [ ] 환경 변수 설정 (NEXT_PUBLIC_API_URL)
- [ ] `npm run dev` 로컬 실행 확인
- [ ] Git commit: "feat: Initialize frontend project structure"

---

## Phase 3-4: 프론트엔드 핵심 기능 (~3-4시간)

### 📋 구현 범위
- 메뉴 업로드 페이지 (사진 업로드, 언어/화폐 선택)
- Control UI (텍스트 번역만 표시)
- Treatment UI (사진 + 설명 + 환율 표시)
- A/B 그룹 기반 조건부 렌더링
- 설문 컴포넌트 (확신도 질문)
- 로딩/에러 상태 처리

### ✅ 완료 기준
- [ ] 업로드 페이지 구현 및 동작 확인
- [ ] Control UI 구현 (FoodInfo 제외)
- [ ] Treatment UI 구현 (FoodInfo 포함)
- [ ] 조건부 렌더링 로직 구현 (abGroup 기반)
- [ ] 설문 페이지 구현
- [ ] 로딩/에러 상태 처리
- [ ] E2E 수동 테스트 (Chrome DevTools)
- [ ] Git commit: "feat: Implement menu upload and A/B test UI"

### 🔑 핵심 UI 로직
- Control 그룹: 메뉴명(번역) + 가격(환율) 표시
- Treatment 그룹: 음식 사진 + 설명 + 메뉴명(번역) + 가격(환율) 표시
- 설문: "이 메뉴를 보고 주문할 확신이 생기셨나요?" (Yes/No)

---

## Phase 3-5: 통합 및 검증 (~2-3시간)

### 📋 구현 범위
- E2E 플로우 테스트 (Playwright 또는 수동)
- 성능 검증 (처리 시간 ≤ 5초)
- A/B 테스트 데이터 무결성 검증
- OCR/매칭 정확도 테스트 (샘플 메뉴 10개)
- 버그 수정 및 리팩토링
- 배포 준비 (환경 설정 문서화)

### 🧪 테스트 시나리오
1. **Control 그룹 플로우**
   - 메뉴 업로드 → Control 그룹 배정 → 텍스트 전용 UI → 설문 제출
2. **Treatment 그룹 플로우**
   - 메뉴 업로드 → Treatment 그룹 배정 → 사진 포함 UI → 설문 제출
3. **관리자 결과 조회**
   - GET /api/admin/ab-test/results 호출 → Control/Treatment 개수 확인

### ✅ 완료 기준
- [ ] E2E 테스트 10회 성공 (Control 5회, Treatment 5회)
- [ ] 처리 시간 평균 ≤ 5초 (10개 샘플 기준)
- [ ] A/B 그룹 배정 정확성 확인 (DB 데이터)
- [ ] OCR 정확도 확인 (수동 검증)
- [ ] 모든 버그 수정 완료
- [ ] 배포 환경 설정 문서 작성
- [ ] Git commit: "test: Add E2E tests and performance validation"
- [ ] PR 생성: `feature/phase3-mvp-implementation` → `develop`

### 🎯 H2 가설 검증 준비
- [ ] 기술 검증 스크립트 작성
- [ ] 샘플 데이터 10개 준비
- [ ] 정확도 측정 자동화

---

## 🔄 세션 전환 가이드

각 Phase 완료 후:
1. ✅ 모든 테스트 통과 확인
2. 💾 Git commit 생성
3. 📝 다음 Phase 시작 전 브리핑 확인
4. 🔄 필요 시 세션 재시작

세션 중단 시:
- 현재 Phase의 진행 상황 기록
- 미완료 작업 TodoWrite에 추가
- Git stash 또는 WIP commit
- 다음 세션 시 이어서 진행
