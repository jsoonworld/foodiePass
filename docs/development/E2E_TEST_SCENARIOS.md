# FoodiePass MVP v2 - E2E Integration Test Scenarios

**Purpose**: End-to-end test scenarios for validating the complete MVP v2 system

**Test Environment**: Local (application-local.yml), H2 in-memory database

---

## 🎯 Test Objectives

### Primary Goals
1. **H1 Validation**: Verify Control vs Treatment group experience difference
2. **H2 Validation**: Verify technical implementation (OCR, translation, matching, currency)
3. **H3 Validation**: Verify survey response collection and analytics

### Technical Goals
- Complete user flow: Menu scan → A/B assignment → Response → Survey
- API integration: All endpoints working together
- Data consistency: FK constraints, no orphaned records
- Performance: Processing time ≤5초

---

## 🔄 Critical User Flows

### Flow 1: Control Group Experience (Text-Only)

**Scenario**: User gets text translation + currency conversion only

**Steps**:
1. POST `/api/menus/scan` with menu image
   - Request includes: image (base64), languages, currencies
2. Backend assigns user to CONTROL group (random 50% chance)
3. Backend processes:
   - OCR extracts text
   - Translation API translates
   - Currency API converts prices
   - **Skips**: Food matching (no images/descriptions)
4. Response returns:
   - `scanId` (UUID)
   - `abGroup`: "CONTROL"
   - `items`: Array with `translatedName`, `priceInfo` ONLY
   - `processingTime` ≤5초
5. User sees text-only menu
6. POST `/api/surveys` with:
   - `scanId` (from step 4)
   - `hasConfidence`: true/false
7. Backend saves survey response

**Expected Outcome**:
- ✅ No `description` or `imageUrl` in response
- ✅ Survey response recorded with `ab_group='CONTROL'`
- ✅ Processing time ≤5초

---

### Flow 2: Treatment Group Experience (Visual Menu)

**Scenario**: User gets full visual menu with images + descriptions

**Steps**:
1. POST `/api/menus/scan` with menu image
2. Backend assigns user to TREATMENT group (random 50% chance)
3. Backend processes:
   - OCR extracts text
   - Translation API translates
   - **Food matching** API finds images/descriptions
   - Currency API converts prices
4. Response returns:
   - `scanId` (UUID)
   - `abGroup`: "TREATMENT"
   - `items`: Array with `translatedName`, `description`, `imageUrl`, `priceInfo`
   - `processingTime` ≤5초
5. User sees visual menu with food images
6. POST `/api/surveys` with:
   - `scanId` (from step 4)
   - `hasConfidence`: true/false
7. Backend saves survey response

**Expected Outcome**:
- ✅ `description` and `imageUrl` present in response
- ✅ Survey response recorded with `ab_group='TREATMENT'`
- ✅ Processing time ≤5초

---

### Flow 3: Admin Analytics Review

**Scenario**: Admin checks A/B test results and survey analytics

**Steps**:
1. GET `/api/admin/ab-test/results`
   - Returns: Control group count, Treatment group count, ratio
2. GET `/api/admin/surveys/analytics`
   - Returns: Control Yes rate, Treatment Yes rate, ratio
3. Verify hypothesis:
   - H3: Treatment Yes rate / Control Yes rate ≥ 2.0?

**Expected Outcome**:
- ✅ Correct counts for each group
- ✅ Accurate Yes response rates
- ✅ Ratio calculation correct

---

## 🧪 Detailed Test Cases

### Test Suite 1: Menu Scan API

#### TC-1.1: Successful Menu Scan (Control)

**Pre-condition**: None

**Request**:
```bash
curl -X POST http://localhost:8080/api/menus/scan \
  -H "Content-Type: application/json" \
  -d '{
    "image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "sourceLanguage": "auto",
    "targetLanguage": "ko",
    "sourceCurrency": "USD",
    "targetCurrency": "KRW"
  }'
```

**Expected Response** (200 OK):
```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "abGroup": "CONTROL",
  "items": [
    {
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "priceInfo": {
        "original": "$15.00",
        "originalValue": 15.0,
        "originalCurrency": "USD",
        "converted": "₩20,000",
        "convertedValue": 20000.0,
        "convertedCurrency": "KRW",
        "exchangeRate": 1333.33
      }
    }
  ],
  "processingTime": 3.8
}
```

**Assertions**:
- [ ] Status code = 200
- [ ] `scanId` is valid UUID
- [ ] `abGroup` = "CONTROL"
- [ ] `items` array not empty
- [ ] NO `description` field
- [ ] NO `imageUrl` field
- [ ] `processingTime` ≤ 5.0
- [ ] Database: 1 new row in `menu_scan` table

**Post-condition**: MenuScan record created in database

---

#### TC-1.2: Successful Menu Scan (Treatment)

**Pre-condition**: None

**Request**: Same as TC-1.1

**Expected Response** (200 OK):
```json
{
  "scanId": "660e8400-e29b-41d4-a716-446655440001",
  "abGroup": "TREATMENT",
  "items": [
    {
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "description": "토마토 소스, 신선한 모짜렐라 치즈, 바질 잎이 올라간 이탈리아 전통 피자",
      "imageUrl": "https://tasteatlas.com/images/dishes/margherita-pizza.jpg",
      "priceInfo": {
        "original": "$15.00",
        "converted": "₩20,000"
      }
    }
  ],
  "processingTime": 4.5
}
```

**Assertions**:
- [ ] Status code = 200
- [ ] `scanId` is valid UUID
- [ ] `abGroup` = "TREATMENT"
- [ ] `description` field present
- [ ] `imageUrl` field present and valid URL
- [ ] `processingTime` ≤ 5.0
- [ ] Database: 1 new row in `menu_scan` table

---

#### TC-1.3: Invalid Image Format

**Request**:
```json
{
  "image": "not-a-base64-string",
  "sourceLanguage": "auto",
  "targetLanguage": "ko",
  "sourceCurrency": "USD",
  "targetCurrency": "KRW"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Invalid image format",
  "message": "Image must be base64 encoded"
}
```

**Assertions**:
- [ ] Status code = 400
- [ ] Error message descriptive

---

#### TC-1.4: Missing Required Fields

**Request**:
```json
{
  "image": "base64...",
  "targetLanguage": "ko"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Validation failed",
  "details": [
    "sourceLanguage is required",
    "sourceCurrency is required",
    "targetCurrency is required"
  ]
}
```

---

#### TC-1.5: Processing Time Limit

**Pre-condition**: Large/complex menu image

**Expected**:
- [ ] Processing completes within 5 seconds
- [ ] Or returns graceful partial results if timeout

---

### Test Suite 2: Survey API

#### TC-2.1: Successful Survey Submission

**Pre-condition**: Valid scanId from TC-1.1

**Request**:
```bash
curl -X POST http://localhost:8080/api/surveys \
  -H "Content-Type: application/json" \
  -d '{
    "scanId": "550e8400-e29b-41d4-a716-446655440000",
    "hasConfidence": true
  }'
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Response recorded"
}
```

**Assertions**:
- [ ] Status code = 200
- [ ] Database: 1 new row in `survey_response` table
- [ ] `ab_group` matches scan's group
- [ ] `has_confidence` = true

---

#### TC-2.2: Invalid scanId

**Request**:
```json
{
  "scanId": "00000000-0000-0000-0000-000000000000",
  "hasConfidence": true
}
```

**Expected Response** (404 Not Found):
```json
{
  "error": "Scan not found",
  "message": "No scan exists with ID: 00000000-0000-0000-0000-000000000000"
}
```

---

#### TC-2.3: Duplicate Survey Submission

**Pre-condition**: Survey already submitted for scanId

**Expected Response** (409 Conflict):
```json
{
  "error": "Duplicate response",
  "message": "Survey already submitted for this scan"
}
```

---

### Test Suite 3: Admin APIs

#### TC-3.1: A/B Test Results

**Pre-condition**: Some scans created

**Request**:
```bash
curl http://localhost:8080/api/admin/ab-test/results
```

**Expected Response** (200 OK):
```json
{
  "control": {
    "totalScans": 45,
    "percentage": 50.0
  },
  "treatment": {
    "totalScans": 45,
    "percentage": 50.0
  },
  "totalScans": 90,
  "isBalanced": true,
  "deviation": 0.0
}
```

**Assertions**:
- [ ] Control + Treatment = totalScans
- [ ] Percentages sum to ~100% (±5%)
- [ ] `isBalanced` = true if within ±5%

---

#### TC-3.2: Survey Analytics

**Pre-condition**: Some survey responses submitted

**Request**:
```bash
curl http://localhost:8080/api/admin/surveys/analytics
```

**Expected Response** (200 OK):
```json
{
  "control": {
    "total": 40,
    "yesCount": 12,
    "noCount": 28,
    "yesRate": 0.30
  },
  "treatment": {
    "total": 40,
    "yesCount": 28,
    "noCount": 12,
    "yesRate": 0.70
  },
  "ratio": 2.33,
  "hypothesisValidated": true
}
```

**Assertions**:
- [ ] `yesCount` + `noCount` = `total` (each group)
- [ ] `yesRate` = yesCount / total
- [ ] `ratio` = treatment.yesRate / control.yesRate
- [ ] `hypothesisValidated` = true if ratio ≥ 2.0

---

### Test Suite 4: Data Consistency

#### TC-4.1: Foreign Key Constraint

**Test**: Delete MenuScan should cascade delete SurveyResponse

**Steps**:
1. Create scan via API
2. Submit survey via API
3. Directly delete scan from database:
   ```sql
   DELETE FROM menu_scan WHERE id = ...;
   ```
4. Query survey_response:
   ```sql
   SELECT * FROM survey_response WHERE scan_id = ...;
   ```

**Expected**: Survey response also deleted (cascade)

---

#### TC-4.2: Unique Constraint

**Test**: Cannot submit multiple surveys for same scan

**Steps**:
1. Create scan via API
2. Submit survey (first time) → Success
3. Submit survey (second time) → Should fail

**Expected**: Second submission returns 409 Conflict

---

#### TC-4.3: Orphaned Records Check

**Test**: No survey responses without corresponding scans

**Query**:
```sql
SELECT sr.*
FROM survey_response sr
LEFT JOIN menu_scan ms ON sr.scan_id = ms.id
WHERE ms.id IS NULL;
```

**Expected**: No rows (empty result)

---

## 🚀 Performance Test Scenarios

### PF-1: Processing Time under Load

**Goal**: Verify ≤5초 processing time under concurrent requests

**Setup**:
```bash
# Use Apache Bench or JMeter
ab -n 100 -c 10 -p menu-scan-payload.json \
   -T application/json \
   http://localhost:8080/api/menus/scan
```

**Assertions**:
- [ ] 95th percentile ≤ 5000ms
- [ ] No errors under load
- [ ] A/B assignment remains ~50:50

---

### PF-2: Database Performance

**Goal**: Analytics queries perform well

**Test**:
```sql
-- Simulate 10,000 survey responses
-- Then measure query time:
EXPLAIN ANALYZE
SELECT
    ab_group,
    COUNT(*) as total,
    SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) as yes_count
FROM survey_response
GROUP BY ab_group;
```

**Expected**: Query time < 100ms with indexes

---

## 📊 Test Data Sets

### Dataset 1: Simple Menu (English → Korean)

**Image**: Basic text menu with 5 items
**Language**: en → ko
**Currency**: USD → KRW

**Purpose**: Basic flow validation

---

### Dataset 2: Complex Menu (French → Japanese)

**Image**: Multi-column menu with 20+ items
**Language**: fr → ja
**Currency**: EUR → JPY

**Purpose**: Stress test OCR and translation

---

### Dataset 3: Low-Quality Image

**Image**: Blurry, angled photo
**Purpose**: Error handling validation

---

## ✅ Integration Test Checklist

### Pre-Integration
- [ ] All unit tests pass (each module)
- [ ] DB migration applied
- [ ] Test data prepared

### During Integration
- [ ] TC-1.1: Control group scan ✅
- [ ] TC-1.2: Treatment group scan ✅
- [ ] TC-1.3: Invalid image ✅
- [ ] TC-1.4: Missing fields ✅
- [ ] TC-1.5: Processing time ✅
- [ ] TC-2.1: Survey submission ✅
- [ ] TC-2.2: Invalid scanId ✅
- [ ] TC-2.3: Duplicate survey ✅
- [ ] TC-3.1: A/B results ✅
- [ ] TC-3.2: Survey analytics ✅
- [ ] TC-4.1: FK cascade ✅
- [ ] TC-4.2: Unique constraint ✅
- [ ] TC-4.3: No orphans ✅
- [ ] PF-1: Load test ✅
- [ ] PF-2: Query performance ✅

### Post-Integration
- [ ] All critical flows work end-to-end
- [ ] No data consistency issues
- [ ] Performance targets met
- [ ] Ready for user testing

---

## 🔧 Test Execution Tools

### Manual Testing
```bash
# Start application
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'

# In another terminal
curl -X POST http://localhost:8080/api/menus/scan \
  -H "Content-Type: application/json" \
  -d @test-data/menu-scan-request.json
```

### Automated Testing
```bash
# Run integration tests
./gradlew test --tests "*IntegrationTest"

# Run specific test suite
./gradlew test --tests "MenuScanApiIntegrationTest"
```

### Load Testing
```bash
# Install k6 (load testing tool)
brew install k6

# Run load test
k6 run scripts/load-test.js
```

---

## 📝 Test Report Template

```markdown
## Integration Test Report - [Date]

### Summary
- Total Tests: XX
- Passed: XX
- Failed: XX
- Skipped: XX

### Critical Flows
- ✅ Control Group Flow
- ✅ Treatment Group Flow
- ✅ Survey Submission
- ✅ Admin Analytics

### Performance
- Average Processing Time: X.Xs
- 95th Percentile: X.Xs
- Max Processing Time: X.Xs

### Issues Found
1. [Issue description]
   - Severity: High/Medium/Low
   - Status: Open/Fixed

### Hypothesis Validation Status
- H1 (Core Value): ⚪ Pending / 🟢 Validated / 🔴 Failed
- H2 (Technical): ⚪ Pending / 🟢 Validated / 🔴 Failed
- H3 (User Behavior): ⚪ Pending / 🟢 Validated / 🔴 Failed

### Recommendation
- [ ] Ready for user testing
- [ ] Needs fixes before user testing
- [ ] Requires re-architecture
```

---

## 🔗 Related Documents

- [INTEGRATION_CHECKLIST.md](./INTEGRATION_CHECKLIST.md) - Integration procedures
- [HYPOTHESIS_VALIDATION.md](./HYPOTHESIS_VALIDATION.md) - Validation criteria
- [API_SPEC.md](./backend/docs/API_SPEC.md) - API documentation

---

**Last Updated**: 2025-11-04
**Maintainer**: Integration Team
