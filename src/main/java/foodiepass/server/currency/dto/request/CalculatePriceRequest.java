package foodiepass.server.currency.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record CalculatePriceRequest(
        String originCurrency,
        String userCurrency,
        List<OrderElementRequest> orders
) {
    public record OrderElementRequest(BigDecimal originPrice, BigDecimal quantity) {
    }
}
