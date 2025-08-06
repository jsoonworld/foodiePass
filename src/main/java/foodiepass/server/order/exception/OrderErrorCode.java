package foodiepass.server.order.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    NULL_CURRENCY(HttpStatus.BAD_REQUEST, "통화 정보는 필수입니다."),
    NULL_MENU_ITEM(HttpStatus.BAD_REQUEST, "메뉴 항목은 필수입니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량은 0보다 커야 합니다."),
    EMPTY_ORDER_ITEMS(HttpStatus.BAD_REQUEST, "주문 항목이 비어있습니다."),
    DIFFERENT_CURRENCIES_IN_ORDER(HttpStatus.BAD_REQUEST, "한 주문에 다른 통화 단위를 사용할 수 없습니다.");

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
