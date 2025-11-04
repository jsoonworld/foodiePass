# Worktree: Menu API Integration

**Branch**: `feature/mvp-menu-api`

**Focus**: 기존 Menu 파이프라인과 A/B 테스트/설문 시스템 통합

---

## 🎯 Goal

기존 MenuService를 활용하여 새로운 `/api/menus/scan` 엔드포인트를 구현하고, A/B 그룹에 따라 조건부 응답을 반환

---

## 📁 Working Directory

**Primary**: `backend/src/main/java/foodiepass/server/menu/api/`

**Test**: `backend/src/test/java/foodiepass/server/menu/api/`

---

## 🚀 Tasks

### 1. DTO Layer (New)
- [ ] `dto/request/MenuScanRequest.java`
  - `image` (Base64 String)
  - `sourceLanguage` (String)
  - `targetLanguage` (String)
  - `sourceCurrency` (String)
  - `targetCurrency` (String)

- [ ] `dto/response/MenuScanResponse.java`
  - `scanId` (UUID)
  - `abGroup` (String: "CONTROL" | "TREATMENT")
  - `items` (List<MenuItemDto>)
  - `processingTime` (Double, seconds)

- [ ] `dto/MenuItemDto.java`
  - `originalName` (String)
  - `translatedName` (String)
  - `description` (String, Treatment만)
  - `imageUrl` (String, Treatment만)
  - `priceInfo` (PriceInfoDto)

### 2. Service Layer (Integration)
- [ ] `MenuScanService.java` (NEW)
  - `scanMenu(MenuScanRequest, String userId)`: 전체 파이프라인 조율
    1. A/B 그룹 배정 (ABTestService 호출)
    2. MenuScan 생성
    3. OCR + Enrichment (기존 MenuService 재사용)
    4. 조건부 필터링 (Control: FoodInfo 제거)
    5. DTO 변환 및 응답

### 3. API Layer (New Endpoint)
- [ ] `MenuScanController.java` (NEW)
  - `POST /api/menus/scan`: 메뉴판 스캔
  - Session ID 또는 Cookie로 userId 관리

### 4. Integration Logic
- [ ] A/B 그룹별 조건부 처리:
  - **CONTROL**: `translatedName`, `priceInfo`만 포함 (FoodInfo 제거)
  - **TREATMENT**: 전체 FoodInfo 포함 (`description`, `imageUrl`)

### 5. Tests
- [ ] `MenuScanServiceTest.java`
  - Control 그룹: FoodInfo 제거 검증
  - Treatment 그룹: FoodInfo 포함 검증
  - 처리 시간 ≤5초 검증
- [ ] `MenuScanControllerTest.java`
  - API 엔드포인트 통합 테스트
  - 에러 케이스 (잘못된 이미지, 언어/통화 등)

---

## 📋 Acceptance Criteria

### H1, H2 검증을 위한 요구사항:
- ✅ OCR 정확도 ≥90% (기존 MenuService 재사용)
- ✅ 환율 정확도 ≥95% (기존 CurrencyService 재사용)
- ✅ 음식 매칭 연관성 ≥70% (기존 FoodScrapper 재사용)
- ✅ 처리 시간 ≤5초 (비동기 처리 유지)
- ✅ Control vs Treatment 조건부 응답
- ✅ >80% 테스트 커버리지

### Success Metrics:
- 응답 시간: <5초 (95th percentile)
- 에러율: <5%
- A/B 그룹 배정 + 파이프라인 실행 완료

---

## 🔗 Dependencies

**기존 모듈 (재사용)**:
- `MenuService`: reconfigure() 메서드 재사용
- `MenuItemEnricher`: 번역 + 스크래핑 + 환율 통합
- `CurrencyService`: 환율 변환
- `LanguageService`: 언어 지원 확인

**새 모듈 (통합)**:
- `ABTestService` (abtest 모듈): assignGroup(), createScan()
- `MenuScan` (abtest 도메인): scanId, abGroup

**External**:
- Spring Web (REST)
- Spring Session (userId 관리)
- Reactor (비동기)

---

## 🧪 How to Run

```bash
cd backend

# Run tests
./gradlew test --tests "foodiepass.server.menu.*"

# Run app (local profile)
./gradlew bootRun --args='--spring.profiles.active=local'

# Test API manually
curl -X POST http://localhost:8080/api/menus/scan \
  -H "Content-Type: application/json" \
  -d '{
    "image": "base64_encoded_string",
    "sourceLanguage": "auto",
    "targetLanguage": "ko",
    "sourceCurrency": "USD",
    "targetCurrency": "KRW"
  }'

# Check coverage
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## 📚 Documentation References

- [IMPLEMENTATION_PLAN.md](backend/docs/IMPLEMENTATION_PLAN.md) - Agent 3 섹션 참조
- [API_SPEC.md](backend/docs/API_SPEC.md) - `/api/menus/scan` 명세
- [CODE_REUSE_GUIDE.md](backend/docs/CODE_REUSE_GUIDE.md) - 기존 코드 재사용 가이드
- [Agent 3 Spec](backend/.claude/agents/agent-3-integration-spec.md) - 상세 구현 스펙

---

## 🔄 Processing Flow

```
1. POST /api/menus/scan
2. Extract userId (session/cookie)
3. ABTestService.assignGroup(userId) → ABGroup
4. ABTestService.createScan(...) → MenuScan
5. MenuService.reconfigure(image, languages, currencies)
   → Mono<List<MenuItem>>
6. Filter based on ABGroup:
   - CONTROL: Remove description, imageUrl
   - TREATMENT: Keep all fields
7. Convert to MenuScanResponse
8. Return response with scanId, abGroup, items
```

---

## 🎨 Response Examples

### Control Group
```json
{
  "scanId": "uuid",
  "abGroup": "CONTROL",
  "items": [
    {
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "priceInfo": {
        "original": "$15.00",
        "converted": "₩20,000"
      }
    }
  ],
  "processingTime": 4.2
}
```

### Treatment Group
```json
{
  "scanId": "uuid",
  "abGroup": "TREATMENT",
  "items": [
    {
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "description": "토마토 소스, 모짜렐라 치즈, 바질",
      "imageUrl": "https://tasteatlas.com/...",
      "priceInfo": {
        "original": "$15.00",
        "converted": "₩20,000"
      }
    }
  ],
  "processingTime": 4.5
}
```

---

## ✅ When You're Done

1. 모든 테스트 통과 확인 (단위 + 통합)
2. Coverage ≥80% 확인
3. 처리 시간 ≤5초 검증 (성능 테스트)
4. Control vs Treatment 응답 차이 검증
5. Commit with message: `feat(menu): integrate A/B test with menu scan pipeline`
6. Push to `feature/mvp-menu-api`
7. 다른 worktree의 작업이 끝나면 통합 테스트 진행

---

## 🚨 DO NOT

- ❌ 기존 MenuService, MenuItemEnricher 수정하지 말 것 (재사용만)
- ❌ ABTest, Survey 모듈 수정하지 말 것 (다른 worktree의 책임)
- ❌ 5초 제한 무시하지 말 것 (비동기 처리 유지 필수)
- ❌ develop 브랜치에 직접 커밋하지 말 것

---

## 💡 Tips

- 기존 MenuService.reconfigure()는 이미 비동기(Reactor Mono) → 그대로 활용
- Session ID 관리: Spring Session 또는 Cookie 활용
- FoodInfo 필터링: DTO 변환 시점에 조건부 처리
- 처리 시간 측정: `Instant.now()` before/after
- Error handling: 외부 API 실패 시 graceful degradation (부분 응답)
