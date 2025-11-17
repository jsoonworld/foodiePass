package foodiepass.server.menu.application;

import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.ReconfigureResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("integration-test")
@DisplayName("MenuService 통합 테스트 - OCR + 번역 + 음식 매칭 + 환율 전체 파이프라인")
@Disabled("Integration test with external APIs - temporarily disabled for test architecture validation")
class MenuServiceIntegrationTest {

    private static final String TEST_IMAGE_PATH = "src/test/resources/images/test-menu.jpg";

    @Autowired
    private MenuService menuService;

    private String base64EncodedImage;

    @BeforeEach
    void setUp() throws IOException {
        assumeTrue(
            System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isBlank(),
            "GEMINI_API_KEY 환경 변수가 설정된 경우에만 통합 테스트를 실행합니다."
        );

        final Path imagePath = Paths.get(TEST_IMAGE_PATH);
        byte[] imageBytes = Files.readAllBytes(imagePath);
        base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);
    }

    @Test
    @DisplayName("일본 메뉴판 → OCR → 한국어 번역 → 환율 변환 전체 파이프라인")
    void reconfigure_japaneseMenu_fullPipeline() {
        // Given: 일본 메뉴판 이미지
        ReconfigureRequest request = new ReconfigureRequest(
            base64EncodedImage,
            "Japanese",
            "Korean",
            "Japanese Yen",
            "South Korean won"
        );

        System.out.println("=== Starting Full Pipeline Test: JA → KO ===");

        // When: 전체 파이프라인 실행
        var result = menuService.reconfigure(request);

        // Then: 검증
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.results()).isNotEmpty();

                System.out.println("=== OCR + 번역 + 환율 결과 ===");
                System.out.println("총 메뉴 개수: " + response.results().size());

                response.results().forEach(item -> {
                    System.out.println("\n--- 메뉴 아이템 ---");
                    System.out.println("원본 이름: " + item.originMenuName());
                    System.out.println("번역 이름: " + item.translatedMenuName());
                    System.out.println("원본 가격: " + item.priceInfo().originPriceWithCurrencyUnit());
                    System.out.println("변환 가격: " + item.priceInfo().userPriceWithCurrencyUnit());

                    // 기본 검증
                    assertThat(item.originMenuName())
                        .as("원본 메뉴 이름이 존재해야 함")
                        .isNotBlank();

                    assertThat(item.translatedMenuName())
                        .as("번역된 메뉴 이름이 존재해야 함")
                        .isNotBlank();

                    assertThat(item.priceInfo().originPriceWithCurrencyUnit())
                        .as("원본 가격이 존재해야 함")
                        .isNotBlank();

                    assertThat(item.priceInfo().userPriceWithCurrencyUnit())
                        .as("변환된 가격이 존재해야 함")
                        .isNotBlank();

                    // 번역 확인: 원본과 번역이 달라야 함
                    assertThat(item.originMenuName())
                        .as("원본과 번역이 달라야 함")
                        .isNotEqualTo(item.translatedMenuName());
                });

                System.out.println("\n✅ 전체 파이프라인 성공");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("영어 메뉴판 → OCR → 한국어 번역 → USD/KRW 환율 변환")
    void reconfigure_englishMenu_fullPipeline() {
        // Given: 영어 메뉴판 이미지 (실제로는 일본어 이미지지만 영어로 처리해봄)
        ReconfigureRequest request = new ReconfigureRequest(
            base64EncodedImage,
            "English",
            "Korean",
            "United States Dollar",
            "South Korean won"
        );

        System.out.println("=== Starting Full Pipeline Test: EN → KO ===");

        // When: 전체 파이프라인 실행
        var result = menuService.reconfigure(request);

        // Then: 검증
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.results()).isNotEmpty();

                System.out.println("=== OCR + 번역 + 환율 결과 ===");
                System.out.println("총 메뉴 개수: " + response.results().size());

                var firstItem = response.results().get(0);
                System.out.println("첫 번째 메뉴: " + firstItem.originMenuName() + " → " + firstItem.translatedMenuName());
                System.out.println("가격: " + firstItem.priceInfo().originPriceWithCurrencyUnit() + " → " + firstItem.priceInfo().userPriceWithCurrencyUnit());

                assertThat(response.results().size())
                    .as("최소 1개 이상의 메뉴 아이템이 있어야 함")
                    .isGreaterThanOrEqualTo(1);
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("동일 언어/화폐: 번역 및 환율 변환 스킵")
    void reconfigure_sameLanguageSameCurrency_skipsTranslationAndConversion() {
        // Given: 동일한 언어와 화폐
        ReconfigureRequest request = new ReconfigureRequest(
            base64EncodedImage,
            "Japanese",
            "Japanese",
            "Japanese Yen",
            "Japanese Yen"
        );

        System.out.println("=== Starting Same Language/Currency Test ===");

        // When: 파이프라인 실행
        var result = menuService.reconfigure(request);

        // Then: 검증
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.results()).isNotEmpty();

                var firstItem = response.results().get(0);
                System.out.println("원본 이름: " + firstItem.originMenuName());
                System.out.println("번역 이름: " + firstItem.translatedMenuName());
                System.out.println("원본 가격: " + firstItem.priceInfo().originPriceWithCurrencyUnit());
                System.out.println("변환 가격: " + firstItem.priceInfo().userPriceWithCurrencyUnit());

                // 동일 언어이므로 번역이 동일할 수 있음 (또는 원본 그대로)
                assertThat(firstItem.originMenuName())
                    .as("동일 언어의 경우 번역이 원본과 같을 수 있음")
                    .isNotBlank();

                System.out.println("✅ 동일 언어/화폐 테스트 성공");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("H2 검증: 전체 파이프라인 처리 시간 측정")
    void reconfigure_fullPipeline_measureProcessingTime() {
        // Given: 일본 메뉴판 이미지
        ReconfigureRequest request = new ReconfigureRequest(
            base64EncodedImage,
            "Japanese",
            "Korean",
            "Japanese Yen",
            "South Korean won"
        );

        System.out.println("=== Starting H2 Verification: Processing Time ===");

        // When: 처리 시간 측정
        long startTime = System.currentTimeMillis();
        var result = menuService.reconfigure(request).block();
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        System.out.println("처리 시간: " + processingTime + "ms (" + (processingTime / 1000.0) + "초)");

        // Then: H2 검증 - 처리 시간 ≤ 5초 (목표)
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotEmpty();

        // 10초 이내 (느슨한 검증)
        assertThat(processingTime)
            .as("처리 시간이 10초 이내여야 함")
            .isLessThan(10000);

        if (processingTime <= 5000) {
            System.out.println("✅ H2 검증 성공: 처리 시간 5초 이내");
        } else {
            System.out.println("⚠️ H2 검증 경고: 처리 시간 5초 초과 (목표: 5초, 실제: " + (processingTime / 1000.0) + "초)");
        }
    }
}
