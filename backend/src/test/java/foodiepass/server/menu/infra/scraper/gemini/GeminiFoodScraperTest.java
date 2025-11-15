package foodiepass.server.menu.infra.scraper.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.menu.domain.FoodInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiFoodScraperTest {

    private GeminiFoodScraper geminiFoodScraper;

    @Mock
    private GeminiClient geminiClient;

    // 실제 ObjectMapper를 사용합니다.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        geminiFoodScraper = new GeminiFoodScraper(geminiClient, objectMapper);
    }

    @Test
    @DisplayName("음식 이름 목록으로 비동기 스크래핑 요청 시 FoodInfo Flux를 반환한다")
    void scrapAsync_shouldReturnFoodInfoFlux() {
        // given
        final String foodName = "Pizza";
        final String jsonResponse = "{\"name\":\"Pizza\",\"description\":\"A delicious pizza\",\"image\":\"pizza.jpg\",\"previewImage\":\"preview.jpg\"}";

        // GeminiClient의 generateText가 호출되면 미리 정의된 jsonResponse를 반환하도록 설정합니다.
        when(geminiClient.generateText(anyString())).thenReturn(jsonResponse);

        // when
        final Flux<FoodInfo> result = geminiFoodScraper.scrapAsync(List.of(foodName));

        // then
        StepVerifier.create(result)
                .expectNextMatches(foodInfo ->
                        foodInfo.getName().equals("Pizza") &&
                                foodInfo.getDescription().equals("A delicious pizza")
                )
                .verifyComplete();

        // geminiClient가 한 번 호출되었는지 확인합니다.
        verify(geminiClient, times(1)).generateText(anyString());
    }

    @Test
    @DisplayName("동일한 음식에 대해 여러 번 요청해도 캐시를 사용하여 한 번만 스크래핑한다")
    void scrapAsync_withSameFoodName_shouldUseCache() {
        // given
        final String foodName = "Pasta";
        final String jsonResponse = "{\"name\":\"Pasta\",\"description\":\"Yummy pasta\",\"image\":\"pasta.jpg\",\"previewImage\":\"preview.jpg\"}";
        when(geminiClient.generateText(anyString())).thenReturn(jsonResponse);

        final List<String> foodNames = List.of(foodName, foodName); // 동일한 음식을 두 번 요청

        // when
        final Flux<FoodInfo> result = geminiFoodScraper.scrapAsync(foodNames);

        // then
        // 결과 스트림을 소비하여 스크래핑이 실행되도록 합니다.
        final List<FoodInfo> foodInfos = result.collectList().block();
        assertThat(foodInfos).hasSize(2); // 결과는 2개가 맞는지 확인

        // 캐시 덕분에 geminiClient는 한 번만 호출되어야 합니다.
        verify(geminiClient, times(1)).generateText(anyString());
    }
}
