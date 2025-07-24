package foodiepass.server.menu.application;

import foodiepass.server.currency.application.CurrencyService;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.dto.response.ReconfigureResponse.FoodItemResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.PriceInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
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
        Mono<String> engNameMono = translationClient.translateAsync(originLanguage, ENGLISH, menuItem.getName())
                .onErrorResume(e -> {
                    log.warn("영문 번역 실패: '{}'. 원본 이름을 사용합니다.", menuItem.getName(), e);
                    return Mono.just(menuItem.getName());
                });

        return engNameMono.flatMap(engName ->
                foodScraper.scrapAsync(List.of(engName))
                        .next()
                        .onErrorResume(e -> {
                            log.warn("스크래핑 실패: '{}'. 기본 FoodInfo를 사용합니다.", engName, e);
                            return Mono.just(new FoodInfo(engName, "상세 정보를 불러올 수 없습니다.", "", ""));
                        })
                        .flatMap(scrapedFoodInfo -> {
                            Mono<String> translatedNameMono = translationClient.translateAsync(ENGLISH, userLanguage, scrapedFoodInfo.getName())
                                    .onErrorResume(e -> Mono.just(scrapedFoodInfo.getName()));

                            Mono<String> translatedDescriptionMono = translationClient.translateAsync(ENGLISH, userLanguage, scrapedFoodInfo.getDescription())
                                    .onErrorResume(e -> Mono.just("상세 설명을 번역할 수 없습니다."));

                            Mono<PriceInfoResponse> priceInfoMono = currencyService.convertAndFormatAsync(menuItem.getPrice(), userCurrency)
                                    .onErrorResume(e -> Mono.just(new PriceInfoResponse("N/A", "N/A")));

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
