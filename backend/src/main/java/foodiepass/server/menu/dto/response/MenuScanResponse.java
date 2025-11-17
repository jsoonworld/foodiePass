package foodiepass.server.menu.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Response for menu scan operation
 *
 * @param scanId Unique identifier for this scan session
 * @param abGroup A/B test group assignment ("CONTROL" | "TREATMENT")
 * @param items List of menu items with translations and enrichments
 * @param processingTime Total processing time in seconds
 */
public record MenuScanResponse(
    UUID scanId,
    String abGroup,
    List<MenuItemDto> items,
    double processingTime
) {
}
