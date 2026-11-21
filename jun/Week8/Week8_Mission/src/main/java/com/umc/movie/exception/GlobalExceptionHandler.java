package com.umc.movie.exception;

import com.umc.movie.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 어디서 예외가 나도 ApiResponse 형식으로 바꿔 응답한다.
 * Controller마다 try-catch를 두지 않아도 된다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 첫 번째 문(DTO 검증) 실패 → 400. result에 필드별 오류를 함께 담는다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        // getFieldErrors()의 순서는 보장되지 않으므로 필드명으로 정렬해 항상 같은 결과를 낸다
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .toList();

        Map<String, String> errors = new LinkedHashMap<>();
        fieldErrors.forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        String message = fieldErrors.isEmpty()
                ? ErrorCode.INVALID_INPUT.getMessage()
                : fieldErrors.get(0).getDefaultMessage();

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.failure(ErrorCode.INVALID_INPUT, message, errors));
    }

    /** JSON 문법 오류, 타입 불일치(score: "abc", score: 3.5) → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.INVALID_REQUEST_BODY);
    }

    /** 경로 값 타입 불일치 (/members/abc/ratings) → 400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(ErrorCode.INVALID_PATH_VARIABLE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return toResponse(ErrorCode.NOT_FOUND_PATH);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED);
    }

    /** 두 번째 문(서비스 검증) 실패 → ErrorCode에 정해 둔 상태(403/404/409) */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return toResponse(e.getErrorCode());
    }

    /** 상태 코드와 code 문자열을 직접 담아 던진 예외 (인증/인가 등). 예외가 들고 온 값을 그대로 응답한다. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.failure(e.getCode(), e.getMessage()));
    }

    /** 세 번째 문(DB 제약) 위반 → 409. 사전 중복 체크를 동시에 통과한 요청이 여기로 온다. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("DB 제약 조건 위반: {}", e.getMostSpecificCause().getMessage());
        return toResponse(ErrorCode.DUPLICATE_DATA);
    }

    /** 예상하지 못한 예외도 같은 형식으로. 내부 정보는 로그에만 남긴다. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.failure(errorCode));
    }
}
