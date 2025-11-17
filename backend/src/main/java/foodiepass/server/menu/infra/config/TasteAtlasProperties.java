package foodiepass.server.menu.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taste-atlas")
public record TasteAtlasProperties(
        String baseUrl,
        Api api,
        Defaults defaults,
        Selector selector
) {
    public record Api(String url, String authToken) {}
    public record Defaults(String imageUrl, String description) {}
    public record Selector(String image, String description) {}
}
