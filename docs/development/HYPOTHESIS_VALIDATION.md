# FoodiePass MVP v2 - Hypothesis Validation Guide

**Purpose**: 핵심 가설(H1, H2, H3) 검증을 위한 측정 기준 및 방법론

**Version**: 1.0
**Updated**: 2025-11-04

---

## 🎯 Hypothesis Overview

### H1: 핵심 가치 가설
> "여행객은 [텍스트 번역만]으로는 여전히 불안하지만, [사진/설명/환율]이 포함된 시각적 메뉴가 제공될 경우 '주문 확신'을 갖게 된다."

**측정 방법**: 설문 응답 (Yes/No)
**성공 기준**: Treatment 그룹 Yes 응답률 ≥ 70%

---

### H2: 기술 실현 가설
> "우리는 OCR, 환율 API, 음식 매칭 시스템을 통해 70% 이상 연관성의 사진/설명과 95% 이상 정확도의 환율을 제공할 수 있다."

**측정 방법**: 기술 테스트 (정확도 측정)
**성공 기준**:
- OCR 정확도 ≥90%
- 환율 정확도 ≥95%
- 음식 매칭 연관성 ≥70%
- 처리 시간 ≤5초

---

### H3: 사용자 행동/인지 가설
> "[시각적 메뉴] 사용 집단은 [텍스트 번역만] 집단 대비 '확신도'가 2배 이상 높다."

**측정 방법**: A/B 테스트 결과 비교
**성공 기준**: Treatment Yes 응답률 / Control Yes 응답률 ≥ 2.0

---

## 📊 H1: 핵심 가치 가설 검증

### Validation Method

**Survey Question**:
> "이 메뉴 정보만으로 자신 있게 주문할 수 있나요?"
> - Yes (주문할 수 있다)
> - No (더 많은 정보가 필요하다)

**Data Collection**:
```sql
SELECT
    COUNT(*) as total_responses,
    SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) as yes_count,
    (SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as yes_rate
FROM survey_response
WHERE ab_group = 'TREATMENT';
```

**Success Criteria**:
- ✅ Treatment Yes 응답률 ≥ 70%
- ⚠️ 60-70%: Marginal (추가 개선 필요)
- ❌ <60%: Failed (근본적 재검토 필요)

---

### Test Execution

**Phase 1: Internal Testing (Day 9-10)**
- **Sample Size**: 20-30명 (팀원, 지인)
- **Purpose**: 기술적 동작 확인 및 초기 피드백

**Test Procedure**:
1. 각 참가자에게 메뉴판 사진 제공 (실제 레스토랑 메뉴)
2. 시스템에 업로드 → 처리
3. 결과 확인 후 설문 응답
4. 자유 피드백 수집

**Data Analysis**:
```python
# Calculate Treatment Yes rate
treatment_yes_rate = treatment_yes_count / treatment_total

# Validate H1
if treatment_yes_rate >= 0.70:
    print("H1: ✅ Validated")
elif treatment_yes_rate >= 0.60:
    print("H1: ⚠️ Marginal - needs improvement")
else:
    print("H1: ❌ Failed - fundamental rethink needed")
```

---

### Failure Scenarios & Actions

| Yes Rate | Status | Action |
|----------|--------|--------|
| ≥70% | ✅ Success | Proceed to user testing |
| 60-69% | ⚠️ Marginal | Improve UI/UX, retry |
| 50-59% | 🔴 Warning | Major improvements needed |
| <50% | ❌ Failed | Rethink value proposition |

---

## 🔧 H2: 기술 실현 가설 검증

### Metric 1: OCR 정확도

**Target**: ≥90%

**Measurement Method**:
1. 준비: 100개 샘플 메뉴판 (다양한 언어/품질)
2. 각 메뉴판에 대해:
   - Ground truth (실제 텍스트) 준비
   - OCR 실행
   - 결과 비교

**Accuracy Calculation**:
```python
# Character-level accuracy (Levenshtein distance)
import Levenshtein

def calculate_ocr_accuracy(ground_truth, ocr_result):
    distance = Levenshtein.distance(ground_truth, ocr_result)
    max_len = max(len(ground_truth), len(ocr_result))
    accuracy = 1 - (distance / max_len)
    return accuracy

# Overall accuracy
accuracies = [calculate_ocr_accuracy(gt, ocr) for gt, ocr in test_cases]
overall_accuracy = sum(accuracies) / len(accuracies)

if overall_accuracy >= 0.90:
    print("OCR: ✅ Validated")
```

**Test Data Categories**:
- High quality: 40 samples (clear, straight-on photos)
- Medium quality: 40 samples (slight angle, good lighting)
- Low quality: 20 samples (angled, poor lighting)

**Acceptance**:
- ✅ Overall ≥90%
- ⚠️ High quality ≥95%, Medium ≥85%, Low ≥70%

---

### Metric 2: 환율 정확도

**Target**: ≥95%

**Measurement Method**:
1. 실시간 환율 API 응답 수집
2. 신뢰할 수 있는 기준(예: Bloomberg, Reuters) 비교
3. 오차율 계산

**Accuracy Calculation**:
```python
def calculate_currency_accuracy(api_rate, reference_rate):
    error = abs(api_rate - reference_rate) / reference_rate
    accuracy = 1 - error
    return accuracy

# Test multiple currency pairs over time
test_pairs = [
    ('USD', 'KRW'),
    ('EUR', 'KRW'),
    ('JPY', 'KRW'),
    # ... more pairs
]

# Collect over 24 hours, every hour
accuracies = []
for hour in range(24):
    for pair in test_pairs:
        api_rate = get_api_rate(pair)
        ref_rate = get_reference_rate(pair)
        acc = calculate_currency_accuracy(api_rate, ref_rate)
        accuracies.append(acc)

overall_accuracy = sum(accuracies) / len(accuracies)

if overall_accuracy >= 0.95:
    print("Currency: ✅ Validated")
```

**Test Script**: `scripts/test-currency-accuracy.js`

**Acceptance**:
- ✅ Accuracy ≥95%
- ⚠️ Accuracy 90-95%: Acceptable but monitor
- ❌ Accuracy <90%: Switch API provider

---

### Metric 3: 음식 매칭 연관성

**Target**: ≥70%

**Measurement Method** (Manual Review):
1. 100개 메뉴 아이템에 대해 음식 매칭 실행
2. 3명의 리뷰어가 각 결과 평가:
   - ✅ Relevant (1.0): 정확한 매칭
   - ⚠️ Somewhat Relevant (0.5): 유사하지만 약간 다름
   - ❌ Not Relevant (0.0): 전혀 다른 음식

**Relevance Calculation**:
```python
# For each menu item
scores = []
for item in test_items:
    matched_food = food_scraper.match(item.name)

    # 3 reviewers score
    reviewer_scores = [
        reviewer1.evaluate(item, matched_food),  # 0.0, 0.5, or 1.0
        reviewer2.evaluate(item, matched_food),
        reviewer3.evaluate(item, matched_food)
    ]

    # Average
    avg_score = sum(reviewer_scores) / len(reviewer_scores)
    scores.append(avg_score)

overall_relevance = sum(scores) / len(scores)

if overall_relevance >= 0.70:
    print("Food Matching: ✅ Validated")
```

**Review Guidelines**:
- **1.0 (Relevant)**: 정확한 음식, 사진이 대표적
- **0.5 (Somewhat)**: 같은 카테고리지만 약간 다름 (예: Margherita pizza → Hawaiian pizza)
- **0.0 (Not Relevant)**: 완전히 다른 음식

**Acceptance**:
- ✅ ≥70%: Validated
- ⚠️ 60-70%: Acceptable with warnings to users
- ❌ <60%: Disable food images feature

---

### Metric 4: 처리 시간

**Target**: ≤5초 (95th percentile)

**Measurement Method**:
```bash
# Load test script
cd scripts
node menu-scan-load-test.js

# Or use Apache Bench
ab -n 100 -c 10 -p sample-request.json \
   -T application/json \
   http://localhost:8080/api/menus/scan

# Extract 95th percentile
```

**Test Conditions**:
- Cold start: App just started
- Warm: App running for 10+ minutes
- Under load: 10 concurrent requests

**Processing Time Breakdown**:
```
Total Time = OCR + Translation + Food Matching + Currency + Overhead

Target breakdown:
- OCR: ~2s
- Translation: ~0.5s
- Food Matching: ~1.5s
- Currency: ~0.1s
- Overhead: ~0.9s
Total: ~5s
```

**Optimization Targets**:
- Parallel processing: OCR + Currency (independent)
- Caching: Exchange rates (1-hour TTL)
- Timeout: Food matching (max 2s)

**Acceptance**:
- ✅ 95th percentile ≤5s
- ⚠️ 95th percentile 5-7s: Acceptable but optimize
- ❌ >7s: Performance improvements required

---

### H2 Overall Validation

**All 4 metrics must pass**:

| Metric | Target | Status |
|--------|--------|--------|
| OCR Accuracy | ≥90% | ⬜ Pending |
| Currency Accuracy | ≥95% | ⬜ Pending |
| Food Matching Relevance | ≥70% | ⬜ Pending |
| Processing Time | ≤5s | ⬜ Pending |

**H2 Status**:
- ✅ All 4 pass → H2 Validated
- ⚠️ 3 pass, 1 marginal → H2 Partially Validated
- ❌ Any fails → H2 Failed

---

## 📈 H3: 사용자 행동/인지 가설 검증

### Validation Method

**A/B Test Design**:
- **Control Group**: Text translation + currency only
- **Treatment Group**: Text + images + descriptions + currency
- **Assignment**: Random 50:50
- **Metric**: Survey Yes response rate

**Data Collection**:
```sql
-- Control group
SELECT
    COUNT(*) as total,
    SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) as yes_count,
    (SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as yes_rate
FROM survey_response
WHERE ab_group = 'CONTROL';

-- Treatment group
SELECT
    COUNT(*) as total,
    SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) as yes_count,
    (SUM(CASE WHEN has_confidence = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as yes_rate
FROM survey_response
WHERE ab_group = 'TREATMENT';

-- Ratio calculation
SELECT
    t.yes_rate / c.yes_rate as ratio
FROM
    (SELECT yes_rate FROM ... WHERE ab_group = 'TREATMENT') t,
    (SELECT yes_rate FROM ... WHERE ab_group = 'CONTROL') c;
```

**Success Criteria**:
- ✅ Ratio ≥ 2.0: H3 Validated
- ⚠️ Ratio 1.5-2.0: Marginal (some value demonstrated)
- ❌ Ratio <1.5: H3 Failed

---

### Sample Size Calculation

**Target Power**: 80%
**Significance Level**: α = 0.05

**Assumptions**:
- Control Yes rate: 30% (baseline)
- Treatment Yes rate: 60% (2x)
- Minimum detectable effect: 30 percentage points

**Required Sample Size** (per group):
```python
from statsmodels.stats.power import zt_ind_solve_power

# Calculate required n
n = zt_ind_solve_power(
    effect_size=0.63,  # Cohen's h for 0.30 vs 0.60
    alpha=0.05,
    power=0.80,
    alternative='larger'
)

print(f"Required sample size per group: {int(n)}")
# ~40 per group, 80 total
```

**MVP Target**: 40-50 responses per group (80-100 total)

---

### Statistical Significance Test

**Method**: Two-proportion Z-test

```python
from statsmodels.stats.proportion import proportions_ztest

# Example data
control_yes = 12
control_total = 40
treatment_yes = 28
treatment_total = 40

# Z-test
z_stat, p_value = proportions_ztest(
    count=[treatment_yes, control_yes],
    nobs=[treatment_total, control_total],
    alternative='larger'
)

print(f"Z-statistic: {z_stat:.3f}")
print(f"P-value: {p_value:.4f}")

if p_value < 0.05:
    print("✅ Statistically significant")
else:
    print("❌ Not statistically significant")

# Effect size (ratio)
treatment_rate = treatment_yes / treatment_total
control_rate = control_yes / control_total
ratio = treatment_rate / control_rate

print(f"Treatment rate: {treatment_rate:.1%}")
print(f"Control rate: {control_rate:.1%}")
print(f"Ratio: {ratio:.2f}x")

if ratio >= 2.0 and p_value < 0.05:
    print("🎉 H3 Validated!")
```

**Interpretation Guide**:

| Ratio | P-value | Interpretation |
|-------|---------|----------------|
| ≥2.0 | <0.05 | ✅ Strong evidence for H3 |
| ≥2.0 | 0.05-0.10 | ⚠️ Marginal, collect more data |
| ≥2.0 | >0.10 | ❌ Not significant, likely noise |
| 1.5-2.0 | <0.05 | ⚠️ Some value, but below target |
| <1.5 | any | ❌ H3 not supported |

---

### A/B Test Monitoring

**Balance Check** (during test):
```sql
-- Check group assignment balance
SELECT
    ab_group,
    COUNT(*) as count,
    COUNT(*) * 100.0 / SUM(COUNT(*)) OVER () as percentage
FROM menu_scan
GROUP BY ab_group;

-- Expected: ~50% each (±5% acceptable)
```

**Response Rate Check**:
```sql
-- Check how many scans have survey responses
SELECT
    ab_group,
    COUNT(DISTINCT ms.id) as total_scans,
    COUNT(DISTINCT sr.scan_id) as responses,
    COUNT(DISTINCT sr.scan_id) * 100.0 / COUNT(DISTINCT ms.id) as response_rate
FROM menu_scan ms
LEFT JOIN survey_response sr ON ms.id = sr.scan_id
GROUP BY ab_group;

-- Target: >70% response rate for both groups
```

---

### Confounding Factors

**Control for**:
1. **Menu Language**: Ensure balanced across groups
2. **Menu Complexity**: Simple vs complex menus
3. **Time of Day**: Morning/afternoon/evening
4. **Device**: Mobile vs desktop (if applicable)

**Mitigation**:
- Random assignment handles most confounds
- Monitor for imbalance, stratify if needed

---

## 📋 Validation Execution Checklist

### Pre-Validation (Day 6-8)

**Technical Preparation**:
- [ ] All 3 modules integrated
- [ ] Database migrated
- [ ] Application running stable
- [ ] Admin APIs accessible

**Test Data Preparation**:
- [ ] 100 sample menus for OCR test
- [ ] Ground truth for OCR accuracy
- [ ] Currency pairs defined for testing
- [ ] 100 menu items for food matching review

---

### Phase 1: H2 Technical Validation (Day 9, Morning)

**Objective**: Verify technical feasibility before user testing

#### Step 1: OCR Accuracy Test
```bash
cd scripts
node test-ocr-accuracy.js --samples 100

# Review results
cat results/ocr-accuracy-report.json
```

- [ ] Overall accuracy ≥90%
- [ ] High quality ≥95%
- [ ] Medium quality ≥85%

**If fail**: Pause, improve OCR, retry

---

#### Step 2: Currency Accuracy Test
```bash
node test-currency-accuracy.js --duration 24h

# Or quick test
node test-currency-accuracy.js --quick
```

- [ ] Accuracy ≥95%
- [ ] API reliability >99%

**If fail**: Switch to backup currency API

---

#### Step 3: Food Matching Relevance Test
```bash
# Generate matching results
node generate-food-matching-results.js --samples 100

# Manual review (3 reviewers)
# Use spreadsheet: results/food-matching-review.csv
```

- [ ] Overall relevance ≥70%
- [ ] Inter-reviewer agreement >80%

**If fail**: Adjust matching algorithm or disable feature

---

#### Step 4: Performance Test
```bash
node menu-scan-load-test.js --requests 100 --concurrency 10

# Check 95th percentile
cat results/performance-report.json | jq '.percentile_95'
```

- [ ] 95th percentile ≤5s
- [ ] No errors under load

**If fail**: Optimize bottlenecks

---

**H2 Decision Point**:
- ✅ All pass → Proceed to user testing (H1, H3)
- ❌ Any fail → Stop, fix issues, retry technical tests

---

### Phase 2: H1 & H3 User Testing (Day 9 Afternoon - Day 10)

**Objective**: Validate user value and behavior difference

#### Step 1: Recruit Participants
- [ ] 80-100 participants recruited
- [ ] Mix of demographics (age, tech-savvy, travel frequency)
- [ ] Real travelers preferred

#### Step 2: Conduct A/B Test
```bash
# Start application
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'

# Monitor in real-time
watch -n 5 'curl -s http://localhost:8080/api/admin/ab-test/results'
```

**For each participant**:
1. Provide real menu photo (in foreign language)
2. Ask to upload via app
3. Show processed result (Control or Treatment based on random assignment)
4. Ask survey question: "이 메뉴 정보만으로 자신 있게 주문할 수 있나요?"
5. Collect Yes/No response

**Monitoring**:
- [ ] Group balance maintained (~50:50)
- [ ] No technical errors
- [ ] Response rate >70%

---

#### Step 3: Data Analysis
```bash
# Export results
curl http://localhost:8080/api/admin/surveys/analytics > results/survey-analytics.json

# Run statistical test
python scripts/analyze-hypothesis.py results/survey-analytics.json
```

**Expected Output**:
```json
{
  "control": {
    "total": 45,
    "yesCount": 14,
    "yesRate": 0.31
  },
  "treatment": {
    "total": 45,
    "yesCount": 28,
    "yesRate": 0.62
  },
  "ratio": 2.0,
  "pValue": 0.002,
  "significant": true,
  "h1_validated": true,
  "h3_validated": true
}
```

**Validation**:
- [ ] H1: Treatment Yes rate ≥70% → ⚠️ 62% (marginal)
- [ ] H3: Ratio ≥2.0 and p<0.05 → ✅ 2.0x, p=0.002

---

### Phase 3: Results Interpretation (Day 10 Afternoon)

#### Decision Matrix

| H1 | H2 | H3 | Decision |
|----|----|----|----------|
| ✅ | ✅ | ✅ | 🎉 All validated - proceed to scale |
| ⚠️ | ✅ | ✅ | Improve UX, likely proceed |
| ✅ | ⚠️ | ✅ | Optimize tech, likely proceed |
| ✅ | ✅ | ⚠️ | Collect more data |
| ❌ | any | any | Rethink value prop |
| any | ❌ | any | Fix tech or pivot |
| any | any | ❌ | Rethink differentiation |

---

## 📊 Reporting Template

### Hypothesis Validation Report

**Date**: [Date]
**Sample Size**: [N]
**Test Duration**: [Hours/Days]

---

#### H1: Core Value Hypothesis

**Target**: Treatment Yes rate ≥70%

**Results**:
- Treatment Yes rate: **XX%**
- Status: ✅ / ⚠️ / ❌

**Interpretation**: [Brief explanation]

---

#### H2: Technical Feasibility Hypothesis

| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| OCR Accuracy | ≥90% | XX% | ✅/⚠️/❌ |
| Currency Accuracy | ≥95% | XX% | ✅/⚠️/❌ |
| Food Matching | ≥70% | XX% | ✅/⚠️/❌ |
| Processing Time | ≤5s | XX s | ✅/⚠️/❌ |

**Overall H2 Status**: ✅ / ⚠️ / ❌

---

#### H3: User Behavior Hypothesis

**Target**: Ratio ≥2.0

**Results**:
- Control Yes rate: **XX%**
- Treatment Yes rate: **XX%**
- Ratio: **X.Xx**
- P-value: **0.XXX**
- Statistical significance: ✅ / ❌

**Status**: ✅ / ⚠️ / ❌

---

#### Overall Conclusion

- [ ] All hypotheses validated → Proceed to scale
- [ ] Partial validation → [Action plan]
- [ ] Failed → [Next steps]

**Recommendation**: [Next actions]

---

## 🔗 Related Documents

- [1-PAGER.md](./docs/1-PAGER.md) - Original hypothesis definitions
- [PRD.md](./docs/PRD.md) - Requirements tied to hypotheses
- [E2E_TEST_SCENARIOS.md](./E2E_TEST_SCENARIOS.md) - Technical test scenarios

---

**Last Updated**: 2025-11-04
**Version**: 1.0
**Status**: Ready for validation
