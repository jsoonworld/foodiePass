package foodiepass.server.language.api;

import foodiepass.server.language.application.LanguageService;
import foodiepass.server.language.dto.response.LanguageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LanguageController API Tests")
class LanguageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private LanguageController languageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(languageController).build();
    }

    @Test
    @DisplayName("GET /language - 모든 언어 목록 조회 성공")
    void getLanguages_success() throws Exception {
        // Given
        List<LanguageResponse> mockLanguages = List.of(
            new LanguageResponse("Korean"),
            new LanguageResponse("English"),
            new LanguageResponse("Japanese"),
            new LanguageResponse("Chinese (Simplified)")
        );

        when(languageService.findAllLanguages()).thenReturn(mockLanguages);

        // When & Then
        mockMvc.perform(get("/api/language")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].languageName").value("Korean"))
            .andExpect(jsonPath("$[1].languageName").value("English"))
            .andExpect(jsonPath("$[2].languageName").value("Japanese"))
            .andExpect(jsonPath("$[3].languageName").value("Chinese (Simplified)"));
    }

    @Test
    @DisplayName("GET /language - 빈 목록 응답")
    void getLanguages_empty_list() throws Exception {
        // Given
        when(languageService.findAllLanguages()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/language")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /language - 응답 형식 검증")
    void getLanguages_response_format() throws Exception {
        // Given
        List<LanguageResponse> mockLanguages = List.of(
            new LanguageResponse("Korean")
        );

        when(languageService.findAllLanguages()).thenReturn(mockLanguages);

        // When & Then
        mockMvc.perform(get("/api/language")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists())
            .andExpect(jsonPath("$[0].languageName").isString());
    }

    @Test
    @DisplayName("GET /language - Content-Type이 application/json임")
    void getLanguages_content_type() throws Exception {
        // Given
        List<LanguageResponse> mockLanguages = List.of(
            new LanguageResponse("Korean")
        );

        when(languageService.findAllLanguages()).thenReturn(mockLanguages);

        // When & Then
        mockMvc.perform(get("/api/language"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /language - 다수의 언어 지원 확인")
    void getLanguages_multiple_languages() throws Exception {
        // Given
        List<LanguageResponse> mockLanguages = List.of(
            new LanguageResponse("Korean"),
            new LanguageResponse("English"),
            new LanguageResponse("Japanese"),
            new LanguageResponse("Chinese (Simplified)"),
            new LanguageResponse("Chinese (Traditional)"),
            new LanguageResponse("Spanish"),
            new LanguageResponse("French"),
            new LanguageResponse("German")
        );

        when(languageService.findAllLanguages()).thenReturn(mockLanguages);

        // When & Then
        mockMvc.perform(get("/api/language")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(8))
            .andExpect(jsonPath("$[0].languageName").exists())
            .andExpect(jsonPath("$[7].languageName").exists());
    }
}
