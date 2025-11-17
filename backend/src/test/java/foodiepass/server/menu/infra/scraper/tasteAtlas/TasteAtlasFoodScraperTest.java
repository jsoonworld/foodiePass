package foodiepass.server.menu.infra.scraper.tasteAtlas;

import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import foodiepass.server.menu.infra.exception.ScrapingErrorCode; // ErrorCode를 import 합니다.
import foodiepass.server.menu.infra.exception.ScrapingException;
import foodiepass.server.menu.infra.scraper.tasteAtlas.dto.TasteAtlasResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasteAtlasFoodScraperTest {

    private TasteAtlasFoodScraper foodScrapper;

    @Mock
    private TasteAtlasApiClient apiClient;
    @Mock
    private TasteAtlasPageParser pageParser;

    // 테스트용 Properties 객체를 만듭니다.
    private TasteAtlasProperties properties;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 기본값들을 설정합니다.
        properties = new TasteAtlasProperties(
                "https://www.tasteatlas.com",
                null, // api
                new TasteAtlasProperties.Defaults("default_image.jpg", "default description"),
                null // selector
        );
        foodScrapper = new TasteAtlasFoodScraper(apiClient, pageParser, properties);
    }

    @Test
    @DisplayName("스크래핑 성공 시 FoodInfo 객체를 반환한다")
    void scrapAsync_onSuccess_returnsFoodInfo() {
        // given
        final String foodName = "Ramen";
        final TasteAtlasResponse.Item item = new TasteAtlasResponse.Item("Ramen", null, "Japanese noodle soup", null, "/ramen");
        final TasteAtlasResponse apiResponse = new TasteAtlasResponse(List.of(item), null);
        final String html = "<html></html>";
        final FoodInfo expectedFoodInfo = new FoodInfo("Ramen", "A tasty ramen.", "ramen.jpg", "ramen_preview.jpg");

        // Mock 객체들의 동작을 정의합니다.
        when(apiClient.search(foodName)).thenReturn(Mono.just(apiResponse));
        when(apiClient.fetchHtml(anyString())).thenReturn(Mono.just(html));
        when(pageParser.parse(html, item)).thenReturn(Mono.just(expectedFoodInfo));

        // when
        final Flux<FoodInfo> result = foodScrapper.scrapAsync(List.of(foodName));

        // then
        StepVerifier.create(result)
                .expectNext(expectedFoodInfo)
                .verifyComplete();
    }

    @Test
    @DisplayName("API 호출에서 에러 발생 시 기본 FoodInfo 객체를 반환한다")
    void scrapAsync_onApiError_returnsDefaultFoodInfo() {
        // given
        final String foodName = "Sushi";
        // apiClient.search가 ScrapingException 에러를 반환하도록 설정
        // null 대신 실제 ErrorCode를 사용하여 예외를 생성합니다.
        final ScrapingErrorCode errorCode = ScrapingErrorCode.TASTE_ATLAS_API_REQUEST_FAILED;
        when(apiClient.search(foodName)).thenReturn(Mono.error(new ScrapingException(errorCode)));

        // when
        final Flux<FoodInfo> result = foodScrapper.scrapAsync(List.of(foodName));

        // then
        StepVerifier.create(result)
                .expectNextMatches(foodInfo ->
                        foodInfo.getName().equals(foodName) &&
                                foodInfo.getDescription().equals(properties.defaults().description()) &&
                                foodInfo.getImage().equals(properties.defaults().imageUrl())
                )
                .verifyComplete();

        // 에러가 났으므로 pageParser는 호출되지 않아야 합니다.
        verify(pageParser, never()).parse(anyString(), any());
    }
}
