package foodiepass.server.menu.application.port.out;

import foodiepass.server.language.domain.Language;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 텍스트 번역을 수행하는 인터페이스
 */
public interface TranslationClient {

    /**
     * 단일 텍스트를 비동기로 번역합니다.
     *
     * @param source 원본 언어
     * @param target 대상 언어
     * @param text 번역할 텍스트
     * @return 번역된 텍스트 (Mono)
     */
    Mono<String> translateAsync(Language source, Language target, String text);

    /**
     * 여러 텍스트를 비동기로 번역합니다.
     *
     * @param source 원본 언어
     * @param target 대상 언어
     * @param texts 번역할 텍스트 목록
     * @return 번역된 텍스트 스트림 (Flux)
     */
    Flux<String> translateAsync(Language source, Language target, List<String> texts);
}
