package foodiepass.server.abtest.domain;

/**
 * A/B test group classification for menu scanning experiments.
 *
 * <p>Used to validate hypothesis H1 and H3:
 * <ul>
 *   <li>CONTROL: Users see text translation + currency conversion only</li>
 *   <li>TREATMENT: Users see photos + descriptions + text + currency</li>
 * </ul>
 */
public enum ABGroup {
    /**
     * Control group: receives text translation and currency conversion only.
     * No food photos or descriptions are provided.
     */
    CONTROL,

    /**
     * Treatment group: receives full visual menu with photos, descriptions,
     * text translation, and currency conversion.
     */
    TREATMENT
}
