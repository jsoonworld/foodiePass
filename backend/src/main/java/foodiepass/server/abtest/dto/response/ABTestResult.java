package foodiepass.server.abtest.dto.response;

/**
 * A/B test analytics result.
 * Provides summary statistics for hypothesis validation (H1, H3).
 *
 * @param controlCount Number of scans in CONTROL group
 * @param treatmentCount Number of scans in TREATMENT group
 * @param totalScans Total number of scans across both groups
 * @param controlPercentage Percentage of scans in CONTROL group (0.0-100.0)
 * @param treatmentPercentage Percentage of scans in TREATMENT group (0.0-100.0)
 */
public record ABTestResult(
    long controlCount,
    long treatmentCount,
    long totalScans,
    double controlPercentage,
    double treatmentPercentage
) {
    /**
     * Creates an ABTestResult with auto-calculated total and percentages.
     *
     * @param controlCount Number of scans in CONTROL group
     * @param treatmentCount Number of scans in TREATMENT group
     */
    public ABTestResult(long controlCount, long treatmentCount) {
        this(
            controlCount,
            treatmentCount,
            controlCount + treatmentCount,
            calculatePercentage(controlCount, controlCount + treatmentCount),
            calculatePercentage(treatmentCount, controlCount + treatmentCount)
        );
    }

    private static double calculatePercentage(long count, long total) {
        return total > 0 ? (count * 100.0 / total) : 0.0;
    }
}
