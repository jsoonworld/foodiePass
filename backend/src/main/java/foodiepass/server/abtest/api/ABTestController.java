package foodiepass.server.abtest.api;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.dto.response.ABTestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller for A/B test analytics.
 * Provides endpoints for hypothesis validation (H1, H3).
 */
@RestController
@RequestMapping("/api/admin/ab-test")
@RequiredArgsConstructor
public class ABTestController {

    private final ABTestService abTestService;

    /**
     * Retrieves A/B test results for admin analysis.
     * Returns scan counts for CONTROL and TREATMENT groups.
     *
     * @return ABTestResult containing group statistics
     */
    @GetMapping("/results")
    public ResponseEntity<ABTestResult> getResults() {
        ABTestResult result = abTestService.getResults();
        return ResponseEntity.ok(result);
    }
}
