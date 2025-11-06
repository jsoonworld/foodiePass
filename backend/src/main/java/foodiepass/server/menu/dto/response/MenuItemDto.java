package foodiepass.server.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * Menu item with translations and food information
 *
 * @param id Unique item identifier
 * @param originalName Original menu item name
 * @param translatedName Translated menu item name
 * @param description Food description (Treatment group only)
 * @param imageUrl Food image URL (Treatment group only)
 * @param priceInfo Price information with currency conversion
 * @param matchConfidence Confidence score for food matching (0.0-1.0)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuItemDto(
    UUID id,
    String originalName,
    String translatedName,
    String description,      // null for CONTROL group
    String imageUrl,         // null for CONTROL group
    PriceInfoDto priceInfo,
    Double matchConfidence   // null if no food match
) {
}
