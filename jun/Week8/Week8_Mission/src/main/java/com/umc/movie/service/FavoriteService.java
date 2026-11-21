package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FavoriteResponse create(Long memberId, CreateFavoriteRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBER404_1", "해당 회원을 찾을 수 없습니다."));

        // 사전 검사는 우리가 정한 code/message로 알려주기 위한 것.
        // 동시에 두 요청이 이 검사를 통과해도 DB의 복합 UNIQUE가 마지막에 막는다(→ 409 COMMON4091).
        if (favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new ApiException(HttpStatus.CONFLICT, "FAVORITE409_1", "이미 이 영화를 즐겨찾기하셨습니다.");
        }

        Favorite favorite = Favorite.builder()
                .member(member)
                .movieId(request.movieId())
                .build();

        return FavoriteResponse.from(favoriteRepository.save(favorite));
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(Long memberId) {
        // 비어 있어도 오류가 아니다. 아직 담은 영화가 없다는 정상 상태 → 200 + []
        return favoriteRepository.findByMember_MemberIdOrderByFavoriteIdDesc(memberId).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FavoriteStatusResponse getStatus(Long memberId, Long movieId) {
        return new FavoriteStatusResponse(favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, movieId));
    }

    @Transactional
    public void delete(Long memberId, Long movieId) {
        // 내 즐겨찾기 중에서만 찾으므로 다른 회원의 즐겨찾기는 애초에 지울 수 없다.
        Favorite favorite = favoriteRepository.findByMember_MemberIdAndMovieId(memberId, movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FAVORITE404_1", "즐겨찾기한 영화를 찾을 수 없습니다."));

        favoriteRepository.delete(favorite);
    }
}
