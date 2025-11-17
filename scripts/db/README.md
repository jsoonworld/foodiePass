# Database Migration Guide

**Version**: 2.0
**Purpose**: Add A/B Test and Survey tables for MVP v2 hypothesis validation

---

## 📊 New Tables

### 1. menu_scan
**Purpose**: Store menu scan sessions with A/B group assignment

**Columns**:
- `id` (UUID): Primary key
- `user_id` (VARCHAR): Session/User identifier
- `ab_group` (VARCHAR): 'CONTROL' or 'TREATMENT'
- `image_url` (VARCHAR): Menu image storage URL
- `source_language`, `target_language`: Language pair
- `source_currency`, `target_currency`: Currency pair
- `created_at` (TIMESTAMP): Scan creation time

**Indexes**:
- `idx_user_id`: Query by user
- `idx_ab_group`: Analytics by group
- `idx_created_at`: Time-based queries

---

### 2. survey_response
**Purpose**: Store user confidence survey responses

**Columns**:
- `id` (UUID): Primary key
- `scan_id` (UUID): FK to menu_scan
- `ab_group` (VARCHAR): Denormalized for analytics
- `has_confidence` (BOOLEAN): Yes (true) / No (false)
- `created_at` (TIMESTAMP): Response time

**Indexes**:
- `idx_scan_id`: FK index
- `idx_ab_group_confidence`: Composite for analytics
- `uq_scan_response`: Unique constraint (1 scan = 1 response)

**Constraints**:
- FK to `menu_scan(id)` with CASCADE
- Unique constraint on `scan_id`

---

## 🚀 Migration Methods

### Method 1: Flyway (Recommended for Production)

**Setup**:
```bash
cd backend

# Configure Flyway in build.gradle (already done)
# Set environment variables
export FLYWAY_URL=jdbc:mysql://localhost:3306/foodiepass
export FLYWAY_USER=root
export FLYWAY_PASSWORD=yourpassword
```

**Run Migration**:
```bash
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo

# Rollback (if needed)
./gradlew flywayClean  # ⚠️ Drops all objects!
```

---

### Method 2: Manual SQL Execution (Development)

**For Local H2 Database** (Auto-create on startup):
- No manual migration needed
- Spring Boot auto-creates tables from JPA entities
- Recommended for development

**For Local MySQL**:
```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# Connect to MySQL
mysql -u root -p

# Create database (if not exists)
CREATE DATABASE IF NOT EXISTS foodiepass_dev;
USE foodiepass_dev;

# Run migration script
SOURCE scripts/db/V2_0__add_abtest_tables.sql;

# Verify
SHOW TABLES;
DESCRIBE menu_scan;
DESCRIBE survey_response;
```

---

### Method 3: Spring Boot Auto DDL (Development Only)

**application-local.yml**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create tables from entities
```

**Pros**: Automatic, no manual SQL
**Cons**: Not suitable for production

---

## 🧪 Testing Migration

### 1. Verify Tables Created

```sql
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('menu_scan', 'survey_response');
```

**Expected Output**:
```text
+------------------+-------------------------------------------------------+
| TABLE_NAME       | TABLE_COMMENT                                         |
+------------------+-------------------------------------------------------+
| menu_scan        | Menu scan sessions with A/B group assignment...       |
| survey_response  | Survey responses for measuring user confidence...     |
+------------------+-------------------------------------------------------+
```

---

### 2. Verify Indexes

```sql
SHOW INDEXES FROM menu_scan;
SHOW INDEXES FROM survey_response;
```

**Expected Indexes**:
- menu_scan: idx_user_id, idx_ab_group, idx_created_at
- survey_response: idx_scan_id, idx_ab_group_confidence, uq_scan_response

---

### 3. Verify FK Constraint

```sql
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME,
    DELETE_RULE
FROM information_schema.REFERENTIAL_CONSTRAINTS
WHERE TABLE_NAME = 'survey_response'
  AND CONSTRAINT_SCHEMA = DATABASE();
```

**Expected Output**:
```text
fk_survey_scan_id | survey_response | menu_scan | CASCADE
```

---

### 4. Test Data Insertion

```sql
-- Insert test menu scan
SET @scan_id = UNHEX(REPLACE(UUID(), '-', ''));
INSERT INTO menu_scan (id, user_id, ab_group, source_language, target_language, source_currency, target_currency)
VALUES (@scan_id, 'test-user-1', 'TREATMENT', 'en', 'ko', 'USD', 'KRW');

-- Insert survey response
INSERT INTO survey_response (id, scan_id, ab_group, has_confidence)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), @scan_id, 'TREATMENT', TRUE);

-- Verify
SELECT
    ms.id, ms.user_id, ms.ab_group,
    sr.has_confidence
FROM menu_scan ms
LEFT JOIN survey_response sr ON ms.id = sr.scan_id;

-- Cleanup
DELETE FROM survey_response;
DELETE FROM menu_scan;
```

---

### 5. Test FK Cascade Delete

```sql
-- Insert test data
SET @scan_id = UNHEX(REPLACE(UUID(), '-', ''));
INSERT INTO menu_scan (id, user_id, ab_group, source_language, target_language, source_currency, target_currency)
VALUES (@scan_id, 'test-user-2', 'CONTROL', 'en', 'ko', 'USD', 'KRW');

INSERT INTO survey_response (id, scan_id, ab_group, has_confidence)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), @scan_id, 'CONTROL', FALSE);

-- Delete parent (should cascade)
DELETE FROM menu_scan WHERE id = @scan_id;

-- Verify child deleted
SELECT COUNT(*) FROM survey_response WHERE scan_id = @scan_id;
-- Expected: 0
```

---

## 🔄 Rollback Procedure

### Development (Safe)

```sql
-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS survey_response;
DROP TABLE IF EXISTS menu_scan;
```

### Production (⚠️ Use with Caution)

```bash
# Create backup first
mysqldump -u root -p foodiepass > backup_before_rollback.sql

# Then rollback
mysql -u root -p foodiepass < scripts/db/rollback_v2_0.sql
```

**rollback_v2_0.sql**:
```sql
-- Backup data (optional)
CREATE TABLE IF NOT EXISTS menu_scan_backup AS SELECT * FROM menu_scan;
CREATE TABLE IF NOT EXISTS survey_response_backup AS SELECT * FROM survey_response;

-- Drop tables
DROP TABLE IF EXISTS survey_response;
DROP TABLE IF EXISTS menu_scan;
```

---

## 📊 Performance Considerations

### Expected Data Volume (MVP)

- **menu_scan**: ~1000 scans/day = ~30K/month
- **survey_response**: ~800 responses/day = ~24K/month

### Index Usage

**Query**: Get all scans for a user
```sql
SELECT * FROM menu_scan WHERE user_id = 'user-123';
-- Uses: idx_user_id
```

**Query**: A/B test analytics
```sql
SELECT
    ab_group,
    has_confidence,
    COUNT(*) as count
FROM survey_response
GROUP BY ab_group, has_confidence;
-- Uses: idx_ab_group_confidence
```

**Query**: Survey responses for a scan
```sql
SELECT * FROM survey_response WHERE scan_id = UNHEX(...);
-- Uses: idx_scan_id
```

---

## 🔒 Security Considerations

1. **PII Protection**: `user_id` should be anonymized session ID, not email/name
2. **Image URLs**: Should use signed URLs with expiration
3. **Access Control**: Admin APIs should require authentication

---

## 📝 Schema Evolution (Future)

**Potential additions**:
- `menu_scan.processing_time_ms`: Track performance
- `menu_scan.error_message`: Track failures
- `survey_response.feedback_text`: Optional user comments

**Migration strategy**:
```sql
-- V2_1__add_processing_metrics.sql
ALTER TABLE menu_scan ADD COLUMN processing_time_ms INT;
ALTER TABLE menu_scan ADD COLUMN error_message TEXT;
```

---

## 🆘 Troubleshooting

### Issue: FK constraint fails

**Cause**: Trying to insert survey_response with non-existent scan_id

**Solution**:
```sql
-- Check if scan exists
SELECT id FROM menu_scan WHERE id = UNHEX(...);

-- If not, insert scan first
```

---

### Issue: Unique constraint violation

**Cause**: Trying to submit multiple responses for same scan

**Solution**:
```sql
-- Check existing response
SELECT * FROM survey_response WHERE scan_id = UNHEX(...);

-- Update instead of insert
UPDATE survey_response SET has_confidence = TRUE WHERE scan_id = UNHEX(...);
```

---

### Issue: UUID conversion errors

**Cause**: UUID format mismatch (string vs binary)

**Solution**:
```java
// Java: Use UUID.randomUUID()
UUID scanId = UUID.randomUUID();

// SQL: Use UNHEX(REPLACE(UUID(), '-', ''))
INSERT INTO menu_scan (id, ...) VALUES (UNHEX(REPLACE(UUID(), '-', '')), ...);
```

---

## 📚 Related Documents

- [DATABASE_SCHEMA.md](../../backend/docs/DATABASE_SCHEMA.md) - Full schema documentation
- [INTEGRATION_CHECKLIST.md](../../INTEGRATION_CHECKLIST.md) - Integration procedures
- [API_SPEC.md](../../backend/docs/API_SPEC.md) - API endpoints using these tables

---

**Last Updated**: 2025-11-04
**Version**: 2.0
**Maintainer**: Integration Team
