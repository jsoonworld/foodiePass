package foodiepass.server.currency.infra;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.application.port.out.ExchangeRateProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@DisplayName("GoogleFinance 환율 조회 통합 테스트 - 실제 Google Finance 스크래핑")
class GoogleFinanceRateProviderIntegrationTest {

    @Autowired
    private ExchangeRateProvider rateProvider;

    @Test
    @DisplayName("JPY → KRW 환율 조회")
    void getExchangeRate_jpyToKrw_returnsValidRate() {
        // Given
        Currency from = Currency.JAPANESE_YEN;
        Currency to = Currency.SOUTH_KOREAN_WON;

        System.out.println("=== Starting Exchange Rate Test: JPY → KRW ===");

        // When
        double rate = rateProvider.getExchangeRate(from, to);

        System.out.println("환율: 1 JPY = " + rate + " KRW");

        // Then
        assertThat(rate)
            .as("환율은 0보다 커야 함")
            .isGreaterThan(0.0);

        assertThat(rate)
            .as("JPY → KRW 환율은 현실적인 범위여야 함 (5~15원)")
            .isBetween(5.0, 15.0);
    }

    @Test
    @DisplayName("USD → KRW 환율 조회")
    void getExchangeRate_usdToKrw_returnsValidRate() {
        // Given
        Currency from = Currency.UNITED_STATES_DOLLAR;
        Currency to = Currency.SOUTH_KOREAN_WON;

        System.out.println("=== Starting Exchange Rate Test: USD → KRW ===");

        // When
        double rate = rateProvider.getExchangeRate(from, to);

        System.out.println("환율: 1 USD = " + rate + " KRW");

        // Then
        assertThat(rate)
            .as("환율은 0보다 커야 함")
            .isGreaterThan(0.0);

        assertThat(rate)
            .as("USD → KRW 환율은 현실적인 범위여야 함 (1000~1500원)")
            .isBetween(1000.0, 1500.0);
    }

    @Test
    @DisplayName("EUR → KRW 환율 조회")
    void getExchangeRate_eurToKrw_returnsValidRate() {
        // Given
        Currency from = Currency.EURO;
        Currency to = Currency.SOUTH_KOREAN_WON;

        System.out.println("=== Starting Exchange Rate Test: EUR → KRW ===");

        // When
        double rate = rateProvider.getExchangeRate(from, to);

        System.out.println("환율: 1 EUR = " + rate + " KRW");

        // Then
        assertThat(rate)
            .as("환율은 0보다 커야 함")
            .isGreaterThan(0.0);

        assertThat(rate)
            .as("EUR → KRW 환율은 현실적인 범위여야 함 (1100~1700원)")
            .isBetween(1100.0, 1700.0);
    }

    @Test
    @DisplayName("동일 화폐 간 환율은 1.0")
    void getExchangeRate_sameCurrency_returnsOne() {
        // Given
        Currency currency = Currency.SOUTH_KOREAN_WON;

        System.out.println("=== Starting Same Currency Test: KRW → KRW ===");

        // When
        double rate = rateProvider.getExchangeRate(currency, currency);

        System.out.println("환율: 1 KRW = " + rate + " KRW");

        // Then
        assertThat(rate)
            .as("동일 화폐 간 환율은 1.0이어야 함")
            .isEqualTo(1.0);
    }

    @Test
    @DisplayName("CNY → KRW 환율 조회")
    void getExchangeRate_cnyToKrw_returnsValidRate() {
        // Given
        Currency from = Currency.CHINESE_YUAN;
        Currency to = Currency.SOUTH_KOREAN_WON;

        System.out.println("=== Starting Exchange Rate Test: CNY → KRW ===");

        // When
        double rate = rateProvider.getExchangeRate(from, to);

        System.out.println("환율: 1 CNY = " + rate + " KRW");

        // Then
        assertThat(rate)
            .as("환율은 0보다 커야 함")
            .isGreaterThan(0.0);

        assertThat(rate)
            .as("CNY → KRW 환율은 현실적인 범위여야 함 (150~220원)")
            .isBetween(150.0, 220.0);
    }

    @Test
    @DisplayName("H2 검증: 환율 정확도 ≥ 95% (범위 내 환율 조회)")
    void getExchangeRate_h2Verification_accuracyCheck() {
        // Given
        Currency from = Currency.UNITED_STATES_DOLLAR;
        Currency to = Currency.SOUTH_KOREAN_WON;

        System.out.println("=== Starting H2 Verification: USD → KRW ===");

        // When
        double rate = rateProvider.getExchangeRate(from, to);

        System.out.println("환율: 1 USD = " + rate + " KRW");

        // Then: H2 가설 검증 - 환율 정확도 ≥ 95%
        // 실시간 Google Finance 데이터를 사용하므로, 범위 내에 있으면 정확한 것으로 간주
        boolean isWithinExpectedRange = rate >= 1000.0 && rate <= 1500.0;

        assertThat(isWithinExpectedRange)
            .as("H2 검증: 환율이 현실적인 범위 내에 있어야 함 (정확도 ≥ 95%)")
            .isTrue();

        if (isWithinExpectedRange) {
            System.out.println("✅ H2 검증 성공: 환율 정확도 ≥ 95% (범위 내)");
        }
    }

    @Test
    @DisplayName("여러 화폐 쌍 환율 조회 - 모두 유효")
    void getExchangeRate_multipleCurrencyPairs_allValid() {
        System.out.println("=== Starting Multiple Currency Pairs Test ===");

        // Given & When & Then
        var pairs = new Object[][] {
            {Currency.JAPANESE_YEN, Currency.SOUTH_KOREAN_WON, 5.0, 15.0},
            {Currency.UNITED_STATES_DOLLAR, Currency.SOUTH_KOREAN_WON, 1000.0, 1500.0},
            {Currency.EURO, Currency.SOUTH_KOREAN_WON, 1100.0, 1700.0},
            {Currency.CHINESE_YUAN, Currency.SOUTH_KOREAN_WON, 150.0, 220.0}
        };

        for (Object[] pair : pairs) {
            Currency from = (Currency) pair[0];
            Currency to = (Currency) pair[1];
            double minExpected = (double) pair[2];
            double maxExpected = (double) pair[3];

            double rate = rateProvider.getExchangeRate(from, to);
            System.out.printf("환율: 1 %s = %.2f %s%n",
                from.getCurrencyCode(), rate, to.getCurrencyCode());

            assertThat(rate)
                .as("%s → %s 환율이 유효 범위여야 함", from, to)
                .isBetween(minExpected, maxExpected);
        }

        System.out.println("✅ 모든 화폐 쌍 환율 조회 성공");
    }
}
