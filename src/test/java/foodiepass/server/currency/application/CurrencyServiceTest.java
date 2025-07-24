package foodiepass.server.currency.application;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.dto.response.CurrencyResponse;
import foodiepass.server.currency.exception.CurrencyException;
import foodiepass.server.menu.application.port.out.ExchangeRateProvider;
import foodiepass.server.menu.dto.response.ReconfigureResponse.PriceInfoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static foodiepass.server.currency.exception.CurrencyErrorCode.CURRENCY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyService 테스트")
class CurrencyServiceTest {

    @InjectMocks
    private CurrencyService currencyService;

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @Test
    @DisplayName("findAllCurrencies 호출 시 모든 통화 목록을 반환한다")
    void findAllCurrencies_shouldReturnAllCurrencies() {
        // given
        int expectedSize = Currency.values().length;

        // when
        List<CurrencyResponse> responses = currencyService.findAllCurrencies();

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(expectedSize);
        assertThat(responses)
                .extracting(CurrencyResponse::currencyName)
                .contains(Currency.SOUTH_KOREAN_WON.getCurrencyName());
    }

    @Test
    @DisplayName("convertAndFormatAsync는 가격을 비동기적으로 변환하고 포맷팅한다")
    void convertAndFormatAsync_shouldConvertAndFormatPrice() {
        // given
        Price originPrice = new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("1000"));
        Currency userCurrency = Currency.JAPANESE_YEN;
        double exchangeRate = 10.5;

        given(exchangeRateProvider.getExchangeRateAsync(Currency.SOUTH_KOREAN_WON, userCurrency))
                .willReturn(Mono.just(exchangeRate));

        // when
        Mono<PriceInfoResponse> result = currencyService.convertAndFormatAsync(originPrice, userCurrency);

        // then
        StepVerifier.create(result)
                .expectNextMatches(priceInfo ->
                        priceInfo.originPriceWithCurrencyUnit().contains("₩") &&
                                priceInfo.userPriceWithCurrencyUnit().contains("¥")
                )
                .verifyComplete();
    }

    @Nested
    @DisplayName("calculateOrdersPrice 메소드는")
    class Describe_calculateOrdersPrice {

        @Test
        @DisplayName("정상적인 요청에 대해 올바른 가격을 계산하여 반환한다")
        void withValidRequest_shouldCalculateAndReturnCorrectPrice() {
            // given
            List<CalculatePriceRequest.OrderElementRequest> orders = List.of(
                    new CalculatePriceRequest.OrderElementRequest(new BigDecimal("15.99"), new BigDecimal("1")),
                    new CalculatePriceRequest.OrderElementRequest(new BigDecimal("4.50"), new BigDecimal("2"))
            );

            CalculatePriceRequest request = new CalculatePriceRequest("United States Dollar", "South Korean won", orders);

            given(exchangeRateProvider.getExchangeRate(Currency.UNITED_STATES_DOLLAR, Currency.SOUTH_KOREAN_WON))
                    .willReturn(1350.50);

            // when
            CalculatePriceResponse response = currencyService.calculateOrdersPrice(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.originTotalPrice().value()).isEqualByComparingTo("24.99");
            assertThat(response.originTotalPrice().currencyCode()).isEqualTo("USD");
            assertThat(response.userTotalPrice().value()).isEqualByComparingTo("33749.00");
            assertThat(response.userTotalPrice().currencyCode()).isEqualTo("KRW");
        }

        @Test
        @DisplayName("유효하지 않은 통화 이름이 포함된 경우 CurrencyException을 던진다")
        void withInvalidCurrencyName_shouldThrowCurrencyException() {
            // given
            List<CalculatePriceRequest.OrderElementRequest> orders = List.of(
                    new CalculatePriceRequest.OrderElementRequest(new BigDecimal("10.00"), new BigDecimal("1"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest("United States Dollar", "Invalid Currency", orders);

            // when & then
            assertThatThrownBy(() -> currencyService.calculateOrdersPrice(request))
                    .isInstanceOf(CurrencyException.class)
                    .hasMessage(CURRENCY_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("원본 통화와 사용자 통화가 같을 경우 환율 1.0을 적용한다")
        void withSameOriginAndUserCurrency_shouldApplyExchangeRateOfOne() {
            // given
            List<CalculatePriceRequest.OrderElementRequest> orders = List.of(
                    new CalculatePriceRequest.OrderElementRequest(new BigDecimal("100.00"), new BigDecimal("1"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest("South Korean won", "South Korean won", orders);

            given(exchangeRateProvider.getExchangeRate(any(Currency.class), any(Currency.class)))
                    .willReturn(1.0);

            // when
            CalculatePriceResponse response = currencyService.calculateOrdersPrice(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.originTotalPrice().value()).isEqualByComparingTo("100.00");
            assertThat(response.userTotalPrice().value()).isEqualByComparingTo("100.00");
            assertThat(response.originTotalPrice().currencyCode()).isEqualTo("KRW");
            assertThat(response.userTotalPrice().currencyCode()).isEqualTo("KRW");
        }
    }
}
