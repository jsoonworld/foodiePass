package foodiepass.server.common.price.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PriceErrorCode implements ErrorCode {

    NULL_CURRENCY(HttpStatus.BAD_REQUEST, "통화 정보는 필수입니다."),
    NULL_AMOUNT(HttpStatus.BAD_REQUEST, "금액 정보는 필수입니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "금액은 음수가 될 수 없습니다."),

    CURRENCIES_DO_NOT_MATCH(HttpStatus.BAD_REQUEST, "서로 다른 통화의 가격은 연산할 수 없습니다. 통화를 일치시켜 주세요."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 음수가 될 수 없습니다. 0 이상의 값을 입력해 주세요.");


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
