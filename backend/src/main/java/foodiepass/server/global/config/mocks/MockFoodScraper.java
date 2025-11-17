package foodiepass.server.global.config.mocks;

import foodiepass.server.menu.application.port.out.FoodScraper;
import foodiepass.server.menu.domain.FoodInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Profile("performance-test")
public class MockFoodScraper implements FoodScraper {

    private static final Map<String, FoodInfo> MOCK_DATA = Map.of(
            "김치찌개", new FoodInfo("김치찌개", "매콤하고 맛있는 김치찌개", "mock_kimchi.jpg", "mock_kimchi_preview.jpg"),
            "된장찌개", new FoodInfo("된장찌개", "구수한 한국의 맛 된장찌개", "mock_doenjang.jpg", "mock_doenjang_preview.jpg"),
            "제육볶음", new FoodInfo("제육볶음", "매콤달콤한 돼지고기 볶음", "mock_jeyuk.jpg", "mock_jeyuk_preview.jpg")
    );

    @Override
    public Flux<FoodInfo> scrapAsync(final List<String> foodNames) {
        List<FoodInfo> results = foodNames.stream()
                .map(name -> MOCK_DATA.getOrDefault(name, new FoodInfo(name, "설명 없음", "default.jpg", "default_preview.jpg")))
                .collect(Collectors.toList());
        return Flux.fromIterable(results);
    }
}
