package foodiepass.server.menu.application.port.out;

import foodiepass.server.currency.domain.Currency;
import reactor.core.publisher.Mono;

/**
 * 환율 정보를 제공하는 인터페이스
 */
public interface ExchangeRateProvider {

    /**
     * 두 통화 간의 환율을 동기적으로 조회합니다.
     *
     * @param from 원본 통화
     * @param to 대상 통화
     * @return 환율 (from 통화 1단위 = to 통화 X단위)
     */
    double getExchangeRate(Currency from, Currency to);

    /**
     * 두 통화 간의 환율을 비동기적으로 조회합니다.
     *
     * @param from 원본 통화
     * @param to 대상 통화
     * @return 환율 (Mono)
     */
    Mono<Double> getExchangeRateAsync(Currency from, Currency to);
}
