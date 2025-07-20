package foodiepass.server.food.exception;

import foodiepass.server.global.error.BaseException;
import foodiepass.server.global.error.ErrorCode;

public class FoodException extends BaseException {
    public FoodException(ErrorCode errorCode) {
        super(errorCode);
    }
}
