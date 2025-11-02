package foodiepass.server.language.domain;

import foodiepass.server.language.exception.LanguageErrorCode;
import foodiepass.server.language.exception.LanguageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Language Enum")
class LanguageTest {

    @Nested
    @DisplayName("fromLanguageCode 메소드는")
    class Describe_fromLanguageCode {

        @DisplayName("유효한 언어 코드가 주어지면 대소문자 구분 없이 정확한 언어를 반환한다")
        @ParameterizedTest(name = "\"{0}\" 코드는 {1}을 반환해야 한다")
        @CsvSource({
                "ko, KOREAN",
                "EN, ENGLISH",
                "ja, JAPANESE",
                "he, HEBREW",
                "iw, HEBREW",
                "zh-CN, CHINESE_SIMPLIFIED",
                "zh, CHINESE_SIMPLIFIED",
                "' ko ', KOREAN"
        })
        void it_returns_correct_language_for_valid_codes(String code, Language expected) {
            // when
            Language result = Language.fromLanguageCode(code);
            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 언어 코드가 주어지면 LanguageException(LANGUAGE_NOT_FOUND)을 던진다")
        void it_throws_notFoundException_for_invalid_code() {
            // when & then
            assertThatThrownBy(() -> Language.fromLanguageCode("invalid-code"))
                    .isInstanceOf(LanguageException.class)
                    .hasMessage(LanguageErrorCode.LANGUAGE_NOT_FOUND.getMessage());
        }

        @DisplayName("null 또는 비어있는 코드가 주어지면 LanguageException(INVALID_LANGUAGE_INPUT)을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void it_throws_invalidInputException_for_blank_code(String invalidCode) {
            // when & then
            assertThatThrownBy(() -> Language.fromLanguageCode(invalidCode))
                    .isInstanceOf(LanguageException.class)
                    .hasMessage(LanguageErrorCode.INVALID_LANGUAGE_INPUT.getMessage());
        }
    }

    @Nested
    @DisplayName("fromLanguageName 메소드는")
    class Describe_fromLanguageName {

        @Test
        @DisplayName("유효한 언어 이름이 주어지면 정확한 언어를 반환한다")
        void it_returns_correct_language_for_valid_name() {
            // given
            String name = "Korean";
            // when
            Language result = Language.fromLanguageName(name);
            // then
            assertThat(result).isEqualTo(Language.KOREAN);
        }

        @Test
        @DisplayName("존재하지 않는 언어 이름이 주어지면 LanguageException(LANGUAGE_NOT_FOUND)을 던진다")
        void it_throws_notFoundException_for_invalid_name() {
            // when & then
            assertThatThrownBy(() -> Language.fromLanguageName("Invalid Language Name"))
                    .isInstanceOf(LanguageException.class)
                    .hasMessage(LanguageErrorCode.LANGUAGE_NOT_FOUND.getMessage());
        }

        @DisplayName("null 또는 비어있는 이름이 주어지면 LanguageException(INVALID_LANGUAGE_INPUT)을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void it_throws_invalidInputException_for_blank_name(String invalidName) {
            // when & then
            assertThatThrownBy(() -> Language.fromLanguageName(invalidName))
                    .isInstanceOf(LanguageException.class)
                    .hasMessage(LanguageErrorCode.INVALID_LANGUAGE_INPUT.getMessage());
        }
    }
}
