package foodiepass.server.currency.application;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.application.port.out.ExchangeRateProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateScheduler 테스트")
class ExchangeRateSchedulerTest {

    @InjectMocks
    private ExchangeRateScheduler exchangeRateScheduler;

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @Mock
    private ExchangeRateCache exchangeRateCache;

    @Test
    @DisplayName("초기화 메소드 호출 시 환율을 가져와 캐시를 올바르게 업데이트한다")
    void initializeCacheOnStartup_shouldFetchRatesAndCorrectlyUpdateCache() {
        // given
        given(exchangeRateProvider.getExchangeRateAsync(any(Currency.class), any(Currency.class)))
                .willAnswer(invocation -> {
                    Currency target = invocation.getArgument(1);

                    if (target.equals(Currency.SOUTH_KOREAN_WON)) {
                        return Mono.just(1350.0);
                    }
                    if (target.equals(Currency.JAPANESE_YEN)) {
                        return Mono.just(150.0);
                    }
                    return Mono.just(1.0);
                });

        // when
        exchangeRateScheduler.initializeCacheOnStartup();

        // then
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> rateCaptor = ArgumentCaptor.forClass(Double.class);

        verify(exchangeRateCache, atLeastOnce()).updateExchangeRate(fromCaptor.capture(), toCaptor.capture(), rateCaptor.capture());

        verify(exchangeRateCache).updateExchangeRate("USD", "KRW", 1350.0);
        verify(exchangeRateCache).updateExchangeRate("KRW", "USD", 1.0 / 1350.0);
        verify(exchangeRateCache).updateExchangeRate("KRW", "JPY", 150.0 / 1350.0);
    }
}
