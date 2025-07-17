package foodiepass.server.language.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import foodiepass.server.language.exception.LanguageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Language Enum 테스트")
class LanguageTest {

    @Nested
    @DisplayName("fromLanguageCode 메소드는")
    class Describe_fromLanguageCode {

        @DisplayName("유효한 언어 코드가 주어지면")
        @ParameterizedTest
        @CsvSource({"ko, KOREAN", "en, ENGLISH", "ja, JAPANESE"})
        void it_returns_correct_language(String code, Language expected) {
            // when
            Language result = Language.fromLanguageCode(code);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @DisplayName("여러 코드를 가진 언어의 어떤 코드가 주어져도")
        @ParameterizedTest
        @ValueSource(strings = {"he", "iw"})
        void it_returns_same_language_for_multiple_codes(String code) {
            // when
            Language result = Language.fromLanguageCode(code);

            // then
            assertThat(result).isEqualTo(Language.HEBREW);
        }

        @DisplayName("코드 앞뒤에 공백이 포함되어도")
        @ParameterizedTest
        @ValueSource(strings = {" ko ", "ko ", " ko"})
        void it_trims_and_returns_correct_language(String codeWithWhitespace) {
            // when
            Language result = Language.fromLanguageCode(codeWithWhitespace);

            // then
            assertThat(result).isEqualTo(Language.KOREAN);
        }

        @Test
        @DisplayName("존재하지 않는 언어 코드가 주어지면")
        void it_throws_LanguageException() {
            // given
            String invalidCode = "INVALID";

            // when & then
            assertThatThrownBy(() -> Language.fromLanguageCode(invalidCode))
                    .isInstanceOf(LanguageException.class)
                    .hasMessageContaining("[LANGUAGE ERROR] 지원하지 않는 언어입니다.");
        }
    }

    @Nested
    @DisplayName("fromLanguageName 메소드는")
    class Describe_fromLanguageName {

        @Test
        @DisplayName("유효한 언어 이름이 주어지면")
        void it_returns_correct_language() {
            // given
            String name = "Korean";

            // when
            Language result = Language.fromLanguageName(name);

            // then
            assertThat(result).isEqualTo(Language.KOREAN);
        }

        @Test
        @DisplayName("존재하지 않는 언어 이름이 주어지면")
        void it_throws_LanguageException() {
            // given
            String invalidName = "Invalid Language Name";

            // when & then
            assertThatThrownBy(() -> Language.fromLanguageName(invalidName))
                    .isInstanceOf(LanguageException.class);
        }
    }
}
