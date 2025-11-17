package foodiepass.server.currency.dto.response;

import foodiepass.server.currency.domain.Currency;

public record CurrencyResponse(String currencyName) {
    public static CurrencyResponse from(final Currency currency) {
        return new CurrencyResponse(currency.getCurrencyName());
    }
}
