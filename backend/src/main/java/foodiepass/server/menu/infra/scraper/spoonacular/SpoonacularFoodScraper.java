package foodiepass.server.menu.infra.scraper.spoonacular;

import foodiepass.server.menu.application.port.out.FoodScraper;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.config.SpoonacularProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component("spoonacularFoodScraper")
@Primary
@Profile("local")
@RequiredArgsConstructor
public class SpoonacularFoodScraper implements FoodScraper {

    private final WebClient webClient;
    private final SpoonacularProperties properties;

    @Override
    public Flux<FoodInfo> scrapAsync(final List<String> foodNames) {
        log.info("[SpoonacularFoodScraper] Starting batch scraping: {} foods", foodNames.size());
        return Flux.fromIterable(foodNames)
                .delayElements(Duration.ofMillis(200))  // Rate limiting
                .flatMap(this::searchFood)
                .onErrorContinue((error, obj) ->
                        log.warn("[SpoonacularFoodScraper] Error scraping food '{}': {}", obj, error.getMessage())
                );
    }

    private Mono<FoodInfo> searchFood(String foodName) {
        log.debug("[SpoonacularFoodScraper] Searching food: '{}'", foodName);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.getApi().searchPath())
                        .queryParam("query", foodName)
                        .queryParam("number", properties.getApi().maxResults())
                        .queryParam("apiKey", properties.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(SpoonacularSearchResponse.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> {
                    if (response.menuItems() != null && !response.menuItems().isEmpty()) {
                        SpoonacularMenuItem item = response.menuItems().get(0);
                        log.info("[SpoonacularFoodScraper] Found food: '{}' -> image: {}", foodName, item.image());
                        return new FoodInfo(
                                item.title(),
                                properties.getDefaults().description(),  // Spoonacular menu item search doesn't include description
                                item.image(),
                                item.image()
                        );
                    } else {
                        log.warn("[SpoonacularFoodScraper] No results for '{}', using defaults", foodName);
                        return createDefaultFoodInfo(foodName);
                    }
                })
                .onErrorResume(error -> {
                    log.error("[SpoonacularFoodScraper] Error searching food '{}': {}", foodName, error.getMessage());
                    return Mono.just(createDefaultFoodInfo(foodName));
                });
    }

    private FoodInfo createDefaultFoodInfo(String foodName) {
        return new FoodInfo(
                foodName,
                properties.getDefaults().description(),
                properties.getDefaults().imageUrl(),
                properties.getDefaults().imageUrl()
        );
    }
}
