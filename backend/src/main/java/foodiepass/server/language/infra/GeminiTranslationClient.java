package foodiepass.server.language.infra;

import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.language.domain.Language;
import foodiepass.server.language.exception.LanguageErrorCode;
import foodiepass.server.language.exception.LanguageException;
import foodiepass.server.menu.application.port.out.TranslationClient;
import foodiepass.server.menu.infra.scraper.gemini.GeminiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Profile(ProfileConstants.NOT_PERFORMANCE_TEST)
public class GeminiTranslationClient implements TranslationClient {

    private final GeminiClient geminiClient;

    public GeminiTranslationClient(final GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    @Override
    public Mono<String> translateAsync(Language source, Language target, String text) {
        if (!StringUtils.hasText(text) || source.equals(target)) {
            return Mono.just(text);
        }

        return Mono.fromCallable(() -> {
                    try {
                        String prompt = buildTranslationPrompt(source, target, text);
                        String translated = geminiClient.generateText(prompt);
                        log.debug("Translated '{}' from {} to {}: '{}'", text, source.name(), target.name(), translated);
                        return translated;
                    } catch (Exception e) {
                        log.error("Translation failed for text: '{}' from {} to {}: {}", text, source.name(), target.name(), e.getMessage());
                        throw new LanguageException(LanguageErrorCode.TRANSLATION_FAILED);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<String> translateAsync(Language source, Language target, List<String> texts) {
        if (CollectionUtils.isEmpty(texts) || source.equals(target)) {
            return Flux.fromIterable(texts);
        }

        return Mono.fromCallable(() -> {
                    try {
                        String prompt = buildBatchTranslationPrompt(source, target, texts);
                        String response = geminiClient.generateText(prompt);
                        List<String> translations = parseBatchTranslation(response, texts.size());
                        log.debug("Batch translated {} texts from {} to {}", texts.size(), source.name(), target.name());
                        return translations;
                    } catch (Exception e) {
                        log.error("Batch translation failed for {} texts from {} to {}: {}", texts.size(), source.name(), target.name(), e.getMessage());
                        throw new LanguageException(LanguageErrorCode.TRANSLATION_FAILED);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    private String buildTranslationPrompt(Language source, Language target, String text) {
        String sourceLanguage = source == Language.AUTO ? "auto-detect" : source.name();
        String targetLanguage = target.name();

        return String.format(
                "Translate the following text from %s to %s. " +
                "Only return the translated text, without any explanations or additional text.\n\n" +
                "Text: %s\n\n" +
                "Translation:",
                sourceLanguage,
                targetLanguage,
                text
        );
    }

    private String buildBatchTranslationPrompt(Language source, Language target, List<String> texts) {
        String sourceLanguage = source == Language.AUTO ? "auto-detect" : source.name();
        String targetLanguage = target.name();

        String numberedTexts = IntStream.range(0, texts.size())
                .mapToObj(i -> String.format("%d. %s", i + 1, texts.get(i)))
                .collect(Collectors.joining("\n"));

        return String.format(
                "Translate the following texts from %s to %s. " +
                "Return only the translated texts in the same numbered format, without any explanations.\n\n" +
                "Texts:\n%s\n\n" +
                "Translations:",
                sourceLanguage,
                targetLanguage,
                numberedTexts
        );
    }

    private List<String> parseBatchTranslation(String response, int expectedCount) {
        String[] lines = response.split("\n");
        List<String> translations = IntStream.range(0, lines.length)
                .mapToObj(i -> lines[i])
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", "").trim())
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        if (translations.size() != expectedCount) {
            log.warn("Expected {} translations but got {}. Response: {}", expectedCount, translations.size(), response);
        }

        return translations;
    }
}
