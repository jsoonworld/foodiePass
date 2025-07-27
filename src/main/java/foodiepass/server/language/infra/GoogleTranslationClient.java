package foodiepass.server.language.infra;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.Translation;
import foodiepass.server.language.domain.Language;
import foodiepass.server.language.exception.LanguageErrorCode;
import foodiepass.server.language.exception.LanguageException;
import foodiepass.server.menu.application.port.out.TranslationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.cloud.translate.Translate.TranslateOption.model;
import static com.google.cloud.translate.Translate.TranslateOption.sourceLanguage;
import static com.google.cloud.translate.Translate.TranslateOption.targetLanguage;

@Component
@Profile("!performance-test")
public class GoogleTranslationClient implements TranslationClient {

    private final Translate translate;
    private final String translationModel;

    public GoogleTranslationClient(
            final Translate translate,
            @Value("${google.translation.model}") final String translationModel
    ) {
        this.translate = translate;
        this.translationModel = translationModel;
    }

    @Override
    public Mono<String> translateAsync(Language source, Language target, String text) {
        if (!StringUtils.hasText(text) || source.equals(target)) {
            return Mono.just(text);
        }

        return Mono.fromCallable(() -> {
                    try {
                        Translation translation = translate.translate(
                                text,
                                sourceLanguage(source.getLanguageCode()),
                                targetLanguage(target.getLanguageCode()),
                                model(translationModel)
                        );
                        return translation.getTranslatedText();
                    } catch (TranslateException e) {
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
                        List<Translation> translations = translate.translate(
                                texts,
                                sourceLanguage(source.getLanguageCode()),
                                targetLanguage(target.getLanguageCode()),
                                model(translationModel)
                        );
                        return translations.stream()
                                .map(Translation::getTranslatedText)
                                .collect(Collectors.toList());
                    } catch (TranslateException e) {
                        throw new LanguageException(LanguageErrorCode.TRANSLATION_FAILED);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }
}
