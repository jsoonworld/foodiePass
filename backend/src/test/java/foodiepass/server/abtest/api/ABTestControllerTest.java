package foodiepass.server.abtest.api;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.dto.response.ABTestResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for ABTestController.
 * Uses @WebMvcTest for lightweight controller-only testing.
 */
@WebMvcTest(ABTestController.class)
class ABTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ABTestService abTestService;

    @Test
    @DisplayName("GET /api/admin/ab-test/results - A/B 테스트 결과를 반환한다")
    void getResults() throws Exception {
        // Given
        ABTestResult mockResult = new ABTestResult(50, 50);
        when(abTestService.getResults()).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/admin/ab-test/results"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.controlCount").value(50))
            .andExpect(jsonPath("$.treatmentCount").value(50))
            .andExpect(jsonPath("$.totalScans").value(100));
    }

    @Test
    @DisplayName("GET /api/admin/ab-test/results - 0건일 때도 정상 응답한다")
    void getResults_noScans() throws Exception {
        // Given
        ABTestResult mockResult = new ABTestResult(0, 0);
        when(abTestService.getResults()).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/admin/ab-test/results"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.controlCount").value(0))
            .andExpect(jsonPath("$.treatmentCount").value(0))
            .andExpect(jsonPath("$.totalScans").value(0));
    }
}
