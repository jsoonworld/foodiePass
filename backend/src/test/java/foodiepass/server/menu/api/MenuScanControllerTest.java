package foodiepass.server.menu.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.menu.application.MenuScanService;
import foodiepass.server.menu.dto.request.MenuScanRequest;
import foodiepass.server.menu.dto.response.MenuItemDto;
import foodiepass.server.menu.dto.response.MenuScanResponse;
import foodiepass.server.menu.dto.response.PriceInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuScanController Integration Tests")
class MenuScanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MenuScanService menuScanService;

    @InjectMocks
    private MenuScanController menuScanController;

    private ObjectMapper objectMapper;
    private MockHttpSession mockSession;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(menuScanController).build();
        objectMapper = new ObjectMapper();
        mockSession = new MockHttpSession();
    }

    @Test
    @DisplayName("POST /scan - CONTROL 그룹 응답 성공 (description과 image null)")
    void scanMenu_CONTROL_group_success() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        List<MenuItemDto> controlItems = List.of(
            new MenuItemDto(
                UUID.randomUUID(),
                "Sushi",
                "스시",
                null,
                null,
                new PriceInfoDto(1500.0, "JPY", "¥1,500", 20000.0, "KRW", "₩20,000"),
                null
            )
        );

        MenuScanResponse mockResponse = new MenuScanResponse(
            UUID.randomUUID(),
            "CONTROL",
            controlItems,
            3.5
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .session(mockSession))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.abGroup").value("CONTROL"))
            .andExpect(jsonPath("$.scanId").exists())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items[0].originalName").value("Sushi"))
            .andExpect(jsonPath("$.items[0].translatedName").value("스시"))
            .andExpect(jsonPath("$.items[0].priceInfo").exists())
            .andExpect(jsonPath("$.processingTime").value(3.5));
    }

    @Test
    @DisplayName("POST /scan - TREATMENT 그룹 응답 성공 (모든 필드 포함)")
    void scanMenu_TREATMENT_group_success() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        List<MenuItemDto> treatmentItems = List.of(
            new MenuItemDto(
                UUID.randomUUID(),
                "Sushi",
                "스시",
                "Fresh raw fish with rice",
                "https://example.com/sushi.jpg",
                new PriceInfoDto(1500.0, "JPY", "¥1,500", 20000.0, "KRW", "₩20,000"),
                null
            ),
            new MenuItemDto(
                UUID.randomUUID(),
                "Ramen",
                "라멘",
                "Japanese noodle soup",
                "https://example.com/ramen.jpg",
                new PriceInfoDto(800.0, "JPY", "¥800", 10500.0, "KRW", "₩10,500"),
                null
            )
        );

        MenuScanResponse mockResponse = new MenuScanResponse(
            UUID.randomUUID(),
            "TREATMENT",
            treatmentItems,
            4.2
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .session(mockSession))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.abGroup").value("TREATMENT"))
            .andExpect(jsonPath("$.scanId").exists())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].originalName").value("Sushi"))
            .andExpect(jsonPath("$.items[0].translatedName").value("스시"))
            .andExpect(jsonPath("$.items[0].description").value("Fresh raw fish with rice"))
            .andExpect(jsonPath("$.items[0].imageUrl").value("https://example.com/sushi.jpg"))
            .andExpect(jsonPath("$.items[0].priceInfo").exists())
            .andExpect(jsonPath("$.processingTime").value(4.2));
    }

    @Test
    @DisplayName("POST /scan - 빈 메뉴 응답 처리")
    void scanMenu_empty_items() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        MenuScanResponse mockResponse = new MenuScanResponse(
            UUID.randomUUID(),
            "CONTROL",
            List.of(),
            2.1
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .session(mockSession))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isEmpty())
            .andExpect(jsonPath("$.scanId").exists())
            .andExpect(jsonPath("$.processingTime").value(2.1));
    }

    @Test
    @DisplayName("POST /scan - 다양한 통화 형식 처리")
    void scanMenu_various_currencies() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            "en",
            "ko",
            "USD",
            "KRW"
        );

        List<MenuItemDto> items = List.of(
            new MenuItemDto(
                UUID.randomUUID(),
                "Burger",
                "버거",
                "American style burger",
                "https://example.com/burger.jpg",
                new PriceInfoDto(15.0, "USD", "$15.00", 20000.0, "KRW", "₩20,000"),
                null
            ),
            new MenuItemDto(
                UUID.randomUUID(),
                "Pizza",
                "피자",
                "Italian pizza",
                "https://example.com/pizza.jpg",
                new PriceInfoDto(10.5, "EUR", "€10.50", 14500.0, "KRW", "₩14,500"),
                null
            )
        );

        MenuScanResponse mockResponse = new MenuScanResponse(
            UUID.randomUUID(),
            "TREATMENT",
            items,
            3.8
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .session(mockSession))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].priceInfo.originalCurrency").value("USD"))
            .andExpect(jsonPath("$.items[0].priceInfo.originalFormatted").value("$15.00"))
            .andExpect(jsonPath("$.items[1].priceInfo.originalCurrency").value("EUR"))
            .andExpect(jsonPath("$.items[1].priceInfo.originalFormatted").value("€10.50"))
            .andExpect(jsonPath("$.items[0].priceInfo.convertedCurrency").value("KRW"))
            .andExpect(jsonPath("$.items[1].priceInfo.convertedCurrency").value("KRW"));
    }

    @Test
    @DisplayName("POST /scan - originLanguageName 기본값 처리 (auto)")
    void scanMenu_default_origin_language() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            null,
            "ko",
            null,
            "KRW"
        );

        MenuScanResponse mockResponse = new MenuScanResponse(
            UUID.randomUUID(),
            "CONTROL",
            List.of(),
            2.5
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .session(mockSession))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scanId").exists());
    }

    @Test
    @DisplayName("POST /scan - Service 에러 발생 시 에러 전파")
    void scanMenu_service_error() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.error(new RuntimeException("OCR service unavailable")));

        // When & Then
        try {
            MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(mockSession))
                .andExpect(request().asyncStarted())
                .andReturn();

            // Async dispatch should handle the error
            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            // If exception is thrown during async processing, that's also acceptable
            // as it indicates error was propagated
            assert e.getCause() instanceof RuntimeException
                || e instanceof jakarta.servlet.ServletException;
        }
    }

    @Test
    @DisplayName("POST /scan - 처리 시간이 응답에 포함됨")
    void scanMenu_includes_processing_time() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        MenuScanResponse mockResponse = new MenuScanResponse(
            UUID.randomUUID(),
            "TREATMENT",
            List.of(
                new MenuItemDto(
                    UUID.randomUUID(),
                    "Item",
                    "아이템",
                    "Description",
                    "https://example.com/item.jpg",
                    new PriceInfoDto(1000.0, "JPY", "¥1,000", 13000.0, "KRW", "₩13,000"),
                    null
                )
            ),
            4.723
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .session(mockSession))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.processingTime").value(4.723));
    }

    @Test
    @DisplayName("POST /scan - scanId가 UUID 형식으로 반환됨")
    void scanMenu_returns_valid_scanId() throws Exception {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            "base64EncodedImage",
            "ja",
            "ko",
            "JPY",
            "KRW"
        );

        UUID expectedScanId = UUID.randomUUID();
        MenuScanResponse mockResponse = new MenuScanResponse(
            expectedScanId,
            "CONTROL",
            List.of(),
            2.0
        );

        when(menuScanService.scanMenu(any(MenuScanRequest.class), anyString()))
            .thenReturn(Mono.just(mockResponse));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/api/menus/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .session(mockSession))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scanId").value(expectedScanId.toString()));
    }
}
