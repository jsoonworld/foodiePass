package foodiepass.server.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.vertexai.VertexAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
@Profile(ProfileConstants.NOT_PERFORMANCE_TEST)
public class GoogleCloudConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public GoogleCredentials googleCredentials(
            @Value("${google.credential.path}") final String credentialPath,
            @Value("${google.auth.scope}") final String scope
    ) throws IOException {
        return GoogleCredentials
                .fromStream(new ClassPathResource(credentialPath).getInputStream())
                .createScoped(scope);
    }

    @Bean
    public VertexAI vertexAI(
            @Value("${google.project-id}") final String projectId,
            @Value("${google.location}") final String location,
            final GoogleCredentials credentials
    ) {
        return new VertexAI.Builder()
                .setProjectId(projectId)
                .setLocation(location)
                .setCredentials(credentials)
                .build();
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
}
