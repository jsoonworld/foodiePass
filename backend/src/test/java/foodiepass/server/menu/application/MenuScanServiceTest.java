package foodiepass.server.menu.application;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.repository.MenuScanRepository;
import foodiepass.server.cache.domain.MenuItemEntity;
import foodiepass.server.cache.repository.MenuItemRepository;
import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.dto.request.MenuScanRequest;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuScanService Unit Tests")
class MenuScanServiceTest {

    @Mock
    private ABTestService abTestService;

    @Mock
    private MenuItemEnricher menuItemEnricher;

    @Mock
    private OcrReader ocrReader;

    @Mock
    private MenuScanRepository menuScanRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuScanService menuScanService;

    private MenuScanRequest validRequest;
    private String testUserId;
    private List<MenuItem> mockMenuItems;
    private List<ReconfigureResponse.FoodItemResponse> mockFoodItemResponses;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";

        validRequest = new MenuScanRequest(
            "base64EncodedImage",
            "Japanese",
            "Korean",
            "Japanese Yen",
            "South Korean won"
        );

        // Mock OCR results (MenuItem)
        mockMenuItems = List.of(
            new MenuItem(
                "Sushi",
                new Price(Currency.JAPANESE_YEN, new BigDecimal("1500")),
                new FoodInfo("Sushi", "Initial", "initial.jpg", "preview.jpg")
            ),
            new MenuItem(
                "Ramen",
                new Price(Currency.JAPANESE_YEN, new BigDecimal("800")),
                new FoodInfo("Ramen", "Initial", "initial.jpg", "preview.jpg")
            )
        );

        // Mock enriched results (FoodItemResponse)
        mockFoodItemResponses = List.of(
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
            "Japanese",
            "Korean",
            "Japanese Yen",
            "South Korean won"
        );
        controlScan.setImageHash("test-image-hash");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(controlScan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
            .thenReturn(Mono.just(mockFoodItemResponses));

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

        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        );
        verify(ocrReader, times(1)).read(anyString());
        verify(menuItemEnricher, times(1)).enrichBatchAsync(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("TREATMENT 그룹: 모든 필드가 포함되어야 함")
    void scanMenu_TREATMENT_group_includes_food_info() {
        // Given
        MenuScan treatmentScan = MenuScan.create(
            testUserId,
            ABGroup.TREATMENT,
            null,
            "Japanese",
            "Korean",
            "Japanese Yen",
            "South Korean won"
        );
        treatmentScan.setImageHash("test-image-hash");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(treatmentScan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
            .thenReturn(Mono.just(mockFoodItemResponses));

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

        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        );
        verify(ocrReader, times(1)).read(anyString());
        verify(menuItemEnricher, times(1)).enrichBatchAsync(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("처리 시간이 올바르게 계산되어야 함")
    void scanMenu_calculates_processing_time() {
        // Given
        MenuScan scan = MenuScan.create(testUserId, ABGroup.TREATMENT, null, "Japanese", "Korean", "Japanese Yen", "South Korean won");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(scan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment with delay (simulate slow operation)
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
            .thenReturn(Mono.just(mockFoodItemResponses).delayElement(java.time.Duration.ofMillis(100)));

        // When
        Mono<MenuScanResponse> result = menuScanService.scanMenu(validRequest, testUserId);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.processingTime()).isGreaterThanOrEqualTo(0.1);
                assertThat(response.processingTime()).isLessThan(1.0);  // Should be under 1 second
            })
            .verifyComplete();

        // Verify
        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(any(), isNull(), anyString(), any(), any(), any(), any());
        verify(ocrReader, times(1)).read(anyString());
        verify(menuItemEnricher, times(1)).enrichBatchAsync(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("다양한 통화 형식의 가격 파싱 테스트")
    void scanMenu_handles_various_price_formats() {
        // Given
        List<ReconfigureResponse.FoodItemResponse> diversePriceResponses = List.of(
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
        );

        MenuScan scan = MenuScan.create(testUserId, ABGroup.TREATMENT, null, "English", "Korean", "United States Dollar", "South Korean won");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(scan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment with diverse price formats
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
            .thenReturn(Mono.just(diversePriceResponses));

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

        // Verify
        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(any(), isNull(), anyString(), any(), any(), any(), any());
        verify(ocrReader, times(1)).read(anyString());
        verify(menuItemEnricher, times(1)).enrichBatchAsync(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("빈 응답에 대한 처리 테스트")
    void scanMenu_handles_empty_response() {
        // Given
        List<ReconfigureResponse.FoodItemResponse> emptyResponses = List.of();
        MenuScan scan = MenuScan.create(testUserId, ABGroup.CONTROL, null, "Japanese", "Korean", "Japanese Yen", "South Korean won");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(scan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment with empty response
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
            .thenReturn(Mono.just(emptyResponses));

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

        // Verify
        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(any(), isNull(), anyString(), any(), any(), any(), any());
        verify(ocrReader, times(1)).read(anyString());
        verify(menuItemEnricher, times(1)).enrichBatchAsync(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("enrichBatchAsync가 올바른 파라미터로 호출되는지 테스트")
    void scanMenu_creates_correct_reconfigure_request() {
        // Given
        MenuScan scan = MenuScan.create(testUserId, ABGroup.CONTROL, null, "Japanese", "Korean", "Japanese Yen", "South Korean won");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(scan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
            .thenReturn(Mono.just(mockFoodItemResponses));

        // When
        menuScanService.scanMenu(validRequest, testUserId).block();

        // Then
        verify(menuItemEnricher).enrichBatchAsync(
            argThat(items -> items.size() == 2),
            eq(Language.JAPANESE),
            eq(Language.KOREAN),
            eq(Currency.JAPANESE_YEN),
            eq(Currency.SOUTH_KOREAN_WON)
        );

        // Verify
        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(any(), isNull(), anyString(), any(), any(), any(), any());
        verify(ocrReader, times(1)).read(anyString());
    }

    @Test
    @DisplayName("null 가격 정보 처리 테스트")
    void scanMenu_handles_null_price_info() {
        // Given
        List<ReconfigureResponse.FoodItemResponse> responseWithNullPrice = List.of(
            new ReconfigureResponse.FoodItemResponse(
                "Item", "아이템", "desc", "img",
                new ReconfigureResponse.PriceInfoResponse(null, null)
            )
        );

        MenuScan scan = MenuScan.create(testUserId, ABGroup.TREATMENT, null, "Japanese", "Korean", "Japanese Yen", "South Korean won");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(scan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment with null price
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
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

        // Verify
        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(any(), isNull(), anyString(), any(), any(), any(), any());
        verify(ocrReader, times(1)).read(anyString());
        verify(menuItemEnricher, times(1)).enrichBatchAsync(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("잘못된 형식의 가격 문자열 처리 테스트")
    void scanMenu_handles_invalid_price_format() {
        // Given
        List<ReconfigureResponse.FoodItemResponse> responseWithInvalidPrice = List.of(
            new ReconfigureResponse.FoodItemResponse(
                "Item", "아이템", "desc", "img",
                new ReconfigureResponse.PriceInfoResponse("invalid", "also-invalid")
            )
        );

        MenuScan scan = MenuScan.create(testUserId, ABGroup.CONTROL, null, "Japanese", "Korean", "Japanese Yen", "South Korean won");

        // Mock: Cache miss
        when(menuScanRepository.findByImageHash(anyString())).thenReturn(Optional.empty());

        // Mock: A/B test assignment
        when(abTestService.assignAndCreateScan(
            eq(testUserId), isNull(), anyString(), eq("Japanese"), eq("Korean"), eq("Japanese Yen"), eq("South Korean won")
        )).thenReturn(scan);

        // Mock: OCR reading
        when(ocrReader.read(anyString())).thenReturn(mockMenuItems);

        // Mock: Save menu items
        when(menuItemRepository.saveAll(any())).thenReturn(null);

        // Mock: Enrichment with invalid price format
        when(menuItemEnricher.enrichBatchAsync(any(), any(Language.class), any(Language.class),
            any(Currency.class), any(Currency.class)))
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

        // Verify
        verify(menuScanRepository, times(1)).findByImageHash(anyString());
        verify(abTestService, times(1)).assignAndCreateScan(any(), isNull(), anyString(), any(), any(), any(), any());
        verify(ocrReader, times(1)).read(anyString());
        verify(menuItemEnricher, times(1)).enrichBatchAsync(any(), any(), any(), any(), any());
    }
}
