package foodiepass.server.menu.application;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.ReconfigureResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.FoodItemResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.PriceInfoResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock
    private OcrReader ocrReader;
    @Mock
    private MenuItemEnricher menuItemEnricher;

    @Test
    @DisplayName("메뉴 재구성 요청 시 OCR과 정보 보강을 거쳐 응답을 반환한다")
    void reconfigure_shouldProcessOcrAndEnrichment() {
        // given
        ReconfigureRequest request = new ReconfigureRequest(
                "base64image",
                "Korean",
                "English",
                Currency.SOUTH_KOREAN_WON.getCurrencyName(),
                Currency.UNITED_STATES_DOLLAR.getCurrencyName()
        );

        FoodInfo dummyFoodInfo = new FoodInfo("김치찌개", "dummy", "dummy.jpg", "dummy.jpg");
        MenuItem menuItem1 = new MenuItem("김치찌개", new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("8000")), dummyFoodInfo);
        List<MenuItem> ocrResult = List.of(menuItem1);

        PriceInfoResponse priceInfo = new PriceInfoResponse("₩8,000", "$6.00");
        FoodItemResponse enrichedItem1 = new FoodItemResponse("김치찌개", "Kimchi Stew", "Spicy stew", "kimchi.jpg", priceInfo);

        // Mocking
        when(ocrReader.read(request.base64EncodedImage())).thenReturn(ocrResult);
        when(menuItemEnricher.enrichAsync(eq(menuItem1), any(Language.class), any(Language.class), any(Currency.class), any(Currency.class)))
                .thenReturn(Mono.just(enrichedItem1));

        // when
        Mono<ReconfigureResponse> responseMono = menuService.reconfigure(request);

        // then
        StepVerifier.create(responseMono)
                .expectNextMatches(response -> {
                    assertThat(response.results()).hasSize(1);
                    assertThat(response.results().get(0).translatedMenuName()).isEqualTo("Kimchi Stew");
                    assertThat(response.results().get(0).priceInfo().userPriceWithCurrencyUnit()).isEqualTo("$6.00");
                    return true;
                })
                .verifyComplete();

        verify(ocrReader, times(1)).read(request.base64EncodedImage());
        verify(menuItemEnricher, times(1)).enrichAsync(any(), any(), any(), any(), any());
    }
}
