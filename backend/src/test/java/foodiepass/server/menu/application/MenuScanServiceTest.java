package foodiepass.server.menu.application;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.menu.dto.request.MenuScanRequest;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.MenuScanResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuScanService Unit Tests")
class MenuScanServiceTest {

    @Mock
    private ABTestService abTestService;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuScanService menuScanService;

    private MenuScanRequest validRequest;
    private String testUserId;
    private ReconfigureResponse mockReconfigureResponse;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";

        validRequest = new MenuScanRequest(
            "base64EncodedImage",
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        mockReconfigureResponse = new ReconfigureResponse(
            List.of(
                new ReconfigureResponse.FoodItemResponse(
                    "Sushi",
                    "스시",
                    "Fresh raw fish with rice",
                    "https://example.com/sushi.jpg",
                    new ReconfigureResponse.PriceInfoResponse("¥1,500", "₩20,000")
                ),
                new ReconfigureResponse.FoodItemResponse(
                    "Ramen",
                    "라멘",
                    "Japanese noodle soup",
                    "https://example.com/ramen.jpg",
                    new ReconfigureResponse.PriceInfoResponse("¥800", "₩10,500")
                )
            )
        );
    }

    @Test
    @DisplayName("CONTROL 그룹: description과 image가 null이어야 함")
    void scanMenu_CONTROL_group_excludes_food_info() {
        // Given
        MenuScan controlScan = MenuScan.create(
            testUserId,
            ABGroup.CONTROL,
            null,
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), eq("ja"), eq("ko"), eq("JPY"), eq("KRW")
        )).thenReturn(controlScan);

        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(mockReconfigureResponse));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.abGroup()).isEqualTo("CONTROL");
                assertThat(response.scanId()).isNotNull();
                assertThat(response.items()).hasSize(2);
                assertThat(response.processingTime()).isGreaterThanOrEqualTo(0.0);

                // CONTROL 그룹: description과 image가 null
                response.items().forEach(item -> {
                    assertThat(item.description()).isNull();
                    assertThat(item.imageUrl()).isNull();
                    assertThat(item.originalName()).isNotNull();
                    assertThat(item.translatedName()).isNotNull();
                    assertThat(item.priceInfo()).isNotNull();
                });

                // Verify price info for first item
                var firstItem = response.items().get(0);
                assertThat(firstItem.originalName()).isEqualTo("Sushi");
                assertThat(firstItem.translatedName()).isEqualTo("스시");
                assertThat(firstItem.priceInfo().originalAmount()).isEqualTo(1500.0);
                assertThat(firstItem.priceInfo().originalCurrency()).isEqualTo("JPY");
                assertThat(firstItem.priceInfo().convertedAmount()).isEqualTo(20000.0);
                assertThat(firstItem.priceInfo().convertedCurrency()).isEqualTo("KRW");
            })
            .verifyComplete();

        verify(abTestService, times(1)).assignAndCreateScan(
            eq(testUserId), isNull(), eq("ja"), eq("ko"), eq("JPY"), eq("KRW")
        );
        verify(menuService, times(1)).reconfigure(any(ReconfigureRequest.class));
    }

    @Test
    @DisplayName("TREATMENT 그룹: 모든 필드가 포함되어야 함")
    void scanMenu_TREATMENT_group_includes_food_info() {
        // Given
        MenuScan treatmentScan = MenuScan.create(
            testUserId,
            ABGroup.TREATMENT,
            null,
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), eq("ja"), eq("ko"), eq("JPY"), eq("KRW")
        )).thenReturn(treatmentScan);

        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(mockReconfigureResponse));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.abGroup()).isEqualTo("TREATMENT");
                assertThat(response.scanId()).isNotNull();
                assertThat(response.items()).hasSize(2);
                assertThat(response.processingTime()).isGreaterThanOrEqualTo(0.0);

                // TREATMENT 그룹: 모든 필드 포함
                response.items().forEach(item -> {
                    assertThat(item.description()).isNotNull();
                    assertThat(item.imageUrl()).isNotNull();
                    assertThat(item.originalName()).isNotNull();
                    assertThat(item.translatedName()).isNotNull();
                    assertThat(item.priceInfo()).isNotNull();
                });

                // Verify full data for first item
                var firstItem = response.items().get(0);
                assertThat(firstItem.originalName()).isEqualTo("Sushi");
                assertThat(firstItem.translatedName()).isEqualTo("스시");
                assertThat(firstItem.description()).isEqualTo("Fresh raw fish with rice");
                assertThat(firstItem.imageUrl()).isEqualTo("https://example.com/sushi.jpg");
                assertThat(firstItem.priceInfo().originalAmount()).isEqualTo(1500.0);
                assertThat(firstItem.priceInfo().convertedAmount()).isEqualTo(20000.0);
            })
            .verifyComplete();

        verify(abTestService, times(1)).assignAndCreateScan(
            eq(testUserId), isNull(), eq("ja"), eq("ko"), eq("JPY"), eq("KRW")
        );
        verify(menuService, times(1)).reconfigure(any(ReconfigureRequest.class));
    }

    @Test
    @DisplayName("처리 시간이 올바르게 계산되어야 함")
    void scanMenu_calculates_processing_time() {
        // Given
        MenuScan scan = MenuScan.create(testUserId, ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW");

        when(abTestService.assignAndCreateScan(any(), any(), any(), any(), any(), any()))
            .thenReturn(scan);

        // Simulate slow operation with delay
        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(mockReconfigureResponse).delayElement(java.time.Duration.ofMillis(100)));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.processingTime()).isGreaterThanOrEqualTo(0.1);
                assertThat(response.processingTime()).isLessThan(1.0);  // Should be under 1 second
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("다양한 통화 형식의 가격 파싱 테스트")
    void scanMenu_handles_various_price_formats() {
        // Given
        ReconfigureResponse diversePriceResponse = new ReconfigureResponse(
            List.of(
                new ReconfigureResponse.FoodItemResponse(
                    "Item1", "아이템1", "desc", "img",
                    new ReconfigureResponse.PriceInfoResponse("$15.00", "₩20,000")
                ),
                new ReconfigureResponse.FoodItemResponse(
                    "Item2", "아이템2", "desc", "img",
                    new ReconfigureResponse.PriceInfoResponse("€10.50", "₩14,500")
                ),
                new ReconfigureResponse.FoodItemResponse(
                    "Item3", "아이템3", "desc", "img",
                    new ReconfigureResponse.PriceInfoResponse("£8.99", "₩12,000")
                )
            )
        );

        MenuScan scan = MenuScan.create(testUserId, ABGroup.TREATMENT, null, "en", "ko", "USD", "KRW");

        when(abTestService.assignAndCreateScan(any(), any(), any(), any(), any(), any()))
            .thenReturn(scan);
        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(diversePriceResponse));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                var items = response.items();
                assertThat(items).hasSize(3);

                // USD
                assertThat(items.get(0).priceInfo().originalAmount()).isEqualTo(15.0);
                assertThat(items.get(0).priceInfo().originalCurrency()).isEqualTo("USD");

                // EUR
                assertThat(items.get(1).priceInfo().originalAmount()).isEqualTo(10.5);
                assertThat(items.get(1).priceInfo().originalCurrency()).isEqualTo("EUR");

                // GBP
                assertThat(items.get(2).priceInfo().originalAmount()).isEqualTo(8.99);
                assertThat(items.get(2).priceInfo().originalCurrency()).isEqualTo("GBP");

                // All converted to KRW
                items.forEach(item -> {
                    assertThat(item.priceInfo().convertedCurrency()).isEqualTo("KRW");
                });
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("빈 응답에 대한 처리 테스트")
    void scanMenu_handles_empty_response() {
        // Given
        ReconfigureResponse emptyResponse = new ReconfigureResponse(List.of());
        MenuScan scan = MenuScan.create(testUserId, ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");

        when(abTestService.assignAndCreateScan(any(), any(), any(), any(), any(), any()))
            .thenReturn(scan);
        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(emptyResponse));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.items()).isEmpty();
                assertThat(response.scanId()).isNotNull();
                assertThat(response.abGroup()).isEqualTo("CONTROL");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("ReconfigureRequest가 올바르게 생성되는지 테스트")
    void scanMenu_creates_correct_reconfigure_request() {
        // Given
        MenuScan scan = MenuScan.create(testUserId, ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");

        when(abTestService.assignAndCreateScan(any(), any(), any(), any(), any(), any()))
            .thenReturn(scan);
        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(mockReconfigureResponse));

        // When
        menuScanService.scanMenu(validRequest, testUserId).block();

        // Then
        verify(menuService).reconfigure(argThat(request -> {
            assertThat(request.base64EncodedImage()).isEqualTo("base64EncodedImage");
            assertThat(request.originLanguageName()).isEqualTo("ja");
            assertThat(request.userLanguageName()).isEqualTo("ko");
            assertThat(request.originCurrencyName()).isEqualTo("JPY");
            assertThat(request.userCurrencyName()).isEqualTo("KRW");
            return true;
        }));
    }

    @Test
    @DisplayName("null 가격 정보 처리 테스트")
    void scanMenu_handles_null_price_info() {
        // Given
        ReconfigureResponse responseWithNullPrice = new ReconfigureResponse(
            List.of(
                new ReconfigureResponse.FoodItemResponse(
                    "Item", "아이템", "desc", "img",
                    new ReconfigureResponse.PriceInfoResponse(null, null)
                )
            )
        );

        MenuScan scan = MenuScan.create(testUserId, ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW");

        when(abTestService.assignAndCreateScan(any(), any(), any(), any(), any(), any()))
            .thenReturn(scan);
        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(responseWithNullPrice));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                var priceInfo = response.items().get(0).priceInfo();
                assertThat(priceInfo.originalAmount()).isEqualTo(0.0);
                assertThat(priceInfo.originalCurrency()).isEqualTo("UNKNOWN");
                assertThat(priceInfo.convertedAmount()).isEqualTo(0.0);
                assertThat(priceInfo.convertedCurrency()).isEqualTo("UNKNOWN");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("잘못된 형식의 가격 문자열 처리 테스트")
    void scanMenu_handles_invalid_price_format() {
        // Given
        ReconfigureResponse responseWithInvalidPrice = new ReconfigureResponse(
            List.of(
                new ReconfigureResponse.FoodItemResponse(
                    "Item", "아이템", "desc", "img",
                    new ReconfigureResponse.PriceInfoResponse("invalid", "also-invalid")
                )
            )
        );

        MenuScan scan = MenuScan.create(testUserId, ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");

        when(abTestService.assignAndCreateScan(any(), any(), any(), any(), any(), any()))
            .thenReturn(scan);
        when(menuService.reconfigure(any(ReconfigureRequest.class)))
            .thenReturn(Mono.just(responseWithInvalidPrice));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                var priceInfo = response.items().get(0).priceInfo();
                assertThat(priceInfo.originalAmount()).isEqualTo(0.0);
                assertThat(priceInfo.convertedAmount()).isEqualTo(0.0);
                assertThat(priceInfo.originalCurrency()).isEqualTo("UNKNOWN");
                assertThat(priceInfo.convertedCurrency()).isEqualTo("UNKNOWN");
            })
            .verifyComplete();
    }
}
