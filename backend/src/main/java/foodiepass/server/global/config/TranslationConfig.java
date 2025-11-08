package foodiepass.server.global.config;

import foodiepass.server.language.infra.GeminiTranslationClient;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.infra.scraper.gemini.GeminiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(ProfileConstants.NOT_PERFORMANCE_TEST)
public class TranslationConfig {

    @Bean
    @Primary
    public TranslationClient translationClient(final GeminiClient geminiClient) {
        return new GeminiTranslationClient(geminiClient);
    }
}
