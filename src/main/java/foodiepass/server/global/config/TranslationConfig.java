package foodiepass.server.global.config;

import com.google.cloud.translate.Translate;
import foodiepass.server.language.infra.GoogleTranslationClient;
import foodiepass.server.menu.domain.TranslationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TranslationConfig {

    @Bean
    @Primary
    public TranslationClient translationClient(
            final Translate translate,
            @Value("${google.translation.model}") final String translationModel
    ) {
        return new GoogleTranslationClient(translate, translationModel);
    }
}
