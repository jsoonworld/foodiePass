package foodiepass.server.menu.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({SpoonacularProperties.class, TasteAtlasProperties.class})
public class WebClientConfig {

    private final SpoonacularProperties spoonacularProperties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(spoonacularProperties.getApi().baseUrl())
                .build();
    }
}
