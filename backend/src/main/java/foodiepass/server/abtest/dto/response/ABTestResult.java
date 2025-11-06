package foodiepass.server.abtest.dto.response;

/**
 * A/B test results summary for admin dashboard
 *
 * @param controlCount Number of users assigned to control group
 * @param treatmentCount Number of users assigned to treatment group
 * @param totalScans Total number of scans performed
 */
public record ABTestResult(
    long controlCount,
    long treatmentCount,
    long totalScans
) {
    public ABTestResult(long controlCount, long treatmentCount) {
        this(controlCount, treatmentCount, controlCount + treatmentCount);
    }
}
