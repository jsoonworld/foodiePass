# FoodiePass Backend - Code Reuse Guide

> **목적**: 기존 v1 코드를 v2 MVP에서 재사용하는 방법 가이드

---

## v1 → v2 전환 전략

### 핵심 원칙
1. **재사용 우선**: 기존 코드 최대한 활용
2. **최소 수정**: 동작하는 코드는 건드리지 않음
3. **계층적 추가**: 새 기능은 새 모듈로 분리

---

## 재사용 가능한 모듈 (✅ 그대로 사용)

### 1. Menu 모듈 (`menu/`)

#### MenuService
**위치**: `foodiepass.server.menu.application.MenuService`

**역할**: 메뉴 파이프라인 조율 (OCR → 번역 → enrichment)

**기존 메서드**:
```java
public ReconfigureResponse reconfigure(ReconfigureRequest request) {
    // OCR → 번역 → 환율 → 음식 매칭 파이프라인
}
```

**v2 활용 방법**:
- ✅ 기존 `reconfigure` 메서드 그대로 사용
- ➕ 새 메서드 `scanMenu` 추가 (A/B 로직 포함)

**새 메서드 추가 예시**:
```java
@Service
public class MenuService {
    // 기존 메서드 유지
    public ReconfigureResponse reconfigure(ReconfigureRequest request) {
        // 기존 로직 유지
    }

    // 새 메서드 추가
    public MenuScanResponse scanMenu(MenuScanRequest request, String sessionId) {
        // 1. A/B 그룹 배정
        ABGroup group = abTestService.getOrAssignGroup(sessionId);

        // 2. 기존 reconfigure 호출
        ReconfigureResponse response = reconfigure(toReconfigureRequest(request));

        // 3. Treatment 그룹만 음식 매칭
        if (group == ABGroup.TREATMENT) {
            // 이미 reconfigure에서 처리됨
        } else {
            // Control 그룹은 FoodInfo 제거
            response.getItems().forEach(item -> {
                item.setFoodImageUrl(null);
                item.setFoodDescription(null);
                item.setMatchConfidence(null);
            });
        }

        // 4. MenuScan 저장
        MenuScan scan = createMenuScan(request, group, response);
        menuScanRepository.save(scan);

        return toMenuScanResponse(scan, response);
    }
}
```

---

#### MenuItemEnricher
**위치**: `foodiepass.server.menu.application.MenuItemEnricher`

**역할**: 메뉴 아이템 enrichment (번역 + 음식 스크래핑 + 환율 변환)

**재사용**: ✅ 그대로 사용 (수정 불필요)

---

#### Ports (Interfaces)

**OcrReader** (`menu.application.port.out.OcrReader`)
```java
public interface OcrReader {
    Mono<List<MenuItem>> readMenuItems(byte[] imageBytes, Language targetLanguage);
}
```
**구현체**: `GeminiOcrReader` (재사용)

---

**FoodScrapper** (`menu.application.port.out.FoodScrapper`)
```java
public interface FoodScrapper {
    Mono<FoodInfo> scrap(String foodName);
}
```
**구현체**: `TasteAtlasFoodScrapper` (재사용)

---

**TranslationClient** (`menu.application.port.out.TranslationClient`)
```java
public interface TranslationClient {
    Mono<String> translate(String text, Language source, Language target);
}
```
**구현체**: `GoogleTranslationClient` (재사용)

---

### 2. Language 모듈 (`language/`)

#### LanguageService
**위치**: `foodiepass.server.language.application.LanguageService`

**역할**: 지원 언어 목록 제공

**재사용**: ✅ 그대로 사용

```java
@Service
public class LanguageService {
    public List<Language> getSupportedLanguages() {
        return Arrays.asList(Language.values());
    }
}
```

---

#### Language (Enum)
**위치**: `foodiepass.server.language.domain.Language`

**재사용**: ✅ 그대로 사용

```java
public enum Language {
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    // ...
}
```

---

### 3. Currency 모듈 (`currency/`)

#### CurrencyService
**위치**: `foodiepass.server.currency.application.CurrencyService`

**역할**: 환율 변환 및 포맷팅

**재사용**: ✅ 그대로 사용
**개선**: ➕ Redis 캐싱 추가 권장

```java
@Service
public class CurrencyService {
    private final ExchangeRateProvider rateProvider;
    private final ExchangeRateCache cache; // ➕ 추가

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        // 캐시 확인
        BigDecimal rate = cache.getRate(from, to);
        if (rate == null) {
            rate = rateProvider.getExchangeRate(from, to);
            cache.putRate(from, to, rate, Duration.ofHours(24));
        }
        return amount.multiply(rate);
    }
}
```

---

#### Currency (Enum)
**위치**: `foodiepass.server.currency.domain.Currency`

**재사용**: ✅ 그대로 사용

```java
public enum Currency {
    KRW("₩", "원"),
    USD("$", "dollar"),
    JPY("¥", "円"),
    // ...
}
```

---

### 4. Infrastructure 레이어 (구현체들)

#### GeminiOcrReader
**위치**: `foodiepass.server.menu.infra.GeminiOcrReader`

**역할**: Google Vertex AI OCR 연동

**재사용**: ✅ 그대로 사용

---

#### GoogleTranslationClient
**위치**: `foodiepass.server.language.infra.GoogleTranslationClient`

**역할**: Google Cloud Translation API 연동

**재사용**: ✅ 그대로 사용

---

#### TasteAtlasFoodScrapper
**위치**: `foodiepass.server.menu.infra.TasteAtlasFoodScrapper`

**역할**: TasteAtlas 음식 정보 스크래핑

**재사용**: ✅ 그대로 사용

---

#### GoogleFinanceRateProvider
**위치**: `foodiepass.server.currency.infra.GoogleFinanceRateProvider`

**역할**: 환율 조회

**재사용**: ✅ 그대로 사용

---

## MVP 제외 모듈 (⚠️ 사용하지 않음)

### Order 모듈 (`order/`)
**이유**: 장바구니 기능은 MVP 범위 밖

**상태**: ⚠️ 유지는 하되, v2 MVP에서는 사용하지 않음

---

### Script 모듈 (`script/`)
**이유**: 주문서 생성은 MVP 범위 밖

**상태**: ⚠️ 유지는 하되, v2 MVP에서는 사용하지 않음

---

## 새로 개발해야 하는 모듈 (➕ NEW)

### 1. ABTest 모듈 (`abtest/`)

**목적**: A/B 그룹 배정 및 관리

**새로 작성할 파일**:
```
abtest/
├── domain/
│   ├── ABGroup.java             # enum (CONTROL, TREATMENT)
│   └── MenuScan.java            # 메뉴 스캔 엔티티
├── application/
│   └── ABTestService.java       # A/B 로직
├── api/
│   └── ABTestController.java    # 관리자 API
└── repository/
    └── MenuScanRepository.java  # JPA Repository
```

**구현 가이드**:
```java
// ABTestService.java
@Service
public class ABTestService {
    private final RedisTemplate<String, String> redis;
    private final Random random = new Random();

    public ABGroup getOrAssignGroup(String sessionId) {
        // Redis에서 조회
        String cached = redis.opsForValue().get("ab:group:" + sessionId);
        if (cached != null) {
            return ABGroup.valueOf(cached);
        }

        // 랜덤 배정 (50:50)
        ABGroup group = random.nextBoolean() ? ABGroup.CONTROL : ABGroup.TREATMENT;

        // Redis에 캐싱 (30분)
        redis.opsForValue().set("ab:group:" + sessionId, group.name(),
            Duration.ofMinutes(30));

        return group;
    }
}
```

---

### 2. Survey 모듈 (`survey/`)

**목적**: 설문 응답 수집 및 분석

**새로 작성할 파일**:
```
survey/
├── domain/
│   └── SurveyResponse.java      # 설문 응답 엔티티
├── application/
│   └── SurveyService.java       # 설문 로직
├── api/
│   └── SurveyController.java    # REST API
├── dto/
│   ├── request/
│   │   └── SurveyRequest.java
│   └── response/
│       └── SurveyAnalytics.java
└── repository/
    └── SurveyResponseRepository.java
```

**구현 가이드**:
```java
// SurveyService.java
@Service
public class SurveyService {
    private final SurveyResponseRepository repository;
    private final MenuScanRepository menuScanRepository;

    public void recordResponse(UUID scanId, Boolean hasConfidence) {
        MenuScan scan = menuScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Scan not found"));

        // 중복 응답 체크
        if (repository.existsByScanId(scanId)) {
            throw new IllegalStateException("Survey already submitted");
        }

        SurveyResponse response = new SurveyResponse();
        response.setScan(scan);
        response.setAbGroup(scan.getAbGroup());
        response.setHasConfidence(hasConfidence);

        repository.save(response);
    }

    public SurveyAnalytics getAnalytics() {
        // Control vs Treatment 응답률 계산
        long controlTotal = repository.countByAbGroup(ABGroup.CONTROL);
        long controlYes = repository.countByAbGroupAndHasConfidence(
            ABGroup.CONTROL, true);

        long treatmentTotal = repository.countByAbGroup(ABGroup.TREATMENT);
        long treatmentYes = repository.countByAbGroupAndHasConfidence(
            ABGroup.TREATMENT, true);

        double controlRate = (double) controlYes / controlTotal;
        double treatmentRate = (double) treatmentYes / treatmentTotal;
        double ratio = treatmentRate / controlRate;

        return new SurveyAnalytics(
            controlTotal, controlYes, controlRate,
            treatmentTotal, treatmentYes, treatmentRate,
            ratio, ratio >= 2.0 // hypothesisValidated
        );
    }
}
```

---

## 컨트롤러 수정 가이드

### MenuController 수정

**기존 엔드포인트**: 유지
```java
POST /api/menus/reconfigure  // 기존 엔드포인트 유지
```

**새 엔드포인트**: 추가
```java
POST /api/menus/scan          // 새 엔드포인트 추가
```

**구현 예시**:
```java
@RestController
@RequestMapping("/api/menus")
public class MenuController {
    private final MenuService menuService;
    private final ABTestService abTestService;

    // 기존 메서드 유지
    @PostMapping("/reconfigure")
    public ResponseEntity<ReconfigureResponse> reconfigure(
        @RequestBody ReconfigureRequest request
    ) {
        // 기존 로직 유지
    }

    // 새 메서드 추가
    @PostMapping("/scan")
    public ResponseEntity<MenuScanResponse> scanMenu(
        @RequestBody MenuScanRequest request,
        HttpSession session
    ) {
        String sessionId = session.getId();
        MenuScanResponse response = menuService.scanMenu(request, sessionId);
        return ResponseEntity.ok(response);
    }
}
```

---

## Redis 설정 추가

**application.yml**:
```yaml
spring:
  session:
    store-type: redis
    redis:
      namespace: spring:session
  data:
    redis:
      host: localhost
      port: 6379
```

**Redis Config**:
```java
@Configuration
@EnableRedisHttpSession
public class RedisConfig {
    // Spring Session 자동 설정
}
```

---

## 체크리스트

### ✅ 재사용 가능 (수정 불필요)
- [ ] `MenuService.reconfigure()` 메서드
- [ ] `MenuItemEnricher`
- [ ] `GeminiOcrReader` (OCR)
- [ ] `GoogleTranslationClient` (번역)
- [ ] `TasteAtlasFoodScrapper` (음식 매칭)
- [ ] `CurrencyService` (환율 변환)
- [ ] `Language` enum
- [ ] `Currency` enum

### ➕ 새로 개발 필요
- [ ] `ABTestService` (A/B 그룹 배정)
- [ ] `SurveyService` (설문 수집)
- [ ] `MenuScan` 엔티티
- [ ] `SurveyResponse` 엔티티
- [ ] `MenuController.scanMenu()` 메서드
- [ ] `SurveyController`
- [ ] `ABTestController` (관리자용)

### 🔧 수정 필요
- [ ] `MenuService`: `scanMenu()` 메서드 추가
- [ ] `MenuController`: `/api/menus/scan` 엔드포인트 추가
- [ ] `MenuItem` 엔티티: `scan_id` FK 추가
- [ ] Redis 설정 추가

### ⚠️ MVP 제외 (건드리지 않음)
- [ ] `Order` 모듈
- [ ] `Script` 모듈

---

## 개발 순서 권장

1. **Phase 1**: 도메인 및 Repository 생성
   - `ABGroup` enum
   - `MenuScan` entity
   - `SurveyResponse` entity
   - JPA Repositories

2. **Phase 2**: 서비스 레이어 구현
   - `ABTestService`
   - `SurveyService`
   - `MenuService.scanMenu()` 추가

3. **Phase 3**: API 레이어 구현
   - `MenuController.scanMenu()` 추가
   - `SurveyController`
   - `ABTestController`

4. **Phase 4**: Redis 설정 및 세션 관리
   - Spring Session 설정
   - Redis 캐싱 설정

5. **Phase 5**: 테스트
   - 단위 테스트 (Service)
   - 통합 테스트 (API)
   - A/B 테스트 검증

---

## 참고 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md) - 전체 아키텍처
- [API_SPEC.md](./API_SPEC.md) - API 엔드포인트 설계
- [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - DB 스키마
- [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) - 구현 계획
