package foodiepass.server.currency.api;

import foodiepass.server.currency.application.CurrencyService;
import foodiepass.server.currency.dto.request.CalculatePriceRequest;
import foodiepass.server.currency.dto.response.CalculatePriceResponse;
import foodiepass.server.currency.dto.response.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/currency")
    public List<CurrencyResponse> getCurrencies() {
        return currencyService.findAllCurrencies();
    }

    @PostMapping("/currency/calculate")
    public CalculatePriceResponse calculateTotalPrice(
            @RequestBody final CalculatePriceRequest calculatePriceRequest
    ) {
        return currencyService.calculateOrdersPrice(calculatePriceRequest);
    }
}
