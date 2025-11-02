package foodiepass.server.currency.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExchangeRateCache 테스트")
class ExchangeRateCacheTest {

    private ExchangeRateCache exchangeRateCache;

    @BeforeEach
    void setUp() {
        exchangeRateCache = new ExchangeRateCache();
    }

    @Test
    @DisplayName("환율 정보를 저장하고 올바르게 조회한다")
    void shouldStoreAndRetrieveRateCorrectly() {
        // given
        String from = "USD";
        String to = "KRW";
        double rate = 1300.0;
        exchangeRateCache.updateExchangeRate(from, to, rate);

        // when
        Optional<Double> retrievedRate = exchangeRateCache.getExchangeRate(from, to);

        // then
        assertThat(retrievedRate).isPresent().contains(rate);
    }

    @Test
    @DisplayName("동일 통화 조회 시 환율 1.0을 반환한다")
    void whenSameCurrency_shouldReturnRateOfOne() {
        // when
        Optional<Double> retrievedRate = exchangeRateCache.getExchangeRate("USD", "USD");

        // then
        assertThat(retrievedRate).isPresent().contains(1.0);
    }

    @Test
    @DisplayName("캐시에 없는 환율 조회 시 Optional.empty를 반환한다")
    void whenRateNotExists_shouldReturnEmptyOptional() {
        // when
        Optional<Double> retrievedRate = exchangeRateCache.getExchangeRate("EUR", "GBP");

        // then
        assertThat(retrievedRate).isEmpty();
    }
}
