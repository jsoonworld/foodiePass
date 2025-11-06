package foodiepass.server.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Analytics data for A/B test survey responses.
 *
 * <p>Used to validate hypothesis H3:
 * Treatment group should have ≥2x higher "Yes" rate than Control group.
 */
@Getter
@AllArgsConstructor
public class SurveyAnalytics {

    /**
     * Analytics for the Control group (text + currency only).
     */
    private final GroupAnalytics control;

    /**
     * Analytics for the Treatment group (photos + descriptions + text + currency).
     */
    private final GroupAnalytics treatment;

    /**
     * Ratio of Treatment Yes rate to Control Yes rate.
     * <p>Target: ≥2.0 to validate H3.
     * <p>Returns null if Control total is 0 (division by zero).
     */
    private final Double ratio;

    /**
     * Analytics data for a single A/B test group.
     */
    @Getter
    @AllArgsConstructor
    public static class GroupAnalytics {
        /**
         * Total number of responses for this group.
         */
        private final long total;

        /**
         * Number of "Yes" responses for this group.
         */
        private final long yesCount;

        /**
         * Percentage of "Yes" responses (0.0 to 1.0).
         * <p>Returns 0.0 if total is 0.
         */
        private final double yesRate;
    }

    /**
     * Factory method to create SurveyAnalytics from raw counts.
     *
     * @param controlTotal Total Control group responses
     * @param controlYes Control group "Yes" responses
     * @param treatmentTotal Total Treatment group responses
     * @param treatmentYes Treatment group "Yes" responses
     * @return SurveyAnalytics instance
     */
    public static SurveyAnalytics of(long controlTotal, long controlYes,
                                      long treatmentTotal, long treatmentYes) {
        double controlYesRate = controlTotal > 0 ? (double) controlYes / controlTotal : 0.0;
        double treatmentYesRate = treatmentTotal > 0 ? (double) treatmentYes / treatmentTotal : 0.0;

        Double ratio = null;
        if (controlYesRate > 0) {
            ratio = treatmentYesRate / controlYesRate;
        }

        GroupAnalytics control = new GroupAnalytics(controlTotal, controlYes, controlYesRate);
        GroupAnalytics treatment = new GroupAnalytics(treatmentTotal, treatmentYes, treatmentYesRate);

        return new SurveyAnalytics(control, treatment, ratio);
    }
}
