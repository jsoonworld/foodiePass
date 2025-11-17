package foodiepass.server.currency.application;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.application.port.out.ExchangeRateProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRateCache exchangeRateCache;

    @PostConstruct
    public void initializeCacheOnStartup() {
        updateAllExchangeRatesInternal();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "updateAllExchangeRates", lockAtLeastFor = "PT5M", lockAtMostFor = "PT1H")
    public void scheduledCacheUpdate() {
        updateAllExchangeRatesInternal();
    }

    void updateAllExchangeRatesInternal() {
        log.info("환율 정보 캐시 갱신 작업을 시작합니다...");

        final Currency baseCurrency = Currency.UNITED_STATES_DOLLAR;

        Mono.just(Currency.values())
                .flatMapMany(currencies -> reactor.core.publisher.Flux.fromArray(currencies)
                        .filter(currency -> !currency.equals(baseCurrency))
                        .flatMap(targetCurrency ->
                                exchangeRateProvider.getExchangeRateAsync(baseCurrency, targetCurrency)
                                        .map(rate -> Tuples.of(targetCurrency.getCurrencyCode(), rate))
                                        .onErrorResume(error -> {
                                            log.error("환율 정보를 가져오는 중 오류 발생: {} -> {}", baseCurrency.getCurrencyCode(), targetCurrency.getCurrencyCode(), error);
                                            return Mono.empty();
                                        })
                        )
                )
                .collectMap(tuple -> tuple.getT1(), tuple -> tuple.getT2())
                .doOnSuccess(ratesMap -> {
                    if (ratesMap.isEmpty()) {
                        log.warn("갱신할 환율 정보를 가져오지 못했습니다.");
                        return;
                    }

                    ratesMap.forEach((targetCode, rate) -> {
                        exchangeRateCache.updateExchangeRate(baseCurrency.getCurrencyCode(), targetCode, rate);
                        exchangeRateCache.updateExchangeRate(targetCode, baseCurrency.getCurrencyCode(), 1.0 / rate);
                    });

                    for (Map.Entry<String, Double> fromEntry : ratesMap.entrySet()) {
                        for (Map.Entry<String, Double> toEntry : ratesMap.entrySet()) {
                            if (fromEntry.getKey().equals(toEntry.getKey())) {
                                continue;
                            }

                            double crossRate = toEntry.getValue() / fromEntry.getValue();
                            exchangeRateCache.updateExchangeRate(fromEntry.getKey(), toEntry.getKey(), crossRate);
                        }
                    }

                    log.info("총 {}개의 통화에 대한 환율 캐시 갱신을 완료했습니다.", ratesMap.size());
                })
                .subscribe(
                        null,
                        error -> log.error("환율 정보 캐시 갱신 스트림에 치명적인 오류가 발생했습니다.", error)
                );
    }
}
