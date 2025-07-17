package foodiepass.server.currency.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import foodiepass.server.currency.exception.CurrencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Currency Enum 테스트")
class CurrencyTest {

    @Nested
    class Describe_fromCurrencyCode {

        @DisplayName("유효한 통화 코드가 주어지면")
        @ParameterizedTest
        @CsvSource({"KRW, SOUTH_KOREAN_WON", "USD, UNITED_STATES_DOLLAR", "JPY, JAPANESE_YEN"})
        void it_returns_correct_currency(String code, Currency expected) {
            // when
            Currency result = Currency.fromCurrencyCode(code);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 통화 코드가 주어지면")
        void it_throws_CurrencyException() {
            // given
            String invalidCode = "INVALID";

            // when & then
            assertThatThrownBy(() -> Currency.fromCurrencyCode(invalidCode))
                    .isInstanceOf(CurrencyException.class)
                    .hasMessageContaining("[CURRENCY ERROR] 유효하지 않은 통화입니다.");
        }
    }

    @Nested
    class Describe_fromCurrencyName {

        @Test
        @DisplayName("유효한 통화 이름이 주어지면")
        void it_returns_correct_currency() {
            // given
            String name = "South Korean won";

            // when
            Currency result = Currency.fromCurrencyName(name);

            // then
            assertThat(result).isEqualTo(Currency.SOUTH_KOREAN_WON);
        }

        @Test
        @DisplayName("존재하지 않는 통화 이름이 주어지면")
        void it_throws_CurrencyException() {
            // given
            String invalidName = "Invalid Currency Name";

            // when & then
            assertThatThrownBy(() -> Currency.fromCurrencyName(invalidName))
                    .isInstanceOf(CurrencyException.class);
        }
    }
}
