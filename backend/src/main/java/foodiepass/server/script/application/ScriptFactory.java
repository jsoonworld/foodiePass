package foodiepass.server.script.application;

import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.order.domain.OrderItem;
import foodiepass.server.script.domain.Script;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static foodiepass.server.language.domain.Language.ENGLISH;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class ScriptFactory {

    private static final String ENGLISH_SCRIPT_PREFIX = "Hello I want to order";
    private static final String ORDER_ITEM_FORMAT = "%d %s";

    private final TranslationClient translationClient;
    private final Map<Language, String> prefixCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        prefixCache.put(ENGLISH, ENGLISH_SCRIPT_PREFIX);
    }

    public Mono<Script> createAsync(final Language sourceLanguage, final Language targetLanguage, final List<OrderItem> orderItems) {
        return getOrTranslatePrefixAsync(sourceLanguage)
                .flatMap(prefix -> {
                    final String concatenatedOrders = concatOrderItems(orderItems);
                    final String travelerScript = prefix + lineSeparator() + concatenatedOrders;

                    return translationClient.translateAsync(sourceLanguage, targetLanguage, travelerScript)
                            .map(localScript -> new Script(travelerScript, localScript));
                });
    }

    private Mono<String> getOrTranslatePrefixAsync(final Language language) {
        if (prefixCache.containsKey(language)) {
            return Mono.just(prefixCache.get(language));
        }
        return translationClient.translateAsync(ENGLISH, language, ENGLISH_SCRIPT_PREFIX)
                .doOnSuccess(translatedPrefix -> prefixCache.put(language, translatedPrefix));
    }

    private String concatOrderItems(final List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> String.format(ORDER_ITEM_FORMAT, item.getQuantity(), item.getName()))
                .collect(joining(lineSeparator()));
    }
}
