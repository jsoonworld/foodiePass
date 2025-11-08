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
            FoodScrapper foodScraper,
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

    public Mono<List<FoodItemResponse>> enrichBatchAsync(
            final List<MenuItem> menuItems,
            final Language originLanguage,
            final Language userLanguage,
            final Currency originCurrency,
            final Currency userCurrency
    ) {
        if (menuItems.isEmpty()) {
            return Mono.just(List.of());
        }

        // Step 1: Batch translate menu names to English (for food matching) AND to user language (for display)
        List<String> menuNames = menuItems.stream()
                .map(MenuItem::getName)
                .toList();

        Mono<List<String>> engNamesMono = translationClient.translateAsync(originLanguage, ENGLISH, menuNames)
                .collectList()
                .onErrorResume(e -> {
                    log.warn("메뉴 이름 영문 번역 실패. 원본을 사용합니다.", e);
                    return Mono.just(menuNames);
                });

        Mono<List<String>> translatedMenuNamesMono = translationClient.translateAsync(originLanguage, userLanguage, menuNames)
                .collectList()
                .onErrorResume(e -> {
                    log.warn("메뉴 이름 사용자 언어 번역 실패. 원본을 사용합니다.", e);
                    return Mono.just(menuNames);
                });

        return Mono.zip(engNamesMono, translatedMenuNamesMono)
                .flatMap(tuple -> {
                    List<String> engNames = tuple.getT1();
                    List<String> translatedMenuNames = tuple.getT2();

                    // Step 2: Batch scrape food info
                    return foodScraper.scrapAsync(engNames)
                            .collectList()
                            .flatMap(foodInfos -> {
                                // Step 3: Batch translate food names and descriptions
                                List<String> foodNames = foodInfos.stream()
                                        .map(FoodInfo::getName)
                                        .toList();
                                List<String> foodDescriptions = foodInfos.stream()
                                        .map(FoodInfo::getDescription)
                                        .toList();

                                Mono<List<String>> translatedNamesMono = translationClient.translateAsync(ENGLISH, userLanguage, foodNames)
                                        .collectList()
                                        .onErrorResume(e -> {
                                            log.warn("배치 음식 이름 번역 실패. 원본을 사용합니다.", e);
                                            return Mono.just(foodNames);
                                        });

                                Mono<List<String>> translatedDescriptionsMono = translationClient.translateAsync(ENGLISH, userLanguage, foodDescriptions)
                                        .collectList()
                                        .onErrorResume(e -> {
                                            log.warn("배치 음식 설명 번역 실패. 원본을 사용합니다.", e);
                                            return Mono.just(foodDescriptions);
                                        });

                                // Step 4: Convert prices
                                return Mono.zip(translatedNamesMono, translatedDescriptionsMono)
                                        .flatMap(translationTuple -> {
                                            List<String> translatedNames = translationTuple.getT1();
                                            List<String> translatedDescriptions = translationTuple.getT2();

                                            List<Mono<FoodItemResponse>> responseMonos = new java.util.ArrayList<>();
                                            for (int i = 0; i < menuItems.size(); i++) {
                                                MenuItem menuItem = menuItems.get(i);
                                                String translatedMenuName = i < translatedMenuNames.size() ? translatedMenuNames.get(i) : menuItem.getName();
                                                String translatedName = i < translatedNames.size() ? translatedNames.get(i) : engNames.get(i);
                                                String translatedDescription = i < translatedDescriptions.size() ? translatedDescriptions.get(i) : "설명 없음";
                                                FoodInfo foodInfo = i < foodInfos.size() ? foodInfos.get(i) : new FoodInfo(engNames.get(i), "상세 정보 없음", "", "");

                                                Mono<FoodItemResponse> responseMono = currencyService.convertAndFormatAsync(menuItem.getPrice(), userCurrency)
                                                        .map(priceInfo -> new FoodItemResponse(
                                                                translatedMenuName,
                                                                translatedName,
                                                                translatedDescription,
                                                                foodInfo.getImage(),
                                                                priceInfo
                                                        ))
                                                        .onErrorResume(e -> Mono.just(new FoodItemResponse(
                                                                translatedMenuName,
                                                                translatedName,
                                                                translatedDescription,
                                                                foodInfo.getImage(),
                                                                new PriceInfoResponse("N/A", "N/A")
                                                        )));

                                                responseMonos.add(responseMono);
                                            }

                                            return Mono.zip(responseMonos, objects -> {
                                                List<FoodItemResponse> responses = new java.util.ArrayList<>();
                                                for (Object obj : objects) {
                                                    responses.add((FoodItemResponse) obj);
                                                }
                                                return responses;
                                            });
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("배치 번역 중 오류 발생. 개별 처리로 fallback합니다.", e);
                    // Fallback to individual processing
                    return reactor.core.publisher.Flux.fromIterable(menuItems)
                            .flatMap(menuItem -> enrichAsync(menuItem, originLanguage, userLanguage, originCurrency, userCurrency))
                            .collectList();
                });
    }
}
