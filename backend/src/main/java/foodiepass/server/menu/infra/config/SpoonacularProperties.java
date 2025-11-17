package foodiepass.server.menu.infra.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spoonacular")
public class SpoonacularProperties {
    private final String apiKey;
    private final Api api;
    private final Defaults defaults;

    public record Api(
            String baseUrl,
            String searchPath,
            int maxResults
    ) {}

    public record Defaults(
            String imageUrl,
            String description
    ) {}
}
