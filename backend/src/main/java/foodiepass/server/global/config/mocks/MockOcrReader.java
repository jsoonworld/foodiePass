package foodiepass.server.global.config.mocks;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("performance-test")
public class MockOcrReader implements OcrReader {

    @Override
    public List<MenuItem> read(final String base64encodedImage) {
        return List.of(
                new MenuItem("김치찌개", new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("8000")), null),
                new MenuItem("된장찌개", new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("7000")), null),
                new MenuItem("제육볶음", new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("9000")), null)
        );
    }
}
