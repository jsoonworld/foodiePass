package foodiepass.server.script.application;

import foodiepass.server.language.domain.Language;
import foodiepass.server.order.domain.OrderItem;
import foodiepass.server.script.domain.Script;
import foodiepass.server.script.dto.request.MenuItemRequest;
import foodiepass.server.script.dto.request.ScriptGenerateRequest;
import foodiepass.server.script.dto.response.ScriptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ScriptFactory scriptFactory;

    @Transactional(readOnly = true)
    public ScriptResponse generateScript(final ScriptGenerateRequest request) {
        final Language sourceLanguage = Language.fromLanguageName(request.userLanguageName());
        final Language targetLanguage = Language.fromLanguageName(request.originLanguageName());

        final List<OrderItem> orderItems = request.menuItems()
                .stream()
                .map(MenuItemRequest::toDomain)
                .toList();

        final Script script = scriptFactory.create(sourceLanguage, targetLanguage, orderItems);

        return new ScriptResponse(script.getTravelerScript(), script.getLocalScript());
    }
}
