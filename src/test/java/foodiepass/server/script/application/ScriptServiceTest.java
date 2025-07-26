package foodiepass.server.script.application;

import foodiepass.server.order.domain.OrderItem;
import foodiepass.server.script.domain.Script;
import foodiepass.server.script.dto.request.MenuItemRequest;
import foodiepass.server.script.dto.request.ScriptGenerateRequest;
import foodiepass.server.script.dto.response.ScriptResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static foodiepass.server.language.domain.Language.JAPANESE;
import static foodiepass.server.language.domain.Language.KOREAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ScriptServiceTest {

    @InjectMocks
    private ScriptService scriptService;

    @Mock
    private ScriptFactory scriptFactory;

    @Test
    @DisplayName("스크립트 생성 요청을 받으면 DTO를 변환하여 팩토리를 호출하고 결과를 반환한다")
    void generateScript_success() {
        // given
        List<MenuItemRequest> menuItemRequests = List.of(
                new MenuItemRequest("Kimchi Jjigae", 1, new BigDecimal("8000"), "KRW")
        );

        ScriptGenerateRequest request = new ScriptGenerateRequest(
                "Korean",
                "Japanese",
                menuItemRequests
        );

        String travelerScript = "주문할게요\n1 Kimchi Jjigae";
        String localScript = "注文します\n1 キムチチゲ";
        Script mockScript = new Script(travelerScript, localScript);

        when(scriptFactory.createAsync(eq(KOREAN), eq(JAPANESE), any(List.class)))
                .thenReturn(Mono.just(mockScript));

        // when
        Mono<ScriptResponse> responseMono = scriptService.generateScript(request);

        // then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.travelerScript()).isEqualTo(travelerScript);
                    assertThat(response.localScript()).isEqualTo(localScript);
                })
                .verifyComplete();

        ArgumentCaptor<List<OrderItem>> orderItemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(scriptFactory, times(1)).createAsync(eq(KOREAN), eq(JAPANESE), orderItemsCaptor.capture());

        List<OrderItem> capturedOrderItems = orderItemsCaptor.getValue();
        assertThat(capturedOrderItems).hasSize(1);
        assertThat(capturedOrderItems.get(0).getName()).isEqualTo("Kimchi Jjigae");
        assertThat(capturedOrderItems.get(0).getQuantity()).isEqualTo(1);
    }
}
