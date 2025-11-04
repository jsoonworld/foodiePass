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

    @Column(length = 10)
    private String sourceCurrency;

    @Column(length = 10)
    private String targetCurrency;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Creates a new MenuScan instance.
     *
     * @param userId User session identifier
     * @param abGroup A/B test group assignment
     * @param imageUrl Optional image URL for audit
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param sourceCurrency Source currency code
     * @param targetCurrency Target currency code
     * @throws IllegalArgumentException if userId or abGroup is null/empty
     */
    public MenuScan(String userId, ABGroup abGroup, String imageUrl,
                    String sourceLanguage, String targetLanguage,
                    String sourceCurrency, String targetCurrency) {
        this(userId, abGroup, imageUrl, sourceLanguage, targetLanguage,
             sourceCurrency, targetCurrency, LocalDateTime.now());
    }

    /**
     * Package-private constructor for testing purposes.
     * Allows explicit timestamp setting to avoid flaky tests with Thread.sleep.
     */
    MenuScan(String userId, ABGroup abGroup, String imageUrl,
             String sourceLanguage, String targetLanguage,
             String sourceCurrency, String targetCurrency,
             LocalDateTime createdAt) {
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
        this.createdAt = createdAt;
    }

    /**
     * Factory method for testing purposes.
     * Creates a MenuScan with explicit timestamp to avoid Thread.sleep in tests.
     *
     * <p><b>WARNING:</b> This method should only be used in tests.
     * Production code should use the standard constructor.
     *
     * @param userId User session identifier
     * @param abGroup A/B test group assignment
     * @param imageUrl Optional image URL for audit
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param sourceCurrency Source currency code
     * @param targetCurrency Target currency code
     * @param createdAt Explicit creation timestamp
     * @return MenuScan instance with specified timestamp
     */
    public static MenuScan forTest(String userId, ABGroup abGroup, String imageUrl,
                                    String sourceLanguage, String targetLanguage,
                                    String sourceCurrency, String targetCurrency,
                                    LocalDateTime createdAt) {
        return new MenuScan(userId, abGroup, imageUrl, sourceLanguage, targetLanguage,
                            sourceCurrency, targetCurrency, createdAt);
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
