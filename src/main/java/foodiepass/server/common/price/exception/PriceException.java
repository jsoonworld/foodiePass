package foodiepass.server.common.price.exception;

import foodiepass.server.global.error.BaseException;

public class PriceException extends BaseException {
    public PriceException(PriceErrorCode errorCode) {
        super(errorCode);
    }
}
