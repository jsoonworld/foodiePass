package foodiepass.server.currency.application;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExchangeRateCache {
    private final Map<String, Double> exchangeRateCache = new ConcurrentHashMap<>();

    public Optional<Double> getExchangeRate(String fromCurrencyCode, String toCurrencyCode) {
        if (fromCurrencyCode.equals(toCurrencyCode)) {
            return Optional.of(1.0);
        }
        String key = fromCurrencyCode + "-" + toCurrencyCode;
        return Optional.ofNullable(exchangeRateCache.get(key));
    }

    public void updateExchangeRate(String fromCurrencyCode, String toCurrencyCode, double rate) {
        String key = fromCurrencyCode + "-" + toCurrencyCode;
        exchangeRateCache.put(key, rate);
    }
}
