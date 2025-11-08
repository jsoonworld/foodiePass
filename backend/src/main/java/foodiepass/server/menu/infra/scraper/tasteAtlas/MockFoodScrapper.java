package foodiepass.server.menu.infra.scraper.tasteAtlas;

import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component("mockFoodScrapper")
@Profile("local")
@RequiredArgsConstructor
public class MockFoodScrapper implements FoodScrapper {

    private final TasteAtlasProperties properties;

    @Override
    public Flux<FoodInfo> scrapAsync(final List<String> foodNames) {
        log.info("[MockFoodScrapper] 배치 스크래핑 시작 (Mock): {} 개 음식", foodNames.size());
        return Flux.fromIterable(foodNames)
                .delayElements(Duration.ofMillis(50))  // 빠른 응답을 위해 짧은 딜레이
                .flatMap(this::getMockFoodInfo);
    }

    private Mono<FoodInfo> getMockFoodInfo(String foodName) {
        log.info("[MockFoodScrapper] Mock FoodInfo 반환: foodName='{}'", foodName);
        return Mono.just(new FoodInfo(
                foodName,
                properties.defaults().description(),
                properties.defaults().imageUrl(),
                properties.defaults().imageUrl()
        ));
    }
}
