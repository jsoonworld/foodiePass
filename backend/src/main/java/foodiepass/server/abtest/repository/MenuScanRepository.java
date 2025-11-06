package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MenuScan entities.
 * Supports A/B test analytics and user session management.
 */
public interface MenuScanRepository extends JpaRepository<MenuScan, UUID> {

    /**
     * Finds the most recent scan for a given user.
     * Used to maintain consistent A/B group assignment across sessions.
     *
     * @param userId User session identifier
     * @return Optional containing the most recent scan, or empty if user has no scans
     */
    Optional<MenuScan> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Counts total scans for a specific A/B group.
     * Used for hypothesis validation (H1, H3) analytics.
     *
     * @param abGroup A/B test group (CONTROL or TREATMENT)
     * @return Number of scans in the specified group
     */
    long countByAbGroup(ABGroup abGroup);
}
