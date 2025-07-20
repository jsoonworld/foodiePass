package foodiepass.server.language.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LanguageErrorCode implements ErrorCode {

    LANGUAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "지원하지 않는 언어입니다."),
    INVALID_LANGUAGE_INPUT(HttpStatus.BAD_REQUEST, "언어 이름 또는 코드는 비어있을 수 없습니다.");
    ;

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
