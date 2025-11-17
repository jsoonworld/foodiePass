package foodiepass.server.menu.application.port.out;

import foodiepass.server.menu.domain.FoodInfo;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 음식 정보(이름, 설명, 이미지)를 스크래핑하는 인터페이스
 */
public interface FoodScraper {

    /**
     * 여러 음식 이름에 대한 정보를 비동기로 스크래핑합니다.
     *
     * @param foodNames 검색할 음식 이름 목록
     * @return 음식 정보 스트림 (Flux)
     */
    Flux<FoodInfo> scrapAsync(List<String> foodNames);
}
