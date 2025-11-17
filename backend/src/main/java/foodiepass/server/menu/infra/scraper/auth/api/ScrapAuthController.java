package foodiepass.server.menu.infra.scraper.auth.api;

import foodiepass.server.menu.infra.scraper.auth.application.ScrapAuthService;
import foodiepass.server.menu.infra.scraper.auth.dto.UpdateJwtRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menu/scrapper-auth")
public class ScrapAuthController {

    private final ScrapAuthService scrapAuthService;

    @PostMapping("/update-jwt")
    @ResponseStatus(HttpStatus.OK)
    public void updateJwt(@RequestBody final UpdateJwtRequest request) {
        scrapAuthService.updateJwt(request);
    }
}
