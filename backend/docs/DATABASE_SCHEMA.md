# FoodiePass Backend - Database Schema

> **목적**: MySQL 데이터베이스 스키마 명세 및 마이그레이션 계획

---

## Database Configuration

| 환경 | DB 엔진 | 설명 |
|---|---|---|
| **Local** | H2 (in-memory) | 로컬 개발용 |
| **Dev** | MySQL 8.0 (RDS) | 개발 서버용 |
| **Prod** | MySQL 8.0 (RDS) | 프로덕션용 |

---

## 테이블 목록

| 테이블명 | 설명 | 상태 |
|---|---|---|
| `menu_scans` | 메뉴 스캔 세션 (메뉴 아이템 JSON 포함) | ➕ 신규 |
| `survey_responses` | 설문 응답 | ➕ 신규 |

**참고**: MenuItem은 JPA Entity가 아닌 Value Object입니다. 메뉴 아이템 정보는 `menu_scans.menu_items_json` 컬럼에 JSON 형태로 저장됩니다.

---

## 테이블 스키마

### 1. menu_scans (신규)

**목적**: 메뉴 스캔 세션 및 A/B 테스트 그룹 정보 저장

```sql
CREATE TABLE menu_scans (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
    session_id VARCHAR(255) NOT NULL COMMENT '사용자 세션 ID',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (CONTROL or TREATMENT)',

    image_url VARCHAR(512) COMMENT 'S3 업로드 이미지 URL',

    source_language VARCHAR(10) COMMENT '소스 언어 (auto or 언어 코드)',
    target_language VARCHAR(10) NOT NULL COMMENT '타겟 언어 (사용자 선택)',
    source_currency VARCHAR(3) COMMENT '소스 화폐',
    target_currency VARCHAR(3) NOT NULL COMMENT '타겟 화폐 (사용자 선택)',

    menu_items_json TEXT COMMENT '메뉴 아이템 리스트 (JSON)',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',

    INDEX idx_session_id (session_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메뉴 스캔 세션';
```

**컬럼 설명**:

| 컬럼명 | 타입 | Null | 설명 |
|---|---|---|---|
| `id` | VARCHAR(36) | NOT NULL | UUID (Primary Key) |
| `session_id` | VARCHAR(255) | NOT NULL | 사용자 세션 ID (Spring Session) |
| `ab_group` | VARCHAR(20) | NOT NULL | A/B 그룹 ('CONTROL' or 'TREATMENT') |
| `image_url` | VARCHAR(512) | NULL | S3에 업로드된 메뉴판 이미지 URL |
| `source_language` | VARCHAR(10) | NULL | 소스 언어 코드 (예: 'en', 'it', 'auto') |
| `target_language` | VARCHAR(10) | NOT NULL | 타겟 언어 코드 (예: 'ko', 'en', 'ja') |
| `source_currency` | VARCHAR(3) | NULL | 소스 화폐 코드 (예: 'USD', 'EUR') |
| `target_currency` | VARCHAR(3) | NOT NULL | 타겟 화폐 코드 (예: 'KRW', 'USD') |
| `menu_items_json` | TEXT | NULL | 메뉴 아이템 리스트 (JSON 배열 형태) |
| `created_at` | TIMESTAMP | NOT NULL | 스캔 생성 시각 |

**인덱스**:
- `idx_session_id`: 세션별 스캔 조회 최적화
- `idx_ab_group`: A/B 그룹별 집계 최적화
- `idx_created_at`: 날짜 범위 조회 최적화

---

### 2. survey_responses (신규)

**목적**: 확신도 설문 응답 저장

```sql
CREATE TABLE survey_responses (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
    scan_id VARCHAR(36) NOT NULL COMMENT 'FK to menu_scans',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (분석용)',

    has_confidence BOOLEAN NOT NULL COMMENT '확신 여부 (Yes=true, No=false)',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '응답 시각',

    FOREIGN KEY (scan_id) REFERENCES menu_scans(id) ON DELETE CASCADE,
    INDEX idx_scan_id (scan_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_created_at (created_at),
    UNIQUE KEY uk_scan_id (scan_id) COMMENT '중복 응답 방지'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='설문 응답';
```

**컬럼 설명**:

| 컬럼명 | 타입 | Null | 설명 |
|---|---|---|---|
| `id` | VARCHAR(36) | NOT NULL | UUID (Primary Key) |
| `scan_id` | VARCHAR(36) | NOT NULL | FK to `menu_scans` (UNIQUE) |
| `ab_group` | VARCHAR(20) | NOT NULL | A/B 그룹 (분석용 denormalization) |
| `has_confidence` | BOOLEAN | NOT NULL | 확신 여부 (Yes=true, No=false) |
| `created_at` | TIMESTAMP | NOT NULL | 응답 제출 시각 |

**인덱스**:
- `idx_scan_id`: 스캔별 응답 조회
- `idx_ab_group`: A/B 그룹별 집계
- `idx_created_at`: 날짜 범위 조회
- `uk_scan_id`: 중복 응답 방지 (UNIQUE)

**제약 조건**:
- FK `scan_id` → `menu_scans(id)` (CASCADE DELETE)
- UNIQUE `scan_id`: 한 스캔당 하나의 응답만 허용

---

## ERD (Entity Relationship Diagram)

```
┌──────────────────┐
│   menu_scans     │
├──────────────────┤
│ id (PK)          │
│ session_id       │
│ ab_group         │
│ image_url        │
│ source_language  │
│ target_language  │
│ source_currency  │
│ target_currency  │
│ menu_items_json  │ ← JSON 배열로 MenuItem 저장
│ created_at       │
└──────────────────┘
         │
         │ 1:1
         ↓
┌──────────────────┐
│ survey_responses │
├──────────────────┤
│ id (PK)          │
│ scan_id (FK,UK)  │
│ ab_group         │
│ has_confidence   │
│ created_at       │
└──────────────────┘
```

**참고**: MenuItem은 별도 테이블이 아닌 Value Object로, `menu_items_json` 컬럼에 JSON 배열로 저장됩니다.

---

## 마이그레이션 계획

### Flyway 마이그레이션 스크립트

#### V1__initial_schema.sql (기존)
```sql
-- 기존 v1 스키마 (있다면)
```

#### V2__create_menu_scan_tables.sql (신규)
```sql
-- menu_scans 테이블 생성
CREATE TABLE menu_scans (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
    session_id VARCHAR(255) NOT NULL COMMENT '사용자 세션 ID',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (CONTROL or TREATMENT)',
    image_url VARCHAR(512) COMMENT 'S3 업로드 이미지 URL',
    source_language VARCHAR(10) COMMENT '소스 언어 (auto or 언어 코드)',
    target_language VARCHAR(10) NOT NULL COMMENT '타겟 언어 (사용자 선택)',
    source_currency VARCHAR(3) COMMENT '소스 화폐',
    target_currency VARCHAR(3) NOT NULL COMMENT '타겟 화폐 (사용자 선택)',
    menu_items_json TEXT COMMENT '메뉴 아이템 리스트 (JSON)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    INDEX idx_session_id (session_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메뉴 스캔 세션';

-- survey_responses 테이블 생성
CREATE TABLE survey_responses (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
    scan_id VARCHAR(36) NOT NULL COMMENT 'FK to menu_scans',
    ab_group VARCHAR(20) NOT NULL COMMENT 'A/B 그룹 (분석용)',
    has_confidence BOOLEAN NOT NULL COMMENT '확신 여부 (Yes=true, No=false)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '응답 시각',
    FOREIGN KEY (scan_id) REFERENCES menu_scans(id) ON DELETE CASCADE,
    INDEX idx_scan_id (scan_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_created_at (created_at),
    UNIQUE KEY uk_scan_id (scan_id) COMMENT '중복 응답 방지'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='설문 응답';
```

---

## JPA Entity 매핑

### MenuScan Entity

```java
@Entity
@Table(name = "menu_scans")
public class MenuScan {
    @Id
    @Column(length = 36)
    private String id; // UUID.randomUUID().toString()

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ab_group", nullable = false, length = 20)
    private ABGroup abGroup;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "source_language", length = 10)
    private String sourceLanguage;

    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage;

    @Column(name = "source_currency", length = 3)
    private String sourceCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "menu_items_json", columnDefinition = "TEXT")
    private String menuItemsJson;  // JSON 문자열

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Helper methods for JSON serialization/deserialization
    // ObjectMapper를 주입받아 사용
    public void setMenuItems(List<MenuItem> items, ObjectMapper objectMapper) throws JsonProcessingException {
        this.menuItemsJson = objectMapper.writeValueAsString(items);
    }

    public List<MenuItem> getMenuItems(ObjectMapper objectMapper) throws JsonProcessingException {
        if (menuItemsJson == null || menuItemsJson.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMapper.readValue(menuItemsJson, new TypeReference<List<MenuItem>>() {});
    }
}
```

**참고**:
- MenuItem은 **Value Object**로, 별도 테이블이 없습니다.
- 메뉴 아이템 리스트는 `menuItemsJson` 필드에 JSON 배열로 저장됩니다.
- Jackson ObjectMapper를 사용하여 직렬화/역직렬화를 수행합니다.

---

### SurveyResponse Entity

```java
@Entity
@Table(name = "survey_responses")
public class SurveyResponse {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id", nullable = false, unique = true)
    private MenuScan scan;

    @Enumerated(EnumType.STRING)
    @Column(name = "ab_group", nullable = false, length = 20)
    private ABGroup abGroup;

    @Column(name = "has_confidence", nullable = false)
    private Boolean hasConfidence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
```

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
FROM survey_responses
WHERE ab_group = 'CONTROL';

-- Treatment 그룹 통계
SELECT
    COUNT(*) as total_responses,
    SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) as yes_count,
    SUM(CASE WHEN has_confidence = FALSE THEN 1 ELSE 0 END) as no_count,
    AVG(CASE WHEN has_confidence = TRUE THEN 1.0 ELSE 0.0 END) as yes_rate
FROM survey_responses
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
     FROM survey_responses WHERE ab_group = 'CONTROL') c,
    (SELECT AVG(CASE WHEN has_confidence = TRUE THEN 1.0 ELSE 0.0 END) as yes_rate
     FROM survey_responses WHERE ab_group = 'TREATMENT') t;
```

---

## 인덱스 최적화 전략

| 쿼리 패턴 | 인덱스 | 설명 |
|---|---|---|
| 세션별 스캔 조회 | `idx_session_id` | 사용자별 스캔 히스토리 |
| A/B 그룹별 집계 | `idx_ab_group` | 그룹별 통계 분석 |
| 날짜 범위 조회 | `idx_created_at` | 기간별 데이터 분석 |
| 중복 응답 방지 | `uk_scan_id` (survey_responses) | 응답 유니크 제약 |

---

## 데이터 보관 정책

| 테이블 | 보관 기간 | 정책 |
|---|---|---|
| `menu_scans` | 6개월 | 6개월 후 아카이브 또는 삭제 (menu_items_json 포함) |
| `survey_responses` | 6개월 | CASCADE DELETE (scan 삭제 시) |

---

## 참고 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md) - 백엔드 아키텍처
- [API_SPEC.md](./API_SPEC.md) - API 엔드포인트 설계
- [HYPOTHESES.md](./HYPOTHESES.md) - 핵심 가설 및 검증 목표
