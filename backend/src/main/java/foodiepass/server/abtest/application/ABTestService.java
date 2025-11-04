package foodiepass.server.abtest.application;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.dto.request.CreateScanRequest;
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
     * Assigns a user to an A/B test group and creates a scan session atomically.
     * This method prevents race conditions by combining group assignment and scan creation
     * in a single transaction.
     *
     * <p>New users are randomly assigned 50:50 to CONTROL or TREATMENT.
     * Existing users maintain their previous group assignment for consistency.
     *
     * <p><b>Recommended Usage:</b> Use this method instead of calling assignGroup()
     * and createScan() separately to ensure atomicity:
     * <pre>{@code
     * MenuScan scan = abTestService.assignAndCreateScan(
     *     userId, imageUrl, sourceLanguage, targetLanguage, sourceCurrency, targetCurrency
     * );
     * }</pre>
     *
     * @param userId User session identifier
     * @param imageUrl Optional image URL for audit trail
     * @param sourceLanguage Source language code (e.g., "ja")
     * @param targetLanguage Target language code (e.g., "ko")
     * @param sourceCurrency Source currency code (e.g., "JPY")
     * @param targetCurrency Target currency code (e.g., "KRW")
     * @return Created MenuScan with assigned group
     */
    @Transactional
    public MenuScan assignAndCreateScan(
        String userId,
        String imageUrl,
        String sourceLanguage,
        String targetLanguage,
        String sourceCurrency,
        String targetCurrency
    ) {
        ABGroup group = findExistingGroupOrAssignNew(userId);

        CreateScanRequest request = new CreateScanRequest(
            userId, group, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency
        );

        return createScan(request);
    }

    /**
     * Assigns a user to an A/B test group.
     * New users are randomly assigned 50:50 to CONTROL or TREATMENT.
     * Existing users maintain their previous group assignment for consistency.
     *
     * <p><b>Note:</b> Prefer using {@link #assignAndCreateScan} instead to prevent
     * race conditions. This method is kept for backward compatibility and testing.
     *
     * @param userId User session identifier
     * @return Assigned A/B group (CONTROL or TREATMENT)
     * @deprecated Use {@link #assignAndCreateScan} instead for atomic operations
     */
    @Deprecated
    public ABGroup assignGroup(String userId) {
        return findExistingGroupOrAssignNew(userId);
    }

    /**
     * Internal method to find existing group or assign a new one.
     *
     * @param userId User session identifier
     * @return Assigned A/B group
     */
    private ABGroup findExistingGroupOrAssignNew(String userId) {
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
     * @param request Request DTO containing scan parameters
     * @return Created MenuScan entity with generated ID and timestamp
     */
    @Transactional
    public MenuScan createScan(CreateScanRequest request) {
        MenuScan scan = new MenuScan(
            request.userId(), request.abGroup(), request.imageUrl(),
            request.sourceLanguage(), request.targetLanguage(),
            request.sourceCurrency(), request.targetCurrency()
        );

        return menuScanRepository.save(scan);
    }

    /**
     * Creates and persists a new menu scan session (legacy method).
     *
     * @param userId User session identifier
     * @param abGroup A/B test group assignment
     * @param imageUrl Optional image URL for audit trail
     * @param sourceLanguage Source language code (e.g., "ja")
     * @param targetLanguage Target language code (e.g., "ko")
     * @param sourceCurrency Source currency code (e.g., "JPY")
     * @param targetCurrency Target currency code (e.g., "KRW")
     * @return Created MenuScan entity with generated ID and timestamp
     * @deprecated Use {@link #createScan(CreateScanRequest)} instead
     */
    @Deprecated
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
        CreateScanRequest request = new CreateScanRequest(
            userId, abGroup, imageUrl,
            sourceLanguage, targetLanguage,
            sourceCurrency, targetCurrency
        );

        return createScan(request);
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
