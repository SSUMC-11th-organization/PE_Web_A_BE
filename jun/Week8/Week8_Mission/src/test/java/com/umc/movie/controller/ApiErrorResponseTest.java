package com.umc.movie.controller;

import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.exception.ApiException;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.service.MemberService;
import com.umc.movie.service.RatingService;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** DB 없이 Controller + 검증 + 전역 예외 처리만 띄워 응답 형식과 상태 코드를 확인한다. */
@WebMvcTest(controllers = {MemberController.class, RatingController.class})
class ApiErrorResponseTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean MemberService memberService;
    @MockitoBean RatingService ratingService;
    @MockitoBean JwtUtil jwtUtil; // AuthInterceptor가 필요로 한다

    private static final String VALID_SIGN_UP = """
            {"email":"umc@example.com","password":"Umc!1234","nickname":"moviefan"}""";

    // ---------- 회원가입 ----------

    @Test
    void 회원가입_성공_201_password_없음() throws Exception {
        given(memberService.signUp(any())).willReturn(new MemberResponse(1L, "umc@example.com", "moviefan"));

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content(VALID_SIGN_UP))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.memberId").value(1))
                .andExpect(jsonPath("$.result.password").doesNotExist());
    }

    @Test
    void 회원가입_형식오류_400_서비스_호출안함() throws Exception {
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-email","password":"weak","nickname":"a"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON4001"))
                // 필드명 정렬 → email이 가장 앞
                .andExpect(jsonPath("$.message").value("올바른 이메일 형식이 아닙니다."))
                .andExpect(jsonPath("$.result.nickname").exists())
                .andExpect(jsonPath("$.result.password").exists());

        verify(memberService, never()).signUp(any());
    }

    @Test
    void 회원가입_이메일중복_409() throws Exception {
        given(memberService.signUp(any())).willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content(VALID_SIGN_UP))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER4091"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void 회원가입_닉네임중복_409_다른코드() throws Exception {
        given(memberService.signUp(any())).willThrow(new BusinessException(ErrorCode.DUPLICATE_NICKNAME));

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content(VALID_SIGN_UP))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER4092"));
    }

    @Test
    void DB_UNIQUE_위반도_409_같은형식() throws Exception {
        given(memberService.signUp(any())).willThrow(new DataIntegrityViolationException("Duplicate entry"));

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content(VALID_SIGN_UP))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON4091"));
    }

    @Test
    void 비밀번호_64자초과_400() throws Exception {
        String longPassword = "Aa1!" + "a".repeat(61);
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"umc@example.com","password":"%s","nickname":"moviefan"}""".formatted(longPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.password").value("비밀번호는 64자 이하로 입력해주세요."));
    }

    // ---------- 평점 (토큰 기반) ----------

    private static final String AUTH = "Authorization";
    private static final String BEARER = "Bearer valid-token";

    @BeforeEach
    void 토큰_검증_준비() {
        // "valid-token"은 1번 회원의 토큰이라고 가정한다
        given(jwtUtil.getMemberId("valid-token")).willReturn(1L);
    }

    @Test
    void 평점등록_성공_201_바디에_memberId없음() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        given(ratingService.create(eq(1L), any()))
                .willReturn(new RatingResponse(10L, 550L, 5, "최고", now, now));

        mockMvc.perform(post("/ratings").header(AUTH, BEARER).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":5,"comment":"최고"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.ratingId").value(10));

        // memberId는 요청이 아니라 토큰에서 나온 1번이 서비스로 전달된다
        verify(ratingService).create(eq(1L), any());
    }

    @Test
    void 토큰없이_평점등록_401_AUTH401_2() throws Exception {
        mockMvc.perform(post("/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":5}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));

        verify(ratingService, never()).create(any(), any());
    }

    @Test
    void 위조된_토큰_401_AUTH401_3() throws Exception {
        given(jwtUtil.getMemberId("fake-token"))
                .willThrow(new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_3", "유효하지 않은 토큰입니다."));

        mockMvc.perform(delete("/ratings/1").header(AUTH, "Bearer fake-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_3"));
    }

    @Test
    void 평점_별점범위오류_400() throws Exception {
        mockMvc.perform(post("/ratings").header(AUTH, BEARER).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":6}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("별점은 5점 이하여야 합니다."));
    }

    @Test
    void 평점_소수별점_400() throws Exception {
        mockMvc.perform(post("/ratings").header(AUTH, BEARER).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":3.5}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4002"));
    }

    @Test
    void 평점_후기500자초과_400() throws Exception {
        mockMvc.perform(patch("/ratings/1").header(AUTH, BEARER).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":3,"comment":"%s"}""".formatted("가".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("후기는 500자 이하로 입력해주세요."));
    }

    @Test
    void 평점_중복등록_409() throws Exception {
        given(ratingService.create(eq(1L), any()))
                .willThrow(new ApiException(HttpStatus.CONFLICT, "RATING409_1", "이미 이 영화에 평점을 남기셨습니다."));

        mockMvc.perform(post("/ratings").header(AUTH, BEARER).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":5}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RATING409_1"));
    }

    @Test
    void 평점수정_남의평점_403_토큰은_유효() throws Exception {
        given(ratingService.update(eq(1L), eq(5L), any()))
                .willThrow(new ApiException(HttpStatus.FORBIDDEN, "RATING403_1",
                        "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다."));

        mockMvc.perform(patch("/ratings/5").header(AUTH, BEARER).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":3}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RATING403_1"));
    }

    @Test
    void 평점삭제_없는평점_404() throws Exception {
        willThrow(new ApiException(HttpStatus.NOT_FOUND, "RATING404_1", "해당 평점을 찾을 수 없습니다."))
                .given(ratingService).delete(1L, 999L);

        mockMvc.perform(delete("/ratings/999").header(AUTH, BEARER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RATING404_1"));
    }

    @Test
    void 평점삭제_성공_200_result_null() throws Exception {
        mockMvc.perform(delete("/ratings/1").header(AUTH, BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").isEmpty());

        verify(ratingService).delete(1L, 1L);
    }

    @Test
    void 유저별_평점조회는_토큰없이도_가능() throws Exception {
        given(ratingService.getRatingsByMember(1L)).willReturn(List.of());

        mockMvc.perform(get("/members/1/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    void 경로값_타입오류_400() throws Exception {
        mockMvc.perform(get("/members/abc/ratings"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4003"));
    }
}
