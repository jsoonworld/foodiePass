package foodiepass.server.global.config;

public final class ProfileConstants {
    private ProfileConstants() {}

    public static final String PERFORMANCE_TEST = "performance-test";
    public static final String INTEGRATION_TEST = "integration-test";
    public static final String TEST = "test";
    public static final String NOT_PERFORMANCE_TEST = "!" + PERFORMANCE_TEST;
    public static final String NOT_TEST_AND_NOT_PERFORMANCE_TEST = "!test & !" + PERFORMANCE_TEST;

    // For Mock Beans that should be active in both test and performance-test profiles
    public static final String TEST_OR_PERFORMANCE_TEST = TEST + " | " + PERFORMANCE_TEST;
}
