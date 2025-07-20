package foodiepass.server.food.domain;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.food.exception.FoodErrorCode;
import foodiepass.server.food.exception.FoodException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public class MenuItem {
    private final String name;
    private final Price price;
    private final FoodInfo foodInfo;

    public MenuItem(String name, Price price, FoodInfo foodInfo) {
        validateName(name);
        validatePrice(price);
        this.name = name;
        this.price = price;
        this.foodInfo = foodInfo;
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new FoodException(FoodErrorCode.INVALID_MENU_ITEM_NAME);
        }
    }

    private void validatePrice(Price price) {
        if (Objects.isNull(price)) {
            throw new FoodException(FoodErrorCode.INVALID_MENU_ITEM_PRICE);
        }
    }
}
