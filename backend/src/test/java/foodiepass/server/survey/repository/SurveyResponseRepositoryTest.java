package foodiepass.server.survey.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.survey.domain.SurveyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SurveyResponseRepository 테스트")
class SurveyResponseRepositoryTest {

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    @Test
    @DisplayName("countByAbGroupAndHasConfidence - Control 그룹 Yes 응답 카운트")
    void countByAbGroupAndHasConfidence_Control_Yes() {
        // Given
        UUID scanId1 = UUID.randomUUID();
        UUID scanId2 = UUID.randomUUID();
        UUID scanId3 = UUID.randomUUID();

        surveyResponseRepository.save(new SurveyResponse(scanId1, ABGroup.CONTROL, true));
        surveyResponseRepository.save(new SurveyResponse(scanId2, ABGroup.CONTROL, true));
        surveyResponseRepository.save(new SurveyResponse(scanId3, ABGroup.CONTROL, false));

        // When
        long count = surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.CONTROL, true);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByAbGroupAndHasConfidence - Treatment 그룹 Yes 응답 카운트")
    void countByAbGroupAndHasConfidence_Treatment_Yes() {
        // Given
        UUID scanId1 = UUID.randomUUID();
        UUID scanId2 = UUID.randomUUID();
        UUID scanId3 = UUID.randomUUID();
        UUID scanId4 = UUID.randomUUID();

        surveyResponseRepository.save(new SurveyResponse(scanId1, ABGroup.TREATMENT, true));
        surveyResponseRepository.save(new SurveyResponse(scanId2, ABGroup.TREATMENT, true));
        surveyResponseRepository.save(new SurveyResponse(scanId3, ABGroup.TREATMENT, true));
        surveyResponseRepository.save(new SurveyResponse(scanId4, ABGroup.TREATMENT, false));

        // When
        long count = surveyResponseRepository.countByAbGroupAndHasConfidence(ABGroup.TREATMENT, true);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("countByAbGroup - 그룹별 전체 응답 카운트")
    void countByAbGroup() {
        // Given
        UUID scanId1 = UUID.randomUUID();
        UUID scanId2 = UUID.randomUUID();
        UUID scanId3 = UUID.randomUUID();

        surveyResponseRepository.save(new SurveyResponse(scanId1, ABGroup.CONTROL, true));
        surveyResponseRepository.save(new SurveyResponse(scanId2, ABGroup.CONTROL, false));
        surveyResponseRepository.save(new SurveyResponse(scanId3, ABGroup.TREATMENT, true));

        // When
        long controlCount = surveyResponseRepository.countByAbGroup(ABGroup.CONTROL);
        long treatmentCount = surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT);

        // Then
        assertThat(controlCount).isEqualTo(2);
        assertThat(treatmentCount).isEqualTo(1);
    }

    @Test
    @DisplayName("existsByScanId - scanId로 존재 여부 확인")
    void existsByScanId() {
        // Given
        UUID existingScanId = UUID.randomUUID();
        UUID nonExistingScanId = UUID.randomUUID();

        surveyResponseRepository.save(new SurveyResponse(existingScanId, ABGroup.CONTROL, true));

        // When & Then
        assertThat(surveyResponseRepository.existsByScanId(existingScanId)).isTrue();
        assertThat(surveyResponseRepository.existsByScanId(nonExistingScanId)).isFalse();
    }

    @Test
    @DisplayName("findByScanId - scanId로 응답 조회")
    void findByScanId() {
        // Given
        UUID scanId = UUID.randomUUID();
        SurveyResponse savedResponse = surveyResponseRepository.save(
                new SurveyResponse(scanId, ABGroup.TREATMENT, true));

        // When
        Optional<SurveyResponse> found = surveyResponseRepository.findByScanId(scanId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getScanId()).isEqualTo(scanId);
        assertThat(found.get().getAbGroup()).isEqualTo(ABGroup.TREATMENT);
        assertThat(found.get().getHasConfidence()).isTrue();
    }

    @Test
    @DisplayName("findByScanId - 존재하지 않는 scanId 조회")
    void findByScanId_NotFound() {
        // Given
        UUID nonExistingScanId = UUID.randomUUID();

        // When
        Optional<SurveyResponse> found = surveyResponseRepository.findByScanId(nonExistingScanId);

        // Then
        assertThat(found).isEmpty();
    }
}
