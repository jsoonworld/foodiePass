# FoodiePass Quality Standards

**Purpose**: Backend 코드 품질 기준 정의 (Exception, Optional, Logging, Performance)

**Context**: BPlusTree3 방법론과 통합된 실전 가이드

**Last Updated**: 2025-01-10 (Phase 1)

---

## 1. Exception Hierarchy Standards

### 1.1 Architecture Pattern

**기존 구조 (유지)**:
```
RuntimeException
    └── BaseException (abstract)
            ├── CurrencyException
            ├── LanguageException
            ├── FoodException
            ├── GeminiException
            ├── ScrapingException
            ├── PriceException
            ├── OrderException
            └── ScriptException
```

**ErrorCode Interface**:
```java
public interface ErrorCode {
    HttpStatus getStatus();
    String getMessage();

    default String getMessage(Object... args) {
        return String.format(this.getMessage(), args);
    }
}
```

### 1.2 Domain Exception Pattern

**✅ RIGHT**: 도메인별 ErrorCode Enum + Exception 클래스
```java
// 1. ErrorCode enum 정의
public enum ABTestErrorCode implements ErrorCode {
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "A/B test group not found"),
    INVALID_GROUP_ASSIGNMENT(HttpStatus.BAD_REQUEST, "Invalid group assignment: %s"),
    SCAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Menu scan not found: %s");

    private final HttpStatus status;
    private final String message;

    ABTestErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() { return status; }

    @Override
    public String getMessage() { return message; }
}

// 2. Exception 클래스 정의
public class ABTestException extends BaseException {
    public ABTestException(ABTestErrorCode errorCode) {
        super(errorCode);
    }

    public ABTestException(ABTestErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}

// 3. 사용 예시
public MenuScan getScanById(UUID scanId) {
    return menuScanRepository.findById(scanId)
        .orElseThrow(() -> new ABTestException(
            ABTestErrorCode.SCAN_NOT_FOUND,
            scanId
        ));
}
```

**❌ WRONG**: Generic Exception 던지기
```java
// Bad: 구체적이지 않은 예외
throw new RuntimeException("Scan not found");

// Bad: 도메인 컨텍스트 없음
throw new IllegalArgumentException("Invalid group");
```

### 1.3 Exception Throwing Guidelines

**원칙**: Fail Fast (Kent Beck)
- 입력 검증 실패 → 즉시 예외 발생
- 비즈니스 규칙 위반 → 즉시 예외 발생
- 외부 API 실패 → 적절한 도메인 예외로 변환

**✅ RIGHT**: 명확한 컨텍스트와 함께 예외 발생
```java
public void assignUserToGroup(String userId, ABGroup group) {
    if (userId == null || userId.isBlank()) {
        throw new ABTestException(
            ABTestErrorCode.INVALID_USER_ID,
            "User ID cannot be null or empty"
        );
    }

    if (group == null) {
        throw new ABTestException(
            ABTestErrorCode.INVALID_GROUP_ASSIGNMENT,
            "Group cannot be null"
        );
    }

    // 비즈니스 로직 진행...
}
```

**❌ WRONG**: Silent failure 또는 null 반환
```java
// Bad: 실패를 숨김
public MenuScan getScanById(UUID scanId) {
    return menuScanRepository.findById(scanId).orElse(null);
}

// Bad: 나중에 NullPointerException 발생 가능
MenuScan scan = service.getScanById(invalidId); // null 반환
scan.getAbGroup(); // NPE 발생!
```

### 1.4 Exception Handling in Controllers

**GlobalExceptionHandler가 처리**:
- `@RestControllerAdvice`로 중앙 집중식 예외 처리
- BaseException → ErrorResponse 변환
- 로깅 + HTTP 상태 코드 매핑

**Controller는 예외를 catch하지 않음** (propagate만 수행):
```java
@PostMapping("/scan")
public Mono<MenuScanResponse> scanMenu(
    @RequestBody @Valid MenuScanRequest request,
    @RequestHeader("User-Id") String userId
) {
    // ✅ 예외 발생 시 GlobalExceptionHandler가 처리
    return menuScanService.scanMenu(request, userId);
}
```

---

## 2. Optional Usage Standards

### 2.1 When to Use Optional

**사용 권장 케이스**:
1. **Repository 조회 결과**: 존재하지 않을 수 있는 엔티티
2. **설정값/매개변수**: 선택적 설정값
3. **메서드 반환값**: null을 반환할 수 있는 경우

**✅ RIGHT**: Repository에서 Optional 반환
```java
public interface MenuScanRepository extends JpaRepository<MenuScan, UUID> {
    Optional<MenuScan> findById(UUID id);
    Optional<MenuScan> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime after);
}

// 사용
MenuScan scan = repository.findById(scanId)
    .orElseThrow(() -> new ABTestException(ABTestErrorCode.SCAN_NOT_FOUND, scanId));
```

**❌ WRONG**: Optional을 필드로 사용
```java
// Bad: Optional은 반환 타입으로만 사용
public class MenuScan {
    private Optional<String> description; // ❌

    // ✅ 대신 null 허용 필드 사용
    private String description; // null 가능
}
```

### 2.2 Optional 체이닝 패턴

**✅ RIGHT**: 함수형 스타일로 체이닝
```java
// 예시 1: 기본값 제공
String userName = userRepository.findById(userId)
    .map(User::getName)
    .orElse("Anonymous");

// 예시 2: 예외 발생
MenuScan scan = menuScanRepository.findById(scanId)
    .orElseThrow(() -> new ABTestException(ABTestErrorCode.SCAN_NOT_FOUND, scanId));

// 예시 3: 조건부 실행
repository.findById(id).ifPresent(scan -> {
    log.info("Found scan: {}", scan.getId());
    // 추가 처리...
});
```

**❌ WRONG**: isPresent() + get() 조합
```java
// Bad: isPresent() 체크 후 get() 호출
Optional<MenuScan> optional = repository.findById(scanId);
if (optional.isPresent()) {
    MenuScan scan = optional.get(); // ❌
    // ...
}

// ✅ 대신 orElseThrow() 또는 ifPresent() 사용
MenuScan scan = repository.findById(scanId)
    .orElseThrow(() -> new ABTestException(ABTestErrorCode.SCAN_NOT_FOUND, scanId));
```

### 2.3 Optional vs Null

**원칙**:
- **공개 API (public method)**: Optional 반환 (명시적 의도 표현)
- **내부 메서드 (private method)**: null 허용 가능 (성능 고려)
- **컬렉션**: 빈 컬렉션 반환 (Optional 불필요)

**✅ RIGHT**:
```java
// 공개 API: Optional 반환
public Optional<MenuScan> findRecentScan(String userId) {
    return menuScanRepository.findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .findFirst();
}

// 컬렉션: 빈 리스트 반환
public List<MenuScan> findAllScans(String userId) {
    return menuScanRepository.findByUserId(userId); // 빈 리스트 반환 가능
}
```

**❌ WRONG**:
```java
// Bad: 컬렉션을 Optional로 감싸기
public Optional<List<MenuScan>> findAllScans(String userId) { // ❌
    return Optional.of(menuScanRepository.findByUserId(userId));
}

// ✅ 대신 빈 컬렉션 반환
public List<MenuScan> findAllScans(String userId) {
    return menuScanRepository.findByUserId(userId); // 빈 리스트 OK
}
```

---

## 3. Logging Standards (SLF4J)

### 3.1 Logger 선언 패턴

**✅ RIGHT**: Lombok `@Slf4j` 사용
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuScanService {

    public Mono<MenuScanResponse> scanMenu(MenuScanRequest request, String userId) {
        log.info("Starting menu scan for user: {}", userId);
        // ...
    }
}
```

**❌ WRONG**: 직접 LoggerFactory 선언 (Lombok 사용 시)
```java
public class MenuScanService {
    private static final Logger log = LoggerFactory.getLogger(MenuScanService.class); // ❌

    // ✅ 대신 @Slf4j 사용
}
```

### 3.2 Log Level Guidelines

| Level | 목적 | 사용 케이스 | 예시 |
|-------|------|------------|------|
| **ERROR** | 시스템 장애, 복구 불가능 | 처리되지 않은 예외, 외부 API 실패 | `log.error(">> 처리되지 않은 예외: {}", e.getMessage(), e)` |
| **WARN** | 예상된 문제, 복구 가능 | 비즈니스 예외, 입력 검증 실패, 파싱 실패 | `log.warn(">> 비즈니스 예외 발생: {}", e.getMessage())` |
| **INFO** | 주요 비즈니스 이벤트 | 사용자 요청, 처리 완료, 상태 변경 | `log.info("User {} assigned to group: {}", userId, group)` |
| **DEBUG** | 디버깅 정보 | 내부 상태, 중간 결과 | `log.debug("Parsing price: {}", rawPrice)` |
| **TRACE** | 상세 디버깅 정보 | 메서드 호출 흐름, 변수 값 | `log.trace("Entering method: scanMenu()")` |

### 3.3 Logging Patterns

**✅ RIGHT**: 구조화된 로그 메시지
```java
// 1. 비즈니스 이벤트 로깅
log.info("User {} assigned to A/B group: {}", userId, abGroup);

// 2. 처리 완료 로깅 (성능 메트릭 포함)
log.info("Menu scan completed for user {} in {}s with {} items",
    userId, processingTime, itemCount);

// 3. 예외 로깅 (스택 트레이스 포함)
log.error(">> 외부 API 호출 실패: {}", e.getMessage(), e);

// 4. 경고 로깅 (복구 가능한 문제)
log.warn("Failed to parse amount from: {}", formattedPrice);
```

**❌ WRONG**: 비구조화된 로그 메시지
```java
// Bad: 문자열 연결 (성능 저하)
log.info("User " + userId + " assigned to group: " + group); // ❌

// Bad: 컨텍스트 부족
log.info("Processing completed"); // ❌

// Bad: 예외 메시지만 로깅 (스택 트레이스 없음)
log.error("Error: " + e.getMessage()); // ❌

// ✅ 대신
log.info("User {} assigned to group: {}", userId, group);
log.info("Menu scan completed for user {} in {}s", userId, processingTime);
log.error(">> 외부 API 호출 실패: {}", e.getMessage(), e);
```

### 3.4 Performance Logging

**원칙**: 주요 비즈니스 작업의 처리 시간 로깅

**✅ RIGHT**: 시작/종료 시간 기록 및 로깅
```java
public Mono<MenuScanResponse> scanMenu(MenuScanRequest request, String userId) {
    Instant startTime = Instant.now();

    return menuService.reconfigure(request)
        .map(response -> {
            double processingTime = Duration.between(startTime, Instant.now()).toMillis() / 1000.0;

            log.info("Menu scan completed for user {} in {}s with {} items",
                userId, processingTime, response.size());

            return new MenuScanResponse(/* ... */, processingTime);
        });
}
```

**성능 임계값 경고**:
```java
private static final double PERFORMANCE_THRESHOLD = 5.0; // 5초

double processingTime = /* ... */;

if (processingTime > PERFORMANCE_THRESHOLD) {
    log.warn("⚠️ Performance threshold exceeded: {}s (threshold: {}s)",
        processingTime, PERFORMANCE_THRESHOLD);
}
```

### 3.5 Logging Anti-Patterns

**❌ 피해야 할 패턴**:
```java
// 1. 민감한 정보 로깅
log.info("User password: {}", password); // ❌

// 2. 과도한 로깅 (반복문 내부)
for (MenuItem item : items) {
    log.info("Processing item: {}", item); // ❌
}

// 3. 로그 레벨 오용
log.error("User logged in successfully"); // ❌ (info가 적절)
log.info("Database connection failed", e); // ❌ (error가 적절)

// 4. 예외 스택 없이 에러 로깅
catch (Exception e) {
    log.error("Error occurred: {}", e.getMessage()); // ❌ 스택 트레이스 누락
}
```

**✅ 올바른 패턴**:
```java
// 1. 민감한 정보 마스킹
log.info("User email: {}", maskEmail(email));

// 2. 요약 로깅
log.info("Processed {} items in {}s", items.size(), processingTime);

// 3. 적절한 로그 레벨
log.info("User logged in successfully: {}", userId);
log.error("Database connection failed", e);

// 4. 예외 스택 포함
catch (Exception e) {
    log.error("Error occurred: {}", e.getMessage(), e); // ✅ 스택 트레이스 포함
}
```

---

## 4. Performance Validation Standards

### 4.1 Performance Requirements (H2)

**목표**: 기술 실현 가설 검증
- **전체 처리 시간**: ≤ 5초 (OCR + 번역 + 매칭 + 환율)
- **OCR 정확도**: ≥ 90%
- **환율 정확도**: ≥ 95%
- **음식 매칭 연관성**: ≥ 70%

### 4.2 Automated Performance Testing

**테스트 패턴**:
```java
@Test
@DisplayName("H2: 전체 처리 시간은 5초 이내여야 한다")
void testPerformanceThreshold() {
    // Given
    MenuScanRequest request = createValidRequest();
    Instant startTime = Instant.now();

    // When
    MenuScanResponse response = menuScanService.scanMenu(request, "test-user")
        .block();

    // Then
    double processingTime = Duration.between(startTime, Instant.now()).toMillis() / 1000.0;

    assertThat(processingTime).isLessThanOrEqualTo(5.0);
    assertThat(response.processingTime()).isLessThanOrEqualTo(5.0);

    log.info("Performance test passed: {}s (threshold: 5.0s)", processingTime);
}
```

### 4.3 Performance Monitoring

**실시간 모니터링**:
```java
@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    private static final double THRESHOLD = 5.0;

    @Around("@annotation(MonitorPerformance)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            double duration = Duration.between(start, Instant.now()).toMillis() / 1000.0;

            if (duration > THRESHOLD) {
                log.warn("⚠️ Performance threshold exceeded: {} took {}s (threshold: {}s)",
                    joinPoint.getSignature().toShortString(), duration, THRESHOLD);
            } else {
                log.info("✅ Performance OK: {} took {}s",
                    joinPoint.getSignature().toShortString(), duration);
            }

            return result;
        } catch (Throwable e) {
            log.error("❌ Performance monitoring failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}

// 사용
@MonitorPerformance
public Mono<MenuScanResponse> scanMenu(MenuScanRequest request, String userId) {
    // ...
}
```

### 4.4 Performance Metrics Collection

**메트릭 수집 패턴**:
```java
@Service
@RequiredArgsConstructor
public class PerformanceMetricsService {

    private final MeterRegistry meterRegistry;

    public void recordProcessingTime(String operation, double durationSeconds) {
        Timer.builder("foodiepass.processing.time")
            .tag("operation", operation)
            .register(meterRegistry)
            .record(Duration.ofMillis((long) (durationSeconds * 1000)));
    }

    public void recordThresholdViolation(String operation, double durationSeconds) {
        Counter.builder("foodiepass.performance.threshold.violations")
            .tag("operation", operation)
            .register(meterRegistry)
            .increment();

        log.warn("⚠️ Performance threshold violated: {} took {}s", operation, durationSeconds);
    }
}
```

---

## 5. Integration with BPlusTree3

### 5.1 Pre-Commit Checklist 연계

**Before Every Commit**:
```bash
# 1. 모든 테스트 통과 (성능 테스트 포함)
./gradlew test

# 2. 로그 레벨 확인
grep -r "log.debug\|log.trace" src/main/java  # 프로덕션 코드에 debug/trace 로그 없어야 함

# 3. Exception 처리 확인
grep -r "catch.*Exception.*\{\s*\}" src/  # Empty catch 블록 없어야 함

# 4. 성능 임계값 검증
grep -r "processingTime" src/test/  # 성능 테스트 존재 확인
```

### 5.2 TDD Cycle with Quality Standards

**Red-Green-Refactor-Verify**:
1. **Red**: 실패하는 테스트 작성 (성능 테스트 포함)
   ```java
   @Test
   void testPerformanceThreshold() {
       // Given: 유효한 요청
       // When: 처리
       // Then: ≤ 5초
   }
   ```

2. **Green**: 테스트 통과하는 최소 구현
   ```java
   public Mono<MenuScanResponse> scanMenu(...) {
       Instant startTime = Instant.now();
       // 구현...
       double time = Duration.between(startTime, Instant.now()).toMillis() / 1000.0;
       return response;
   }
   ```

3. **Refactor**: 코드 품질 개선
   - Exception 계층 확인
   - Optional 사용 리팩토링
   - 로깅 추가

4. **Verify**: 품질 표준 준수 확인
   - 성능 임계값 확인
   - 예외 처리 완전성 확인
   - 로그 레벨 적절성 확인

---

## 6. Summary: Quality Gates

### Pre-Commit Quality Gates

**MUST PASS (모든 커밋에서 필수)**:
- ✅ 모든 테스트 통과 (단위 + 통합 + 성능)
- ✅ 컴파일 경고 제로
- ✅ 모든 Exception이 BaseException 계층 내
- ✅ 공개 API는 Optional 반환 (null 대신)
- ✅ 성능 임계값 준수 (≤ 5초)
- ✅ 로그 레벨 적절성
- ✅ 예외 스택 트레이스 포함 (error 레벨)

### Code Review Checklist

**Exception Handling**:
- [ ] 도메인 Exception이 BaseException을 extends하는가?
- [ ] ErrorCode enum이 정의되어 있는가?
- [ ] 예외 메시지에 충분한 컨텍스트가 포함되어 있는가?

**Optional Usage**:
- [ ] Repository 반환값이 Optional인가?
- [ ] isPresent() + get() 패턴을 피했는가?
- [ ] 컬렉션은 빈 컬렉션을 반환하는가?

**Logging**:
- [ ] @Slf4j 어노테이션을 사용하는가?
- [ ] 로그 레벨이 적절한가?
- [ ] 예외 로깅 시 스택 트레이스를 포함하는가?
- [ ] 민감한 정보를 로깅하지 않는가?

**Performance**:
- [ ] 성능 테스트가 존재하는가?
- [ ] 처리 시간이 5초 이내인가?
- [ ] 성능 메트릭을 로깅하는가?

---

## Appendix: Quick Reference

### Exception Pattern Template
```java
// 1. ErrorCode enum
public enum DomainErrorCode implements ErrorCode {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found: %s");
    // ...
}

// 2. Exception class
public class DomainException extends BaseException {
    public DomainException(DomainErrorCode errorCode) { super(errorCode); }
    public DomainException(DomainErrorCode errorCode, Object... args) { super(errorCode, args); }
}

// 3. Usage
throw new DomainException(DomainErrorCode.RESOURCE_NOT_FOUND, resourceId);
```

### Optional Pattern Template
```java
// Repository
Optional<Entity> findById(UUID id);

// Service
Entity entity = repository.findById(id)
    .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, id));
```

### Logging Pattern Template
```java
@Slf4j
public class Service {
    public void operation() {
        log.info("Starting operation for user: {}", userId);
        try {
            // ...
            log.info("Operation completed in {}s", duration);
        } catch (Exception e) {
            log.error("Operation failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### Performance Test Template
```java
@Test
void testPerformance() {
    Instant start = Instant.now();

    service.operation();

    double duration = Duration.between(start, Instant.now()).toMillis() / 1000.0;
    assertThat(duration).isLessThanOrEqualTo(5.0);
}
```
