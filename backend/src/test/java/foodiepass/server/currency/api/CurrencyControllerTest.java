package foodiepass.server.currency.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.currency.application.CurrencyService;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.request.CalculatePriceRequest.OrderElementRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.dto.response.CalculatePriceResponse.FormattedPrice;
import foodiepass.server.currency.dto.response.CurrencyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyController API Tests")
class CurrencyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /currency - 모든 통화 목록 조회 성공")
    void getCurrencies_success() throws Exception {
        // Given
        List<CurrencyResponse> mockCurrencies = List.of(
            new CurrencyResponse("KRW"),
            new CurrencyResponse("USD"),
            new CurrencyResponse("JPY"),
            new CurrencyResponse("EUR")
        );

        when(currencyService.findAllCurrencies()).thenReturn(mockCurrencies);

        // When & Then
        MvcResult mvcResult = mockMvc.perform(get("/api/currency")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].currencyName").value("KRW"))
            .andExpect(jsonPath("$[1].currencyName").value("USD"))
            .andExpect(jsonPath("$[2].currencyName").value("JPY"))
            .andExpect(jsonPath("$[3].currencyName").value("EUR"));
    }

    @Test
    @DisplayName("GET /currency - 빈 목록 응답")
    void getCurrencies_empty_list() throws Exception {
        // Given
        when(currencyService.findAllCurrencies()).thenReturn(List.of());

        // When & Then
        MvcResult mvcResult = mockMvc.perform(get("/api/currency")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /currency/calculate - 환율 계산 성공")
    void calculateTotalPrice_success() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "JPY",
            "KRW",
            List.of(
                new OrderElementRequest(new BigDecimal("1500"), new BigDecimal("2")),
                new OrderElementRequest(new BigDecimal("800"), new BigDecimal("1"))
            )
        );

        FormattedPrice originPrice = new FormattedPrice(
            new BigDecimal("3800.00"),
            "JPY",
            "¥3,800"
        );

        FormattedPrice userPrice = new FormattedPrice(
            new BigDecimal("50000.00"),
            "KRW",
            "₩50,000"
        );

        CalculatePriceResponse mockResponse = new CalculatePriceResponse(originPrice, userPrice);

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTotalPrice.value").value(3800.00))
            .andExpect(jsonPath("$.originTotalPrice.currencyCode").value("JPY"))
            .andExpect(jsonPath("$.originTotalPrice.formatted").value("¥3,800"))
            .andExpect(jsonPath("$.userTotalPrice.value").value(50000.00))
            .andExpect(jsonPath("$.userTotalPrice.currencyCode").value("KRW"))
            .andExpect(jsonPath("$.userTotalPrice.formatted").value("₩50,000"));
    }

    @Test
    @DisplayName("POST /currency/calculate - 단일 주문 항목 계산")
    void calculateTotalPrice_single_order() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "USD",
            "KRW",
            List.of(new OrderElementRequest(new BigDecimal("15.50"), new BigDecimal("1")))
        );

        FormattedPrice originPrice = new FormattedPrice(
            new BigDecimal("15.50"),
            "USD",
            "$15.50"
        );

        FormattedPrice userPrice = new FormattedPrice(
            new BigDecimal("20500.00"),
            "KRW",
            "₩20,500"
        );

        CalculatePriceResponse mockResponse = new CalculatePriceResponse(originPrice, userPrice);

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTotalPrice.value").value(15.50))
            .andExpect(jsonPath("$.userTotalPrice.value").value(20500.00));
    }

    @Test
    @DisplayName("POST /currency/calculate - 다수 주문 항목 계산")
    void calculateTotalPrice_multiple_orders() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "EUR",
            "KRW",
            List.of(
                new OrderElementRequest(new BigDecimal("10.50"), new BigDecimal("3")),
                new OrderElementRequest(new BigDecimal("8.00"), new BigDecimal("2")),
                new OrderElementRequest(new BigDecimal("12.75"), new BigDecimal("1"))
            )
        );

        FormattedPrice originPrice = new FormattedPrice(
            new BigDecimal("60.25"),
            "EUR",
            "€60.25"
        );

        FormattedPrice userPrice = new FormattedPrice(
            new BigDecimal("85000.00"),
            "KRW",
            "₩85,000"
        );

        CalculatePriceResponse mockResponse = new CalculatePriceResponse(originPrice, userPrice);

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTotalPrice.value").value(60.25))
            .andExpect(jsonPath("$.originTotalPrice.currencyCode").value("EUR"))
            .andExpect(jsonPath("$.userTotalPrice.value").value(85000.00))
            .andExpect(jsonPath("$.userTotalPrice.currencyCode").value("KRW"));
    }

    @Test
    @DisplayName("POST /currency/calculate - 소수점 처리 검증")
    void calculateTotalPrice_decimal_precision() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "USD",
            "KRW",
            List.of(new OrderElementRequest(new BigDecimal("9.99"), new BigDecimal("1")))
        );

        FormattedPrice originPrice = new FormattedPrice(
            new BigDecimal("9.99"),
            "USD",
            "$9.99"
        );

        FormattedPrice userPrice = new FormattedPrice(
            new BigDecimal("13200.00"),
            "KRW",
            "₩13,200"
        );

        CalculatePriceResponse mockResponse = new CalculatePriceResponse(originPrice, userPrice);

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTotalPrice.value").value(9.99))
            .andExpect(jsonPath("$.userTotalPrice.value").value(13200.00));
    }

    @Test
    @DisplayName("POST /currency/calculate - 동일 통화 간 환율 계산")
    void calculateTotalPrice_same_currency() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "KRW",
            "KRW",
            List.of(new OrderElementRequest(new BigDecimal("10000"), new BigDecimal("2")))
        );

        FormattedPrice price = new FormattedPrice(
            new BigDecimal("20000.00"),
            "KRW",
            "₩20,000"
        );

        CalculatePriceResponse mockResponse = new CalculatePriceResponse(price, price);

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTotalPrice.value").value(20000.00))
            .andExpect(jsonPath("$.userTotalPrice.value").value(20000.00))
            .andExpect(jsonPath("$.originTotalPrice.currencyCode").value("KRW"))
            .andExpect(jsonPath("$.userTotalPrice.currencyCode").value("KRW"));
    }

    @Test
    @DisplayName("POST /currency/calculate - 빈 주문 목록 처리")
    void calculateTotalPrice_empty_orders() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "USD",
            "KRW",
            List.of()
        );

        FormattedPrice originPrice = new FormattedPrice(
            new BigDecimal("0.00"),
            "USD",
            "$0.00"
        );

        FormattedPrice userPrice = new FormattedPrice(
            new BigDecimal("0.00"),
            "KRW",
            "₩0"
        );

        CalculatePriceResponse mockResponse = new CalculatePriceResponse(originPrice, userPrice);

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTotalPrice.value").value(0.00))
            .andExpect(jsonPath("$.userTotalPrice.value").value(0.00));
    }

    @Test
    @DisplayName("POST /currency/calculate - Service 에러 발생 시 에러 전파")
    void calculateTotalPrice_service_error() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "INVALID",
            "KRW",
            List.of(new OrderElementRequest(new BigDecimal("100"), new BigDecimal("1")))
        );

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.error(new RuntimeException("Invalid currency code")));

        // When & Then
        try {
            MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            // Exception during async processing is also acceptable
            assert e.getCause() instanceof RuntimeException
                || e instanceof jakarta.servlet.ServletException;
        }
    }

    @Test
    @DisplayName("POST /currency/calculate - 응답 형식 검증")
    void calculateTotalPrice_response_format() throws Exception {
        // Given
        CalculatePriceRequest request = new CalculatePriceRequest(
            "JPY",
            "KRW",
            List.of(new OrderElementRequest(new BigDecimal("1000"), new BigDecimal("1")))
        );

        FormattedPrice originPrice = new FormattedPrice(
            new BigDecimal("1000.00"),
            "JPY",
            "¥1,000"
        );

        FormattedPrice userPrice = new FormattedPrice(
            new BigDecimal("13000.00"),
            "KRW",
            "₩13,000"
        );

        CalculatePriceResponse mockResponse = new CalculatePriceResponse(originPrice, userPrice);

        when(currencyService.calculateOrdersPriceAsync(any(CalculatePriceRequest.class)))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/currency/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTotalPrice").exists())
            .andExpect(jsonPath("$.originTotalPrice.value").exists())
            .andExpect(jsonPath("$.originTotalPrice.currencyCode").exists())
            .andExpect(jsonPath("$.originTotalPrice.formatted").exists())
            .andExpect(jsonPath("$.userTotalPrice").exists())
            .andExpect(jsonPath("$.userTotalPrice.value").exists())
            .andExpect(jsonPath("$.userTotalPrice.currencyCode").exists())
            .andExpect(jsonPath("$.userTotalPrice.formatted").exists());
    }
}
