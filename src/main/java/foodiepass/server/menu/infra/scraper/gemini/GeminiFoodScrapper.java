package foodiepass.server.menu.infra.scraper.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.exception.GeminiErrorCode;
import foodiepass.server.menu.infra.exception.GeminiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GeminiFoodScrapper implements FoodScrapper {

    private static final String FOOD_INFO_PROMPT_TEMPLATE = """
        Get the 200-character description and image url for food %s from the tasteatlas site
        Please print it out in valid JSON format
        {
            "image": "valid image url of food (String)",
            "description": "200-character description of food(String)"
        }
        """;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Mono<FoodInfo>> foodInfoCache = new ConcurrentHashMap<>();

    @Override
    public Flux<FoodInfo> scrapAsync(final List<String> foodNames) {
        return Flux.fromIterable(foodNames)
                .flatMap(this::getFoodInfoReactively);
    }

    private Mono<FoodInfo> getFoodInfoReactively(String foodName) {
        return foodInfoCache.computeIfAbsent(foodName, this::scrapFoodInfoReactively);
    }

    private Mono<FoodInfo> scrapFoodInfoReactively(final String foodName) {
        return Mono.fromCallable(() -> {
                    try {
                        final String prompt = createPromptForFoodInfo(foodName);
                        final String jsonResponse = geminiClient.generateText(prompt);
                        return objectMapper.readValue(jsonResponse, FoodInfo.class);
                    } catch (final JsonProcessingException e) {
                        throw new GeminiException(GeminiErrorCode.FOOD_INFO_SCRAP_FAILED);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .cache();
    }

    private String createPromptForFoodInfo(final String foodName) {
        return String.format(FOOD_INFO_PROMPT_TEMPLATE, foodName);
    }
}
