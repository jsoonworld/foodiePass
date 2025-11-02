package foodiepass.server.common.price.domain;

import foodiepass.server.common.price.exception.PriceErrorCode;
import foodiepass.server.common.price.exception.PriceException;
import foodiepass.server.currency.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Price 클래스")
class PriceTest {

    private final Currency KRW = Currency.SOUTH_KOREAN_WON;
    private final Currency USD = Currency.UNITED_STATES_DOLLAR;

    @Nested
    @DisplayName("생성자는")
    class Describe_constructor {

        @Test
        @DisplayName("유효한 통화와 금액으로 Price 객체를 성공적으로 생성한다")
        void shouldCreatePriceSuccessfully() {
            // given
            BigDecimal amount = new BigDecimal("1000");

            // when
            Price price = new Price(KRW, amount);

            // then
            assertThat(price.getCurrency()).isEqualTo(KRW);
            assertThat(price.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("통화 정보가 null이면 PriceException(NULL_CURRENCY)을 던진다")
        void shouldThrowExceptionWhenCurrencyIsNull() {
            // when & then
            assertThatThrownBy(() -> new Price(null, new BigDecimal("1000")))
                    .isInstanceOf(PriceException.class)
                    .hasMessage(PriceErrorCode.NULL_CURRENCY.getMessage());
        }

        @Test
        @DisplayName("금액 정보가 null이면 PriceException(NULL_AMOUNT)을 던진다")
        void shouldThrowExceptionWhenAmountIsNull() {
            // when & then
            assertThatThrownBy(() -> new Price(KRW, null))
                    .isInstanceOf(PriceException.class)
                    .hasMessage(PriceErrorCode.NULL_AMOUNT.getMessage());
        }

        @Test
        @DisplayName("금액이 음수이면 PriceException(INVALID_AMOUNT)을 던진다")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // when & then
            assertThatThrownBy(() -> new Price(KRW, new BigDecimal("-100")))
                    .isInstanceOf(PriceException.class)
                    .hasMessage(PriceErrorCode.INVALID_AMOUNT.getMessage());
        }
    }

    @Nested
    @DisplayName("add 메서드는")
    class Describe_add {

        @Test
        @DisplayName("같은 통화의 가격을 더하면, 합산된 Price 객체를 반환한다")
        void whenAddingSameCurrency_shouldReturnSummedPrice() {
            // given
            Price price1 = new Price(KRW, new BigDecimal("1000"));
            Price price2 = new Price(KRW, new BigDecimal("500"));
            Price expectedPrice = new Price(KRW, new BigDecimal("1500"));

            // when
            Price result = price1.add(price2);

            // then
            assertThat(result).isEqualTo(expectedPrice);
        }

        @Test
        @DisplayName("다른 통화의 가격을 더하면, PriceException(CURRENCIES_DO_NOT_MATCH) 예외를 발생시킨다")
        void whenAddingDifferentCurrency_shouldThrowPriceException() {
            // given
            Price wonPrice = new Price(KRW, new BigDecimal("1000"));
            Price dollarPrice = new Price(USD, new BigDecimal("1.00"));

            // when & then
            assertThatThrownBy(() -> wonPrice.add(dollarPrice))
                    .isInstanceOf(PriceException.class)
                    .hasMessage(PriceErrorCode.CURRENCIES_DO_NOT_MATCH.getMessage());
        }
    }

    @Nested
    @DisplayName("multiply 메서드는")
    class Describe_multiply {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5})
        @DisplayName("0 또는 양수인 수량을 곱하면, 곱셈된 Price 객체를 반환한다")
        void whenMultiplyingByPositiveOrZeroQuantity_shouldReturnMultipliedPrice(int quantity) {
            // given
            Price price = new Price(KRW, new BigDecimal("1000"));
            BigDecimal expectedAmount = new BigDecimal("1000").multiply(BigDecimal.valueOf(quantity));
            Price expectedPrice = new Price(KRW, expectedAmount);

            // when
            Price result = price.multiply(quantity);

            // then
            assertThat(result).isEqualTo(expectedPrice);
        }

        @Test
        @DisplayName("음수인 수량을 곱하면, PriceException(INVALID_QUANTITY) 예외를 발생시킨다")
        void whenMultiplyingByNegativeQuantity_shouldThrowPriceException() {
            // given
            Price price = new Price(KRW, new BigDecimal("1000"));

            // when & then
            assertThatThrownBy(() -> price.multiply(-1))
                    .isInstanceOf(PriceException.class)
                    .hasMessage(PriceErrorCode.INVALID_QUANTITY.getMessage());
        }
    }

    @Nested
    @DisplayName("zero 정적 팩토리 메서드는")
    class Describe_zero {

        @Test
        @DisplayName("주어진 통화에 대해 0 값을 가진 Price 객체를 생성한다")
        void shouldReturnZeroAmountPriceWithGivenCurrency() {
            // given
            Price expectedPrice = new Price(KRW, BigDecimal.ZERO);

            // when
            Price actualPrice = Price.zero(KRW);

            // then
            assertThat(actualPrice).isEqualTo(expectedPrice);
        }
    }
}
