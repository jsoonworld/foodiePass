package foodiepass.server.currency.application;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.request.CalculatePriceRequest.OrderElementRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.dto.response.CurrencyResponse;
import foodiepass.server.currency.exception.CurrencyException;
import foodiepass.server.menu.dto.response.ReconfigureResponse.PriceInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Stream;

import static foodiepass.server.currency.exception.CurrencyErrorCode.EXCHANGE_RATE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final ExchangeRateCache exchangeRateCache;

    public List<CurrencyResponse> findAllCurrencies() {
        return Stream.of(Currency.values())
                .map(CurrencyResponse::from)
                .toList();
    }

    public Mono<PriceInfoResponse> convertAndFormatAsync(final Price originPrice, final Currency userCurrency) {
        final Currency originCurrency = originPrice.getCurrency();

        return Mono.fromSupplier(() -> {
            double exchangeRate = exchangeRateCache.getExchangeRate(originCurrency.getCurrencyCode(), userCurrency.getCurrencyCode())
                    .orElseThrow(() -> new CurrencyException(EXCHANGE_RATE_NOT_FOUND));

            final BigDecimal userPriceValue = originPrice.getAmount()
                    .multiply(BigDecimal.valueOf(exchangeRate))
                    .setScale(2, RoundingMode.HALF_UP);

            final String originFormatted = formatPrice(originPrice.getAmount(), originCurrency);
            final String userFormatted = formatPrice(userPriceValue, userCurrency);

            return new PriceInfoResponse(originFormatted, userFormatted);
        });
    }

    public Mono<CalculatePriceResponse> calculateOrdersPriceAsync(final CalculatePriceRequest request) {
        final Currency originCurrency = Currency.fromCurrencyName(request.originCurrency());
        final Currency userCurrency = Currency.fromCurrencyName(request.userCurrency());
        final BigDecimal originTotalPrice = calculateTotalPrice(request.orders());

        return Mono.fromSupplier(() -> {
            double exchangeRate = exchangeRateCache.getExchangeRate(originCurrency.getCurrencyCode(), userCurrency.getCurrencyCode())
                    .orElseThrow(() -> new CurrencyException(EXCHANGE_RATE_NOT_FOUND));

            final BigDecimal userTotalPrice = originTotalPrice.multiply(BigDecimal.valueOf(exchangeRate))
                    .setScale(2, RoundingMode.HALF_UP);

            return CalculatePriceResponse.of(originCurrency, originTotalPrice, userCurrency, userTotalPrice);
        });
    }

    private BigDecimal calculateTotalPrice(final List<OrderElementRequest> orderElementRequests) {
        return orderElementRequests.stream()
                .map(order -> order.originPrice().multiply(order.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatPrice(final BigDecimal amount, final Currency currency) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(java.util.Currency.getInstance(currency.getCurrencyCode()));
        return format.format(amount);
    }
}
