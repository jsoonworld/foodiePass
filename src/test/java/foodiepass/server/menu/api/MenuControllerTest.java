package foodiepass.server.menu.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.menu.application.MenuService;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.ReconfigureResponse;
import foodiepass.server.menu.dto.response.ReconfigureResponse.FoodItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuService menuService;

    @Test
    @DisplayName("POST /menu/reconfigure 요청 시 메뉴 재구성 결과를 성공적으로 반환한다")
    void reconfigure_shouldReturnReconfiguredMenu() throws Exception {
        // given
        ReconfigureRequest request = new ReconfigureRequest(
                "base64image", "Korean", "English", "South Korean Won", "US Dollar"
        );

        FoodItemResponse foodItemResponse = new FoodItemResponse("김치찌개", "Kimchi Stew", "Spicy stew", "image.jpg", null);
        ReconfigureResponse mockResponse = new ReconfigureResponse(Collections.singletonList(foodItemResponse));

        when(menuService.reconfigure(any(ReconfigureRequest.class))).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/menu/reconfigure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.results[0].originMenuName").value("김치찌개"))
                .andExpect(jsonPath("$.result.results[0].translatedMenuName").value("Kimchi Stew"));
    }
}
