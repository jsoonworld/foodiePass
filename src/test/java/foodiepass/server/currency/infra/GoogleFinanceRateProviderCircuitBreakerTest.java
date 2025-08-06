package foodiepass.server.currency.infra;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.application.port.out.ExchangeRateProvider;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.infra.exception.ScrapingErrorCode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class GoogleFinanceRateProviderCircuitBreakerTest {

    @Autowired
    private ExchangeRateProvider exchangeRateProvider;

    @MockitoBean
    private OcrReader ocrReader;

    static WireMockServer wireMockServer;

    @BeforeAll
    static void startMockServer() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterAll
    static void stopMockServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("jsoup.google-finance.url-format", () -> wireMockServer.baseUrl() + "/finance/quote/%s-%s");
    }

    @Test
    @DisplayName("ExchangeRateProvider 호출이 반복 실패하면 서킷 브레이커가 열린다")
    void circuitBreaker_shouldOpen_onRepeatedFailures() {
        // given
        stubFor(get(urlPathMatching("/finance/quote/USD-JPY"))
                .willReturn(aResponse().withStatus(503)));

        // when & then
        for (int i = 0; i < 10; i++) {
            assertThatThrownBy(() -> {
                exchangeRateProvider.getExchangeRate(Currency.UNITED_STATES_DOLLAR, Currency.JAPANESE_YEN);
            }).isInstanceOf(foodiepass.server.menu.infra.exception.ScrapingException.class);
        }

        assertThatThrownBy(() -> {
            exchangeRateProvider.getExchangeRate(Currency.UNITED_STATES_DOLLAR, Currency.JAPANESE_YEN);
        })
                .isInstanceOf(foodiepass.server.menu.infra.exception.ScrapingException.class)
                .extracting("errorCode")
                .isEqualTo(ScrapingErrorCode.EXTERNAL_API_CIRCUIT_OPEN);
    }
}
