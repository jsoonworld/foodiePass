package foodiepass.server.currency.api;

import foodiepass.server.currency.application.CurrencyService;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.dto.response.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public Mono<List<CurrencyResponse>> getCurrencies() {
        return Mono.just(currencyService.findAllCurrencies());
    }

    @PostMapping("/calculate")
    public Mono<CalculatePriceResponse> calculateTotalPrice(
            @RequestBody final CalculatePriceRequest calculatePriceRequest
    ) {
        return currencyService.calculateOrdersPriceAsync(calculatePriceRequest);
    }
}
