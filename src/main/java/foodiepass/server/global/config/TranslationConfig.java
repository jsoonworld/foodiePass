package foodiepass.server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import foodiepass.server.food.domain.TranslationClient;
import foodiepass.server.language.infra.GoogleTranslationClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

@Configuration
public class TranslationConfig {

    @Bean
    public GoogleCredentials googleCredentials(
            @Value("${google.credential.path}") final Resource credentialResource
    ) throws IOException {
        return GoogleCredentials.fromStream(credentialResource.getInputStream());
    }

    @Bean
    public Translate translate(
            final GoogleCredentials credentials,
            @Value("${google.project-id}") final String projectId
    ) {
        return TranslateOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .getService();
    }

    @Bean
    @Primary
    public TranslationClient translationClient(
            final Translate translate,
            @Value("${google.translation.model}") final String translationModel
    ) {
        return new GoogleTranslationClient(translate, translationModel);
    }
}
