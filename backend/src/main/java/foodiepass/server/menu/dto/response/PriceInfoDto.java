package foodiepass.server.menu.dto.response;

/**
 * Price information with currency conversion
 *
 * @param originalAmount Original price amount
 * @param originalCurrency Original currency code (e.g., "USD")
 * @param originalFormatted Formatted original price (e.g., "$15.00")
 * @param convertedAmount Converted price amount
 * @param convertedCurrency Target currency code (e.g., "KRW")
 * @param convertedFormatted Formatted converted price (e.g., "₩20,000")
 */
public record PriceInfoDto(
    double originalAmount,
    String originalCurrency,
    String originalFormatted,
    double convertedAmount,
    String convertedCurrency,
    String convertedFormatted
) {
}
