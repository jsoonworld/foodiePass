package foodiepass.server.language.infra;

import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.TranslationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("integration-test")
@DisplayName("GoogleTranslation 통합 테스트 - 실제 Google Translation API 호출")
@Disabled("Integration test with external Google Translation API - temporarily disabled for test architecture validation")
class GoogleTranslationIntegrationTest {

    @Autowired
    private TranslationClient translationClient;

    @BeforeEach
    void setUp() {
        assumeTrue(
            System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null
                || System.getenv("GOOGLE_CREDENTIALS_PATH") != null,
            "Google Cloud credentials가 설정된 경우에만 통합 테스트를 실행합니다."
        );
    }

    @Test
    @DisplayName("일본어 → 영어 번역")
    void translateAsync_japaneseToEnglish_translatesCorrectly() {
        // Given
        String japanese = "寿司";
        Language source = Language.JAPANESE;
        Language target = Language.ENGLISH;

        System.out.println("=== Starting Translation Test: JA → EN ===");
        System.out.println("Input: " + japanese);

        // When
        var result = translationClient.translateAsync(source, target, japanese);

        // Then
        StepVerifier.create(result)
            .assertNext(translated -> {
                assertThat(translated).isNotBlank();
                System.out.println("Output: " + translated);

                // "Sushi"가 포함되어야 함 (대소문자 무관)
                assertThat(translated.toLowerCase())
                    .as("일본어 '寿司'는 영어로 'sushi'로 번역되어야 함")
                    .contains("sushi");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("영어 → 한국어 번역")
    void translateAsync_englishToKorean_translatesCorrectly() {
        // Given
        String english = "Fresh raw fish with rice";
        Language source = Language.ENGLISH;
        Language target = Language.KOREAN;

        System.out.println("=== Starting Translation Test: EN → KO ===");
        System.out.println("Input: " + english);

        // When
        var result = translationClient.translateAsync(source, target, english);

        // Then
        StepVerifier.create(result)
            .assertNext(translated -> {
                assertThat(translated).isNotBlank();
                System.out.println("Output: " + translated);

                // 번역된 결과가 원문과 달라야 함
                assertThat(translated).isNotEqualTo(english);

                // 한국어 키워드가 포함되어야 함 (쌀, 생선, 신선 등)
                boolean containsKoreanKeyword = translated.contains("쌀")
                    || translated.contains("생선")
                    || translated.contains("신선")
                    || translated.contains("날것")
                    || translated.contains("날");

                assertThat(containsKoreanKeyword)
                    .as("번역된 한국어에 관련 키워드가 포함되어야 함")
                    .isTrue();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("일본어 → 한국어 번역")
    void translateAsync_japaneseToKorean_translatesCorrectly() {
        // Given
        String japanese = "天ぷら";
        Language source = Language.JAPANESE;
        Language target = Language.KOREAN;

        System.out.println("=== Starting Translation Test: JA → KO ===");
        System.out.println("Input: " + japanese);

        // When
        var result = translationClient.translateAsync(source, target, japanese);

        // Then
        StepVerifier.create(result)
            .assertNext(translated -> {
                assertThat(translated).isNotBlank();
                System.out.println("Output: " + translated);

                // "튀김" 또는 "덴푸라"가 포함되어야 함
                boolean containsExpectedTranslation = translated.contains("튀김")
                    || translated.contains("덴푸라")
                    || translated.contains("템푸라");

                assertThat(containsExpectedTranslation)
                    .as("일본어 '天ぷら'는 한국어로 '튀김' 또는 '덴푸라'로 번역되어야 함")
                    .isTrue();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("동일 언어 번역 시 원문 그대로 반환")
    void translateAsync_sameLanguage_returnsOriginal() {
        // Given
        String text = "Hello World";
        Language language = Language.ENGLISH;

        System.out.println("=== Starting Same Language Test ===");
        System.out.println("Input: " + text);

        // When
        var result = translationClient.translateAsync(language, language, text);

        // Then
        StepVerifier.create(result)
            .assertNext(translated -> {
                assertThat(translated).isEqualTo(text);
                System.out.println("Output: " + translated);
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("빈 문자열은 그대로 반환")
    void translateAsync_emptyString_returnsEmpty() {
        // Given
        String emptyText = "";
        Language source = Language.JAPANESE;
        Language target = Language.KOREAN;

        // When
        var result = translationClient.translateAsync(source, target, emptyText);

        // Then
        StepVerifier.create(result)
            .assertNext(translated -> {
                assertThat(translated).isEmpty();
            })
            .verifyComplete();
    }
}
