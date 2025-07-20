package foodiepass.server.currency.exception;

import foodiepass.server.global.error.BaseException;
import foodiepass.server.global.error.ErrorCode;

public class CurrencyException extends BaseException {
    public CurrencyException(ErrorCode errorCode) {
        super(errorCode);
    }
}
