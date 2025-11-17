package foodiepass.server.menu.infra.scraper.tasteAtlas;

import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.menu.application.port.out.FoodScraper;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import foodiepass.server.menu.infra.scraper.tasteAtlas.dto.TasteAtlasResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component("tasteAtlasFoodScraper")
@Profile("!local & !performance-test")  // local 환경에서는 MockFoodScraper 사용
@RequiredArgsConstructor
public class TasteAtlasFoodScraper implements FoodScraper {

    private final TasteAtlasApiClient apiClient;
    private final TasteAtlasPageParser pageParser;
    private final TasteAtlasProperties properties;

    private final Map<String, Mono<FoodInfo>> foodInfoCache = new ConcurrentHashMap<>();

    @Override
    public Flux<FoodInfo> scrapAsync(final List<String> foodNames) {
        log.info("배치 스크래핑 시작: {} 개 음식", foodNames.size());
        return Flux.fromIterable(foodNames)
                .delayElements(Duration.ofMillis(300))  // 각 요청 사이 0.3초 대기
                .flatMap(this::getFoodInfo, 3);  // 최대 3개씩 동시 처리
    }

    private Mono<FoodInfo> getFoodInfo(String foodName) {
        return foodInfoCache.computeIfAbsent(foodName, this::fetchAndCache);
    }

    private Mono<FoodInfo> fetchAndCache(String foodName) {
        log.info("TasteAtlas 스크래핑 시작: foodName='{}'", foodName);

        return apiClient.search(foodName)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .flatMap(response -> Mono.justOrEmpty(findFirstItem(response)))
                .flatMap(item -> {
                    String fullUrl = properties.baseUrl() + item.urlLink();
                    return apiClient.fetchHtml(fullUrl)
                            .flatMap(html -> pageParser.parse(html, item));
                })
                .doOnSuccess(foodInfo -> log.info("스크래핑 성공: foodName='{}'", foodInfo.getName()))
                .onErrorResume(error -> {
                    log.error("'{}' 정보 스크래핑 실패. 기본 정보를 반환합니다.", foodName, error);
                    return Mono.just(getDefaultFoodInfo(foodName));
                })
                .cache(Duration.ofHours(1));
    }

    private FoodInfo getDefaultFoodInfo(String foodName) {
        log.warn("기본(Default) FoodInfo 반환: foodName='{}'", foodName);
        return new FoodInfo(
                foodName,
                properties.defaults().description(),
                properties.defaults().imageUrl(),
                properties.defaults().imageUrl()
        );
    }

    private Optional<TasteAtlasResponse.Item> findFirstItem(TasteAtlasResponse response) {
        if (response == null) {
            return Optional.empty();
        }
        if (!CollectionUtils.isEmpty(response.customItems())) {
            return Optional.of(response.customItems().get(0));
        }
        if (!CollectionUtils.isEmpty(response.items())) {
            return Optional.of(response.items().get(0));
        }
        log.debug("TasteAtlas 응답에서 아이템을 찾지 못했습니다.");
        return Optional.empty();
    }
}
