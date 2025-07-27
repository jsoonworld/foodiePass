package foodiepass.server.global.config;

public final class ProfileConstants {
    private ProfileConstants() {}

    public static final String PERFORMANCE_TEST = "performance-test";
    public static final String NOT_PERFORMANCE_TEST = "!" + PERFORMANCE_TEST;
    public static final String NOT_TEST_AND_NOT_PERFORMANCE_TEST = "!test & !" + PERFORMANCE_TEST;
}
