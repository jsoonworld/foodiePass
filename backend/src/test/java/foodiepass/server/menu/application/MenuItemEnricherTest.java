package foodiepass.server.menu.application;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.application.CurrencyService;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.dto.response.ReconfigureResponse.FoodItemResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.PriceInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuItemEnricherTest {

    private MenuItemEnricher menuItemEnricher;

    @Mock
    private FoodScrapper foodScraper;
    @Mock
    private TranslationClient translationClient;
    @Mock
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        menuItemEnricher = new MenuItemEnricher(foodScraper, translationClient, currencyService);
    }

    @Test
    @DisplayName("메뉴 아이템 정보를 비동기적으로 보강하여 FoodItemResponse를 반환한다")
    void enrichAsync_shouldEnrichMenuItemAndReturnResponse() {
        // given
        Price originalPrice = new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("13000"));
        FoodInfo dummyFoodInfo = new FoodInfo("Kimchi Jjigae", "initial", "initial.jpg", "initial_preview.jpg");
        MenuItem menuItem = new MenuItem("Kimchi Jjigae", originalPrice, dummyFoodInfo);

        Language originLanguage = Language.fromLanguageName("Korean");
        Language userLanguage = Language.fromLanguageName("Japanese");
        Currency originCurrency = Currency.SOUTH_KOREAN_WON;
        Currency userCurrency = Currency.JAPANESE_YEN;

        FoodInfo scrapedFoodInfo = new FoodInfo("Kimchi Stew", "Spicy kimchi stew with pork", "image.jpg", "preview.jpg");
        PriceInfoResponse priceInfoResponse = new PriceInfoResponse("₩13,000", "¥1,300");

        // Mocking
        when(translationClient.translateAsync(originLanguage, Language.fromLanguageName("English"), "Kimchi Jjigae"))
                .thenReturn(Mono.just("Kimchi Stew"));
        when(foodScraper.scrapAsync(List.of("Kimchi Stew")))
                .thenReturn(Flux.just(scrapedFoodInfo));
        when(translationClient.translateAsync(Language.fromLanguageName("English"), userLanguage, "Kimchi Stew"))
                .thenReturn(Mono.just("キムチチゲ"));
        when(translationClient.translateAsync(Language.fromLanguageName("English"), userLanguage, "Spicy kimchi stew with pork"))
                .thenReturn(Mono.just("豚肉入りの辛いキムチチゲ"));
        when(currencyService.convertAndFormatAsync(any(Price.class), eq(userCurrency)))
                .thenReturn(Mono.just(priceInfoResponse));

        // when
        Mono<FoodItemResponse> result = menuItemEnricher.enrichAsync(menuItem, originLanguage, userLanguage, originCurrency, userCurrency);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.originMenuName().equals("Kimchi Jjigae") &&
                                response.translatedMenuName().equals("キムチチゲ") &&
                                response.description().equals("豚肉入りの辛いキムチチゲ") &&
                                response.image().equals("image.jpg") &&
                                response.priceInfo().equals(priceInfoResponse)
                )
                .verifyComplete();
    }
}
