package com.umc.movie.controller;

import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.service.MemberService;
import com.umc.movie.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

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

    // ---------- 평점 ----------

    @Test
    void 평점등록_성공_201() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        given(ratingService.create(eq(1L), any()))
                .willReturn(new RatingResponse(10L, 550L, 5, "최고", now, now));

        mockMvc.perform(post("/members/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":5,"comment":"최고"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.ratingId").value(10));
    }

    @Test
    void 평점_별점범위오류_400() throws Exception {
        mockMvc.perform(post("/members/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":6}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("별점은 5점 이하여야 합니다."));
    }

    @Test
    void 평점_소수별점_400() throws Exception {
        mockMvc.perform(post("/members/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":3.5}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4002"));
    }

    @Test
    void 평점_후기500자초과_400() throws Exception {
        mockMvc.perform(patch("/members/1/ratings/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":3,"comment":"%s"}""".formatted("가".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("후기는 500자 이하로 입력해주세요."));
    }

    @Test
    void 평점_중복등록_409() throws Exception {
        given(ratingService.create(eq(1L), any())).willThrow(new BusinessException(ErrorCode.DUPLICATE_RATING));

        mockMvc.perform(post("/members/1/ratings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId":550,"score":5}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RATING4091"));
    }

    @Test
    void 평점수정_남의평점_403() throws Exception {
        given(ratingService.update(eq(2L), eq(1L), any())).willThrow(new BusinessException(ErrorCode.RATING_FORBIDDEN));

        mockMvc.perform(patch("/members/2/ratings/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":3}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RATING4031"));
    }

    @Test
    void 평점삭제_없는평점_404() throws Exception {
        willThrow(new BusinessException(ErrorCode.RATING_NOT_FOUND)).given(ratingService).delete(1L, 999L);

        mockMvc.perform(delete("/members/1/ratings/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RATING4041"));
    }

    @Test
    void 평점삭제_성공_204_본문없음() throws Exception {
        mockMvc.perform(delete("/members/1/ratings/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void 경로값_타입오류_400() throws Exception {
        mockMvc.perform(get("/members/abc/ratings"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4003"));
    }
}
