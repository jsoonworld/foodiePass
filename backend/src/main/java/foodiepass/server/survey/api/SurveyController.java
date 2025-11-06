package foodiepass.server.survey.api;

import foodiepass.server.survey.application.SurveyService;
import foodiepass.server.survey.dto.request.SurveyRequest;
import foodiepass.server.survey.dto.response.SurveyAnalytics;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for survey response submission and analytics.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/surveys - Submit a survey response</li>
 *   <li>GET /api/admin/surveys/analytics - Retrieve A/B test analytics (Admin)</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * Submits a user's confidence survey response.
     *
     * @param request Survey request containing scanId and hasConfidence
     * @return Success message
     */
    @PostMapping("/api/surveys")
    public ResponseEntity<Map<String, Object>> submitSurvey(
            @Valid @RequestBody SurveyRequest request) {
        try {
            surveyService.saveSurveyResponse(
                    request.getScanId(),
                    request.getHasConfidence()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Survey response recorded successfully"
            ));
        } catch (IllegalArgumentException e) {
            // Scan not found
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (IllegalStateException e) {
            // Duplicate response
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Retrieves analytics data for A/B test validation.
     *
     * <p>Admin-only endpoint for monitoring hypothesis H3 validation progress.
     *
     * <p><strong>TODO</strong>: Add authentication/authorization before production deployment.
     * This endpoint should be protected with admin role validation (e.g., @PreAuthorize("hasRole('ADMIN')")).
     * For MVP internal testing, authentication is deferred to avoid scope creep.
     *
     * @return Survey analytics with Control/Treatment comparison
     */
    @GetMapping("/api/admin/surveys/analytics")
    public ResponseEntity<SurveyAnalytics> getAnalytics() {
        SurveyAnalytics analytics = surveyService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }
}
