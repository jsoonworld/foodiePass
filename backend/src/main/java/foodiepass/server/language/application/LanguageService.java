package foodiepass.server.language.application;

import foodiepass.server.language.domain.Language;
import foodiepass.server.language.dto.response.LanguageResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LanguageService {

    private static final List<LanguageResponse> CACHED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(LanguageResponse::from)
                    .collect(Collectors.toUnmodifiableList());

    public List<LanguageResponse> findAllLanguages() {
        return CACHED_LANGUAGES;
    }
}
