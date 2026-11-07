package com.umc.movie.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 실패 응답에 쓰는 code/message/HTTP 상태를 한곳에 모은 목록.
 * code 규칙: 도메인 + HTTP 상태 + 순번 (예: MEMBER4091)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON4001", "입력값이 올바르지 않습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON4002", "요청 본문의 형식이 올바르지 않습니다."),
    INVALID_PATH_VARIABLE(HttpStatus.BAD_REQUEST, "COMMON4003", "요청 경로의 값이 올바르지 않습니다."),
    NOT_FOUND_PATH(HttpStatus.NOT_FOUND, "COMMON4041", "존재하지 않는 API입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON4051", "지원하지 않는 HTTP 메서드입니다."),
    DUPLICATE_DATA(HttpStatus.CONFLICT, "COMMON4091", "이미 존재하는 데이터입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5001", "서버 내부 오류가 발생했습니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4041", "존재하지 않는 유저입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER4091", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER4092", "이미 사용 중인 닉네임입니다."),

    // 평점
    RATING_FORBIDDEN(HttpStatus.FORBIDDEN, "RATING4031", "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다."),
    RATING_NOT_FOUND(HttpStatus.NOT_FOUND, "RATING4041", "존재하지 않는 평점입니다."),
    DUPLICATE_RATING(HttpStatus.CONFLICT, "RATING4091", "이미 이 영화에 평점을 남기셨습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
