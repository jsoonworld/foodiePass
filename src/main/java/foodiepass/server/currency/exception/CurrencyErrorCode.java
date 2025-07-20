package foodiepass.server.currency.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CurrencyErrorCode implements ErrorCode {

    CURRENCY_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 통화입니다."),
    INVALID_CURRENCY_INPUT(HttpStatus.BAD_REQUEST, "통화 이름 또는 코드는 비어있을 수 없습니다.");

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
