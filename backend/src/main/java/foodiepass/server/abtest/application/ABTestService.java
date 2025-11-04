package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.response.ABTestResult;
import foodiepass.server.abtest.repository.MenuScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for A/B test group assignment and analytics.
 * Supports hypothesis validation (H1, H3) by managing test groups and collecting metrics.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ABTestService {

    private final MenuScanRepository menuScanRepository;

    /**
     * Assigns a user to an A/B test group.
     * New users are randomly assigned 50:50 to CONTROL or TREATMENT.
     * Existing users maintain their previous group assignment for consistency.
     *
     * <p><b>Usage:</b> This method should be called within the same transaction as
     * {@link #createScan} to ensure atomicity. The typical usage pattern is:
     * <pre>{@code
     * @Transactional
     * public void handleMenuScan(String userId, ...) {
     *     ABGroup group = abTestService.assignGroup(userId);
     *     MenuScan scan = abTestService.createScan(userId, group, ...);
     *     // proceed with menu processing
     * }
     * }</pre>
     *
     * @param userId User session identifier
     * @return Assigned A/B group (CONTROL or TREATMENT)
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
     * Creates and persists a new menu scan session.
     *
     * @param userId User session identifier
     * @param abGroup A/B test group assignment
     * @param imageUrl Optional image URL for audit trail
     * @param sourceLanguage Source language code (e.g., "ja")
     * @param targetLanguage Target language code (e.g., "ko")
     * @param sourceCurrency Source currency code (e.g., "JPY")
     * @param targetCurrency Target currency code (e.g., "KRW")
     * @return Created MenuScan entity with generated ID and timestamp
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
        MenuScan scan = new MenuScan(
            userId, abGroup, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency
        );

        return menuScanRepository.save(scan);
    }

    /**
     * Retrieves A/B test analytics for hypothesis validation.
     * Provides counts for CONTROL and TREATMENT groups.
     *
     * @return ABTestResult containing group counts and total scans
     */
    public ABTestResult getResults() {
        long controlCount = menuScanRepository.countByAbGroup(ABGroup.CONTROL);
        long treatmentCount = menuScanRepository.countByAbGroup(ABGroup.TREATMENT);

        return new ABTestResult(controlCount, treatmentCount);
    }

    /**
     * Randomly assigns a user to CONTROL or TREATMENT group (50:50 split).
     * Uses ThreadLocalRandom for thread-safe random number generation.
     *
     * @return Randomly assigned ABGroup
     */
    private ABGroup randomAssign() {
        return ThreadLocalRandom.current().nextBoolean()
            ? ABGroup.CONTROL
            : ABGroup.TREATMENT;
    }
}
