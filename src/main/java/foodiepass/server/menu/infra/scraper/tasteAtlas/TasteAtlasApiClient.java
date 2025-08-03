package foodiepass.server.menu.infra.scraper.tasteAtlas;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import foodiepass.server.menu.infra.exception.ScrapingErrorCode;
import foodiepass.server.menu.infra.exception.ScrapingException;
import foodiepass.server.menu.infra.scraper.auth.domain.Authenticatable;
import foodiepass.server.menu.infra.scraper.tasteAtlas.dto.TasteAtlasResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; 
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Profile(ProfileConstants.NOT_PERFORMANCE_TEST)
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

    @CircuitBreaker(name = "tasteAtlas", fallbackMethod = "fallbackSearch")
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

    @CircuitBreaker(name = "tasteAtlas", fallbackMethod = "fallbackFetchHtml")
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

    public Mono<TasteAtlasResponse> fallbackSearch(String foodName, Throwable t) {
        log.warn("Circuit Breaker is open for TasteAtlas search. foodName: {}. error: {}", foodName, t.getMessage());
        return Mono.empty();
    }

    public Mono<String> fallbackFetchHtml(final String url, Throwable t) {
        log.warn("Circuit Breaker is open for TasteAtlas fetchHtml. url: {}. error: {}", url, t.getMessage());
        return Mono.empty();
    }
}
