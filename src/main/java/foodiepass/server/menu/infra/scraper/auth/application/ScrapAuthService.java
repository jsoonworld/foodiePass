package foodiepass.server.menu.infra.scraper.auth.application;

import foodiepass.server.menu.infra.scraper.auth.domain.Authenticatable;
import foodiepass.server.menu.infra.scraper.auth.dto.UpdateJwtRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScrapAuthService {

    private final List<Authenticatable> authenticatables;

    public ScrapAuthService(final List<Authenticatable> authenticatables) {
        this.authenticatables = authenticatables;
    }

    public void updateJwt(final UpdateJwtRequest request) {
        authenticatables.forEach(authenticatable -> authenticatable.updateAuth(request.token()));
    }
}
