package foodiepass.server.menu.infra.exception;

import foodiepass.server.global.error.BaseException;
import foodiepass.server.global.error.ErrorCode;

public class GeminiException extends BaseException {
    public GeminiException(ErrorCode errorCode) {
        super(errorCode);
    }
}
