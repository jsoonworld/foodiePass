package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.response.ABTestResult;
import foodiepass.server.abtest.repository.MenuScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for managing A/B test group assignments and analytics
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ABTestService {

    private final MenuScanRepository menuScanRepository;

    /**
     * Assigns a user to an A/B test group.
     * - New users: Random assignment (50:50)
     * - Existing users: Maintain previous group
     *
     * @param userId User session identifier
     * @return Assigned ABGroup
     */
    public ABGroup assignGroup(String userId) {
        Optional<MenuScan> existingScan = menuScanRepository
            .findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (existingScan.isPresent()) {
            return existingScan.get().getAbGroup();
        }

        return randomAssign();
    }

    /**
     * Atomically assigns a user to an A/B group and creates a MenuScan record.
     * This prevents race conditions where concurrent requests might assign different groups.
     *
     * @param userId User session identifier
     * @param imageUrl Optional image URL for audit
     * @param imageHash Optional SHA-256 hash of menu image for deduplication
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param sourceCurrency Source currency code
     * @param targetCurrency Target currency code
     * @return Saved MenuScan instance with assigned group
     */
    @Transactional
    public MenuScan assignAndCreateScan(
        String userId,
        String imageUrl,
        String imageHash,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency
    ) {
        // Check for existing scan within transaction
        Optional<MenuScan> existingScan = menuScanRepository
            .findFirstByUserIdOrderByCreatedAtDesc(userId);

        ABGroup abGroup;
        if (existingScan.isPresent()) {
            abGroup = existingScan.get().getAbGroup();
        } else {
            abGroup = randomAssign();
        }

        MenuScan scan = MenuScan.create(
            userId, abGroup, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency
        );

        // Set imageHash for deduplication if provided
        if (imageHash != null && !imageHash.isEmpty()) {
            scan.setImageHash(imageHash);
        }

        return menuScanRepository.save(scan);
    }

    /**
     * Creates and saves a new MenuScan record
     *
     * @param userId User session identifier
     * @param abGroup A/B test group assignment
     * @param imageUrl Optional image URL for audit
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param sourceCurrency Source currency code
     * @param targetCurrency Target currency code
     * @return Saved MenuScan instance
     */
    @Transactional
    public MenuScan createScan(
        String userId,
        ABGroup abGroup,
        String imageUrl,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency
    ) {
        MenuScan scan = MenuScan.create(
            userId, abGroup, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency
        );

        return menuScanRepository.save(scan);
    }

    /**
     * Retrieves A/B test results summary (admin only)
     * Uses a single GROUP BY query to ensure data consistency
     *
     * @return ABTestResult with group counts
     */
    public ABTestResult getResults() {
        List<Object[]> groupCounts = menuScanRepository.countGroupByAbGroup();

        long controlCount = 0;
        long treatmentCount = 0;

        for (Object[] row : groupCounts) {
            ABGroup group = (ABGroup) row[0];
            Long count = (Long) row[1];

            if (group == ABGroup.CONTROL) {
                controlCount = count;
            } else if (group == ABGroup.TREATMENT) {
                treatmentCount = count;
            }
        }

        return new ABTestResult(controlCount, treatmentCount);
    }

    /**
     * Random group assignment (50:50)
     */
    private ABGroup randomAssign() {
        return ThreadLocalRandom.current().nextBoolean()
            ? ABGroup.CONTROL
            : ABGroup.TREATMENT;
    }
}
