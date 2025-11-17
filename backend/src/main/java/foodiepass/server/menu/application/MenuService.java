package foodiepass.server.menu.application;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.language.domain.Language;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.dto.request.ReconfigureRequest;
import foodiepass.server.menu.dto.response.ReconfigureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final OcrReader ocrReader;
    private final MenuItemEnricher menuItemEnricher;

    public Mono<ReconfigureResponse> reconfigure(final ReconfigureRequest request) {
        final Language originLanguage = Language.fromLanguageName(request.originLanguageName());
        final Language userLanguage = Language.fromLanguageName(request.userLanguageName());
        final Currency originCurrency = Currency.fromCurrencyName(request.originCurrencyName());
        final Currency userCurrency = Currency.fromCurrencyName(request.userCurrencyName());

        final List<MenuItem> menuItems = ocrReader.read(request.base64EncodedImage());

        return menuItemEnricher.enrichBatchAsync(
                menuItems,
                originLanguage,
                userLanguage,
                originCurrency,
                userCurrency
        ).map(ReconfigureResponse::new);
    }
}
