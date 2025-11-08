package foodiepass.server.abtest.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single menu scan session with A/B group assignment.
 *
 * <p>Stores metadata about each menu scanning operation for analytics
 * and hypothesis validation (H1, H3).
 */
@Entity
@Table(name = "menu_scan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuScan {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ABGroup abGroup;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String sourceLanguage;

    @Column(length = 50)
    private String targetLanguage;

    @Column(length = 100)
    private String sourceCurrency;

    @Column(length = 100)
    private String targetCurrency;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private MenuScan(String userId, ABGroup abGroup, String imageUrl,
                     String sourceLanguage, String targetLanguage,
                     String sourceCurrency, String targetCurrency) {
        validateUserId(userId);
        validateABGroup(abGroup);

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.abGroup = abGroup;
        this.imageUrl = imageUrl;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new MenuScan instance.
     *
     * @param userId User session identifier
     * @param abGroup A/B test group assignment
     * @param imageUrl Optional image URL for audit
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param sourceCurrency Source currency code
     * @param targetCurrency Target currency code
     * @return New MenuScan instance
     * @throws IllegalArgumentException if userId or abGroup is null/empty
     */
    public static MenuScan create(String userId, ABGroup abGroup, String imageUrl,
                                   String sourceLanguage, String targetLanguage,
                                   String sourceCurrency, String targetCurrency) {
        return new MenuScan(userId, abGroup, imageUrl, sourceLanguage, targetLanguage,
                sourceCurrency, targetCurrency);
    }

    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
    }

    private void validateABGroup(ABGroup abGroup) {
        if (abGroup == null) {
            throw new IllegalArgumentException("abGroup cannot be null");
        }
    }
}
