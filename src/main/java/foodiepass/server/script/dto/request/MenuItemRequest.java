package foodiepass.server.script.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.order.domain.OrderItem;

import java.math.BigDecimal;

public record MenuItemRequest(
        String name,
        int quantity,
        @JsonProperty("priceAmount") BigDecimal priceAmount,
        String currencyCode
) {
    public OrderItem toDomain() {
        FoodInfo foodInfo = new FoodInfo(name, "Description from DTO", "image.url", "preview.url");
        Currency currency = Currency.fromCurrencyCode(currencyCode);
        Price price = new Price(currency, priceAmount);
        MenuItem menuItem = new MenuItem(name, price, foodInfo);
        return new OrderItem(menuItem, quantity);
    }
}
