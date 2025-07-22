package foodiepass.server.menu.infra.scraper.auth.application;

import foodiepass.server.menu.infra.scraper.auth.domain.Authenticatable;
import foodiepass.server.menu.infra.scraper.auth.dto.UpdateJwtRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapAuthService {

    private final List<Authenticatable> authenticatables;

    public void updateJwt(final UpdateJwtRequest request) {
        authenticatables.forEach(authenticatable -> authenticatable.updateAuth(request.token()));
    }
}
