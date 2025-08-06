package foodiepass.server.currency.dto.response;

import foodiepass.server.currency.domain.Currency;
import java.math.BigDecimal;

import static java.math.RoundingMode.*;

public record CalculatePriceResponse(
        FormattedPrice originTotalPrice,
        FormattedPrice userTotalPrice
) {
    public record FormattedPrice(
            BigDecimal value,
            String currencyCode,
            String formatted
    ) {}

    public static CalculatePriceResponse of(
            Currency originCurrency, BigDecimal originTotal,
            Currency userCurrency, BigDecimal userTotal) {

        FormattedPrice originPrice = new FormattedPrice(
                originTotal.setScale(2, HALF_UP),
                originCurrency.getCurrencyCode(),
                originCurrency.format(originTotal)
        );

        FormattedPrice userPrice = new FormattedPrice(
                userTotal.setScale(2, HALF_UP),
                userCurrency.getCurrencyCode(),
                userCurrency.format(userTotal)
        );

        return new CalculatePriceResponse(originPrice, userPrice);
    }
}
