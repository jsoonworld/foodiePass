package foodiepass.server.script.application;

import foodiepass.server.language.domain.Language;
import foodiepass.server.order.domain.OrderItem;
import foodiepass.server.script.dto.request.MenuItemRequest;
import foodiepass.server.script.dto.request.ScriptGenerateRequest;
import foodiepass.server.script.dto.response.ScriptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ScriptFactory scriptFactory;

    public Mono<ScriptResponse> generateScript(final ScriptGenerateRequest request) {
        final Language sourceLanguage = Language.fromLanguageName(request.userLanguageName());
        final Language targetLanguage = Language.fromLanguageName(request.originLanguageName());

        final List<OrderItem> orderItems = request.menuItems()
                .stream()
                .map(MenuItemRequest::toDomain)
                .toList();

        return scriptFactory.createAsync(sourceLanguage, targetLanguage, orderItems)
                .map(script -> new ScriptResponse(script.getTravelerScript(), script.getLocalScript()));
    }
}
