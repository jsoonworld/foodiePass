package foodiepass.server.config;

import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.ExchangeRateProvider;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.infra.scraper.gemini.GeminiClient;
import foodiepass.server.menu.infra.scraper.tasteAtlas.TasteAtlasFoodScrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test configuration that mocks all external API dependencies.
 * This prevents actual API calls during controller integration tests.
 */
@TestConfiguration
public class MockExternalDependenciesConfig {

    @MockBean
    private ExchangeRateProvider exchangeRateProvider;

    @MockBean
    private TasteAtlasFoodScrapper tasteAtlasFoodScrapper;

    @MockBean
    private GeminiClient geminiClient;

    @MockBean
    private TranslationClient translationClient;

    @MockBean
    private OcrReader ocrReader;

    @PostConstruct
    public void setupMockBehavior() {
        // Mock ExchangeRateProvider - return default rate of 1.0
        when(exchangeRateProvider.getExchangeRateAsync(any(), any()))
                .thenReturn(Mono.just(1.0));

        // Mock TasteAtlasFoodScrapper - return empty Flux
        when(tasteAtlasFoodScrapper.scrapAsync(anyList()))
                .thenReturn(Flux.empty());

        // Mock GeminiClient - return empty response
        when(geminiClient.generateText(anyString()))
                .thenReturn("");
        when(geminiClient.generateText(any(), anyString(), anyString()))
                .thenReturn("");

        // Mock TranslationClient - return original text (echo)
        when(translationClient.translateAsync(any(Language.class), any(Language.class), anyString()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(2, String.class)));
        when(translationClient.translateAsync(any(Language.class), any(Language.class), anyList()))
                .thenAnswer(invocation -> Flux.fromIterable(invocation.getArgument(2, List.class)));
    }
}
