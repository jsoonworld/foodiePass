package foodiepass.server.currency.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CurrencyErrorCode implements ErrorCode {

    CURRENCY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 통화를 찾을 수 없습니다."),
    ;
    private static final String PREFIX = "[CURRENCY ERROR] ";
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
