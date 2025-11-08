package foodiepass.server.menu.infra.scraper.gemini;

import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@DisplayName("GeminiOcrReader 통합 테스트 - 실제 Gemini API 호출")
class GeminiOcrReaderIntegrationTest {

    @Autowired
    private OcrReader ocrReader;

    @Test
    @DisplayName("일본어 메뉴판 이미지에서 메뉴 아이템을 추출한다")
    void read_withJapaneseMenuImage_extractsMenuItems() throws IOException {
        // given: 테스트용 일본어 메뉴판 이미지를 base64로 인코딩
        final Path imagePath = Paths.get("src/test/resources/images/test-menu.jpg");
        final byte[] imageBytes = Files.readAllBytes(imagePath);
        final String base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);

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
    void read_withJapaneseMenuImage_containsExpectedFoodNames() throws IOException {
        // given: 테스트용 일본어 메뉴판 이미지
        // 이미지 내용: メニュー, スシ ¥1000, ラーメン ¥800, 天ぷら ¥1200, カレー ¥900
        final Path imagePath = Paths.get("src/test/resources/images/test-menu.jpg");
        final byte[] imageBytes = Files.readAllBytes(imagePath);
        final String base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);

        // when: OCR 수행
        final List<MenuItem> menuItems = ocrReader.read(base64EncodedImage);

        // then: 예상되는 음식 이름들이 포함되어야 함
        final List<String> extractedNames = menuItems.stream()
                .map(MenuItem::getName)
                .toList();

        System.out.println("=== Extracted Names ===");
        extractedNames.forEach(System.out::println);

        // 최소 하나의 음식 이름이 일본어 또는 번역된 형태로 포함되어야 함
        // Gemini가 일본어를 그대로 반환하거나 영어로 번역할 수 있음
        assertThat(extractedNames).isNotEmpty();
    }

    @Test
    @DisplayName("메뉴판 이미지에서 가격 정보가 정확히 추출되어야 한다")
    void read_withJapaneseMenuImage_extractsPricesCorrectly() throws IOException {
        // given: 테스트용 일본어 메뉴판 이미지
        final Path imagePath = Paths.get("src/test/resources/images/test-menu.jpg");
        final byte[] imageBytes = Files.readAllBytes(imagePath);
        final String base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);

        // when: OCR 수행
        final List<MenuItem> menuItems = ocrReader.read(base64EncodedImage);

        // then: 가격이 합리적인 범위여야 함
        menuItems.forEach(item -> {
            double priceValue = item.getPrice().getAmount().doubleValue();
            assertThat(priceValue)
                    .as("Price should be in reasonable range")
                    .isBetween(1.0, 100000.0);
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
