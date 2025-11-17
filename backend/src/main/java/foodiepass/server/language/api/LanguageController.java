package foodiepass.server.language.api;

import foodiepass.server.language.application.LanguageService;
import foodiepass.server.language.dto.response.LanguageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping("/api/language")
    public List<LanguageResponse> getLanguages() {
        return languageService.findAllLanguages();
    }
}
