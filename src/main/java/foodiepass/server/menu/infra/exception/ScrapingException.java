package foodiepass.server.menu.infra.exception;

import foodiepass.server.global.error.BaseException;
import foodiepass.server.global.error.ErrorCode;

public class ScrapingException extends BaseException {
    public ScrapingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
