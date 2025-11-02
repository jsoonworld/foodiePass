package foodiepass.server.global.success;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalSuccessCode implements SuccessCode {
    OK(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "리뷰가 성공적으로 생성되었습니다.");

    private final HttpStatus status;
    private final String message;
}
