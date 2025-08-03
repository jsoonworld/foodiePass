package foodiepass.server.currency.application;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.exception.CurrencyException;
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
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyService 테스트")
class CurrencyServiceTest {

    @InjectMocks
    private CurrencyService currencyService;

    @Mock
    private ExchangeRateCache exchangeRateCache;

    @Nested
    @DisplayName("calculateOrdersPriceAsync 메소드는")
    class Describe_calculateOrdersPriceAsync {

        @Test
        @DisplayName("캐시에 환율 정보가 있으면 올바른 가격을 계산하여 반환한다")
        void withRateInCache_shouldCalculateAndReturnCorrectPrice() {
            // given
            List<CalculatePriceRequest.OrderElementRequest> orders = List.of(
                    new CalculatePriceRequest.OrderElementRequest(new BigDecimal("10.00"), new BigDecimal("2"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest("United States Dollar", "South Korean won", orders);

            String from = Currency.UNITED_STATES_DOLLAR.getCurrencyCode();
            String to = Currency.SOUTH_KOREAN_WON.getCurrencyCode();
            double exchangeRate = 1350.50;

            given(exchangeRateCache.getExchangeRate(from, to)).willReturn(Optional.of(exchangeRate));

            // when
            Mono<CalculatePriceResponse> result = currencyService.calculateOrdersPriceAsync(request);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(response -> {
                        boolean priceMatches = response.userTotalPrice().value().compareTo(new BigDecimal("27010.00")) == 0;
                        boolean currencyMatches = response.userTotalPrice().currencyCode().equals("KRW");
                        return priceMatches && currencyMatches;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("캐시에 환율 정보가 없으면 CurrencyException을 발생시킨다")
        void withNoRateInCache_shouldThrowCurrencyException() {
            // given
            List<CalculatePriceRequest.OrderElementRequest> orders = List.of(
                    new CalculatePriceRequest.OrderElementRequest(new BigDecimal("10.00"), new BigDecimal("1"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest("United States Dollar", "Japanese Yen", orders);

            String from = Currency.UNITED_STATES_DOLLAR.getCurrencyCode();
            String to = Currency.JAPANESE_YEN.getCurrencyCode();

            given(exchangeRateCache.getExchangeRate(from, to)).willReturn(Optional.empty());

            // when
            Mono<CalculatePriceResponse> result = currencyService.calculateOrdersPriceAsync(request);

            // then
            StepVerifier.create(result)
                    .expectError(CurrencyException.class)
                    .verify();
        }
    }
}
