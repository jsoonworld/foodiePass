package foodiepass.server.order.domain;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.order.exception.OrderErrorCode;
import foodiepass.server.order.exception.OrderException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class OrderItem {

    private final MenuItem menuItem;
    private final int quantity;

    public OrderItem(final MenuItem menuItem, final int quantity) {
        validateMenuItem(menuItem);
        validateQuantity(quantity);
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public Price calculateTotalPrice() {
        return menuItem.getPrice().multiply(quantity);
    }

    public String getName() {
        return menuItem.getName();
    }

    private void validateMenuItem(MenuItem menuItem) {
        if (Objects.isNull(menuItem)) {
            throw new OrderException(OrderErrorCode.NULL_MENU_ITEM);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_QUANTITY);
        }
    }
}
