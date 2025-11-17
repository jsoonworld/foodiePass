package foodiepass.server.language.exception;

import foodiepass.server.global.error.BaseException;

public class LanguageException extends BaseException {
    public LanguageException(LanguageErrorCode errorCode) {
        super(errorCode);
    }
}
