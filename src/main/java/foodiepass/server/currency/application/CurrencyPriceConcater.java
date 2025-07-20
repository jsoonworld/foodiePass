package foodiepass.server.currency.application;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Component
public class CurrencyPriceConcater {

    public String concatPriceWithCurrency(final String isoCode, final Double amount) {
        java.util.Currency currencyInstance = java.util.Currency.getInstance(isoCode);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        numberFormat.setCurrency(currencyInstance);
        return numberFormat.format(amount);
    }
}
