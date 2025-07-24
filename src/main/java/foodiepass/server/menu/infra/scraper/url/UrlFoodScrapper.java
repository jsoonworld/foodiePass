package foodiepass.server.menu.infra.scraper.url;

import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import foodiepass.server.menu.infra.scraper.tasteAtlas.TasteAtlasApiClient;
import foodiepass.server.menu.infra.scraper.tasteAtlas.TasteAtlasPageParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class UrlFoodScrapper implements FoodScrapper {

    private final TasteAtlasApiClient apiClient;
    private final TasteAtlasPageParser pageParser;
    private final TasteAtlasProperties properties;

    private final Map<String, Mono<FoodInfo>> foodInfoCache = new ConcurrentHashMap<>();

    @Override
    public Flux<FoodInfo> scrapAsync(final List<String> foodNames) {
        return Flux.fromIterable(foodNames)
                .flatMap(this::getFoodInfo);
    }

    private Mono<FoodInfo> getFoodInfo(String foodName) {
        return foodInfoCache.computeIfAbsent(foodName, this::scrapAndCache);
    }

    private Mono<FoodInfo> scrapAndCache(String foodName) {
        return apiClient.search(foodName)
                .flatMap(response ->
                        Flux.fromIterable(response.customItems())
                                .concatWith(Flux.fromIterable(response.items()))
                                .next()
                )
                .flatMap(item -> {
                    String fullUrl = properties.baseUrl() + item.urlLink();
                    return apiClient.fetchHtml(fullUrl)
                            .flatMap(html -> pageParser.parse(html, item));
                })
                .onErrorResume(error -> {
                    System.err.println("Failed to scrap info for " + foodName + ": " + error.getMessage());
                    return Mono.just(getDefaultFoodInfo(foodName));
                })
                .cache(Duration.ofHours(1));
    }

    private FoodInfo getDefaultFoodInfo(String foodName) {
        return new FoodInfo(
                foodName,
                properties.defaults().description(),
                properties.defaults().imageUrl(),
                properties.defaults().imageUrl()
        );
    }
}
