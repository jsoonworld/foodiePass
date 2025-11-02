package foodiepass.server.menu.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FoodErrorCode implements ErrorCode {

    FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 음식 정보입니다."),
    MENU_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메뉴 항목입니다."),
    INVALID_FOOD_NAME(HttpStatus.BAD_REQUEST, "음식 이름은 비어있을 수 없습니다."),
    INVALID_MENU_ITEM_NAME(HttpStatus.BAD_REQUEST, "메뉴 이름은 비어있을 수 없습니다."),
    INVALID_MENU_ITEM_PRICE(HttpStatus.BAD_REQUEST, "메뉴 가격 정보는 필수입니다.");

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
