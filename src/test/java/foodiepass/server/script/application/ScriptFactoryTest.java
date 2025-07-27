package foodiepass.server.script.application;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static foodiepass.server.language.domain.Language.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScriptFactory 클래스")
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
    @DisplayName("createAsync 메소드는")
    class CreateAsync {
        @Test
        @DisplayName("캐시에 없는 언어는 접두사와 전체 스크립트를 모두 번역한다")
        void createScript_withCacheMiss() {
            // given
            final Language sourceLanguage = KOREAN;
            final Language targetLanguage = JAPANESE;
            final String travelerScriptPrefix = "주문할게요";
            final String travelerScript = travelerScriptPrefix + "\n" + "1 Kimchi Jjigae\n2 Bibimbap";
            final String localScript = "注文します\n1 キムチチゲ\n2 ビビンバ";

            when(translationClient.translateAsync(eq(ENGLISH), eq(sourceLanguage), anyString()))
                    .thenReturn(Mono.just(travelerScriptPrefix));
            when(translationClient.translateAsync(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript)))
                    .thenReturn(Mono.just(localScript));

            // when
            Mono<Script> resultMono = scriptFactory.createAsync(sourceLanguage, targetLanguage, orderItems);

            // then
            StepVerifier.create(resultMono)
                    .assertNext(script -> {
                        assertThat(script.getTravelerScript()).isEqualTo(travelerScript);
                        assertThat(script.getLocalScript()).isEqualTo(localScript);
                    })
                    .verifyComplete();

            verify(translationClient, times(1)).translateAsync(eq(ENGLISH), eq(sourceLanguage), anyString());
            verify(translationClient, times(1)).translateAsync(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript));
        }

        @Test
        @DisplayName("캐시에 있는 언어는 접두사 번역을 생략한다")
        void createScript_withCacheHit() {
            // given
            final Language sourceLanguage = KOREAN;
            final Language targetLanguage = JAPANESE;
            final String travelerScriptPrefix = "주문할게요";
            final String travelerScript = travelerScriptPrefix + "\n" + "1 Kimchi Jjigae\n2 Bibimbap";
            final String localScript = "注文します\n1 キムチチゲ\n2 ビビンバ";

            when(translationClient.translateAsync(eq(ENGLISH), eq(sourceLanguage), anyString()))
                    .thenReturn(Mono.just(travelerScriptPrefix));
            when(translationClient.translateAsync(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript)))
                    .thenReturn(Mono.just(localScript));

            // when
            scriptFactory.createAsync(sourceLanguage, targetLanguage, orderItems).block();

            // when
            Mono<Script> resultMono = scriptFactory.createAsync(sourceLanguage, targetLanguage, orderItems);

            // then
            StepVerifier.create(resultMono).expectNextCount(1).verifyComplete();

            verify(translationClient, times(1)).translateAsync(eq(ENGLISH), eq(sourceLanguage), anyString());
        }

        @Test
        @DisplayName("소스 언어가 영어이면 접두사 번역을 생략한다")
        void createScript_withEnglishSource() {
            // given
            final Language sourceLanguage = ENGLISH;
            final Language targetLanguage = JAPANESE;
            final String travelerScript = "Hello I want to order\n1 Kimchi Jjigae\n2 Bibimbap";
            final String localScript = "こんにちは、注文したいです...";

            when(translationClient.translateAsync(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript)))
                    .thenReturn(Mono.just(localScript));

            // when
            Mono<Script> resultMono = scriptFactory.createAsync(sourceLanguage, targetLanguage, orderItems);

            // then
            StepVerifier.create(resultMono).expectNextCount(1).verifyComplete();

            verify(translationClient, never()).translateAsync(eq(ENGLISH), eq(sourceLanguage), anyString());
            verify(translationClient, times(1)).translateAsync(eq(sourceLanguage), eq(targetLanguage), eq(travelerScript));
        }
    }
}
