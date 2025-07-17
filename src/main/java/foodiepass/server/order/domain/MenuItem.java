package foodiepass.server.order.domain;

import foodiepass.server.price.domain.Price;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MenuItem {
    private final String name;
    private final Price price;
    private final FoodInfo foodInfo;
}
