package foodiepass.server.food.domain;

import foodiepass.server.currency.domain.Currency;

public interface ExchangeRateProvider {
    Double getExchangeRate(final Currency from, final Currency to);
}
