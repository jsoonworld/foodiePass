package foodiepass.server.currency.api;

import foodiepass.server.currency.application.ExchangeRateCache;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.request.CalculatePriceRequest.OrderElementRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.dto.response.CurrencyResponse;
import foodiepass.server.global.success.SuccessResponse;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.application.port.out.OcrReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(CurrencyControllerIntegrationTest.TestConfig.class)
@DisplayName("CurrencyController 통합 테스트 - 실제 API 호출")
class CurrencyControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExchangeRateCache exchangeRateCache;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OcrReader ocrReader() {
            return mock(OcrReader.class);
        }

        @Bean
        public FoodScrapper foodScrapper() {
            return mock(FoodScrapper.class);
        }
    }

    @BeforeEach
    void setUp() {
        // 테스트용 환율 데이터 주입
        exchangeRateCache.updateExchangeRate("USD", "KRW", 1350.50);
        exchangeRateCache.updateExchangeRate("USD", "JPY", 150.00);
        exchangeRateCache.updateExchangeRate("JPY", "KRW", 9.00);
        exchangeRateCache.updateExchangeRate("KRW", "USD", 0.00074);
    }

    @Nested
    @DisplayName("GET /currency")
    class GetCurrencies {

        @Test
        @DisplayName("모든 통화 목록을 반환한다")
        void returnsAllCurrencies() {
            // when: 실제 HTTP GET 요청
            ResponseEntity<SuccessResponse<List<CurrencyResponse>>> response = restTemplate.exchange(
                    "/currency",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            // then: 상태 코드 200 OK
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // then: SuccessResponse 검증
            SuccessResponse<List<CurrencyResponse>> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.status()).isEqualTo(200);

            // then: 응답 result가 존재하고 비어있지 않음
            List<CurrencyResponse> currencies = body.result();
            assertThat(currencies).isNotNull();
            assertThat(currencies).isNotEmpty();

            // then: 모든 Currency enum 값이 포함되어야 함
            assertThat(currencies).hasSizeGreaterThanOrEqualTo(Currency.values().length);
        }

        @Test
        @DisplayName("각 통화 응답에 currencyName이 포함된다")
        void eachCurrencyContainsCurrencyName() {
            // when: 실제 HTTP GET 요청
            ResponseEntity<SuccessResponse<List<CurrencyResponse>>> response = restTemplate.exchange(
                    "/currency",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            // then: 각 통화가 currencyName을 가져야 함
            SuccessResponse<List<CurrencyResponse>> body = response.getBody();
            assertThat(body).isNotNull();

            List<CurrencyResponse> currencies = body.result();
            assertThat(currencies).isNotNull();

            currencies.forEach(currency -> {
                assertThat(currency.currencyName()).isNotBlank();
            });
        }

        @Test
        @DisplayName("특정 통화들이 응답에 포함된다")
        void containsExpectedCurrencies() {
            // when: 실제 HTTP GET 요청
            ResponseEntity<SuccessResponse<List<CurrencyResponse>>> response = restTemplate.exchange(
                    "/currency",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            // then: 예상되는 통화 이름들이 포함되어야 함
            SuccessResponse<List<CurrencyResponse>> body = response.getBody();
            assertThat(body).isNotNull();

            List<CurrencyResponse> currencies = body.result();
            assertThat(currencies).isNotNull();

            List<String> currencyNames = currencies.stream()
                    .map(CurrencyResponse::currencyName)
                    .toList();

            // 최소한 이 통화들은 포함되어야 함
            List<String> expectedCurrencyNames = Arrays.stream(Currency.values())
                    .map(Currency::getCurrencyName)
                    .toList();

            assertThat(currencyNames).containsAll(expectedCurrencyNames);
        }
    }

    @Nested
    @DisplayName("POST /currency/calculate")
    class CalculateTotalPrice {

        @Test
        @DisplayName("환율이 캐시에 있을 때 올바른 가격을 계산하여 반환한다")
        void withRateInCache_calculatesCorrectPrice() {
            // given: 주문 가격 계산 요청 (USD -> KRW)
            List<OrderElementRequest> orders = List.of(
                    new OrderElementRequest(new BigDecimal("10.00"), new BigDecimal("2")),
                    new OrderElementRequest(new BigDecimal("5.00"), new BigDecimal("1"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest(
                    "United States Dollar",
                    "South Korean won",
                    orders
            );

            // when: 실제 HTTP POST 요청
            ResponseEntity<SuccessResponse<CalculatePriceResponse>> response = restTemplate.exchange(
                    "/currency/calculate",
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    new ParameterizedTypeReference<>() {}
            );

            // then: 상태 코드 200 OK
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // then: SuccessResponse 검증
            SuccessResponse<CalculatePriceResponse> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.status()).isEqualTo(200);

            // then: 가격이 올바르게 계산되어야 함
            CalculatePriceResponse responseBody = body.result();
            assertThat(responseBody).isNotNull();

            // 원화 총액: (10*2 + 5*1) = 25 USD
            assertThat(responseBody.originTotalPrice().value()).isEqualByComparingTo("25.00");
            assertThat(responseBody.originTotalPrice().currencyCode()).isEqualTo("USD");

            // 사용자 통화 총액: 25 * 1350.50 = 33762.50 KRW
            assertThat(responseBody.userTotalPrice().value()).isEqualByComparingTo("33762.50");
            assertThat(responseBody.userTotalPrice().currencyCode()).isEqualTo("KRW");
        }

        @Test
        @DisplayName("동일 통화 간 변환 시 1:1 비율로 계산된다")
        void withSameCurrency_returnsOneToOneRate() {
            // given: 동일 통화로 계산 요청 (USD -> USD)
            List<OrderElementRequest> orders = List.of(
                    new OrderElementRequest(new BigDecimal("10.00"), new BigDecimal("2"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest(
                    "United States Dollar",
                    "United States Dollar",
                    orders
            );

            // when: 실제 HTTP POST 요청
            ResponseEntity<SuccessResponse<CalculatePriceResponse>> response = restTemplate.exchange(
                    "/currency/calculate",
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    new ParameterizedTypeReference<>() {}
            );

            // then: 상태 코드 200 OK
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // then: 원화와 사용자 통화가 동일해야 함
            CalculatePriceResponse responseBody = response.getBody().result();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.originTotalPrice().value())
                    .isEqualByComparingTo(responseBody.userTotalPrice().value());
        }

        @Test
        @DisplayName("다양한 통화 조합에서 정확히 계산된다 (JPY -> KRW)")
        void withDifferentCurrencyPair_calculatesCorrectly() {
            // given: JPY -> KRW 변환 요청
            List<OrderElementRequest> orders = List.of(
                    new OrderElementRequest(new BigDecimal("1000.00"), new BigDecimal("1"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest(
                    "Japanese Yen",
                    "South Korean won",
                    orders
            );

            // when: 실제 HTTP POST 요청
            ResponseEntity<SuccessResponse<CalculatePriceResponse>> response = restTemplate.exchange(
                    "/currency/calculate",
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    new ParameterizedTypeReference<>() {}
            );

            // then: 상태 코드 200 OK
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // then: 가격이 올바르게 계산되어야 함
            CalculatePriceResponse responseBody = response.getBody().result();
            assertThat(responseBody).isNotNull();

            assertThat(responseBody.originTotalPrice().value()).isEqualByComparingTo("1000.00");
            assertThat(responseBody.originTotalPrice().currencyCode()).isEqualTo("JPY");

            // 1000 JPY * 9.00 = 9000 KRW
            assertThat(responseBody.userTotalPrice().value()).isEqualByComparingTo("9000.00");
            assertThat(responseBody.userTotalPrice().currencyCode()).isEqualTo("KRW");
        }

        @Test
        @Disabled("TODO: Reactor의 에러 처리 메커니즘으로 인해 예상과 다른 응답이 반환됨. 추후 수정 필요")
        @DisplayName("환율이 캐시에 없을 때 에러를 반환한다")
        void withNoRateInCache_returnsError() {
            // given: 캐시에 없는 환율 쌍 (EUR -> CNY)
            List<OrderElementRequest> orders = List.of(
                    new OrderElementRequest(new BigDecimal("10.00"), new BigDecimal("1"))
            );
            CalculatePriceRequest request = new CalculatePriceRequest(
                    "Euro",
                    "Chinese Yuan",
                    orders
            );

            // when: 실제 HTTP POST 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    "/currency/calculate",
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    String.class
            );

            // then: 에러 상태 코드 반환 (4xx 또는 5xx)
            assertThat(response.getStatusCode().isError()).isTrue();
        }

        @Test
        @DisplayName("빈 주문 목록으로 요청 시 총액이 0이다")
        void withEmptyOrders_returnsTotalPriceZero() {
            // given: 빈 주문 목록
            List<OrderElementRequest> orders = List.of();
            CalculatePriceRequest request = new CalculatePriceRequest(
                    "United States Dollar",
                    "South Korean won",
                    orders
            );

            // when: 실제 HTTP POST 요청
            ResponseEntity<SuccessResponse<CalculatePriceResponse>> response = restTemplate.exchange(
                    "/currency/calculate",
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    new ParameterizedTypeReference<>() {}
            );

            // then: 상태 코드 200 OK
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // then: 총액이 0이어야 함
            CalculatePriceResponse responseBody = response.getBody().result();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.originTotalPrice().value()).isEqualByComparingTo("0.00");
            assertThat(responseBody.userTotalPrice().value()).isEqualByComparingTo("0.00");
        }
    }
}
