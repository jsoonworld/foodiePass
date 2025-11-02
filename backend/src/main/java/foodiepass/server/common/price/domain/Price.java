package foodiepass.server.common.price.domain;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.common.price.exception.PriceErrorCode;
import foodiepass.server.common.price.exception.PriceException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class Price {

    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;

    private final BigDecimal amount;
    private final Currency currency;

    public Price(Currency currency, BigDecimal amount) {
        validateCurrency(currency);
        validateAmount(amount);
        this.currency = currency;
        this.amount = amount;
    }

    public static Price zero(final Currency currency) {
        return new Price(currency, ZERO_AMOUNT);
    }

    public Price add(final Price other) {
        validateCurrencyEquality(other);
        BigDecimal addedAmount = this.amount.add(other.getAmount());
        return new Price(this.currency, addedAmount);
    }

    public Price multiply(final int quantity) {
        validateQuantity(quantity);
        BigDecimal multipliedAmount = this.amount.multiply(BigDecimal.valueOf(quantity));
        return new Price(this.currency, multipliedAmount);
    }

    private void validateCurrency(Currency currency) {
        if (Objects.isNull(currency)) {
            throw new PriceException(PriceErrorCode.NULL_CURRENCY);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (Objects.isNull(amount)) {
            throw new PriceException(PriceErrorCode.NULL_AMOUNT);
        }
        if (amount.compareTo(ZERO_AMOUNT) < 0) {
            throw new PriceException(PriceErrorCode.INVALID_AMOUNT);
        }
    }

    private void validateCurrencyEquality(Price other) {
        if (!this.currency.equals(other.getCurrency())) {
            throw new PriceException(PriceErrorCode.CURRENCIES_DO_NOT_MATCH);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new PriceException(PriceErrorCode.INVALID_QUANTITY);
        }
    }
}
