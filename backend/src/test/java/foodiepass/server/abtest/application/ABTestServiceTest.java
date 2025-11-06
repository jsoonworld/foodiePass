package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.response.ABTestResult;
import foodiepass.server.abtest.repository.MenuScanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "performance-test"})
class ABTestServiceTest {

    @Autowired
    private ABTestService abTestService;

    @Autowired
    private MenuScanRepository menuScanRepository;

    @BeforeEach
    void setUp() {
        menuScanRepository.deleteAll();
    }

    @Test
    @DisplayName("신규 사용자는 A/B 그룹에 배정된다")
    void assignGroup_newUser() {
        // Given
        String userId = "new-user-123";

        // When
        ABGroup group = abTestService.assignGroup(userId);

        // Then
        assertNotNull(group);
        assertTrue(group == ABGroup.CONTROL || group == ABGroup.TREATMENT);
    }

    @Test
    @DisplayName("기존 사용자는 이전 그룹을 유지한다")
    void assignGroup_existingUser_maintainsSameGroup() {
        // Given
        String userId = "existing-user-456";
        MenuScan existingScan = new MenuScan(
            userId, ABGroup.CONTROL, null,
            "ja", "ko", "JPY", "KRW"
        );
        menuScanRepository.save(existingScan);

        // When
        ABGroup assignedGroup = abTestService.assignGroup(userId);

        // Then
        assertEquals(ABGroup.CONTROL, assignedGroup);
    }

    @Test
    @DisplayName("여러 사용자를 배정하면 대략 50:50 비율이 된다")
    void assignGroup_multipleUsers_balancedRatio() {
        // Given
        int totalUsers = 1000;

        // When
        int controlCount = 0;
        for (int i = 0; i < totalUsers; i++) {
            ABGroup group = abTestService.assignGroup("user-" + i);
            if (group == ABGroup.CONTROL) controlCount++;
        }

        // Then
        double controlRatio = (double) controlCount / totalUsers * 100;
        assertTrue(controlRatio >= 40.0 && controlRatio <= 60.0,
            "Control 비율: " + controlRatio + "% (40-60% 범위 내여야 함)");
    }

    @Test
    @DisplayName("MenuScan을 생성할 수 있다")
    void createScan() {
        // Given
        String userId = "user-123";
        ABGroup abGroup = ABGroup.TREATMENT;

        // When
        MenuScan scan = abTestService.createScan(
            userId, abGroup, "https://s3.../menu.jpg",
            "ja", "ko", "JPY", "KRW"
        );

        // Then
        assertNotNull(scan);
        assertNotNull(scan.getId());
        assertEquals(userId, scan.getUserId());
        assertEquals(abGroup, scan.getAbGroup());
        assertEquals("ko", scan.getTargetLanguage());
    }

    @Test
    @DisplayName("A/B 테스트 결과를 조회할 수 있다")
    void getResults() {
        // Given
        abTestService.createScan("user1", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");
        abTestService.createScan("user2", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");
        abTestService.createScan("user3", ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW");

        // When
        ABTestResult result = abTestService.getResults();

        // Then
        assertEquals(2, result.controlCount());
        assertEquals(1, result.treatmentCount());
        assertEquals(3, result.totalScans());
    }
}
