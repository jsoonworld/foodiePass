package foodiepass.server.survey.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.repository.MenuScanRepository;
import foodiepass.server.survey.domain.SurveyResponse;
import foodiepass.server.survey.dto.response.SurveyAnalytics;
import foodiepass.server.survey.repository.SurveyResponseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SurveyService 테스트")
class SurveyServiceTest {

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Mock
    private MenuScanRepository menuScanRepository;

    @InjectMocks
    private SurveyService surveyService;

    @Test
    @DisplayName("saveSurveyResponse - 정상적인 응답 저장")
    void saveSurveyResponse_Success() {
        // Given
        UUID scanId = UUID.randomUUID();
        Boolean hasConfidence = true;

        MenuScan menuScan = MenuScan.create(
                "user123",
                ABGroup.TREATMENT,
                "http://example.com/image.jpg",
                "en",
                "ko",
                "USD",
                "KRW"
        );

        given(menuScanRepository.findById(scanId)).willReturn(Optional.of(menuScan));
        given(surveyResponseRepository.existsByScanId(scanId)).willReturn(false);
        given(surveyResponseRepository.save(any(SurveyResponse.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // When
        surveyService.saveSurveyResponse(scanId, hasConfidence);

        // Then
        verify(menuScanRepository).findById(scanId);
        verify(surveyResponseRepository).existsByScanId(scanId);
        verify(surveyResponseRepository).save(any(SurveyResponse.class));
    }

    @Test
    @DisplayName("saveSurveyResponse - 존재하지 않는 scanId")
    void saveSurveyResponse_ScanNotFound() {
        // Given
        UUID scanId = UUID.randomUUID();
        Boolean hasConfidence = true;

        given(menuScanRepository.findById(scanId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> surveyService.saveSurveyResponse(scanId, hasConfidence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MenuScan not found");

        verify(menuScanRepository).findById(scanId);
        verify(surveyResponseRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveSurveyResponse - 중복 응답 방지")
    void saveSurveyResponse_DuplicateResponse() {
        // Given
        UUID scanId = UUID.randomUUID();
        Boolean hasConfidence = true;

        MenuScan menuScan = MenuScan.create(
                "user123",
                ABGroup.CONTROL,
                "http://example.com/image.jpg",
                "en",
                "ko",
                "USD",
                "KRW"
        );

        given(menuScanRepository.findById(scanId)).willReturn(Optional.of(menuScan));
        given(surveyResponseRepository.existsByScanId(scanId)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> surveyService.saveSurveyResponse(scanId, hasConfidence))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(menuScanRepository).findById(scanId);
        verify(surveyResponseRepository).existsByScanId(scanId);
        verify(surveyResponseRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAnalytics - Control과 Treatment 그룹 분석")
    void getAnalytics() {
        // Given
        // Control: 100 total, 30 Yes → 30%
        given(surveyResponseRepository.countByAbGroup(ABGroup.CONTROL)).willReturn(100L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.CONTROL, true))
                .willReturn(30L);

        // Treatment: 100 total, 70 Yes → 70%
        given(surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT)).willReturn(100L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true))
                .willReturn(70L);

        // When
        SurveyAnalytics analytics = surveyService.getAnalytics();

        // Then
        assertThat(analytics.getControl().getTotal()).isEqualTo(100);
        assertThat(analytics.getControl().getYesCount()).isEqualTo(30);
        assertThat(analytics.getControl().getYesRate()).isEqualTo(0.30);

        assertThat(analytics.getTreatment().getTotal()).isEqualTo(100);
        assertThat(analytics.getTreatment().getYesCount()).isEqualTo(70);
        assertThat(analytics.getTreatment().getYesRate()).isEqualTo(0.70);

        // Ratio: 70% / 30% = 2.33
        assertThat(analytics.getRatio()).isNotNull();
        assertThat(analytics.getRatio()).isEqualTo(70.0 / 30.0);
    }

    @Test
    @DisplayName("getAnalytics - Control 응답이 0일 때 ratio는 null")
    void getAnalytics_ControlZero_RatioIsNull() {
        // Given
        given(surveyResponseRepository.countByAbGroup(ABGroup.CONTROL)).willReturn(0L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.CONTROL, true))
                .willReturn(0L);

        given(surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT)).willReturn(50L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true))
                .willReturn(35L);

        // When
        SurveyAnalytics analytics = surveyService.getAnalytics();

        // Then
        assertThat(analytics.getControl().getTotal()).isEqualTo(0);
        assertThat(analytics.getControl().getYesRate()).isEqualTo(0.0);

        assertThat(analytics.getTreatment().getTotal()).isEqualTo(50);
        assertThat(analytics.getTreatment().getYesRate()).isEqualTo(0.70);

        assertThat(analytics.getRatio()).isNull();
    }

    @Test
    @DisplayName("getAnalytics - 응답이 없을 때")
    void getAnalytics_NoResponses() {
        // Given
        given(surveyResponseRepository.countByAbGroup(ABGroup.CONTROL)).willReturn(0L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.CONTROL, true))
                .willReturn(0L);

        given(surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT)).willReturn(0L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true))
                .willReturn(0L);

        // When
        SurveyAnalytics analytics = surveyService.getAnalytics();

        // Then
        assertThat(analytics.getControl().getTotal()).isEqualTo(0);
        assertThat(analytics.getControl().getYesCount()).isEqualTo(0);
        assertThat(analytics.getControl().getYesRate()).isEqualTo(0.0);

        assertThat(analytics.getTreatment().getTotal()).isEqualTo(0);
        assertThat(analytics.getTreatment().getYesCount()).isEqualTo(0);
        assertThat(analytics.getTreatment().getYesRate()).isEqualTo(0.0);

        assertThat(analytics.getRatio()).isNull();
    }

    @Test
    @DisplayName("getAnalytics - H3 검증: ratio ≥ 2.0")
    void getAnalytics_HypothesisValidation_Success() {
        // Given
        // Control: 100 total, 30 Yes → 30%
        given(surveyResponseRepository.countByAbGroup(ABGroup.CONTROL)).willReturn(100L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.CONTROL, true))
                .willReturn(30L);

        // Treatment: 100 total, 70 Yes → 70%
        given(surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT)).willReturn(100L);
        given(surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true))
                .willReturn(70L);

        // When
        SurveyAnalytics analytics = surveyService.getAnalytics();

        // Then
        assertThat(analytics.getRatio()).isNotNull();
        assertThat(analytics.getRatio()).isGreaterThanOrEqualTo(2.0);
    }
}
