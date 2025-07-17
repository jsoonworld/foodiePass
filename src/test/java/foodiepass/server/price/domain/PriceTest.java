package foodiepass.server.price.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.price.exception.PriceErrorCode;
import foodiepass.server.price.exception.PriceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Price 도메인 테스트")
class PriceTest {

    @Nested
    @DisplayName("add 메서드는")
    class Describe_add {

        @Test
        @DisplayName("같은 통화의 가격을 더하면, 합산된 Price 객체를 반환한다")
        void whenAddingSameCurrency_shouldReturnSummedPrice() {
            // given
            final Price price1 = new Price(Currency.SOUTH_KOREAN_WON, 1000.0);
            final Price price2 = new Price(Currency.SOUTH_KOREAN_WON, 500.0);

            // when
            final Price result = price1.add(price2);

            // then
            assertThat(result.getAmount()).isEqualTo(1500.0);
            assertThat(result.getCurrency()).isEqualTo(Currency.SOUTH_KOREAN_WON);
        }

        @Test
        @DisplayName("다른 통화의 가격을 더하면, PriceException(CURRENCIES_DO_NOT_MATCH) 예외를 발생시킨다")
        void whenAddingDifferentCurrency_shouldThrowPriceException() {
            // given
            final Price wonPrice = new Price(Currency.SOUTH_KOREAN_WON, 1000.0);
            final Price dollarPrice = new Price(Currency.UNITED_STATES_DOLLAR, 1.0);

            // when & then
            assertThatThrownBy(() -> wonPrice.add(dollarPrice))
                    .isInstanceOf(PriceException.class)
                    .hasMessageContaining(PriceErrorCode.CURRENCIES_DO_NOT_MATCH.getMessage());
        }
    }

    @Nested
    @DisplayName("multiply 메서드는")
    class Describe_multiply {

        @Test
        @DisplayName("양수인 수량을 곱하면, 곱셈된 Price 객체를 반환한다")
        void whenMultiplyingByPositiveQuantity_shouldReturnMultipliedPrice() {
            // given
            final Price price = new Price(Currency.SOUTH_KOREAN_WON, 1000.0);

            // when
            final Price result = price.multiply(3);

            // then
            assertThat(result.getAmount()).isEqualTo(3000.0);
            assertThat(result.getCurrency()).isEqualTo(Currency.SOUTH_KOREAN_WON);
        }

        @Test
        @DisplayName("음수인 수량을 곱하면, PriceException(INVALID_QUANTITY) 예외를 발생시킨다")
        void whenMultiplyingByNegativeQuantity_shouldThrowPriceException() {
            // given
            final Price price = new Price(Currency.SOUTH_KOREAN_WON, 1000.0);

            // when & then
            assertThatThrownBy(() -> price.multiply(-1))
                    .isInstanceOf(PriceException.class)
                    .hasMessageContaining(PriceErrorCode.INVALID_QUANTITY.getMessage());
        }
    }
}
