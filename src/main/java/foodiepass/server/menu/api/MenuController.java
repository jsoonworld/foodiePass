package foodiepass.server.menu.api;

import foodiepass.server.menu.application.MenuService;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.ReconfigureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/reconfigure")
    public ReconfigureResponse reconfigure(@RequestBody final ReconfigureRequest request) {
        return menuService.reconfigure(request);
    }
}
