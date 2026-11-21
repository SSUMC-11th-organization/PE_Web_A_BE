package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 모든 경로가 인터셉터로 보호된다. memberId는 URL/바디가 아니라 토큰에서 꺼낸 값을 쓴다. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> create(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody CreateFavoriteRequest request
    ) {
        FavoriteResponse response = favoriteService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("즐겨찾기를 등록했습니다.", response));
    }

    @GetMapping
    public ApiResponse<List<FavoriteResponse>> getMyFavorites(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId
    ) {
        return ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", favoriteService.getMyFavorites(memberId));
    }

    @GetMapping("/{movieId}")
    public ApiResponse<FavoriteStatusResponse> getStatus(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long movieId
    ) {
        return ApiResponse.ok("즐겨찾기 여부를 조회했습니다.", favoriteService.getStatus(memberId, movieId));
    }

    // 평점 삭제와 같이 공통 응답 형식을 지키려고 204 대신 200 + result null
    @DeleteMapping("/{movieId}")
    public ApiResponse<Void> delete(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long movieId
    ) {
        favoriteService.delete(memberId, movieId);
        return ApiResponse.ok("즐겨찾기를 해제했습니다.", null);
    }
}
