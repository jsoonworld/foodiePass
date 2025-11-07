# 통합 테스트 마스터 플랜

> **목표**: 실제 API를 사용하여 전체 파이프라인이 동작하는지 검증

## 🎯 최종 목표

메뉴판 사진 업로드 → OCR → 번역 → 음식 매칭 → 환율 변환 → A/B 분기
**전체 플로우가 실제로 동작하는지 확인**

---

## 📋 Phase 1: Google Cloud 설정 (로컬 환경)

### 목표
- Google Cloud 프로젝트 생성
- Vertex AI API 활성화
- 서비스 계정 키 생성
- 로컬 환경 변수 설정

### 단계

#### 1.1 Google Cloud 프로젝트 생성
```bash
# 웹 콘솔에서 진행: https://console.cloud.google.com/

1. "새 프로젝트" 클릭
2. 프로젝트 이름: "foodiepass-mvp" 또는 원하는 이름
3. 프로젝트 ID 복사 (예: foodiepass-mvp-123456)
```

#### 1.2 Vertex AI API 활성화
```bash
# 웹 콘솔에서 진행

1. 프로젝트 선택
2. "API 및 서비스" → "라이브러리"
3. 다음 API 검색 및 활성화:
   - Vertex AI API
   - Cloud Translation API
   - (선택) Cloud Vision API (백업용)
```

#### 1.3 서비스 계정 생성 및 키 다운로드
```bash
# 웹 콘솔 또는 gcloud CLI

# 방법 1: 웹 콘솔
1. "IAM 및 관리자" → "서비스 계정"
2. "서비스 계정 만들기"
3. 이름: "foodiepass-sa"
4. 역할 부여:
   - Vertex AI User
   - Cloud Translation API User
5. "키 만들기" → JSON 선택
6. 다운로드된 키를 안전한 위치에 저장

# 방법 2: gcloud CLI (선택)
gcloud iam service-accounts create foodiepass-sa \
  --display-name="FoodiePass Service Account"

gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
  --member="serviceAccount:foodiepass-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/aiplatform.user"

gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
  --member="serviceAccount:foodiepass-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/cloudtranslate.user"

gcloud iam service-accounts keys create ~/foodiepass-credentials.json \
  --iam-account=foodiepass-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com
```

#### 1.4 로컬 환경 변수 설정
```bash
# backend/.env 파일 생성
cat > .env << 'EOF'
# Google Cloud
GOOGLE_CREDENTIALS_PATH=/Users/YOUR_USERNAME/foodiepass-credentials.json
GCP_PROJECT_ID=your-project-id
GCP_LOCATION=us-central1
GEMINI_MODEL_VISION=gemini-1.5-flash
GEMINI_MODEL_PRO=gemini-1.5-pro

# TasteAtlas
TASTE_ATLAS_API_URL=https://www.tasteatlas.com/api/search
TASTE_ATLAS_AUTH_TOKEN=
TASTE_ATLAS_BASE_URL=https://www.tasteatlas.com
TASTE_ATLAS_DEFAULT_IMAGE_URL=https://via.placeholder.com/400
TASTE_ATLAS_DEFAULT_DESCRIPTION=음식 정보를 불러올 수 없습니다
TASTE_ATLAS_SELECTOR_IMAGE=img.food-image
TASTE_ATLAS_SELECTOR_DESCRIPTION=div.description

# Google Finance
GOOGLE_FINANCE_URL_FORMAT=https://www.google.com/finance/quote/%s-%s
GOOGLE_FINANCE_SELECTOR=div.YMlKec.fxKbKc
EOF

# .env 파일 로드 (터미널에서)
export $(cat .env | xargs)
```

### 검증
```bash
# 환경 변수 확인
echo $GOOGLE_CREDENTIALS_PATH
echo $GCP_PROJECT_ID

# 파일 존재 확인
ls -l $GOOGLE_CREDENTIALS_PATH
```

**✅ Phase 1 완료 조건**: 모든 환경 변수가 설정되고 credentials.json 파일 존재

---

## 📋 Phase 2: 개별 API 단위 테스트

### 목표
- 각 외부 API가 독립적으로 동작하는지 검증
- 문제 발생 시 어느 API에서 실패하는지 명확히 파악

### 2.1 Google Gemini OCR 테스트

#### 테스트 파일 생성
```java
// src/test/java/foodiepass/server/menu/infra/GeminiOcrIntegrationTest.java

package foodiepass.server.menu.infra;

import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Gemini OCR 통합 테스트")
class GeminiOcrIntegrationTest {

    @Autowired
    private OcrReader ocrReader;

    @Test
    @DisplayName("실제 메뉴판 이미지에서 텍스트 추출")
    void extractTextFromRealMenuImage() throws Exception {
        // Given: 실제 메뉴판 이미지 (테스트용 샘플)
        // TODO: src/test/resources/test-menu.jpg 파일 추가 필요
        byte[] imageBytes = Files.readAllBytes(
            Paths.get("src/test/resources/test-menu.jpg")
        );
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // When: OCR 실행
        List<MenuItem> menuItems = ocrReader.read(base64Image);

        // Then: 검증
        assertThat(menuItems).isNotEmpty();
        assertThat(menuItems.get(0).getName()).isNotBlank();
        assertThat(menuItems.get(0).getPrice()).isNotNull();

        // 결과 출력 (디버깅용)
        System.out.println("=== OCR 결과 ===");
        menuItems.forEach(item -> {
            System.out.println("이름: " + item.getName());
            System.out.println("가격: " + item.getPrice());
        });
    }

    @Test
    @DisplayName("간단한 텍스트 이미지 OCR 테스트")
    void ocrSimpleTextImage() {
        // Given: 간단한 텍스트 이미지 (직접 생성 또는 준비)
        // 예: "Sushi - $10" 같은 간단한 텍스트

        // When & Then
        // 실제 구현
    }
}
```

#### 실행 및 검증
```bash
# 1. 테스트 이미지 준비
mkdir -p src/test/resources
# 실제 일본 메뉴판 사진 또는 테스트 이미지를 test-menu.jpg로 저장

# 2. 테스트 실행
./gradlew test --tests GeminiOcrIntegrationTest

# 3. 결과 확인
# - OCR이 메뉴 이름과 가격을 제대로 추출했는지 확인
# - 출력된 결과가 의미 있는지 확인
```

**✅ 성공 조건**:
- 메뉴 이름이 정확하게 추출됨
- 가격이 숫자로 파싱됨
- 테스트 통과

**❌ 실패 시 조치**:
1. API 키 확인
2. Vertex AI API 활성화 확인
3. 프로젝트 ID 및 리전 확인
4. 에러 메시지 분석

---

### 2.2 Google Translation API 테스트

#### 테스트 파일 생성
```java
// src/test/java/foodiepass/server/language/infra/GoogleTranslationIntegrationTest.java

package foodiepass.server.language.infra;

import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.TranslationClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Google Translation 통합 테스트")
class GoogleTranslationIntegrationTest {

    @Autowired
    private TranslationClient translationClient;

    @Test
    @DisplayName("일본어 → 영어 번역")
    void translateJapaneseToEnglish() {
        // Given
        String japanese = "寿司";
        Language source = Language.JAPANESE;
        Language target = Language.ENGLISH;

        // When
        var result = translationClient.translateAsync(source, target, japanese);

        // Then
        StepVerifier.create(result)
            .assertNext(translated -> {
                assertThat(translated).isNotBlank();
                assertThat(translated.toLowerCase()).contains("sushi");
                System.out.println("번역 결과: " + japanese + " → " + translated);
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("영어 → 한국어 번역")
    void translateEnglishToKorean() {
        // Given
        String english = "Fresh raw fish with rice";
        Language source = Language.ENGLISH;
        Language target = Language.KOREAN;

        // When
        var result = translationClient.translateAsync(source, target, english);

        // Then
        StepVerifier.create(result)
            .assertNext(translated -> {
                assertThat(translated).isNotBlank();
                assertThat(translated).contains("생선");
                System.out.println("번역 결과: " + english + " → " + translated);
            })
            .verifyComplete();
    }
}
```

#### 실행 및 검증
```bash
./gradlew test --tests GoogleTranslationIntegrationTest
```

**✅ 성공 조건**:
- 번역이 정확하게 수행됨
- 결과가 의미 있는 번역임

---

### 2.3 TasteAtlas 스크래핑 테스트

#### 테스트 파일 생성
```java
// src/test/java/foodiepass/server/menu/infra/TasteAtlasFoodScrapperIntegrationTest.java

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("TasteAtlas 스크래핑 통합 테스트")
class TasteAtlasFoodScrapperIntegrationTest {

    @Autowired
    @Qualifier("tasteAtlasFoodScrapper")
    private FoodScrapper foodScrapper;

    @Test
    @DisplayName("실제 음식 정보 스크래핑 - Sushi")
    void scrapSushiInfo() {
        // Given
        List<String> foodNames = List.of("Sushi");

        // When
        var result = foodScrapper.scrapAsync(foodNames).next();

        // Then
        StepVerifier.create(result)
            .assertNext(foodInfo -> {
                assertThat(foodInfo.getName()).isNotBlank();
                assertThat(foodInfo.getDescription()).isNotBlank();
                assertThat(foodInfo.getImage()).isNotBlank();

                System.out.println("=== 스크래핑 결과 ===");
                System.out.println("이름: " + foodInfo.getName());
                System.out.println("설명: " + foodInfo.getDescription());
                System.out.println("이미지: " + foodInfo.getImage());
            })
            .verifyComplete();
    }
}
```

#### 실행 및 검증
```bash
./gradlew test --tests TasteAtlasFoodScrapperIntegrationTest
```

**✅ 성공 조건**:
- 음식 이름, 설명, 이미지 URL이 모두 존재
- 이미지 URL이 유효함

---

### 2.4 환율 조회 테스트

#### 테스트 파일 생성
```java
// src/test/java/foodiepass/server/currency/infra/GoogleFinanceRateProviderIntegrationTest.java

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Google Finance 환율 조회 통합 테스트")
class GoogleFinanceRateProviderIntegrationTest {

    @Autowired
    private ExchangeRateProvider rateProvider;

    @Test
    @DisplayName("JPY → KRW 환율 조회")
    void getExchangeRateJpyToKrw() {
        // Given
        Currency from = Currency.JAPANESE_YEN;
        Currency to = Currency.SOUTH_KOREAN_WON;

        // When
        BigDecimal rate = rateProvider.getExchangeRate(from, to);

        // Then
        assertThat(rate).isNotNull();
        assertThat(rate).isGreaterThan(BigDecimal.ZERO);

        System.out.println("환율: 1 JPY = " + rate + " KRW");
    }
}
```

**✅ Phase 2 완료 조건**: 모든 개별 API 테스트 통과

---

## 📋 Phase 3: 부분 파이프라인 통합 테스트

### 목표
- 2개 이상의 API를 연결하여 동작 확인
- 실제 데이터 플로우 검증

### 3.1 OCR + 번역 통합 테스트

```java
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("OCR + 번역 통합 테스트")
class OcrTranslationIntegrationTest {

    @Autowired
    private MenuService menuService;

    @Test
    @DisplayName("일본 메뉴판 → OCR → 한국어 번역")
    void ocrAndTranslateJapaneseMenu() throws Exception {
        // Given: 일본 메뉴판 이미지
        byte[] imageBytes = Files.readAllBytes(
            Paths.get("src/test/resources/japanese-menu.jpg")
        );
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        ReconfigureRequest request = new ReconfigureRequest(
            base64Image,
            "Japanese",
            "Korean",
            "JPY",
            "KRW"
        );

        // When: 전체 파이프라인 실행
        var result = menuService.reconfigure(request);

        // Then: 검증
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.results()).isNotEmpty();

                var firstItem = response.results().get(0);
                assertThat(firstItem.originMenuName()).isNotBlank();
                assertThat(firstItem.translatedMenuName()).isNotBlank();

                System.out.println("원본: " + firstItem.originMenuName());
                System.out.println("번역: " + firstItem.translatedMenuName());
            })
            .verifyComplete();
    }
}
```

---

## 📋 Phase 4: 전체 파이프라인 E2E 테스트

### 목표
- 실제 사용자 시나리오 전체 플로우 검증
- A/B 테스트 분기 포함

### 4.1 전체 파이프라인 E2E 테스트

```java
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("전체 파이프라인 E2E 테스트")
class FullPipelineE2ETest {

    @Autowired
    private MenuScanService menuScanService;

    @Test
    @DisplayName("메뉴판 업로드 → 전체 처리 → A/B 분기")
    void fullPipelineWithABTest() throws Exception {
        // Given: 실제 메뉴판 이미지
        byte[] imageBytes = Files.readAllBytes(
            Paths.get("src/test/resources/test-menu.jpg")
        );
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        MenuScanRequest request = new MenuScanRequest(
            base64Image,
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        String userId = "test-user-e2e";

        // When: 전체 플로우 실행
        var result = menuScanService.scanMenu(request, userId);

        // Then: 검증
        StepVerifier.create(result)
            .assertNext(response -> {
                // 기본 검증
                assertThat(response.scanId()).isNotNull();
                assertThat(response.abGroup()).isIn("CONTROL", "TREATMENT");
                assertThat(response.items()).isNotEmpty();
                assertThat(response.processingTime()).isLessThan(10.0); // 10초 이내

                // A/B 그룹별 검증
                if (response.abGroup().equals("TREATMENT")) {
                    // Treatment: 사진 + 설명 있어야 함
                    response.items().forEach(item -> {
                        assertThat(item.imageUrl()).isNotNull();
                        assertThat(item.description()).isNotNull();
                    });
                } else {
                    // Control: 사진 + 설명 없어야 함
                    response.items().forEach(item -> {
                        assertThat(item.imageUrl()).isNull();
                        assertThat(item.description()).isNull();
                    });
                }

                // 결과 출력
                System.out.println("\n=== E2E 테스트 결과 ===");
                System.out.println("Scan ID: " + response.scanId());
                System.out.println("A/B Group: " + response.abGroup());
                System.out.println("Processing Time: " + response.processingTime() + "s");
                System.out.println("Items: " + response.items().size());

                response.items().forEach(item -> {
                    System.out.println("\n--- 메뉴 아이템 ---");
                    System.out.println("원본: " + item.originalName());
                    System.out.println("번역: " + item.translatedName());
                    System.out.println("가격: " + item.priceInfo().convertedFormatted());
                    if (item.description() != null) {
                        System.out.println("설명: " + item.description());
                        System.out.println("이미지: " + item.imageUrl());
                    }
                });
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("여러 사용자의 A/B 그룹 배정 균형 확인")
    void multipleUsersABGroupBalance() throws Exception {
        // Given: 동일한 메뉴판 이미지
        byte[] imageBytes = Files.readAllBytes(
            Paths.get("src/test/resources/test-menu.jpg")
        );
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        MenuScanRequest request = new MenuScanRequest(
            base64Image, "ja", "ko", "JPY", "KRW"
        );

        // When: 10명의 사용자로 테스트
        int controlCount = 0;
        int treatmentCount = 0;

        for (int i = 0; i < 10; i++) {
            String userId = "user-" + i;
            var result = menuScanService.scanMenu(request, userId).block();

            if (result.abGroup().equals("CONTROL")) {
                controlCount++;
            } else {
                treatmentCount++;
            }
        }

        // Then: 대략 50:50 비율
        System.out.println("\nA/B 그룹 배정 결과:");
        System.out.println("Control: " + controlCount);
        System.out.println("Treatment: " + treatmentCount);

        // 최소 1명은 각 그룹에 배정되어야 함
        assertThat(controlCount).isGreaterThan(0);
        assertThat(treatmentCount).isGreaterThan(0);
    }
}
```

**✅ Phase 4 완료 조건**:
- 전체 파이프라인 성공
- 처리 시간 5초 이내 (H2 검증)
- A/B 분기 정확하게 동작

---

## 📋 Phase 5: 실제 서버 실행 및 API 테스트

### 5.1 서버 실행

```bash
# 환경 변수 로드
export $(cat .env | xargs)

# 서버 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 서버 정상 시작 확인
# - "Started ServerApplication" 메시지 확인
# - 포트 8080 리스닝 확인
```

### 5.2 Postman으로 API 테스트

#### 테스트 케이스 1: 메뉴 스캔
```http
POST http://localhost:8080/api/menus/scan
Content-Type: application/json

{
  "base64EncodedImage": "...(실제 base64 이미지)...",
  "originLanguageName": "ja",
  "userLanguageName": "ko",
  "originCurrencyName": "JPY",
  "userCurrencyName": "KRW"
}
```

**예상 응답**:
```json
{
  "scanId": "uuid...",
  "abGroup": "TREATMENT",
  "items": [
    {
      "id": "uuid...",
      "originalName": "寿司",
      "translatedName": "스시",
      "description": "신선한 생선과 밥",
      "imageUrl": "https://...",
      "priceInfo": {
        "originalAmount": 1500,
        "originalCurrency": "JPY",
        "originalFormatted": "¥1,500",
        "convertedAmount": 15000,
        "convertedCurrency": "KRW",
        "convertedFormatted": "₩15,000"
      }
    }
  ],
  "processingTime": 3.5
}
```

#### 테스트 케이스 2: 설문 제출
```http
POST http://localhost:8080/api/surveys
Content-Type: application/json

{
  "scanId": "uuid-from-previous-response",
  "hasConfidence": true
}
```

**✅ Phase 5 완료 조건**:
- 서버가 정상 시작됨
- Postman 테스트 성공
- 응답 데이터가 올바름

---

## 🎯 전체 체크리스트

### Phase 1: 환경 설정
- [ ] Google Cloud 프로젝트 생성
- [ ] Vertex AI API 활성화
- [ ] Cloud Translation API 활성화
- [ ] 서비스 계정 생성
- [ ] credentials.json 다운로드
- [ ] .env 파일 생성
- [ ] 환경 변수 로드 확인

### Phase 2: 개별 API 테스트
- [ ] GeminiOcrIntegrationTest 작성 및 실행
- [ ] GoogleTranslationIntegrationTest 작성 및 실행
- [ ] TasteAtlasFoodScrapperIntegrationTest 작성 및 실행
- [ ] GoogleFinanceRateProviderIntegrationTest 작성 및 실행

### Phase 3: 부분 통합 테스트
- [ ] OcrTranslationIntegrationTest 작성 및 실행

### Phase 4: 전체 파이프라인 E2E
- [ ] FullPipelineE2ETest 작성 및 실행
- [ ] A/B 그룹 배정 확인
- [ ] 처리 시간 측정

### Phase 5: 실제 서버 테스트
- [ ] 서버 실행 성공
- [ ] Postman POST /api/menus/scan 테스트
- [ ] Postman POST /api/surveys 테스트
- [ ] 브라우저에서 프론트엔드 연동 테스트

---

## 📊 성공 기준

### 기술 검증 (H2)
- ✅ OCR 정확도: 메뉴 이름과 가격 추출 성공
- ✅ 번역 정확도: 의미 있는 번역 결과
- ✅ 음식 매칭: 관련 있는 이미지와 설명
- ✅ 환율 정확도: 실시간 환율 조회 성공
- ✅ 처리 시간: 5초 이내

### A/B 테스트 검증
- ✅ Control 그룹: 텍스트 + 가격만
- ✅ Treatment 그룹: 사진 + 설명 + 텍스트 + 가격
- ✅ 그룹 배정: 대략 50:50 비율

---

## 🚨 문제 해결 가이드

### Google Cloud 관련
**문제**: API 호출 실패 (403, 401)
- credentials.json 경로 확인
- 서비스 계정 권한 확인
- API 활성화 확인
- 프로젝트 ID 확인

**문제**: Quota 초과
- Google Cloud Console → API & Services → Quotas
- 무료 할당량 확인
- 필요시 결제 계정 연결

### TasteAtlas 스크래핑 관련
**문제**: 스크래핑 실패
- 웹사이트 구조 변경 가능성
- 셀렉터 업데이트 필요
- 기본값(Fallback) 동작 확인

### 환율 조회 관련
**문제**: Google Finance 스크래핑 실패
- URL 형식 확인
- 셀렉터 업데이트 필요
- 대체 환율 API 고려

---

## 📝 진행 상황 기록

| Phase | 시작일 | 완료일 | 상태 | 비고 |
|-------|--------|--------|------|------|
| Phase 1 | | | ⏳ Pending | 환경 설정 |
| Phase 2 | | | ⏳ Pending | 개별 API 테스트 |
| Phase 3 | | | ⏳ Pending | 부분 통합 |
| Phase 4 | | | ⏳ Pending | E2E 테스트 |
| Phase 5 | | | ⏳ Pending | 서버 실행 |

---

## 🎓 학습 포인트

이 과정을 통해:
1. **외부 API 통합 테스트 방법** 학습
2. **점진적 통합 전략** 실습
3. **실제 서비스 배포 준비** 경험
4. **문제 해결 능력** 향상

---

**다음 단계**: Phase 1부터 시작하여 단계적으로 진행
