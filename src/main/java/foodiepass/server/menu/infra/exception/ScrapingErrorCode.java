package foodiepass.server.menu.infra.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScrapingErrorCode implements ErrorCode {
    SCRAPING_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "환율 정보 스크래핑 연결에 실패했습니다."),
    RATE_ELEMENT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "환율 정보를 담고 있는 요소를 찾지 못했습니다."),
    RATE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "스크래핑한 환율 정보를 파싱하는 데 실패했습니다."),

    TASTE_ATLAS_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TasteAtlas API 요청에 실패했습니다."),
    TASTE_ATLAS_JSON_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TasteAtlas API 응답 파싱에 실패했습니다."),
    TASTE_ATLAS_HTML_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TasteAtlas HTML 페이지를 가져오는 데 실패했습니다."),
    TASTE_ATLAS_NO_RESULT(HttpStatus.NOT_FOUND, "TasteAtlas 검색 결과가 없습니다."),

    EXTERNAL_API_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "외부 API 서킷이 열려 요청을 처리할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
