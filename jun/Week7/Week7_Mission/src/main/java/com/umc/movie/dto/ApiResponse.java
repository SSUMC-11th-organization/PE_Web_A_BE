package com.umc.movie.dto;

import com.umc.movie.exception.ErrorCode;

/**
 * 성공/실패 응답이 공통으로 사용하는 틀.
 * 화면은 isSuccess와 code만 보고 분기하고, message는 그대로 사용자에게 보여줄 수 있다.
 */
public record ApiResponse<T>(boolean isSuccess, String code, String message, T result) {

    public static <T> ApiResponse<T> created(String message, T result) {
        return new ApiResponse<>(true, "COMMON201", message, result);
    }

    public static <T> ApiResponse<T> ok(String message, T result) {
        return new ApiResponse<>(true, "COMMON200", message, result);
    }

    public static ApiResponse<Void> failure(ErrorCode errorCode) {
        return failure(errorCode, errorCode.getMessage(), null);
    }

    /** ErrorCode 없이 code 문자열을 직접 지정하는 실패 응답 (ApiException용) */
    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message, T result) {
        return new ApiResponse<>(false, errorCode.getCode(), message, result);
    }
}
