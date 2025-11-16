package foodiepass.server.menu.infra.scraper.gemini;

import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("integration-test")
@DisplayName("GeminiOcrReader 통합 테스트 - 실제 Gemini API 호출")
@Disabled("Integration test with external Gemini API - temporarily disabled for test architecture validation")
class GeminiOcrReaderIntegrationTest {

    private static final String TEST_IMAGE_PATH = "src/test/resources/images/test-menu.jpg";

    @Autowired
    private OcrReader ocrReader;

    private String base64EncodedImage;
    private byte[] imageBytes;

    @BeforeEach
    void setUp() throws IOException {
        assumeTrue(
            System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isBlank(),
            "GEMINI_API_KEY 환경 변수가 설정된 경우에만 통합 테스트를 실행합니다."
        );

        final Path imagePath = Paths.get(TEST_IMAGE_PATH);
        imageBytes = Files.readAllBytes(imagePath);
        base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);
    }

    @Test
    @DisplayName("일본어 메뉴판 이미지에서 메뉴 아이템을 추출한다")
    void read_withJapaneseMenuImage_extractsMenuItems() {
        // given: 테스트용 일본어 메뉴판 이미지 (setUp에서 준비됨)
        System.out.println("=== Starting OCR Test ===");
        System.out.println("Image size: " + imageBytes.length + " bytes");
        System.out.println("Base64 length: " + base64EncodedImage.length());

        // when: OCR 수행
        final List<MenuItem> menuItems;
        try {
            menuItems = ocrReader.read(base64EncodedImage);
        } catch (Exception e) {
            System.err.println("=== OCR Failed ===");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getClass().getName());
                System.err.println("Cause message: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw e;
        }

        // then: 메뉴 아이템이 추출되어야 함
        assertThat(menuItems).isNotNull();
        assertThat(menuItems).isNotEmpty();

        // 추출된 메뉴 아이템 출력 (디버깅용)
        System.out.println("=== Extracted Menu Items ===");
        menuItems.forEach(item ->
            System.out.printf("Name: %s, Price: %s %s%n",
                item.getName(),
                item.getPrice().getAmount(),
                item.getPrice().getCurrency())
        );

        // 기본 검증: 최소 1개 이상의 메뉴 아이템
        assertThat(menuItems.size()).isGreaterThanOrEqualTo(1);

        // 각 메뉴 아이템이 이름과 가격을 가지고 있는지 검증
        menuItems.forEach(item -> {
            assertThat(item.getName()).isNotBlank();
            assertThat(item.getPrice()).isNotNull();
            assertThat(item.getPrice().getAmount()).isNotNull();
            assertThat(item.getPrice().getAmount().doubleValue()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("일본어 메뉴판에서 특정 음식 이름이 포함되어야 한다")
    void read_withJapaneseMenuImage_containsExpectedFoodNames() {
        // given: 테스트용 일본어 메뉴판 이미지 (setUp에서 준비됨)
        // 이미지 내용: メニュー, スシ ¥1000, ラーメン ¥800, 天ぷら ¥1200, カレー ¥900

        // when: OCR 수행
        final List<MenuItem> menuItems = ocrReader.read(base64EncodedImage);

        // then: 예상되는 음식 이름들이 포함되어야 함
        final List<String> extractedNames = menuItems.stream()
                .map(MenuItem::getName)
                .toList();

        System.out.println("=== Extracted Names ===");
        extractedNames.forEach(System.out::println);

        // Gemini가 일본어를 그대로 반환하거나 영어로 번역할 수 있으므로
        // 예상 키워드 중 일부가 포함되어 있는지 확인
        final List<String> expectedKeywords = List.of(
            "スシ", "Sushi", "寿司",
            "ラーメン", "Ramen",
            "天ぷら", "Tempura",
            "カレー", "Curry"
        );

        boolean containsExpectedItem = extractedNames.stream()
            .anyMatch(name -> {
                String lowerName = name.toLowerCase();
                return expectedKeywords.stream()
                    .anyMatch(keyword ->
                        name.contains(keyword) || lowerName.contains(keyword.toLowerCase()));
            });

        assertThat(containsExpectedItem)
            .as("최소 하나의 예상 메뉴 항목이 추출되어야 함")
            .isTrue();
    }

    @Test
    @DisplayName("메뉴판 이미지에서 가격 정보가 정확히 추출되어야 한다")
    void read_withJapaneseMenuImage_extractsPricesCorrectly() {
        // given: 테스트용 일본어 메뉴판 이미지 (setUp에서 준비됨)

        // when: OCR 수행
        final List<MenuItem> menuItems = ocrReader.read(base64EncodedImage);

        // then: 가격이 합리적인 범위여야 함
        menuItems.forEach(item -> {
            double priceValue = item.getPrice().getAmount().doubleValue();
            assertThat(priceValue)
                    .as("가격이 일본 메뉴판의 합리적 범위여야 함")
                    .isBetween(100.0, 10000.0);

            assertThat(item.getPrice().getCurrency().getCurrencyCode())
                    .as("통화는 JPY여야 함")
                    .contains("JPY");
        });

        System.out.println("=== Extracted Prices ===");
        menuItems.forEach(item ->
            System.out.printf("%s: %s %s%n",
                item.getName(),
                item.getPrice().getAmount(),
                item.getPrice().getCurrency())
        );
    }
}
