package foodiepass.server.currency.infra;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.application.port.out.ExchangeRateProvider;
import foodiepass.server.menu.infra.exception.ScrapingErrorCode;
import foodiepass.server.menu.infra.exception.ScrapingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Slf4j
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
    @Cacheable(value = "exchangeRates", key = "#from.currencyCode + '::' + #to.currencyCode")
    @CircuitBreaker(name = "exchangeRate", fallbackMethod = "fallbackGetExchangeRate")
    public double getExchangeRate(final Currency from, final Currency to) {
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

    @Override
    @Cacheable(value = "exchangeRatesAsync", key = "#from.currencyCode + '::' + #to.currencyCode")
    public Mono<Double> getExchangeRateAsync(final Currency from, final Currency to) {
        return Mono.fromCallable(() -> getExchangeRate(from, to))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public double fallbackGetExchangeRate(final Currency from, final Currency to, final Throwable t) {
        log.warn("Circuit Breaker is open for getExchangeRate. from: {}, to: {}. error: {}", from.getCurrencyCode(), to.getCurrencyCode(), t.getMessage());
        throw new ScrapingException(ScrapingErrorCode.EXTERNAL_API_CIRCUIT_OPEN);
    }
}
