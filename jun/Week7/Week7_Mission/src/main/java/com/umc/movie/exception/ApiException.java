package com.umc.movie.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 상태 코드, code 문자열, 메시지를 함께 담아 던지는 예외.
 * 서비스나 인터셉터 같은 안쪽에서 던져도 GlobalExceptionHandler가 공통 응답 형식으로 바꿔준다.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status; // 응답으로 내보낼 HTTP 상태 코드
    private final String code;       // 응답 바디의 code 필드에 실릴 문자열

    public ApiException(HttpStatus status, String code, String message) {
        super(message); // message는 부모인 RuntimeException이 들고 있다
        this.status = status;
        this.code = code;
    }
}
