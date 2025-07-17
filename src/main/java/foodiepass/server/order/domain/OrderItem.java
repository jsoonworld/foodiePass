package foodiepass.server.order.domain;

import foodiepass.server.price.domain.Price;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderItem {

    private final MenuItem menuItem;
    private final int quantity;

    public Price calculateTotalPrice() {
        return menuItem.getPrice().multiply(quantity);
    }
}
