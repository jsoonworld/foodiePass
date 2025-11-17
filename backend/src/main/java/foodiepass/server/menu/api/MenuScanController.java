package foodiepass.server.menu.api;

import foodiepass.server.menu.application.MenuScanService;
import foodiepass.server.menu.dto.request.MenuScanRequest;
import foodiepass.server.menu.dto.response.MenuScanResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for menu scanning operations with A/B test integration
 * Endpoint: POST /api/menus/scan
 */
@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuScanController {

    private final MenuScanService menuScanService;

    /**
     * Scans a menu image and returns enriched results
     * - Assigns user to A/B test group (Control or Treatment)
     * - Executes OCR, translation, food matching, and currency conversion
     * - Returns conditional response based on A/B group assignment
     *
     * @param request Menu scan request with image and preferences
     * @param session HTTP session for user identification
     * @return MenuScanResponse with scanId, abGroup, items, and processingTime
     */
    @PostMapping("/scan")
    public Mono<ResponseEntity<MenuScanResponse>> scanMenu(
        @RequestBody MenuScanRequest request,
        HttpSession session
    ) {
        // Use session ID as userId for A/B test assignment
        String userId = session.getId();
        log.info("Received menu scan request from user: {}", userId);
        log.debug("Request details - userLanguageName: {}, userCurrencyName: {}",
                request.userLanguageName(), request.userCurrencyName());

        return menuScanService.scanMenu(request, userId)
            .map(ResponseEntity::ok)
            .doOnSuccess(response ->
                log.info("Menu scan completed successfully for user: {}", userId))
            .doOnError(error ->
                log.error("Menu scan failed for user: {}", userId, error));
    }
}
