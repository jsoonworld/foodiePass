package foodiepass.server.survey.domain;

import foodiepass.server.abtest.domain.ABGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user's confidence survey response for a menu scan.
 *
 * <p>Collects user confidence (Yes/No) to validate hypothesis H1 and H3:
 * Whether visual menu elements (photos + descriptions) increase ordering confidence
 * compared to text-only translation.
 */
@Entity
@Table(
    name = "survey_response",
    indexes = {
        @Index(name = "idx_scan_id", columnList = "scan_id"),
        @Index(name = "idx_ab_group", columnList = "ab_group"),
        @Index(name = "idx_ab_group_confidence", columnList = "ab_group,has_confidence")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyResponse {

    @Id
    private UUID id;

    @Column(name = "scan_id", nullable = false)
    private UUID scanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ab_group", nullable = false, length = 20)
    private ABGroup abGroup;

    @Column(name = "has_confidence", nullable = false)
    private Boolean hasConfidence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Creates a new SurveyResponse instance.
     *
     * @param scanId ID of the associated menu scan
     * @param abGroup A/B test group from the menu scan
     * @param hasConfidence User's confidence response (true = Yes, false = No)
     * @throws IllegalArgumentException if scanId, abGroup, or hasConfidence is null
     */
    public SurveyResponse(UUID scanId, ABGroup abGroup, Boolean hasConfidence) {
        validateScanId(scanId);
        validateABGroup(abGroup);
        validateHasConfidence(hasConfidence);

        this.id = UUID.randomUUID();
        this.scanId = scanId;
        this.abGroup = abGroup;
        this.hasConfidence = hasConfidence;
        this.createdAt = LocalDateTime.now();
    }

    private void validateScanId(UUID scanId) {
        if (scanId == null) {
            throw new IllegalArgumentException("scanId cannot be null");
        }
    }

    private void validateABGroup(ABGroup abGroup) {
        if (abGroup == null) {
            throw new IllegalArgumentException("abGroup cannot be null");
        }
    }

    private void validateHasConfidence(Boolean hasConfidence) {
        if (hasConfidence == null) {
            throw new IllegalArgumentException("hasConfidence cannot be null");
        }
    }
}
