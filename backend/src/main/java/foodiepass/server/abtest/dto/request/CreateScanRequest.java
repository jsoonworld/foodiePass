package foodiepass.server.abtest.dto.request;

import foodiepass.server.abtest.domain.ABGroup;

/**
 * Request DTO for creating a menu scan session.
 * Encapsulates all parameters needed for scan creation to improve code readability
 * and maintainability.
 *
 * @param userId User session identifier
 * @param abGroup A/B test group assignment
 * @param imageUrl Optional image URL for audit trail
 * @param sourceLanguage Source language code (e.g., "ja")
 * @param targetLanguage Target language code (e.g., "ko")
 * @param sourceCurrency Source currency code (e.g., "JPY")
 * @param targetCurrency Target currency code (e.g., "KRW")
 */
public record CreateScanRequest(
    String userId,
    ABGroup abGroup,
    String imageUrl,
    String sourceLanguage,
    String targetLanguage,
    String sourceCurrency,
    String targetCurrency
) {
}
