package foodiepass.server.menu.infra.scraper.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.exception.GeminiErrorCode;
import foodiepass.server.menu.infra.exception.GeminiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component("geminiFoodScrapper")
@Profile(ProfileConstants.NOT_PERFORMANCE_TEST)
@RequiredArgsConstructor
public class GeminiFoodScrapper implements FoodScrapper {

    private static final String FOOD_INFO_PROMPT_TEMPLATE = """
        Get the 200-character description and image url for food %s.
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
        log.info("Gemini 배치 음식 정보 스크래핑 시작: {} 개 음식", foodNames.size());
        return Flux.fromIterable(foodNames)
                .delayElements(Duration.ofMillis(500))  // 각 요청 사이 0.5초 대기
                .flatMap(this::getFoodInfoReactively, 2);  // 최대 2개씩 동시 처리
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
