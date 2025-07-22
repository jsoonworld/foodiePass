package foodiepass.server.menu.infra.scraper.auth.application;

import foodiepass.server.menu.infra.scraper.auth.domain.Authenticatable;
import foodiepass.server.menu.infra.scraper.auth.dto.UpdateJwtRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScrapAuthServiceTest {

    @InjectMocks
    private ScrapAuthService scrapAuthService;

    @Mock
    private Authenticatable authenticator1;
    @Mock
    private Authenticatable authenticator2;

    @Test
    @DisplayName("JWT 업데이트 요청 시 모든 Authenticatable 구현체들의 인증 정보가 갱신된다")
    void updateJwt_shouldUpdateAuthForAllAuthenticatables() {
        // given
        scrapAuthService = new ScrapAuthService(List.of(authenticator1, authenticator2));
        final String newToken = "new-test-token";
        final UpdateJwtRequest request = new UpdateJwtRequest(newToken);

        // when
        scrapAuthService.updateJwt(request);

        // then
        verify(authenticator1, times(1)).updateAuth(newToken);
        verify(authenticator2, times(1)).updateAuth(newToken);
    }
}
