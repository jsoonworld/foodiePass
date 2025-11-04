package foodiepass.server.menu.application;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.menu.dto.request.MenuScanRequest;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for menu scan operations with A/B test integration
 * Orchestrates: Group assignment → Pipeline execution → Conditional filtering → Response
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuScanService {

    private final ABTestService abTestService;
    private final MenuService menuService;

    /**
     * Scans a menu image and returns enriched results based on A/B test group
     *
     * @param request Menu scan request with image and language/currency preferences
     * @param userId User session identifier
     * @return Mono of MenuScanResponse with conditional fields based on A/B group
     */
    public Mono<MenuScanResponse> scanMenu(MenuScanRequest request, String userId) {
        Instant startTime = Instant.now();

        // Step 1 & 2: Atomically assign A/B group and create MenuScan record
        // This prevents race conditions with concurrent requests
        MenuScan menuScan = abTestService.assignAndCreateScan(
            userId,
            null,  // imageUrl not stored for MVP
            request.originLanguageName(),
            request.userLanguageName(),
            request.originCurrencyName(),
            request.userCurrencyName()
        );

        ABGroup abGroup = menuScan.getAbGroup();
        log.info("User {} assigned to A/B group: {}", userId, abGroup);

        // Step 3: Execute OCR + Enrichment pipeline (reuse existing MenuService)
        ReconfigureRequest reconfigureRequest = new ReconfigureRequest(
            request.base64EncodedImage(),
            request.originLanguageName(),
            request.userLanguageName(),
            request.originCurrencyName(),
            request.userCurrencyName()
        );

        return menuService.reconfigure(reconfigureRequest)
            .map(response -> {
                // Step 4: Convert to DTOs with conditional filtering
                List<MenuItemDto> itemDtos = response.results().stream()
                    .map(item -> convertToDto(item, abGroup))
                    .collect(Collectors.toList());

                // Step 5: Calculate processing time
                double processingTime = Duration.between(startTime, Instant.now()).toMillis() / 1000.0;

                log.info("Menu scan completed for user {} in {}s with {} items",
                    userId, processingTime, itemDtos.size());

                return new MenuScanResponse(
                    menuScan.getId(),
                    abGroup.name(),
                    itemDtos,
                    processingTime
                );
            });
    }

    /**
     * Converts FoodItemResponse to MenuItemDto with conditional fields based on A/B group
     * - CONTROL: Only name, translation, and price (no food info)
     * - TREATMENT: Full enrichment with description and image
     */
    private MenuItemDto convertToDto(ReconfigureResponse.FoodItemResponse item, ABGroup abGroup) {
        PriceInfoDto priceInfo = convertPriceInfo(item.priceInfo());

        // Conditional fields based on A/B group
        if (abGroup == ABGroup.TREATMENT) {
            // Treatment group: Include full food information
            return new MenuItemDto(
                UUID.randomUUID(),
                item.originMenuName(),
                item.translatedMenuName(),
                item.description(),
                item.image(),
                priceInfo,
                null  // TODO: Add confidence score if available
            );
        } else {
            // Control group: Exclude food information
            return new MenuItemDto(
                UUID.randomUUID(),
                item.originMenuName(),
                item.translatedMenuName(),
                null,  // No description for CONTROL
                null,  // No image for CONTROL
                priceInfo,
                null
            );
        }
    }

    /**
     * Converts PriceInfoResponse to PriceInfoDto with proper parsing
     */
    private PriceInfoDto convertPriceInfo(ReconfigureResponse.PriceInfoResponse priceInfo) {
        // Parse original price
        String originalFormatted = priceInfo.originPriceWithCurrencyUnit();
        double originalAmount = extractAmount(originalFormatted);
        String originalCurrency = extractCurrency(originalFormatted);

        // Parse converted price
        String convertedFormatted = priceInfo.userPriceWithCurrencyUnit();
        double convertedAmount = extractAmount(convertedFormatted);
        String convertedCurrency = extractCurrency(convertedFormatted);

        return new PriceInfoDto(
            originalAmount,
            originalCurrency,
            originalFormatted,
            convertedAmount,
            convertedCurrency,
            convertedFormatted
        );
    }

    /**
     * Extracts numeric amount from formatted price string
     * Examples: "$15.00" → 15.0, "₩20,000" → 20000.0
     */
    private double extractAmount(String formattedPrice) {
        if (formattedPrice == null || formattedPrice.isEmpty()) {
            return 0.0;
        }

        try {
            // Remove currency symbols and whitespace, keep only digits, dots, and commas
            String numericPart = formattedPrice.replaceAll("[^0-9.,]", "");
            // Remove commas for parsing
            numericPart = numericPart.replace(",", "");
            return Double.parseDouble(numericPart);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse amount from: {}", formattedPrice);
            return 0.0;
        }
    }

    /**
     * Extracts currency code from formatted price string
     * Examples: "$15.00" → "USD", "₩20,000" → "KRW", "€10.50" → "EUR"
     */
    private String extractCurrency(String formattedPrice) {
        if (formattedPrice == null || formattedPrice.isEmpty()) {
            return "UNKNOWN";
        }

        // Map common currency symbols to codes
        if (formattedPrice.contains("$")) return "USD";
        if (formattedPrice.contains("₩")) return "KRW";
        if (formattedPrice.contains("€")) return "EUR";
        if (formattedPrice.contains("¥")) return "JPY";
        if (formattedPrice.contains("£")) return "GBP";
        if (formattedPrice.contains("元")) return "CNY";

        // Try to extract 3-letter currency code (e.g., "USD 15.00")
        String[] parts = formattedPrice.split("\\s+");
        for (String part : parts) {
            if (part.matches("[A-Z]{3}")) {
                return part;
            }
        }

        log.warn("Could not extract currency from: {}", formattedPrice);
        return "UNKNOWN";
    }
}
