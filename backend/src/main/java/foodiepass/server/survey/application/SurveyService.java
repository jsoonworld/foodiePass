package foodiepass.server.survey.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.repository.MenuScanRepository;
import foodiepass.server.survey.domain.SurveyResponse;
import foodiepass.server.survey.dto.response.SurveyAnalytics;
import foodiepass.server.survey.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing survey responses and analytics.
 *
 * <p>Handles:
 * <ul>
 *   <li>Saving user confidence responses</li>
 *   <li>Validating scan IDs</li>
 *   <li>Preventing duplicate responses</li>
 *   <li>Computing analytics for hypothesis validation (H3)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyResponseRepository surveyResponseRepository;
    private final MenuScanRepository menuScanRepository;

    /**
     * Saves a survey response for a menu scan.
     *
     * @param scanId ID of the menu scan
     * @param hasConfidence User's confidence response (true = Yes, false = No)
     * @throws IllegalArgumentException if scanId doesn't exist
     * @throws IllegalStateException if response already exists for this scan
     */
    @Transactional
    public void saveSurveyResponse(UUID scanId, Boolean hasConfidence) {
        // Validate scan exists
        MenuScan menuScan = menuScanRepository.findById(scanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "MenuScan not found with id: " + scanId));

        // Prevent duplicate responses
        if (surveyResponseRepository.existsByScanId(scanId)) {
            throw new IllegalStateException(
                    "Survey response already exists for scan: " + scanId);
        }

        // Create and save response
        SurveyResponse response = new SurveyResponse(
                scanId,
                menuScan.getAbGroup(),
                hasConfidence
        );

        surveyResponseRepository.save(response);
    }

    /**
     * Retrieves analytics data for A/B test validation.
     *
     * <p>Computes:
     * <ul>
     *   <li>Control group: total, yes count, yes rate</li>
     *   <li>Treatment group: total, yes count, yes rate</li>
     *   <li>Ratio: treatment yes rate / control yes rate</li>
     * </ul>
     *
     * @return SurveyAnalytics containing all metrics
     */
    public SurveyAnalytics getAnalytics() {
        // Control group counts
        long controlTotal = surveyResponseRepository.countByAbGroup(ABGroup.CONTROL);
        long controlYes = surveyResponseRepository.countByAbGroupAndHasConfidence(
                ABGroup.CONTROL, true);

        // Treatment group counts
        long treatmentTotal = surveyResponseRepository.countByAbGroup(ABGroup.TREATMENT);
        long treatmentYes = surveyResponseRepository.countByAbGroupAndHasConfidence(
                ABGroup.TREATMENT, true);

        return SurveyAnalytics.of(controlTotal, controlYes, treatmentTotal, treatmentYes);
    }
}
