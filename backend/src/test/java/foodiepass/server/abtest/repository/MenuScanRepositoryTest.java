package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MenuScanRepositoryTest {

    @Autowired
    private MenuScanRepository menuScanRepository;

    @BeforeEach
    void setUp() {
        menuScanRepository.deleteAll();
    }

    @Test
    @DisplayName("MenuScan을 저장할 수 있다")
    void saveMenuScan() {
        // Given
        MenuScan menuScan = new MenuScan(
            "user-123", ABGroup.CONTROL, null,
            "ja", "ko", "JPY", "KRW"
        );

        // When
        MenuScan saved = menuScanRepository.save(menuScan);

        // Then
        assertNotNull(saved.getId());
        assertEquals("user-123", saved.getUserId());
        assertEquals(ABGroup.CONTROL, saved.getAbGroup());
    }

    @Test
    @DisplayName("userId로 가장 최근 스캔을 조회할 수 있다")
    void findFirstByUserIdOrderByCreatedAtDesc() throws InterruptedException {
        // Given
        String userId = "user-123";
        MenuScan scan1 = new MenuScan(userId, ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW");
        menuScanRepository.save(scan1);

        Thread.sleep(10); // 시간 차이 보장

        MenuScan scan2 = new MenuScan(userId, ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW");
        menuScanRepository.save(scan2);

        // When
        Optional<MenuScan> result = menuScanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(scan2.getId(), result.get().getId());
        assertEquals(ABGroup.TREATMENT, result.get().getAbGroup());
    }

    @Test
    @DisplayName("존재하지 않는 userId로 조회하면 Optional.empty를 반환한다")
    void findFirstByUserIdOrderByCreatedAtDesc_notFound() {
        // Given
        String userId = "non-existent-user";

        // When
        Optional<MenuScan> result = menuScanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("ABGroup별로 개수를 셀 수 있다")
    void countByAbGroup() {
        // Given
        menuScanRepository.save(new MenuScan("user1", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW"));
        menuScanRepository.save(new MenuScan("user2", ABGroup.CONTROL, null, "ja", "ko", "JPY", "KRW"));
        menuScanRepository.save(new MenuScan("user3", ABGroup.TREATMENT, null, "ja", "ko", "JPY", "KRW"));

        // When
        long controlCount = menuScanRepository.countByAbGroup(ABGroup.CONTROL);
        long treatmentCount = menuScanRepository.countByAbGroup(ABGroup.TREATMENT);

        // Then
        assertEquals(2, controlCount);
        assertEquals(1, treatmentCount);
    }
}
