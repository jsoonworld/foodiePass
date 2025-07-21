package foodiepass.server.currency.exception;

import foodiepass.server.global.error.BaseException;

public class CurrencyException extends BaseException {
    public CurrencyException(CurrencyErrorCode errorCode) {
        super(errorCode);
    }
}
