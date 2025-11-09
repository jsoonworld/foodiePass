# FoodiePass MVP - 상세 구현 계획

**작성일**: 2025-11-09
**기준 브랜치**: develop
**목표**: H1, H2, H3 가설 검증을 위한 MVP 완성 및 배포

---

## 📊 현재 상태 (As-Is)

### ✅ 완료된 것
- A/B 테스트 시스템 (ABTestService, ABGroup enum)
- 설문 시스템 (SurveyService)
- 메뉴 스캔 API (`POST /api/menus/scan`)
- Control/Treatment UI 분기
- 프론트엔드 기본 구조 (React/Next.js)
- 환경변수 보안 처리 (.env, .env.example)

### ❌ 해결 필요한 것
- API 500 에러 2건 (actuator/health, surveys/results)
- E2E 테스트 미완성
- 성능 검증 미완료 (5초 이내 목표)
- 배포 환경 미구성

### ⚠️ 검증 필요한 것
- 프론트엔드 동작 테스트
- Control/Treatment UI 차이 검증
- 전체 플로우 (업로드 → 스캔 → 결과 → 설문) 동작 확인

---

## 🎯 구현 계획 개요

```
Phase 1: 긴급 수정 (1일)           ← 현재 여기
Phase 2: 통합 테스트 (2일)
Phase 3: 성능 최적화 (1일)
Phase 4: E2E 자동화 (1일)
Phase 5: 배포 준비 (2일)
Phase 6: 사용자 테스트 (3일)
```

**총 예상 기간**: 10일

---

## Phase 1: 긴급 수정 및 안정화 (Day 1)

### 목표
- 모든 API 엔드포인트 정상화
- 백엔드/프론트엔드 로컬 실행 안정화

### 1.1 API 500 에러 디버깅 (2시간)

#### Step 1: 에러 재현 및 로그 분석
```bash
# 백엔드 서버 시작 (로그 모니터링)
cd backend
export SPOONACULAR_API_KEY="1fe91ac5a2614fe985481f65a21ce6f6"
./gradlew bootRun --args='--spring.profiles.active=local' 2>&1 | tee /tmp/backend-debug.log

# 별도 터미널에서 에러 재현
curl -v http://localhost:8080/actuator/health
curl -v http://localhost:8080/api/admin/surveys/results

# 로그에서 스택트레이스 확인
grep -A 20 "Exception" /tmp/backend-debug.log
```

**체크포인트**:
- [ ] 에러 스택트레이스 확인
- [ ] 에러 원인 파악 (NullPointerException, Bean 초기화 실패 등)

#### Step 2: Actuator Health 엔드포인트 수정
**예상 원인**: Spring Boot Actuator 의존성 또는 설정 누락

**수정 방향**:
```yaml
# backend/src/main/resources/application-local.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

**검증**:
```bash
curl http://localhost:8080/actuator/health
# 예상 응답: {"status":"UP", ...}
```

#### Step 3: Survey Results API 수정
**예상 원인**: SurveyResultsDto 생성 시 null 값 처리 이슈

**파일**: `backend/src/main/java/foodiepass/server/survey/application/SurveyService.java`

**수정 방향**:
```java
public SurveyResultsDto getResults() {
    // null-safe 처리 추가
    Long controlYes = surveyRepository.countByAbGroupAndHasConfidence(ABGroup.CONTROL, true);
    if (controlYes == null) controlYes = 0L;
    // ... 나머지 필드도 동일

    return new SurveyResultsDto(
        controlYes != null ? controlYes : 0L,
        // ...
    );
}
```

**검증**:
```bash
curl http://localhost:8080/api/admin/surveys/results
# 예상 응답: {"controlYes":0, "controlNo":0, ...}
```

**완료 기준**:
- [ ] actuator/health 200 OK 반환
- [ ] surveys/results 200 OK 반환 (빈 데이터라도)

---

### 1.2 백그라운드 프로세스 정리 (30분)

#### Step 1: 모든 백그라운드 프로세스 종료
```bash
# Gradle 프로세스 종료
ps aux | grep gradle | grep -v grep | awk '{print $2}' | xargs kill -9

# Node/npm 프로세스 종료
ps aux | grep node | grep -v grep | awk '{print $2}' | xargs kill -9

# 포트 8080, 3000 사용 프로세스 종료
lsof -ti:8080 | xargs kill -9
lsof -ti:3000 | xargs kill -9
```

#### Step 2: 임시 파일 정리
```bash
# 로그 파일 삭제
rm -f /tmp/backend-*.log
rm -f /tmp/frontend-*.log

# 빌드 캐시 정리
cd backend && ./gradlew clean
cd frontend && rm -rf .next
```

**완료 기준**:
- [ ] 실행 중인 gradle 프로세스 0개
- [ ] 실행 중인 node 프로세스 0개 (IDE 제외)
- [ ] 포트 8080, 3000 사용 프로세스 0개

---

### 1.3 프론트엔드 실행 검증 (1시간)

#### Step 1: 프론트엔드 의존성 확인
```bash
cd frontend
npm install  # 누락된 의존성 설치
npm audit fix  # 보안 취약점 수정 (선택)
```

#### Step 2: 프론트엔드 서버 시작
```bash
npm run dev

# 예상 출력:
# ▲ Next.js 14.x.x
# - Local: http://localhost:3000
```

#### Step 3: 기본 페이지 동작 확인
```bash
# 브라우저에서 확인
open http://localhost:3000

# curl로 확인
curl -I http://localhost:3000
# 예상: HTTP/1.1 200 OK
```

**완료 기준**:
- [ ] 프론트엔드 서버 정상 시작
- [ ] localhost:3000 접속 가능
- [ ] 메뉴 업로드 UI 렌더링 확인

---

### 1.4 Git 상태 정리 (30분)

#### Step 1: 현재 브랜치 상태 확인
```bash
git status
git branch -vv
git log --oneline -10
```

#### Step 2: 문서 커밋 및 푸시
```bash
git add docs/IMPLEMENTATION_PLAN.md
git commit -m "docs: add detailed implementation plan"
git push origin feature/docs-refinement
```

#### Step 3: 머지된 브랜치 정리 (선택)
```bash
# develop에 머지된 로컬 브랜치 확인
git branch --merged develop

# 안전하게 삭제 가능한 브랜치 삭제
# git branch -d feature/local-integration-test
```

**완료 기준**:
- [ ] 작업 내용 커밋 완료
- [ ] feature/docs-refinement 푸시 완료
- [ ] 불필요한 로컬 브랜치 정리

---

### Phase 1 완료 체크리스트

- [ ] actuator/health API 정상 동작 (200 OK)
- [ ] surveys/results API 정상 동작 (200 OK)
- [ ] 백그라운드 프로세스 정리 완료
- [ ] 프론트엔드 서버 정상 실행
- [ ] Git 상태 정리 완료

---

## Phase 2: 통합 테스트 (Day 2-3)

### 목표
- 백엔드 ↔ 프론트엔드 통합 동작 검증
- Control/Treatment 분기 정확성 검증
- 전체 사용자 플로우 동작 확인

### 2.1 API 통합 테스트 (3시간)

#### Test Case 1: 메뉴 스캔 (Control 그룹)
```bash
# 테스트 이미지 준비
curl -X POST http://localhost:8080/api/menus/scan \
  -F "image=@test-menu.jpg" \
  -F "targetLanguage=KOREAN" \
  -F "targetCurrency=KRW"

# 응답 검증
# {
#   "scanId": "uuid",
#   "abGroup": "CONTROL",  ← 확인
#   "items": [
#     {
#       "name": "Pasta Carbonara",
#       "translatedName": "파스타 카르보나라",
#       "priceInTargetCurrency": 15000,
#       "imageUrl": null,  ← Control은 null
#       "description": null  ← Control은 null
#     }
#   ]
# }
```

#### Test Case 2: 메뉴 스캔 (Treatment 그룹)
```bash
# 여러 번 요청하여 Treatment 그룹 할당받을 때까지 반복
curl -X POST http://localhost:8080/api/menus/scan \
  -F "image=@test-menu.jpg" \
  -F "targetLanguage=KOREAN" \
  -F "targetCurrency=KRW"

# 응답 검증
# {
#   "scanId": "uuid",
#   "abGroup": "TREATMENT",  ← 확인
#   "items": [
#     {
#       "name": "Pasta Carbonara",
#       "translatedName": "파스타 카르보나라",
#       "priceInTargetCurrency": 15000,
#       "imageUrl": "https://...",  ← Treatment는 URL 있음
#       "description": "크림 소스 파스타..."  ← Treatment는 설명 있음
#     }
#   ]
# }
```

#### Test Case 3: 설문 제출
```bash
# Control 그룹 설문
curl -X POST http://localhost:8080/api/surveys \
  -H "Content-Type: application/json" \
  -d '{
    "scanId": "control-scan-id",
    "hasConfidence": false
  }'

# Treatment 그룹 설문
curl -X POST http://localhost:8080/api/surveys \
  -H "Content-Type: application/json" \
  -d '{
    "scanId": "treatment-scan-id",
    "hasConfidence": true
  }'
```

#### Test Case 4: A/B 테스트 결과 확인
```bash
curl http://localhost:8080/api/admin/ab-test/results

# 예상 응답:
# {
#   "controlCount": 1,
#   "treatmentCount": 1,
#   "totalScans": 2
# }

curl http://localhost:8080/api/admin/surveys/results

# 예상 응답:
# {
#   "controlYes": 0,
#   "controlNo": 1,
#   "treatmentYes": 1,
#   "treatmentNo": 0,
#   "controlTotal": 1,
#   "treatmentTotal": 1
# }
```

**완료 기준**:
- [ ] Control 그룹: imageUrl, description null 확인
- [ ] Treatment 그룹: imageUrl, description 값 있음 확인
- [ ] 설문 제출 200 OK
- [ ] A/B 테스트 결과 정확히 집계됨

---

### 2.2 프론트엔드 UI 테스트 (3시간)

#### Test Case 1: 메뉴 업로드 페이지
**URL**: http://localhost:3000

**검증 항목**:
- [ ] 파일 업로드 버튼 표시
- [ ] 언어 선택 드롭다운 (KOREAN, JAPANESE, ENGLISH 등)
- [ ] 화폐 선택 드롭다운 (KRW, JPY, USD 등)
- [ ] "스캔 시작" 버튼 클릭 가능

#### Test Case 2: Control UI (텍스트 번역만)
**시나리오**:
1. 메뉴판 이미지 업로드
2. 언어: KOREAN, 화폐: KRW 선택
3. 스캔 시작
4. (A/B 그룹이 CONTROL인 경우)

**검증 항목**:
- [ ] 메뉴 이름 표시 (원문 + 번역)
- [ ] 가격 표시 (환율 변환)
- [ ] ❌ 음식 사진 없음
- [ ] ❌ 음식 설명 없음
- [ ] 설문 표시: "이 정보만으로 주문할 자신이 있나요?"

#### Test Case 3: Treatment UI (시각적 메뉴)
**시나리오**:
1. 메뉴판 이미지 업로드
2. 언어: KOREAN, 화폐: KRW 선택
3. 스캔 시작
4. (A/B 그룹이 TREATMENT인 경우)

**검증 항목**:
- [ ] 메뉴 이름 표시 (원문 + 번역)
- [ ] 가격 표시 (환율 변환)
- [ ] ✅ 음식 사진 표시
- [ ] ✅ 음식 설명 표시
- [ ] 설문 표시: "이 정보만으로 주문할 자신이 있나요?"

#### Test Case 4: 설문 제출
**시나리오**:
1. "예" 또는 "아니오" 버튼 클릭
2. 제출 성공 메시지 표시

**검증 항목**:
- [ ] 버튼 클릭 시 API 호출 (Network 탭 확인)
- [ ] 제출 성공 메시지 표시
- [ ] 페이지 전환 또는 리셋

**완료 기준**:
- [ ] Control UI 동작 확인
- [ ] Treatment UI 동작 확인
- [ ] 설문 제출 동작 확인

---

### 2.3 전체 플로우 통합 테스트 (2시간)

#### End-to-End 플로우
```
1. 메뉴판 업로드
   ↓
2. 언어/화폐 선택
   ↓
3. 스캔 시작 (A/B 그룹 자동 배정)
   ↓
4. 결과 표시 (Control or Treatment)
   ↓
5. 설문 응답
   ↓
6. 완료
```

#### 테스트 데이터 준비
```bash
# 다양한 메뉴판 이미지 준비 (3-5개)
scripts/generate_test_menu_images.sh

# 예상 이미지:
# - test-menu-italian.jpg (영어 메뉴)
# - test-menu-japanese.jpg (일본어 메뉴)
# - test-menu-korean.jpg (한글 메뉴)
```

#### 반복 테스트 (10회)
```bash
# 스크립트 작성
cat > scripts/e2e_manual_test.sh << 'EOF'
#!/bin/bash
for i in {1..10}; do
  echo "=== Test Run $i ==="
  # 메뉴 스캔 요청
  response=$(curl -s -X POST http://localhost:8080/api/menus/scan \
    -F "image=@test-menu-italian.jpg" \
    -F "targetLanguage=KOREAN" \
    -F "targetCurrency=KRW")

  scan_id=$(echo $response | jq -r '.scanId')
  ab_group=$(echo $response | jq -r '.abGroup')

  echo "Scan ID: $scan_id, Group: $ab_group"

  # 설문 제출 (랜덤하게 yes/no)
  has_confidence=$((RANDOM % 2))
  curl -s -X POST http://localhost:8080/api/surveys \
    -H "Content-Type: application/json" \
    -d "{\"scanId\":\"$scan_id\",\"hasConfidence\":$has_confidence}"

  echo ""
done
EOF

chmod +x scripts/e2e_manual_test.sh
./scripts/e2e_manual_test.sh
```

#### 결과 검증
```bash
# A/B 테스트 결과 확인
curl http://localhost:8080/api/admin/ab-test/results

# 예상: controlCount + treatmentCount = 10

# 설문 결과 확인
curl http://localhost:8080/api/admin/surveys/results

# 예상: controlTotal + treatmentTotal = 10
```

**완료 기준**:
- [ ] 10회 테스트 모두 성공
- [ ] Control/Treatment 그룹 분배 비율 대략 50:50
- [ ] 설문 응답 모두 정상 집계

---

### Phase 2 완료 체크리스트

- [ ] 모든 API 엔드포인트 통합 테스트 통과
- [ ] Control UI 정상 동작
- [ ] Treatment UI 정상 동작
- [ ] 전체 E2E 플로우 10회 연속 성공

---

## Phase 3: 성능 최적화 (Day 4)

### 목표
- 메뉴 스캔 처리 시간 5초 이내 달성
- 병목 지점 식별 및 개선

### 3.1 성능 측정 (1시간)

#### Step 1: 각 단계별 소요 시간 측정
```java
// backend/src/main/java/foodiepass/server/menu/application/MenuService.java
public MenuScanResultDto scanMenu(MultipartFile image, Language target, Currency currency) {
    long startTime = System.currentTimeMillis();

    // OCR
    long ocrStart = System.currentTimeMillis();
    List<MenuItem> items = ocrReader.readMenu(image, target);
    log.info("OCR took: {}ms", System.currentTimeMillis() - ocrStart);

    // Translation
    long translationStart = System.currentTimeMillis();
    translateItems(items, target);
    log.info("Translation took: {}ms", System.currentTimeMillis() - translationStart);

    // Currency
    long currencyStart = System.currentTimeMillis();
    convertCurrency(items, currency);
    log.info("Currency conversion took: {}ms", System.currentTimeMillis() - currencyStart);

    // Food Scraping (Treatment only)
    long scrapingStart = System.currentTimeMillis();
    if (abGroup == ABGroup.TREATMENT) {
        enrichWithFoodInfo(items);
    }
    log.info("Food scraping took: {}ms", System.currentTimeMillis() - scrapingStart);

    log.info("Total processing time: {}ms", System.currentTimeMillis() - startTime);
}
```

#### Step 2: 테스트 실행 및 로그 분석
```bash
# 테스트 실행
curl -X POST http://localhost:8080/api/menus/scan \
  -F "image=@test-menu-italian.jpg" \
  -F "targetLanguage=KOREAN" \
  -F "targetCurrency=KRW"

# 로그 확인
grep "took:" /tmp/backend-debug.log

# 예상 출력:
# OCR took: 2000ms
# Translation took: 500ms
# Currency conversion took: 100ms
# Food scraping took: 3000ms  ← 병목 가능성 높음
# Total processing time: 5600ms  ← 목표 초과
```

**완료 기준**:
- [ ] 각 단계별 소요 시간 측정 완료
- [ ] 병목 지점 식별 (가장 오래 걸리는 단계)

---

### 3.2 병목 최적화 (3시간)

#### 최적화 1: 병렬 처리
**문제**: OCR, 번역, 환율 변환이 순차적으로 실행

**해결**:
```java
// 환율 조회와 OCR을 병렬로 실행
CompletableFuture<List<MenuItem>> ocrFuture =
    CompletableFuture.supplyAsync(() -> ocrReader.readMenu(image, target));

CompletableFuture<Double> rateFuture =
    CompletableFuture.supplyAsync(() -> currencyService.getRate(currency));

CompletableFuture.allOf(ocrFuture, rateFuture).join();

List<MenuItem> items = ocrFuture.get();
Double rate = rateFuture.get();
```

#### 최적화 2: Food Scraping 캐싱
**문제**: 동일한 음식명에 대해 반복 스크래핑

**해결**:
```java
@Cacheable(value = "foodInfo", key = "#foodName")
public FoodInfo getFoodInfo(String foodName) {
    return tasteAtlasScraper.scrape(foodName);
}
```

**캐시 설정**:
```yaml
# application-local.yml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h
```

#### 최적화 3: Timeout 설정
**문제**: Food scraping이 무한정 대기

**해결**:
```java
@Service
public class TasteAtlasFoodScrapper {
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    public FoodInfo scrape(String foodName) {
        try {
            return Jsoup.connect(url)
                .timeout((int) TIMEOUT.toMillis())
                .get();
        } catch (SocketTimeoutException e) {
            log.warn("Timeout scraping {}, returning empty", foodName);
            return FoodInfo.empty();
        }
    }
}
```

**완료 기준**:
- [ ] 병렬 처리 적용
- [ ] Food scraping 캐싱 적용
- [ ] Timeout 설정 적용
- [ ] 전체 처리 시간 5초 이내 달성

---

### 3.3 성능 재측정 (1시간)

#### Step 1: 최적화 후 재측정
```bash
# 10회 연속 테스트
for i in {1..10}; do
  echo "=== Test $i ==="
  time curl -X POST http://localhost:8080/api/menus/scan \
    -F "image=@test-menu-italian.jpg" \
    -F "targetLanguage=KOREAN" \
    -F "targetCurrency=KRW"
done | grep "Total processing time"
```

#### Step 2: 결과 분석
```
예상 결과 (최적화 후):
- OCR took: 2000ms
- Translation took: 500ms (병렬 실행)
- Currency conversion took: 100ms (병렬 실행)
- Food scraping took: 1500ms (캐싱 + 병렬 + timeout)
- Total processing time: 3500ms ✅ (5초 이내)
```

**완료 기준**:
- [ ] 평균 처리 시간 ≤ 5초
- [ ] 95 percentile 처리 시간 ≤ 7초
- [ ] 최악의 경우 ≤ 10초

---

### Phase 3 완료 체크리스트

- [ ] 성능 측정 로직 추가
- [ ] 병목 지점 최적화 (병렬, 캐싱, timeout)
- [ ] 처리 시간 5초 이내 목표 달성
- [ ] 성능 테스트 결과 문서화

---

## Phase 4: E2E 자동화 테스트 (Day 5)

### 목표
- Playwright를 이용한 E2E 테스트 자동화
- CI/CD 파이프라인에 통합 가능한 테스트 스크립트

### 4.1 Playwright 설정 (1시간)

#### Step 1: Playwright 설치
```bash
cd frontend
npm install --save-dev @playwright/test
npx playwright install
```

#### Step 2: 설정 파일 작성
```typescript
// frontend/playwright.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests/e2e',
  use: {
    baseURL: 'http://localhost:3000',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  webServer: {
    command: 'npm run dev',
    port: 3000,
    reuseExistingServer: true,
  },
});
```

**완료 기준**:
- [ ] Playwright 설치 완료
- [ ] 설정 파일 작성 완료

---

### 4.2 E2E 테스트 작성 (3시간)

#### Test 1: Control 그룹 플로우
```typescript
// frontend/tests/e2e/control-flow.spec.ts
import { test, expect } from '@playwright/test';

test('Control group full flow', async ({ page }) => {
  // 1. 메인 페이지 접속
  await page.goto('/');

  // 2. 파일 업로드
  const fileInput = await page.locator('input[type="file"]');
  await fileInput.setInputFiles('tests/fixtures/test-menu-italian.jpg');

  // 3. 언어/화폐 선택
  await page.selectOption('select[name="language"]', 'KOREAN');
  await page.selectOption('select[name="currency"]', 'KRW');

  // 4. 스캔 시작
  await page.click('button:has-text("스캔 시작")');

  // 5. 로딩 표시 확인
  await expect(page.locator('text=메뉴를 분석 중입니다')).toBeVisible();

  // 6. 결과 표시 대기 (최대 10초)
  await page.waitForSelector('[data-testid="menu-results"]', { timeout: 10000 });

  // 7. Control UI 검증
  const menuItem = page.locator('[data-testid="menu-item"]').first();
  await expect(menuItem).toBeVisible();

  // 메뉴 이름 표시
  await expect(menuItem.locator('[data-testid="menu-name"]')).toBeVisible();

  // 음식 사진 없음
  await expect(menuItem.locator('[data-testid="food-image"]')).not.toBeVisible();

  // 음식 설명 없음
  await expect(menuItem.locator('[data-testid="food-description"]')).not.toBeVisible();

  // 8. 설문 응답
  await page.click('button:has-text("아니오")');

  // 9. 완료 메시지
  await expect(page.locator('text=응답이 제출되었습니다')).toBeVisible();
});
```

#### Test 2: Treatment 그룹 플로우
```typescript
// frontend/tests/e2e/treatment-flow.spec.ts
import { test, expect } from '@playwright/test';

test('Treatment group full flow', async ({ page }) => {
  // ... Control과 동일한 1-6단계 ...

  // 7. Treatment UI 검증
  const menuItem = page.locator('[data-testid="menu-item"]').first();

  // 음식 사진 있음
  await expect(menuItem.locator('[data-testid="food-image"]')).toBeVisible();
  const imageUrl = await menuItem.locator('[data-testid="food-image"]').getAttribute('src');
  expect(imageUrl).toBeTruthy();

  // 음식 설명 있음
  await expect(menuItem.locator('[data-testid="food-description"]')).toBeVisible();
  const description = await menuItem.locator('[data-testid="food-description"]').textContent();
  expect(description).toBeTruthy();

  // 8. 설문 응답
  await page.click('button:has-text("예")');

  // 9. 완료 메시지
  await expect(page.locator('text=응답이 제출되었습니다')).toBeVisible();
});
```

#### Test 3: A/B 그룹 분배 검증
```typescript
// frontend/tests/e2e/ab-test-distribution.spec.ts
import { test, expect } from '@playwright/test';

test('A/B group distribution is roughly 50/50', async ({ page }) => {
  const results = { control: 0, treatment: 0 };

  // 20회 반복 테스트
  for (let i = 0; i < 20; i++) {
    await page.goto('/');
    await page.locator('input[type="file"]').setInputFiles('tests/fixtures/test-menu.jpg');
    await page.selectOption('select[name="language"]', 'KOREAN');
    await page.selectOption('select[name="currency"]', 'KRW');
    await page.click('button:has-text("스캔 시작")');

    await page.waitForSelector('[data-testid="menu-results"]', { timeout: 10000 });

    // Control인지 Treatment인지 판단
    const hasFoodImage = await page.locator('[data-testid="food-image"]').isVisible();
    if (hasFoodImage) {
      results.treatment++;
    } else {
      results.control++;
    }

    // 리셋
    await page.goto('/');
  }

  // 분배 비율 검증 (40-60% 범위)
  expect(results.control).toBeGreaterThanOrEqual(8);
  expect(results.control).toBeLessThanOrEqual(12);
  expect(results.treatment).toBeGreaterThanOrEqual(8);
  expect(results.treatment).toBeLessThanOrEqual(12);

  console.log(`Control: ${results.control}, Treatment: ${results.treatment}`);
});
```

**완료 기준**:
- [ ] Control 플로우 테스트 작성
- [ ] Treatment 플로우 테스트 작성
- [ ] A/B 분배 테스트 작성

---

### 4.3 테스트 실행 및 결과 확인 (1시간)

```bash
# 백엔드 서버 시작 (별도 터미널)
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'

# E2E 테스트 실행
cd frontend
npx playwright test

# 특정 테스트만 실행
npx playwright test control-flow

# UI 모드로 실행 (디버깅)
npx playwright test --ui

# 리포트 확인
npx playwright show-report
```

**완료 기준**:
- [ ] 모든 E2E 테스트 통과
- [ ] 테스트 실행 시간 ≤ 5분
- [ ] 테스트 리포트 생성 확인

---

### Phase 4 완료 체크리스트

- [ ] Playwright 설정 완료
- [ ] E2E 테스트 3개 이상 작성
- [ ] 모든 테스트 통과
- [ ] CI/CD 통합 준비 완료

---

## Phase 5: 배포 준비 (Day 6-7)

### 목표
- 스테이징 환경 구성
- 환경변수 관리
- 배포 스크립트 작성

### 5.1 인프라 설정 (4시간)

#### AWS 리소스 생성
```bash
# 1. RDS (MySQL) 생성
aws rds create-db-instance \
  --db-instance-identifier foodiepass-staging \
  --engine mysql \
  --engine-version 8.0 \
  --db-instance-class db.t3.micro \
  --allocated-storage 20 \
  --master-username admin \
  --master-user-password SECURE_PASSWORD

# 2. EC2 인스턴스 생성 (백엔드)
aws ec2 run-instances \
  --image-id ami-xxx \
  --instance-type t3.small \
  --key-name foodiepass-key \
  --security-group-ids sg-xxx

# 3. S3 버킷 생성 (프론트엔드)
aws s3 mb s3://foodiepass-frontend-staging
```

#### 환경변수 설정
```bash
# EC2에서
cat > /home/ec2-user/.env << EOF
SPOONACULAR_API_KEY=xxx
GEMINI_API_KEY=xxx
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
DB_HOST=foodiepass-staging.xxx.rds.amazonaws.com
DB_PASSWORD=SECURE_PASSWORD
EOF
```

**완료 기준**:
- [ ] RDS 인스턴스 생성 완료
- [ ] EC2 인스턴스 생성 완료
- [ ] S3 버킷 생성 완료
- [ ] 환경변수 설정 완료

---

### 5.2 배포 스크립트 작성 (3시간)

#### 백엔드 배포 스크립트
```bash
# scripts/deploy-backend.sh
#!/bin/bash
set -e

echo "=== FoodiePass Backend Deployment ==="

# 1. 빌드
cd backend
./gradlew clean build -x test

# 2. JAR 파일 EC2로 전송
scp -i foodiepass-key.pem \
  build/libs/server-0.0.1-SNAPSHOT.jar \
  ec2-user@EC2_IP:/home/ec2-user/app.jar

# 3. 환경변수 전송
scp -i foodiepass-key.pem \
  .env \
  ec2-user@EC2_IP:/home/ec2-user/.env

# 4. 서비스 재시작
ssh -i foodiepass-key.pem ec2-user@EC2_IP << 'EOF'
  sudo systemctl stop foodiepass
  sudo systemctl start foodiepass
  sudo systemctl status foodiepass
EOF

echo "=== Backend deployment complete ==="
```

#### 프론트엔드 배포 스크립트
```bash
# scripts/deploy-frontend.sh
#!/bin/bash
set -e

echo "=== FoodiePass Frontend Deployment ==="

# 1. 빌드
cd frontend
npm run build

# 2. S3로 업로드
aws s3 sync .next/static s3://foodiepass-frontend-staging/static
aws s3 sync public s3://foodiepass-frontend-staging/public

# 3. CloudFront 캐시 무효화
aws cloudfront create-invalidation \
  --distribution-id DISTRIBUTION_ID \
  --paths "/*"

echo "=== Frontend deployment complete ==="
```

**완료 기준**:
- [ ] 백엔드 배포 스크립트 작성
- [ ] 프론트엔드 배포 스크립트 작성
- [ ] 스크립트 실행 권한 설정 (chmod +x)

---

### 5.3 스테이징 환경 테스트 (3시간)

#### Health Check
```bash
# 백엔드
curl https://api-staging.foodiepass.com/actuator/health

# 프론트엔드
curl -I https://staging.foodiepass.com
```

#### E2E 테스트 (스테이징)
```bash
# Playwright 설정 변경
export BASE_URL=https://staging.foodiepass.com
npx playwright test
```

**완료 기준**:
- [ ] 스테이징 환경 접속 가능
- [ ] Health check 200 OK
- [ ] E2E 테스트 통과 (스테이징 환경)

---

### Phase 5 완료 체크리스트

- [ ] AWS 인프라 구성 완료
- [ ] 배포 스크립트 작성 완료
- [ ] 스테이징 환경 정상 동작 확인
- [ ] 배포 가이드 문서 작성

---

## Phase 6: 사용자 테스트 (Day 8-10)

### 목표
- 실제 사용자 20명 테스트
- H1, H2, H3 가설 검증 데이터 수집

### 6.1 테스트 준비 (1일)

#### 테스트 참가자 모집
- [ ] 해외 여행 경험 있는 사용자 20명
- [ ] 다양한 연령대 (20-50대)
- [ ] 외국어 능력 다양 (초급~고급)

#### 테스트 시나리오 작성
```markdown
# 사용자 테스트 시나리오

## 배경 설정
당신은 이탈리아 로마의 레스토랑에 와 있습니다.
메뉴판이 이탈리아어로만 작성되어 있어 무엇을 주문할지 고민입니다.

## 작업
1. 제공된 메뉴판 사진을 FoodiePass에 업로드하세요
2. 언어: 한국어, 화폐: 원화(KRW)로 설정하세요
3. 스캔 결과를 확인하세요
4. 설문에 답하세요: "이 정보만으로 주문할 자신이 있나요?"

## 질문
- 어떤 정보가 도움이 되었나요?
- 더 필요한 정보가 있나요?
- 실제 여행 시 이 서비스를 사용하시겠습니까?
```

**완료 기준**:
- [ ] 테스트 참가자 20명 확보
- [ ] 테스트 시나리오 작성 완료
- [ ] 테스트 메뉴판 이미지 3종 준비

---

### 6.2 사용자 테스트 실행 (2일)

#### 진행 방식
- 개별 1:1 세션 (20-30분)
- 화면 공유 및 관찰
- Think-aloud 프로토콜 (생각을 말하며 진행)

#### 데이터 수집
```bash
# A/B 테스트 결과 확인
curl https://api-staging.foodiepass.com/api/admin/ab-test/results

# 설문 결과 확인
curl https://api-staging.foodiepass.com/api/admin/surveys/results
```

**완료 기준**:
- [ ] 20명 테스트 완료
- [ ] A/B 그룹 각 10명씩 배정
- [ ] 설문 응답 100% 수집

---

### 6.3 데이터 분석 및 가설 검증 (1일)

#### H2: 기술 실현 가설 검증
```bash
# 성능 로그 분석
grep "Total processing time" /var/log/foodiepass.log | awk '{sum+=$4; count++} END {print "Average:", sum/count, "ms"}'

# 목표: 평균 ≤ 5000ms
```

**검증 기준**:
- [ ] OCR 정확도 ≥ 90%
- [ ] 환율 정확도 ≥ 95%
- [ ] 음식 매칭 연관성 ≥ 70% (수동 평가)
- [ ] 처리 시간 ≤ 5초

#### H1 & H3: 사용자 행동 가설 검증
```python
# scripts/analyze_results.py
import requests

# 데이터 가져오기
results = requests.get('https://api-staging.foodiepass.com/api/admin/surveys/results').json()

control_yes_rate = results['controlYes'] / results['controlTotal']
treatment_yes_rate = results['treatmentYes'] / results['treatmentTotal']

ratio = treatment_yes_rate / control_yes_rate

print(f"Control Yes Rate: {control_yes_rate:.2%}")
print(f"Treatment Yes Rate: {treatment_yes_rate:.2%}")
print(f"Ratio: {ratio:.2f}x")

# 검증
assert treatment_yes_rate >= 0.7, "H1 failed: Treatment < 70%"
assert ratio >= 2.0, "H3 failed: Ratio < 2.0"

print("✅ H1, H3 validated!")
```

**검증 기준**:
- [ ] Treatment 그룹 Yes 응답률 ≥ 70% (H1)
- [ ] Treatment / Control Yes 비율 ≥ 2.0 (H3)

---

### Phase 6 완료 체크리스트

- [ ] 20명 사용자 테스트 완료
- [ ] H1, H2, H3 가설 검증 완료
- [ ] 결과 리포트 작성
- [ ] 다음 단계 계획 수립

---

## 전체 프로젝트 완료 체크리스트

### Phase 1: 긴급 수정
- [ ] API 500 에러 수정
- [ ] 백그라운드 프로세스 정리
- [ ] 프론트엔드 실행 검증

### Phase 2: 통합 테스트
- [ ] API 통합 테스트
- [ ] 프론트엔드 UI 테스트
- [ ] E2E 수동 테스트

### Phase 3: 성능 최적화
- [ ] 성능 측정
- [ ] 병목 최적화
- [ ] 5초 목표 달성

### Phase 4: E2E 자동화
- [ ] Playwright 설정
- [ ] 테스트 작성
- [ ] CI/CD 통합

### Phase 5: 배포 준비
- [ ] AWS 인프라
- [ ] 배포 스크립트
- [ ] 스테이징 환경

### Phase 6: 사용자 테스트
- [ ] 20명 테스트
- [ ] 가설 검증
- [ ] 결과 분석

---

## 다음 단계 (MVP 이후)

### 가설 검증 성공 시
1. **제품 확장**
   - 더 많은 언어 지원
   - 더 많은 음식 DB 구축
   - 알레르기 정보 추가

2. **비즈니스 모델**
   - 프리미엄 기능 (무제한 스캔)
   - 레스토랑 파트너십
   - 여행사 제휴

3. **기술 개선**
   - AI 모델 자체 학습
   - 실시간 번역
   - 오프라인 모드

### 가설 검증 실패 시
1. **피봇 방향**
   - 다른 use case 탐색
   - 다른 타겟 사용자
   - 다른 가치 제안

2. **기술 R&D**
   - OCR 정확도 개선
   - 음식 매칭 알고리즘 개선

---

**문서 작성일**: 2025-11-09
**다음 리뷰**: Phase 1 완료 후 (예상: 2025-11-10)
**최종 업데이트**: 각 Phase 완료 시
