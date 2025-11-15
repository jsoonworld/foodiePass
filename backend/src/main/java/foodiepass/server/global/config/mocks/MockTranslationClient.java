package foodiepass.server.global.config.mocks;

import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.TranslationClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Profile(ProfileConstants.TEST_OR_PERFORMANCE_TEST)
public class MockTranslationClient implements TranslationClient {

    @Override
    public Mono<String> translateAsync(Language source, Language target, String text) {
        return Mono.just(text);
    }

    @Override
    public Flux<String> translateAsync(Language source, Language target, List<String> texts) {
        return Flux.fromIterable(texts);
    }
}
