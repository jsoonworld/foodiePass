package foodiepass.server.language.dto.response;

import foodiepass.server.language.domain.Language;

public record LanguageResponse(String languageName) {
    public static LanguageResponse from(final Language language) {
        return new LanguageResponse(language.getLanguageName());
    }
}
