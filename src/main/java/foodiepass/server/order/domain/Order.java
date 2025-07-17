package foodiepass.server.order.domain;

import java.util.List;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.price.domain.Price;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Order {

    private final List<OrderItem> items;
    private final Currency currency;

    public Price calculateTotalPrice() {
        return items.stream()
                .map(OrderItem::calculateTotalPrice)
                .reduce(Price.zero(this.currency), Price::add);
    }
}
