package foodiepass.server.price.domain;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.price.exception.PriceErrorCode;
import foodiepass.server.price.exception.PriceException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Price {

    private final Currency currency;
    private final double amount;

    public Price add(final Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new PriceException(PriceErrorCode.CURRENCIES_DO_NOT_MATCH);
        }
        return new Price(this.currency, this.amount + other.amount);
    }

    public Price multiply(final int quantity) {
        if (quantity < 0) {
            throw new PriceException(PriceErrorCode.INVALID_QUANTITY);
        }
        return new Price(this.currency, this.amount * quantity);
    }
}
