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

        // Step 1: Assign A/B group
        ABGroup abGroup = abTestService.assignGroup(userId);
        log.info("User {} assigned to A/B group: {}", userId, abGroup);

        // Step 2: Create MenuScan record
        MenuScan menuScan = abTestService.createScan(
            userId,
            abGroup,
            null,  // imageUrl not stored for MVP
            request.originLanguageName(),
            request.userLanguageName(),
            request.originCurrencyName(),
            request.userCurrencyName()
        );

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
     * Converts PriceInfoResponse to PriceInfoDto
     */
    private PriceInfoDto convertPriceInfo(ReconfigureResponse.PriceInfoResponse priceInfo) {
        // Parse the formatted strings
        // Format: "₩20,000" or "$15.00"
        return new PriceInfoDto(
            0.0,  // originalAmount - would need parsing
            "USD",  // originalCurrency - would need extraction
            priceInfo.originPriceWithCurrencyUnit(),
            0.0,  // convertedAmount - would need parsing
            "KRW",  // convertedCurrency - would need extraction
            priceInfo.userPriceWithCurrencyUnit()
        );
    }
}
