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
    public MenuScanRequest {
        // Compact constructor for validation and default values
        if (base64EncodedImage == null || base64EncodedImage.isBlank()) {
            throw new IllegalArgumentException("Image is required");
        }
        if (userLanguageName == null || userLanguageName.isBlank()) {
            throw new IllegalArgumentException("Target language is required");
        }
        if (userCurrencyName == null || userCurrencyName.isBlank()) {
            throw new IllegalArgumentException("Target currency is required");
        }
        if (originLanguageName == null || originLanguageName.isBlank()) {
            originLanguageName = "auto";
        }
    }
}
