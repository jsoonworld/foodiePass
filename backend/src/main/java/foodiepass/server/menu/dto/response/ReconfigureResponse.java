package foodiepass.server.menu.dto.response;

import java.util.List;

public record ReconfigureResponse(
        List<FoodItemResponse> results
) {
    public record FoodItemResponse(
            String originMenuName,
            String translatedMenuName,
            String description,
            String image,
            PriceInfoResponse priceInfo
    ) {}

    public record PriceInfoResponse(
            String originPriceWithCurrencyUnit,
            String userPriceWithCurrencyUnit
    ) {}
}
