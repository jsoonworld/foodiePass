package foodiepass.server.abtest.dto.response;

/**
 * A/B test analytics result.
 * Provides summary statistics for hypothesis validation (H1, H3).
 *
 * @param controlCount Number of scans in CONTROL group
 * @param treatmentCount Number of scans in TREATMENT group
 * @param totalScans Total number of scans across both groups
 */
public record ABTestResult(
    long controlCount,
    long treatmentCount,
    long totalScans
) {
    /**
     * Creates an ABTestResult with auto-calculated total.
     *
     * @param controlCount Number of scans in CONTROL group
     * @param treatmentCount Number of scans in TREATMENT group
     */
    public ABTestResult(long controlCount, long treatmentCount) {
        this(controlCount, treatmentCount, controlCount + treatmentCount);
    }
}
