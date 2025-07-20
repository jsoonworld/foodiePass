package foodiepass.server.script.exception;

import foodiepass.server.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScriptErrorCode implements ErrorCode {

    SCRIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스크립트입니다."),
    INVALID_TRAVELER_SCRIPT(HttpStatus.BAD_REQUEST, "여행자 스크립트는 비어있을 수 없습니다."),
    INVALID_LOCAL_SCRIPT(HttpStatus.BAD_REQUEST, "현지인 스크립트는 비어있을 수 없습니다.");

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
