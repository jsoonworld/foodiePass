package foodiepass.server.script.exception;

import foodiepass.server.global.error.BaseException;
import foodiepass.server.global.error.ErrorCode;

public class ScriptException extends BaseException {
    public ScriptException(ErrorCode errorCode) {
        super(errorCode);
    }
}
