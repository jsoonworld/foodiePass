package foodiepass.server.menu.application;

import foodiepass.server.currency.application.CurrencyService;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.dto.response.ReconfigureResponse.FoodItemResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.PriceInfoResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MenuItemEnricher {

    private final FoodScrapper foodScraper;
    private final TranslationClient translationClient;
    private final CurrencyService currencyService;

    private static final Language ENGLISH = Language.fromLanguageName("English");

    public MenuItemEnricher(
            @Qualifier("tasteAtlasFoodScrapper") FoodScrapper foodScraper,
            TranslationClient translationClient,
            CurrencyService currencyService
    ) {
        this.foodScraper = foodScraper;
        this.translationClient = translationClient;
        this.currencyService = currencyService;
    }

    public Mono<FoodItemResponse> enrichAsync(
            final MenuItem menuItem,
            final Language originLanguage,
            final Language userLanguage,
            final Currency originCurrency,
            final Currency userCurrency
    ) {
        Mono<String> engNameMono = translationClient.translateAsync(originLanguage, ENGLISH, menuItem.getName());

        return engNameMono.flatMap(engName ->
                foodScraper.scrapAsync(List.of(engName))
                        .next()
                        .flatMap(scrapedFoodInfo -> {
                            Mono<String> translatedNameMono = translationClient.translateAsync(ENGLISH, userLanguage, scrapedFoodInfo.getName());
                            Mono<String> translatedDescriptionMono = translationClient.translateAsync(ENGLISH, userLanguage, scrapedFoodInfo.getDescription());

                            Mono<PriceInfoResponse> priceInfoMono = currencyService.convertAndFormatAsync(menuItem.getPrice(), userCurrency);

                            return Mono.zip(translatedNameMono, translatedDescriptionMono, priceInfoMono)
                                    .map(tuple -> new FoodItemResponse(
                                            menuItem.getName(),
                                            tuple.getT1(),
                                            tuple.getT2(),
                                            scrapedFoodInfo.getImage(),
                                            tuple.getT3()
                                    ));
                        })
        );
    }
}
