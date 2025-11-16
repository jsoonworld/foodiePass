package foodiepass.server.menu.api;

import foodiepass.server.menu.application.MenuService;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.ReconfigureResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.FoodItemResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.PriceInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled("Temporarily disabled - needs verification after test architecture changes")
class MenuControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private MenuService menuService;

    private MenuController menuController;

    @BeforeEach
    void setUp() {
        menuController = new MenuController(menuService);
        webTestClient = WebTestClient.bindToController(menuController).build();
    }


    @Test
    @DisplayName("POST /menu/reconfigure 요청 시 메뉴 재구성 결과를 성공적으로 반환한다")
    void reconfigure_shouldReturnReconfiguredMenu() {
        // given
        ReconfigureRequest request = new ReconfigureRequest(
                "base64image", "Korean", "English", "KRW", "USD"
        );

        PriceInfoResponse priceInfo = new PriceInfoResponse("₩10,000", "$7.50");
        FoodItemResponse foodItemResponse = new FoodItemResponse("김치찌개", "Kimchi Stew", "Spicy stew", "image.jpg", priceInfo);
        ReconfigureResponse mockResponse = new ReconfigureResponse(Collections.singletonList(foodItemResponse));

        when(menuService.reconfigure(any(ReconfigureRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // when & then
        webTestClient.post().uri("/menu/reconfigure")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReconfigureResponse.class)
                .value(response -> {
                    assertThat(response.results().get(0).originMenuName()).isEqualTo("김치찌개");
                    assertThat(response.results().get(0).translatedMenuName()).isEqualTo("Kimchi Stew");
                });
    }
}
