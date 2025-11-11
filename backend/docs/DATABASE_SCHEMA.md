# FoodiePass Backend - Database Schema

> **목적**: MySQL 데이터베이스 스키마 명세 및 마이그레이션 계획

---

## Database Configuration

| 환경 | DB 엔진 | 설명 |
|---|---|---|
| **Local** | H2 (in-memory) | 로컬 개발용 (hibernate.ddl-auto: create-drop) |
| **Dev** | MySQL 8.0 (RDS) | 개발 서버용 |
| **Prod** | MySQL 8.0 (RDS) | 프로덕션용 |

---

## 테이블 목록

| 테이블명 | 설명 | 상태 |
|---|---|---|
| `menu_scan` | 메뉴 스캔 세션 메타데이터 (A/B 그룹 정보) | ✅ 구현완료 |
| `survey_response` | 설문 응답 | ✅ 구현완료 |

**참고**: MenuItem은 JPA Entity가 아닌 Value Object입니다. 메뉴 아이템은 DB에 저장되지 않고, API 응답으로만 반환됩니다 (Stateless 방식).

---

## 테이블 스키마

### 1. menu_scan (구현완료)

**목적**: 메뉴 스캔 세션 메타데이터 및 A/B 테스트 그룹 정보 저장

```sql
CREATE TABLE menu_scan (
    id BINARY(16) PRIMARY KEY COMMENT 'UUID (binary format)',
    user_id VARCHAR(255) NOT NULL COMMENT '사용자 식별자',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (CONTROL or TREATMENT)',

    image_url VARCHAR(500) COMMENT '메뉴판 이미지 URL (optional)',

    source_language VARCHAR(50) COMMENT '소스 언어 이름',
    target_language VARCHAR(50) COMMENT '타겟 언어 이름',
    source_currency VARCHAR(100) COMMENT '소스 화폐 이름',
    target_currency VARCHAR(100) COMMENT '타겟 화폐 이름',

    menu_items_json TEXT COMMENT '메뉴 아이템 리스트 (JSON 배열)',

    created_at DATETIME NOT NULL COMMENT '생성 시각'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메뉴 스캔 세션 메타데이터';
```

**컬럼 설명**:

| 컬럼명 | 타입 | Null | 설명 |
|---|---|---|---|
| `id` | BINARY(16) | NOT NULL | UUID (binary 형식, Primary Key) |
| `user_id` | VARCHAR(255) | NOT NULL | 사용자 식별자 (세션 ID 또는 임시 ID) |
| `ab_group` | VARCHAR(20) | NOT NULL | A/B 그룹 ('CONTROL' or 'TREATMENT') |
| `image_url` | VARCHAR(500) | NULL | 메뉴판 이미지 URL (MVP에서는 null) |
| `source_language` | VARCHAR(50) | NULL | 소스 언어 이름 (예: 'Japanese', 'Italian') |
| `target_language` | VARCHAR(50) | NULL | 타겟 언어 이름 (예: 'Korean', 'English') |
| `source_currency` | VARCHAR(100) | NULL | 소스 화폐 이름 (예: 'Japanese Yen') |
| `target_currency` | VARCHAR(100) | NULL | 타겟 화폐 이름 (예: 'South Korean won') |
| `menu_items_json` | TEXT | NULL | 메뉴 아이템 리스트 (JSON 배열 형식) |
| `created_at` | DATETIME | NOT NULL | 스캔 생성 시각 |

**인덱스**: 없음 (MVP에서는 기본 PK만 사용)

**설계 특징**:
- MenuItem은 별도 테이블이 아닌 menu_items_json 컬럼에 JSON 배열로 저장됨
- Helper 메서드를 통해 JSON ↔ List<MenuItem> 변환 수행
- 언어/화폐는 이름으로 저장 (Language/Currency enum의 name 속성)

---

### 2. survey_response (구현완료)

**목적**: 확신도 설문 응답 저장

```sql
CREATE TABLE survey_response (
    id BINARY(16) PRIMARY KEY COMMENT 'UUID (binary format)',
    scan_id BINARY(16) NOT NULL COMMENT 'Reference to menu_scan.id',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (분석용 denormalization)',

    has_confidence BOOLEAN NOT NULL COMMENT '확신 여부 (Yes=true, No=false)',

    created_at DATETIME NOT NULL COMMENT '응답 시각',

    INDEX idx_scan_id (scan_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_ab_group_confidence (ab_group, has_confidence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='설문 응답';
```

**컬럼 설명**:

| 컬럼명 | 타입 | Null | 설명 |
|---|---|---|---|
| `id` | BINARY(16) | NOT NULL | UUID (binary 형식, Primary Key) |
| `scan_id` | BINARY(16) | NOT NULL | menu_scan.id 참조 (FK 제약 없음) |
| `ab_group` | VARCHAR(20) | NOT NULL | A/B 그룹 (분석용 denormalization) |
| `has_confidence` | BOOLEAN | NOT NULL | 확신 여부 (Yes=true, No=false) |
| `created_at` | DATETIME | NOT NULL | 응답 제출 시각 |

**인덱스**:
- `idx_scan_id`: 스캔별 응답 조회
- `idx_ab_group`: A/B 그룹별 집계
- `idx_ab_group_confidence`: A/B 그룹 + 확신도 복합 인덱스 (집계 쿼리 최적화)

**설계 특징**:
- FK 제약 조건 없음 (application-level 관계 관리)
- ab_group denormalization으로 집계 쿼리 성능 최적화

---

## ERD (Entity Relationship Diagram)

```text
┌───────────────────┐
│   menu_scan       │
├───────────────────┤
│ id (PK)           │ BINARY(16)
│ user_id           │
│ ab_group          │
│ image_url         │
│ source_language   │
│ target_language   │
│ source_currency   │
│ target_currency   │
│ created_at        │
└───────────────────┘
         │
         │ 1:N (app-level reference, no FK)
         ↓
┌───────────────────┐
│ survey_response   │
├───────────────────┤
│ id (PK)           │ BINARY(16)
│ scan_id           │ BINARY(16) (references menu_scan.id)
│ ab_group          │ (denormalized)
│ has_confidence    │
│ created_at        │
└───────────────────┘
```

**참고**:
- MenuItem은 별도 테이블 없음 (Stateless API 응답)
- FK 제약 조건 없음 (application-level 관계 관리)

---

## 마이그레이션 계획

### 현재 전략: Hibernate DDL Auto (Local)

**Local 환경**: `hibernate.ddl-auto: create-drop`
- H2 in-memory 데이터베이스
- 애플리케이션 시작 시 자동으로 테이블 생성
- 애플리케이션 종료 시 자동으로 테이블 삭제

### Production 마이그레이션 (TODO)

**Dev/Prod 환경**: Flyway 또는 Liquibase 사용 권장

#### V1__create_menu_scan_tables.sql
```sql
-- menu_scan 테이블 생성
CREATE TABLE menu_scan (
    id BINARY(16) PRIMARY KEY COMMENT 'UUID (binary format)',
    user_id VARCHAR(255) NOT NULL COMMENT '사용자 식별자',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (CONTROL or TREATMENT)',
    image_url VARCHAR(500) COMMENT '메뉴판 이미지 URL (optional)',
    source_language VARCHAR(50) COMMENT '소스 언어 이름',
    target_language VARCHAR(50) COMMENT '타겟 언어 이름',
    source_currency VARCHAR(100) COMMENT '소스 화폐 이름',
    target_currency VARCHAR(100) COMMENT '타겟 화폐 이름',
    menu_items_json TEXT COMMENT '메뉴 아이템 리스트 (JSON 배열)',
    created_at DATETIME NOT NULL COMMENT '생성 시각'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메뉴 스캔 세션 메타데이터';

-- survey_response 테이블 생성
CREATE TABLE survey_response (
    id BINARY(16) PRIMARY KEY COMMENT 'UUID (binary format)',
    scan_id BINARY(16) NOT NULL COMMENT 'Reference to menu_scan.id',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (분석용 denormalization)',
    has_confidence BOOLEAN NOT NULL COMMENT '확신 여부 (Yes=true, No=false)',
    created_at DATETIME NOT NULL COMMENT '응답 시각',
    INDEX idx_scan_id (scan_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_ab_group_confidence (ab_group, has_confidence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='설문 응답';
```

---

## JPA Entity 매핑

### MenuScan Entity

**파일**: `foodiepass.server.abtest.domain.MenuScan`

```java
@Entity
@Table(name = "menu_scan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuScan {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;  // Binary UUID for efficient storage

    @Column(nullable = false)
    private String userId;  // User identifier (session ID or temp ID)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ABGroup abGroup;  // CONTROL or TREATMENT

    @Column(length = 500)
    private String imageUrl;  // Optional: menu image URL

    @Column(length = 50)
    private String sourceLanguage;  // Language name (e.g., "Japanese")

    @Column(length = 50)
    private String targetLanguage;  // Language name (e.g., "Korean")

    @Column(length = 100)
    private String sourceCurrency;  // Currency name (e.g., "Japanese Yen")

    @Column(length = 100)
    private String targetCurrency;  // Currency name (e.g., "South Korean won")

    @Column(name = "menu_items_json", columnDefinition = "TEXT")
    private String menuItemsJson;  // MenuItem list serialized as JSON

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Transient helper methods for JSON conversion
    @Transient
    public List<MenuItem> getMenuItems(ObjectMapper objectMapper) throws JsonProcessingException {
        if (menuItemsJson == null || menuItemsJson.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMapper.readValue(menuItemsJson, new TypeReference<List<MenuItem>>() {});
    }

    @Transient
    public void setMenuItems(List<MenuItem> items, ObjectMapper objectMapper) throws JsonProcessingException {
        this.menuItemsJson = objectMapper.writeValueAsString(items);
    }

    // Factory method
    public static MenuScan create(String userId, ABGroup abGroup, String imageUrl,
                                   String sourceLanguage, String targetLanguage,
                                   String sourceCurrency, String targetCurrency) {
        return new MenuScan(userId, abGroup, imageUrl, sourceLanguage, targetLanguage,
                sourceCurrency, targetCurrency);
    }
}
```

**설계 특징**:
- `id`: BINARY(16) 형식의 UUID (효율적인 저장)
- `userId`: 사용자 식별자 (세션 ID 또는 임시 ID)
- `menuItemsJson`: MenuItem 리스트를 JSON 문자열로 직렬화하여 저장
- Helper 메서드(`getMenuItems`, `setMenuItems`)를 통해 JSON ↔ List<MenuItem> 변환

---

### SurveyResponse Entity

**파일**: `foodiepass.server.survey.domain.SurveyResponse`

```java
@Entity
@Table(
    name = "survey_response",
    indexes = {
        @Index(name = "idx_scan_id", columnList = "scan_id"),
        @Index(name = "idx_ab_group", columnList = "ab_group"),
        @Index(name = "idx_ab_group_confidence", columnList = "ab_group,has_confidence")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyResponse {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "scan_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID scanId;  // Reference to MenuScan.id (no FK constraint)

    @Enumerated(EnumType.STRING)
    @Column(name = "ab_group", nullable = false, length = 20)
    private ABGroup abGroup;  // Denormalized for analytics queries

    @Column(name = "has_confidence", nullable = false)
    private Boolean hasConfidence;  // true = Yes, false = No

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SurveyResponse(UUID scanId, ABGroup abGroup, Boolean hasConfidence) {
        this.id = UUID.randomUUID();
        this.scanId = scanId;
        this.abGroup = abGroup;
        this.hasConfidence = hasConfidence;
        this.createdAt = LocalDateTime.now();
    }
}
```

**설계 특징**:
- `scanId`: MenuScan.id 참조 (FK 제약 없음)
- `abGroup`: Denormalized for analytics performance
- Composite index on `(ab_group, has_confidence)` for A/B test analytics

---

## 쿼리 예시

### A/B 테스트 결과 집계

```sql
-- Control 그룹 통계
SELECT
    COUNT(*) as total_responses,
    SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) as yes_count,
    SUM(CASE WHEN has_confidence = FALSE THEN 1 ELSE 0 END) as no_count,
    AVG(CASE WHEN has_confidence = TRUE THEN 1.0 ELSE 0.0 END) as yes_rate
FROM survey_response
WHERE ab_group = 'CONTROL';

-- Treatment 그룹 통계
SELECT
    COUNT(*) as total_responses,
    SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) as yes_count,
    SUM(CASE WHEN has_confidence = FALSE THEN 1 ELSE 0 END) as no_count,
    AVG(CASE WHEN has_confidence = TRUE THEN 1.0 ELSE 0.0 END) as yes_rate
FROM survey_response
WHERE ab_group = 'TREATMENT';

-- A/B 비교 (Treatment / Control 비율)
SELECT
    t.yes_rate / c.yes_rate as ratio,
    CASE
        WHEN t.yes_rate / c.yes_rate >= 2.0 THEN true
        ELSE false
    END as hypothesis_validated
FROM
    (SELECT AVG(CASE WHEN has_confidence = TRUE THEN 1.0 ELSE 0.0 END) as yes_rate
     FROM survey_response WHERE ab_group = 'CONTROL') c,
    (SELECT AVG(CASE WHEN has_confidence = TRUE THEN 1.0 ELSE 0.0 END) as yes_rate
     FROM survey_response WHERE ab_group = 'TREATMENT') t;
```

### Repository 메서드 (JPA)

**SurveyResponseRepository**:
```java
// A/B 그룹별 확신도 집계
long controlYes = surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.CONTROL, true);
long controlTotal = surveyResponseRepository.countByAbGroup(ABGroup.CONTROL);

long treatmentYes = surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true);
long treatmentTotal = surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT);

double controlYesRate = (double) controlYes / controlTotal;
double treatmentYesRate = (double) treatmentYes / treatmentTotal;
double ratio = treatmentYesRate / controlYesRate;

// 가설 검증: ratio >= 2.0
boolean hypothesisValidated = ratio >= 2.0;
```

---

## 인덱스 최적화 전략

| 쿼리 패턴 | 인덱스 | 설명 |
|---|---|---|
| 스캔별 응답 조회 | `idx_scan_id` (survey_response) | 특정 스캔의 응답 조회 |
| A/B 그룹별 집계 | `idx_ab_group` (survey_response) | 그룹별 통계 분석 |
| A/B 그룹 + 확신도 집계 | `idx_ab_group_confidence` (survey_response) | 복합 인덱스로 집계 쿼리 최적화 |

**MVP 단계**:
- menu_scan 테이블에는 인덱스 없음 (PK만 사용)
- survey_response 테이블에만 집계용 인덱스 생성

---

## 데이터 보관 정책

| 테이블 | 보관 기간 | 정책 |
|---|---|---|
| `menu_scan` | MVP: 무제한 | 추후 6개월 후 아카이브 또는 삭제 |
| `survey_response` | MVP: 무제한 | scan 삭제 시 application-level cascade |

**MVP 단계**: 데이터 삭제 정책 없음 (분석 데이터 보존)

---

## 참고 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md) - 백엔드 아키텍처
- [API_SPEC.md](./API_SPEC.md) - API 엔드포인트 설계
- [HYPOTHESES.md](./HYPOTHESES.md) - 핵심 가설 및 검증 목표
