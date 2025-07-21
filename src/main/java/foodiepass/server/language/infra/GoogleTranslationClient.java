package foodiepass.server.language.infra;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.Translation;
import foodiepass.server.food.domain.TranslationClient;
import foodiepass.server.language.domain.Language;
import foodiepass.server.language.exception.LanguageErrorCode;
import foodiepass.server.language.exception.LanguageException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.google.cloud.translate.Translate.TranslateOption.model;
import static com.google.cloud.translate.Translate.TranslateOption.sourceLanguage;
import static com.google.cloud.translate.Translate.TranslateOption.targetLanguage;

@Component
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
    @Cacheable(cacheNames = "translations", key = "{#from.name(), #to.name(), #content}")
    public String translate(
            @NonNull final Language from,
            @NonNull final Language to,
            @NonNull final String content
    ) {
        if (!StringUtils.hasText(content) || from.equals(to)) {
            return content;
        }

        try {
            final Translation translation = translate.translate(
                    content,
                    sourceLanguage(from.getLanguageCode()),
                    targetLanguage(to.getLanguageCode()),
                    model(translationModel)
            );
            return translation.getTranslatedText();
        } catch (final TranslateException e) {
            throw new LanguageException(LanguageErrorCode.TRANSLATION_FAILED);
        }
    }
}
