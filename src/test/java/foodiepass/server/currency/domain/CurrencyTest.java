package foodiepass.server.currency.domain;

import foodiepass.server.currency.exception.CurrencyErrorCode;
import foodiepass.server.currency.exception.CurrencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Currency Enum")
class CurrencyTest {

    @Nested
    @DisplayName("fromCurrencyCode 메서드는")
    class Describe_fromCurrencyCode {

        @DisplayName("유효한 통화 코드로 정확한 Currency 상수를 반환한다")
        @ParameterizedTest(name = "{0} 코드는 {1}을 반환해야 한다")
        @CsvSource({
                "KRW, SOUTH_KOREAN_WON",
                "USD, UNITED_STATES_DOLLAR",
                "JPY, JAPANESE_YEN",
                "CNH, CHINESE_YUAN_OFFSHORE"
        })
        void shouldReturnCorrectCurrency_forValidCode(String code, Currency expected) {
            // when
            Currency result = Currency.fromCurrencyCode(code);
            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("소문자 통화 코드로도 대소문자 구분 없이 정확히 찾아 반환한다")
        void shouldReturnCorrectCurrency_forLowercaseCode() {
            // when
            Currency result = Currency.fromCurrencyCode("usd");
            // then
            assertThat(result).isEqualTo(Currency.UNITED_STATES_DOLLAR);
        }

        @Test
        @DisplayName("존재하지 않는 통화 코드를 입력하면 CurrencyException(CURRENCY_NOT_FOUND)을 던진다")
        void shouldThrowException_forNonExistentCode() {
            // when & then
            assertThatThrownBy(() -> Currency.fromCurrencyCode("ABC"))
                    .isInstanceOf(CurrencyException.class)
                    .hasMessage(CurrencyErrorCode.CURRENCY_NOT_FOUND.getMessage());
        }

        @DisplayName("null 또는 비어있는 코드를 입력하면 CurrencyException(INVALID_CURRENCY_INPUT)을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void shouldThrowException_forBlankCode(String invalidCode) {
            // when & then
            assertThatThrownBy(() -> Currency.fromCurrencyCode(invalidCode))
                    .isInstanceOf(CurrencyException.class)
                    .hasMessage(CurrencyErrorCode.INVALID_CURRENCY_INPUT.getMessage());
        }
    }

    @Nested
    @DisplayName("fromCurrencyName 메서드는")
    class Describe_fromCurrencyName {

        @Test
        @DisplayName("유효한 통화 이름으로 정확한 Currency 상수를 반환한다")
        void shouldReturnCorrectCurrency_forValidName() {
            // when
            Currency result = Currency.fromCurrencyName("South Korean won");
            // then
            assertThat(result).isEqualTo(Currency.SOUTH_KOREAN_WON);
        }

        @Test
        @DisplayName("존재하지 않는 통화 이름을 입력하면 CurrencyException(CURRENCY_NOT_FOUND)을 던진다")
        void shouldThrowException_forNonExistentName() {
            // when & then
            assertThatThrownBy(() -> Currency.fromCurrencyName("Invalid Currency Name"))
                    .isInstanceOf(CurrencyException.class)
                    .hasMessage(CurrencyErrorCode.CURRENCY_NOT_FOUND.getMessage());
        }

        @DisplayName("null 또는 비어있는 이름을 입력하면 CurrencyException(INVALID_CURRENCY_INPUT)을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void shouldThrowException_forBlankName(String invalidName) {
            // when & then
            assertThatThrownBy(() -> Currency.fromCurrencyName(invalidName))
                    .isInstanceOf(CurrencyException.class)
                    .hasMessage(CurrencyErrorCode.INVALID_CURRENCY_INPUT.getMessage());
        }
    }
}
