package foodiepass.server.script.application;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.language.domain.Language;
import foodiepass.server.order.domain.OrderItem;
import foodiepass.server.script.domain.Script;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static foodiepass.server.language.domain.Language.ENGLISH;
import static foodiepass.server.language.domain.Language.JAPANESE;
import static foodiepass.server.language.domain.Language.KOREAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptFactoryTest {

    @InjectMocks
    private ScriptFactory scriptFactory;

    @Mock
    private TranslationClient translationClient;

    @Mock
    private Price mockPrice;

    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        scriptFactory.init();

        MenuItem kimchiJjigae = createTestMenuItem("Kimchi Jjigae");
        MenuItem bibimbap = createTestMenuItem("Bibimbap");

        orderItems = List.of(
                new OrderItem(kimchiJjigae, 1),
                new OrderItem(bibimbap, 2)
        );
    }

    private MenuItem createTestMenuItem(String name) {
        FoodInfo foodInfo = new FoodInfo(name, "description", "image.url", "preview.url");
        return new MenuItem(name, mockPrice, foodInfo);
    }

    @Nested
    @DisplayName("스크립트 생성 시")
    class CreateScript {

        @Test
        @DisplayName("캐시에 없는 언어는 접두사와 전체 스크립트를 모두 번역한다")
        void createScript_withCacheMiss() {
            // given
            final Language sourceLanguage = KOREAN;
            final Language targetLanguage = JAPANESE;
            final String koreanPrefix = "주문할게요";
            final String travelerScript = koreanPrefix + "\n" + "1 Kimchi Jjigae\n2 Bibimbap";
            final String localScript = "注文します\n1 キムチチゲ\n2 ビビンバ";

            when(translationClient.translate(eq(ENGLISH), eq(sourceLanguage), eq("Hello I want to order")))
                    .thenReturn(koreanPrefix);
            when(translationClient.translate(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript)))
                    .thenReturn(localScript);

            // when
            Script result = scriptFactory.create(sourceLanguage, targetLanguage, orderItems);

            // then
            assertThat(result.getTravelerScript()).isEqualTo(travelerScript);
            assertThat(result.getLocalScript()).isEqualTo(localScript);

            verify(translationClient, times(1)).translate(eq(ENGLISH), eq(sourceLanguage), eq("Hello I want to order"));
            verify(translationClient, times(1)).translate(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript));
        }

        @Test
        @DisplayName("캐시에 있는 언어는 접두사 번역을 생략한다")
        void createScript_withCacheHit() {
            // given
            final Language sourceLanguage = KOREAN;
            final Language targetLanguage = JAPANESE;
            final String koreanPrefix = "주문할게요";
            final String travelerScript = koreanPrefix + "\n" + "1 Kimchi Jjigae\n2 Bibimbap";
            final String firstLocalScript = "첫번째 번역 결과...";
            final String secondLocalScript = "두번째 번역 결과...";

            when(translationClient.translate(eq(ENGLISH), eq(sourceLanguage), eq("Hello I want to order")))
                    .thenReturn(koreanPrefix);
            when(translationClient.translate(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript)))
                    .thenReturn(firstLocalScript, secondLocalScript);

            // when: 첫 번째 호출 (캐시 저장)
            scriptFactory.create(sourceLanguage, targetLanguage, orderItems);
            // when: 두 번째 호출 (캐시 사용)
            Script result = scriptFactory.create(sourceLanguage, targetLanguage, orderItems);

            // then
            assertThat(result.getTravelerScript()).isEqualTo(travelerScript);
            assertThat(result.getLocalScript()).isEqualTo(secondLocalScript);

            verify(translationClient, times(1)).translate(eq(ENGLISH), eq(sourceLanguage), eq("Hello I want to order"));
            verify(translationClient, times(2)).translate(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript));
        }

        @Test
        @DisplayName("소스 언어가 영어이면 접두사 번역을 생략한다")
        void createScript_withEnglishSource() {
            // given
            final Language sourceLanguage = ENGLISH;
            final Language targetLanguage = JAPANESE;
            final String travelerScript = "Hello I want to order\n1 Kimchi Jjigae\n2 Bibimbap";
            final String localScript = "こんにちは、注文したいです...";

            when(translationClient.translate(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript)))
                    .thenReturn(localScript);

            // when
            Script result = scriptFactory.create(sourceLanguage, targetLanguage, orderItems);

            // then
            assertThat(result.getTravelerScript()).isEqualTo(travelerScript);
            assertThat(result.getLocalScript()).isEqualTo(localScript);

            verify(translationClient, times(1)).translate(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript));
        }
    }
}
