package foodiepass.server.script.dto.request;

import java.util.List;

public record ScriptGenerateRequest(
        String userLanguageName,
        String originLanguageName,
        List<MenuItemRequest> menuItems
) {
}
