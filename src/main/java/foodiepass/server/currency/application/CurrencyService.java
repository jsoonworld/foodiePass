package foodiepass.server.currency.application;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.request.CalculatePriceRequest.OrderElementRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.dto.response.CurrencyResponse;
import foodiepass.server.food.domain.ExchangeRateProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final ExchangeRateProvider exchangeRateProvider;

    public List<CurrencyResponse> findAllCurrencies() {
        return Stream.of(Currency.values())
                .map(CurrencyResponse::from)
                .toList();
    }

    public CalculatePriceResponse calculateOrdersPrice(final CalculatePriceRequest request) {
        final Currency originCurrency = Currency.fromCurrencyName(request.originCurrency());
        final Currency userCurrency = Currency.fromCurrencyName(request.userCurrency());

        final BigDecimal originTotalPrice = calculateTotalPrice(request.orders());

        final Double exchangeRate = exchangeRateProvider.getExchangeRate(originCurrency, userCurrency);

        final BigDecimal userTotalPrice = originTotalPrice.multiply(BigDecimal.valueOf(exchangeRate))
                .setScale(2, RoundingMode.HALF_UP);

        return CalculatePriceResponse.of(originCurrency, originTotalPrice, userCurrency, userTotalPrice);
    }

    private BigDecimal calculateTotalPrice(final List<OrderElementRequest> orderElementRequests) {
        return orderElementRequests.stream()
                .map(order -> order.originPrice().multiply(order.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
