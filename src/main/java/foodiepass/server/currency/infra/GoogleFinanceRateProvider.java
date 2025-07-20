package foodiepass.server.currency.infra;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.food.domain.ExchangeRateProvider;
import foodiepass.server.food.infra.exception.ScrapingErrorCode;
import foodiepass.server.food.infra.exception.ScrapingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleFinanceRateProvider implements ExchangeRateProvider {

    private final String googleFinanceUrlFormat;
    private final String exchangeRateSelector;

    public GoogleFinanceRateProvider(
            @Value("${jsoup.google-finance.url-format}") final String googleFinanceUrlFormat,
            @Value("${jsoup.google-finance.selector}") final String exchangeRateSelector) {
        this.googleFinanceUrlFormat = googleFinanceUrlFormat;
        this.exchangeRateSelector = exchangeRateSelector;
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#from.currencyCode + '-' + #to.currencyCode", unless = "#result == null")
    public Double getExchangeRate(final Currency from, final Currency to) {
        if (from.equals(to)) {
            return 1.0;
        }

        final String url = String.format(googleFinanceUrlFormat, from.getCurrencyCode(), to.getCurrencyCode());
        try {
            final Document doc = Jsoup.connect(url).get();
            final Element exchangeRateElement = doc.selectFirst(exchangeRateSelector);

            if (exchangeRateElement == null) {
                throw new ScrapingException(ScrapingErrorCode.RATE_ELEMENT_NOT_FOUND);
            }

            final String exchangeRateString = exchangeRateElement.text().replaceAll(",", "");
            return Double.parseDouble(exchangeRateString);

        } catch (IOException e) {
            throw new ScrapingException(ScrapingErrorCode.SCRAPING_CONNECTION_FAILED);
        } catch (NumberFormatException e) {
            throw new ScrapingException(ScrapingErrorCode.RATE_PARSING_FAILED);
        }
    }
}
