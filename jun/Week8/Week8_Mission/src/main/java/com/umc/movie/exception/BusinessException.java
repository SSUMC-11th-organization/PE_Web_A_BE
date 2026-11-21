package com.umc.movie.exception;

import lombok.Getter;

/** 서비스 검증(두 번째 문)에서 업무 규칙 위반을 알릴 때 던지는 예외. */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
