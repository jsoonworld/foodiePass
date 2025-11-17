package foodiepass.server.menu.application;

import foodiepass.server.abtest.application.ABTestService;
import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.abtest.repository.MenuScanRepository;
import foodiepass.server.cache.domain.MenuItemEntity;
import foodiepass.server.cache.repository.MenuItemRepository;
import foodiepass.server.cache.util.ImageHashUtil;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.dto.request.MenuScanRequest;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    private final MenuItemEnricher menuItemEnricher;
    private final OcrReader ocrReader;
    private final MenuScanRepository menuScanRepository;
    private final MenuItemRepository menuItemRepository;

    /**
     * Scans a menu image and returns enriched results based on A/B test group.
     * Uses imageHash for deduplication to avoid redundant API calls.
     *
     * @param request Menu scan request with image and language/currency preferences
     * @param userId User session identifier
     * @return Mono of MenuScanResponse with conditional fields based on A/B group
     */
    public Mono<MenuScanResponse> scanMenu(MenuScanRequest request, String userId) {
        Instant startTime = Instant.now();

        // Step 1: Compute imageHash for deduplication
        String imageHash = ImageHashUtil.computeHash(request.base64EncodedImage());
        log.info("Computed imageHash: {}", imageHash);

        // Step 2: Check if this image has been processed before
        Optional<MenuScan> cachedScan = menuScanRepository.findByImageHash(imageHash);

        if (cachedScan.isPresent()) {
            log.info("Cache HIT for imageHash: {}", imageHash);
            return handleCacheHit(cachedScan.get(), userId,
                request.originLanguageName(), request.userLanguageName(),
                request.originCurrencyName(), request.userCurrencyName(),
                startTime);
        }

        log.info("Cache MISS for imageHash: {}", imageHash);
        return handleCacheMiss(request, userId, imageHash, startTime);
    }

    /**
     * Handles cache hit: Load menu items from DB, enrich, and return response.
     * This avoids expensive OCR API calls while still performing translation/enrichment.
     */
    private Mono<MenuScanResponse> handleCacheHit(MenuScan cachedScan, String userId, String originLanguageName,
                                                   String userLanguageName, String originCurrencyName,
                                                   String userCurrencyName, Instant startTime) {
        // Create new MenuScan for this user (for analytics)
        MenuScan newMenuScan = abTestService.assignAndCreateScan(
            userId,
            null,
            null, // No imageHash for cache hit analytics scan
            originLanguageName,
            userLanguageName,
            originCurrencyName,
            userCurrencyName
        );

        ABGroup abGroup = newMenuScan.getAbGroup();
        log.info("User {} assigned to A/B group: {} (cache hit)", userId, abGroup);

        // Load menu items (OCR results) from DB
        List<MenuItemEntity> menuItemEntities = menuItemRepository.findByMenuScanId(cachedScan.getId());
        log.info("Loaded {} menu items from cache (OCR results)", menuItemEntities.size());

        List<MenuItem> menuItems = menuItemEntities.stream()
            .map(MenuItemEntity::toMenuItem)
            .collect(Collectors.toList());

        // Perform enrichment (translation, food scraping, currency conversion)
        Language originLanguage = Language.fromLanguageName(originLanguageName);
        Language userLanguage = Language.fromLanguageName(userLanguageName);
        Currency originCurrency = Currency.fromCurrencyName(originCurrencyName);
        Currency userCurrency = Currency.fromCurrencyName(userCurrencyName);

        return menuItemEnricher.enrichBatchAsync(
                menuItems,
                originLanguage,
                userLanguage,
                originCurrency,
                userCurrency
            )
            .map(foodItemResponses -> {
                // Convert to DTOs with A/B group filtering
                List<MenuItemDto> itemDtos = foodItemResponses.stream()
                    .map(item -> convertToDto(item, abGroup))
                    .collect(Collectors.toList());

                double processingTime = Duration.between(startTime, Instant.now()).toMillis() / 1000.0;

                log.info("Menu scan completed (cache hit) for user {} in {}s with {} items",
                    userId, processingTime, itemDtos.size());

                return new MenuScanResponse(
                    newMenuScan.getId(),
                    abGroup.name(),
                    itemDtos,
                    processingTime
                );
            });
    }

    /**
     * Handles cache miss: Execute OCR pipeline, save OCR results, and return response.
     */
    private Mono<MenuScanResponse> handleCacheMiss(MenuScanRequest request, String userId, String imageHash, Instant startTime) {
        // Create MenuScan and assign A/B group with imageHash for deduplication
        MenuScan menuScan = abTestService.assignAndCreateScan(
            userId,
            null,
            imageHash,
            request.originLanguageName(),
            request.userLanguageName(),
            request.originCurrencyName(),
            request.userCurrencyName()
        );

        ABGroup abGroup = menuScan.getAbGroup();
        log.info("User {} assigned to A/B group: {} (cache miss)", userId, abGroup);

        // Step 1: Execute OCR to extract menu items
        List<MenuItem> ocrResults = ocrReader.read(request.base64EncodedImage());
        log.info("OCR extracted {} menu items", ocrResults.size());

        // Step 2: Save OCR results to DB for caching
        saveMenuItems(ocrResults, menuScan);
        log.info("Saved {} OCR results to cache", ocrResults.size());

        // Step 3: Perform enrichment (translation, food scraping, currency conversion)
        Language originLanguage = Language.fromLanguageName(request.originLanguageName());
        Language userLanguage = Language.fromLanguageName(request.userLanguageName());
        Currency originCurrency = Currency.fromCurrencyName(request.originCurrencyName());
        Currency userCurrency = Currency.fromCurrencyName(request.userCurrencyName());

        return menuItemEnricher.enrichBatchAsync(
                ocrResults,
                originLanguage,
                userLanguage,
                originCurrency,
                userCurrency
            )
            .map(enrichedResults -> {
                // Convert enriched results to DTOs
                List<MenuItemDto> itemDtos = enrichedResults.stream()
                    .map(item -> convertToDto(item, abGroup))
                    .collect(Collectors.toList());

                double processingTime = Duration.between(startTime, Instant.now()).toMillis() / 1000.0;

                log.info("Menu scan completed (cache miss) for user {} in {}s with {} items",
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
     * Saves OCR results (MenuItem list) to database as MenuItemEntity.
     */
    private void saveMenuItems(List<MenuItem> menuItems, MenuScan menuScan) {
        List<MenuItemEntity> entities = menuItems.stream()
            .map(item -> MenuItemEntity.from(item, menuScan))
            .collect(Collectors.toList());

        menuItemRepository.saveAll(entities);
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
