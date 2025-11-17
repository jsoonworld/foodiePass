package foodiepass.server.order.exception;

import foodiepass.server.global.error.BaseException;
import foodiepass.server.global.error.ErrorCode;

public class OrderException extends BaseException {
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
