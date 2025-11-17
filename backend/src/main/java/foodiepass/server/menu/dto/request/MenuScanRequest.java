package foodiepass.server.menu.dto.request;

/**
 * Request for menu scan operation with A/B test integration
 *
 * @param base64EncodedImage Menu image in base64 format (required)
 * @param originLanguageName Source language name (optional, default "auto")
 * @param userLanguageName Target language name (required)
 * @param originCurrencyName Source currency name (optional, auto-detect)
 * @param userCurrencyName Target currency name (required)
 */
public record MenuScanRequest(
    String base64EncodedImage,
    String originLanguageName,  // Optional, default "auto"
    String userLanguageName,
    String originCurrencyName,  // Optional, auto-detect
    String userCurrencyName
) {
    /**
     * Canonical constructor with validation and default value handling
     */
    public MenuScanRequest(
        String base64EncodedImage,
        String originLanguageName,
        String userLanguageName,
        String originCurrencyName,
        String userCurrencyName
    ) {
        // Validation for required fields
        if (base64EncodedImage == null || base64EncodedImage.isBlank()) {
            throw new IllegalArgumentException("Image is required");
        }
        if (userLanguageName == null || userLanguageName.isBlank()) {
            throw new IllegalArgumentException("Target language is required");
        }
        if (userCurrencyName == null || userCurrencyName.isBlank()) {
            throw new IllegalArgumentException("Target currency is required");
        }

        // Assign fields with default value handling
        this.base64EncodedImage = base64EncodedImage;
        this.originLanguageName = (originLanguageName == null || originLanguageName.isBlank()) ? "auto" : originLanguageName;
        this.userLanguageName = userLanguageName;
        this.originCurrencyName = (originCurrencyName == null || originCurrencyName.isBlank()) ? "auto" : originCurrencyName;
        this.userCurrencyName = userCurrencyName;
    }
}
