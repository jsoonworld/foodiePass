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
 *
 * <p><b>Security Note:</b> This controller currently lacks authentication/authorization.
 * For MVP internal testing, this is acceptable. Before production deployment, add:
 * <pre>{@code
 * @PreAuthorize("hasRole('ADMIN')")
 * }</pre>
 * to the endpoints and configure Spring Security with admin role management.
 */
@RestController
@RequestMapping("/api/admin/ab-test")
@RequiredArgsConstructor
public class ABTestController {

    private final ABTestService abTestService;

    /**
     * Retrieves A/B test results for admin analysis.
     * Returns scan counts and percentages for CONTROL and TREATMENT groups.
     *
     * <p><b>Security Warning:</b> This endpoint is currently unsecured.
     * Add authentication before production deployment:
     * <pre>{@code
     * @PreAuthorize("hasRole('ADMIN')")
     * }</pre>
     *
     * @return ABTestResult containing group statistics and percentages
     */
    @GetMapping("/results")
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") before production
    public ResponseEntity<ABTestResult> getResults() {
        ABTestResult result = abTestService.getResults();
        return ResponseEntity.ok(result);
    }
}
