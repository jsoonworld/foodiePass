package foodiepass.server.language.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LanguageErrorCode implements ErrorCode {

    LANGUAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "지원하지 않는 언어입니다."),
    ;

    private static final String PREFIX = "[LANGUAGE ERROR] ";
    private final HttpStatus status;
    private final String rawMessage;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return PREFIX + rawMessage;
    }
}
