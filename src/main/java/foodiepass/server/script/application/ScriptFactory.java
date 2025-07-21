package foodiepass.server.script.application;

import foodiepass.server.food.domain.TranslationClient;
import foodiepass.server.language.domain.Language;
import foodiepass.server.order.domain.OrderItem;
import foodiepass.server.script.domain.Script;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

    public Script create(final Language sourceLanguage, final Language targetLanguage, final List<OrderItem> orderItems) {
        final String prefix = getOrTranslatePrefix(sourceLanguage);
        final String concatenatedOrders = concatOrderItems(orderItems);
        final String travelerScript = prefix + lineSeparator() + concatenatedOrders;
        final String localScript = translationClient.translate(sourceLanguage, targetLanguage, travelerScript);

        return new Script(travelerScript, localScript);
    }

    private String getOrTranslatePrefix(final Language language) {
        return prefixCache.computeIfAbsent(
                language,
                lang -> translationClient.translate(ENGLISH, lang, ENGLISH_SCRIPT_PREFIX)
        );
    }

    private String concatOrderItems(final List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> String.format(ORDER_ITEM_FORMAT, item.getQuantity(), item.getName()))
                .collect(joining(lineSeparator()));
    }
}
