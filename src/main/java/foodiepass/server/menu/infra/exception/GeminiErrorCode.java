package foodiepass.server.menu.infra.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeminiErrorCode implements ErrorCode {
    GEMINI_API_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API 호출 중 오류가 발생했습니다."),
    INVALID_GEMINI_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API로부터 유효한 응답을 받지 못했습니다."),

    OCR_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini OCR 요청 처리 중 오류가 발생했습니다."),

    FOOD_INFO_SCRAP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "음식 정보 스크래핑 중 오류가 발생했습니다.");

    ;

    private final HttpStatus status;
    private final String message;
}
