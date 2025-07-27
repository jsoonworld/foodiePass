package foodiepass.server.menu.infra.scraper.tasteAtlas;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import foodiepass.server.menu.infra.exception.ScrapingErrorCode;
import foodiepass.server.menu.infra.exception.ScrapingException;
import foodiepass.server.menu.infra.scraper.auth.domain.Authenticatable;
import foodiepass.server.menu.infra.scraper.tasteAtlas.dto.TasteAtlasResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Profile("!performance-test")
public class TasteAtlasApiClient implements Authenticatable {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final TasteAtlasProperties properties;
    private volatile String authToken;

    public TasteAtlasApiClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, TasteAtlasProperties properties) {
        this.webClient = webClientBuilder.baseUrl(properties.api().url()).build();
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.authToken = properties.api().authToken();
    }

    @Override
    public void updateAuth(String token) {
        log.info("TasteAtlas API auth token updated.");
        this.authToken = token;
    }

    public Mono<TasteAtlasResponse> search(String foodName) {
        String searchQuery = String.format(properties.api().url(), foodName.replace(" ", "+"));

        return webClient.get()
                .uri(searchQuery)
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseResponse)
                .onErrorMap(e -> !(e instanceof ScrapingException), e -> new ScrapingException(ScrapingErrorCode.TASTE_ATLAS_API_REQUEST_FAILED));
    }

    public Mono<String> fetchHtml(final String url) {
        return WebClient.create().get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorMap(e -> new ScrapingException(ScrapingErrorCode.TASTE_ATLAS_HTML_FETCH_FAILED));
    }

    private Mono<TasteAtlasResponse> parseResponse(String body) {
        try {
            return Mono.just(objectMapper.readValue(body, TasteAtlasResponse.class));
        } catch (Exception e) {
            return Mono.error(new ScrapingException(ScrapingErrorCode.TASTE_ATLAS_JSON_PARSING_FAILED));
        }
    }
}
