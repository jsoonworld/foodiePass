package foodiepass.server.order.domain;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.order.exception.OrderErrorCode;
import foodiepass.server.order.exception.OrderException;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class Order {
    private final List<OrderItem> items;
    private final Currency currency;

    public Order(List<OrderItem> items, Currency currency) {
        validateItems(items);
        validateCurrency(currency);
        validateItemsCurrencyConsistency(items, currency);
        this.items = items;
        this.currency = currency;
    }

    public Price calculateTotalPrice() {
        return items.stream()
                .map(OrderItem::calculateTotalPrice)
                .reduce(Price.zero(this.currency), Price::add);
    }

    private void validateItems(List<OrderItem> items) {
        if (Objects.isNull(items) || items.isEmpty()) {
            throw new OrderException(OrderErrorCode.EMPTY_ORDER_ITEMS);
        }
    }

    private void validateCurrency(Currency currency) {
        if (Objects.isNull(currency)) {
            throw new OrderException(OrderErrorCode.NULL_CURRENCY);
        }
    }

    private void validateItemsCurrencyConsistency(List<OrderItem> items, Currency orderCurrency) {
        boolean hasMismatchedCurrency = items.stream()
                .map(item -> item.getMenuItem().getPrice().getCurrency())
                .anyMatch(itemCurrency -> !itemCurrency.equals(orderCurrency));

        if (hasMismatchedCurrency) {
            throw new OrderException(OrderErrorCode.DIFFERENT_CURRENCIES_IN_ORDER);
        }
    }
}
