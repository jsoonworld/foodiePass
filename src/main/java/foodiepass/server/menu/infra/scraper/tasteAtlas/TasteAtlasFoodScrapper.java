package foodiepass.server.menu.infra.scraper.tasteAtlas;

import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import foodiepass.server.menu.infra.scraper.tasteAtlas.dto.TasteAtlasResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component("tasteAtlasFoodScrapper")
@RequiredArgsConstructor
public class TasteAtlasFoodScrapper implements FoodScrapper {

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
        Mono<FoodInfo> cachedMono = foodInfoCache.get(foodName);
        if (cachedMono != null) {
            log.debug("캐시 히트: foodName='{}'", foodName);
            return cachedMono;
        }
        log.debug("캐시 미스: foodName='{}'", foodName);
        return foodInfoCache.computeIfAbsent(foodName, this::fetchAndCache);
    }

    private Mono<FoodInfo> fetchAndCache(String foodName) {
        log.info("TasteAtlas 스크래핑 시작: foodName='{}'", foodName);

        return apiClient.search(foodName)
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

    private String generateFullName(TasteAtlasResponse.Item item) {
        if (!StringUtils.hasText(item.otherName())) {
            return item.name();
        }
        return String.format("%s (%s)", item.name(), item.otherName());
    }
}
