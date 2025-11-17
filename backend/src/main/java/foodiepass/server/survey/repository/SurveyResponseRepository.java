package foodiepass.server.survey.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.survey.domain.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing SurveyResponse entities.
 *
 * <p>Provides queries for collecting analytics data to validate hypothesis H3:
 * Treatment group should have ≥2x higher confidence rate than Control group.
 */
@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {

    /**
     * Counts the number of responses for a specific A/B group and confidence level.
     *
     * @param abGroup A/B test group (CONTROL or TREATMENT)
     * @param hasConfidence Confidence response (true = Yes, false = No)
     * @return Count of matching responses
     */
    long countByAbGroupAndHasConfidence(ABGroup abGroup, Boolean hasConfidence);

    /**
     * Counts total responses for a specific A/B group.
     *
     * @param abGroup A/B test group (CONTROL or TREATMENT)
     * @return Total count of responses for the group
     */
    long countByAbGroup(ABGroup abGroup);

    /**
     * Checks if a survey response already exists for a given scan ID.
     *
     * @param scanId Menu scan ID
     * @return true if response exists, false otherwise
     */
    boolean existsByScanId(UUID scanId);

    /**
     * Finds a survey response by scan ID.
     *
     * @param scanId Menu scan ID
     * @return Optional containing the response if found
     */
    Optional<SurveyResponse> findByScanId(UUID scanId);
}
