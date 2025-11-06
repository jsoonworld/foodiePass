package foodiepass.server.survey.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.survey.application.SurveyService;
import foodiepass.server.survey.dto.request.SurveyRequest;
import foodiepass.server.survey.dto.response.SurveyAnalytics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SurveyController.class)
@DisplayName("SurveyController 테스트")
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SurveyService surveyService;

    @Test
    @DisplayName("POST /api/surveys - 정상적인 설문 응답 제출")
    void submitSurvey_Success() throws Exception {
        // Given
        UUID scanId = UUID.randomUUID();
        SurveyRequest request = new SurveyRequest(scanId, true);

        willDoNothing().given(surveyService).saveSurveyResponse(any(UUID.class), any(Boolean.class));

        // When & Then
        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Survey response recorded successfully"));

        verify(surveyService).saveSurveyResponse(scanId, true);
    }

    @Test
    @DisplayName("POST /api/surveys - scanId가 null인 경우 400 Bad Request")
    void submitSurvey_NullScanId() throws Exception {
        // Given
        SurveyRequest request = new SurveyRequest(null, true);

        // When & Then
        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(surveyService, never()).saveSurveyResponse(any(), any());
    }

    @Test
    @DisplayName("POST /api/surveys - hasConfidence가 null인 경우 400 Bad Request")
    void submitSurvey_NullHasConfidence() throws Exception {
        // Given
        UUID scanId = UUID.randomUUID();
        SurveyRequest request = new SurveyRequest(scanId, null);

        // When & Then
        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(surveyService, never()).saveSurveyResponse(any(), any());
    }

    @Test
    @DisplayName("POST /api/surveys - 존재하지 않는 scanId로 404 Not Found")
    void submitSurvey_ScanNotFound() throws Exception {
        // Given
        UUID scanId = UUID.randomUUID();
        SurveyRequest request = new SurveyRequest(scanId, true);

        willThrow(new IllegalArgumentException("MenuScan not found with id: " + scanId))
                .given(surveyService).saveSurveyResponse(any(UUID.class), any(Boolean.class));

        // When & Then
        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("MenuScan not found with id: " + scanId));

        verify(surveyService).saveSurveyResponse(scanId, true);
    }

    @Test
    @DisplayName("POST /api/surveys - 중복 응답으로 409 Conflict")
    void submitSurvey_DuplicateResponse() throws Exception {
        // Given
        UUID scanId = UUID.randomUUID();
        SurveyRequest request = new SurveyRequest(scanId, false);

        willThrow(new IllegalStateException("Survey response already exists for scan: " + scanId))
                .given(surveyService).saveSurveyResponse(any(UUID.class), any(Boolean.class));

        // When & Then
        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Survey response already exists for scan: " + scanId));

        verify(surveyService).saveSurveyResponse(scanId, false);
    }

    @Test
    @DisplayName("GET /api/admin/surveys/analytics - 분석 결과 조회")
    void getAnalytics() throws Exception {
        // Given
        SurveyAnalytics analytics = SurveyAnalytics.of(
                100L, 30L,  // Control: 100 total, 30 Yes → 30%
                100L, 70L   // Treatment: 100 total, 70 Yes → 70%
        );

        given(surveyService.getAnalytics()).willReturn(analytics);

        // When & Then
        mockMvc.perform(get("/api/admin/surveys/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.control.total").value(100))
                .andExpect(jsonPath("$.control.yesCount").value(30))
                .andExpect(jsonPath("$.control.yesRate").value(0.30))
                .andExpect(jsonPath("$.treatment.total").value(100))
                .andExpect(jsonPath("$.treatment.yesCount").value(70))
                .andExpect(jsonPath("$.treatment.yesRate").value(0.70))
                .andExpect(jsonPath("$.ratio").value(70.0 / 30.0));

        verify(surveyService).getAnalytics();
    }

    @Test
    @DisplayName("GET /api/admin/surveys/analytics - 응답이 없을 때")
    void getAnalytics_NoResponses() throws Exception {
        // Given
        SurveyAnalytics analytics = SurveyAnalytics.of(0L, 0L, 0L, 0L);

        given(surveyService.getAnalytics()).willReturn(analytics);

        // When & Then
        mockMvc.perform(get("/api/admin/surveys/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.control.total").value(0))
                .andExpect(jsonPath("$.control.yesCount").value(0))
                .andExpect(jsonPath("$.control.yesRate").value(0.0))
                .andExpect(jsonPath("$.treatment.total").value(0))
                .andExpect(jsonPath("$.treatment.yesCount").value(0))
                .andExpect(jsonPath("$.treatment.yesRate").value(0.0))
                .andExpect(jsonPath("$.ratio").doesNotExist());

        verify(surveyService).getAnalytics();
    }
}
