package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock FavoriteRepository favoriteRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks FavoriteService favoriteService;

    private Member member(long id) {
        Member member = Member.builder().email(id + "@umc.com").password("hash").nickname("user" + id).build();
        ReflectionTestUtils.setField(member, "memberId", id);
        return member;
    }

    private Favorite favorite(Member owner, long favoriteId, long movieId) {
        Favorite favorite = Favorite.builder().member(owner).movieId(movieId).build();
        ReflectionTestUtils.setField(favorite, "favoriteId", favoriteId);
        return favorite;
    }

    @Test
    void 즐겨찾기_등록_성공() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(1L)));
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(false);
        given(favoriteRepository.save(any())).willAnswer(inv -> {
            Favorite saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "favoriteId", 7L);
            return saved;
        });

        FavoriteResponse response = favoriteService.create(1L, new CreateFavoriteRequest(550L));

        assertThat(response.favoriteId()).isEqualTo(7L);
        assertThat(response.movieId()).isEqualTo(550L);
    }

    @Test
    void 같은영화_중복등록하면_409_저장안함() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(1L)));
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        assertThatThrownBy(() -> favoriteService.create(1L, new CreateFavoriteRequest(550L)))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("FAVORITE409_1");
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void 없는_회원이_등록하면_404() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.create(99L, new CreateFavoriteRequest(550L)))
                .extracting("code").isEqualTo("MEMBER404_1");
    }

    @Test
    void 내_목록은_최신순_그대로_변환() {
        Member member = member(1L);
        given(favoriteRepository.findByMember_MemberIdOrderByFavoriteIdDesc(1L))
                .willReturn(List.of(favorite(member, 2L, 238L), favorite(member, 1L, 550L)));

        List<FavoriteResponse> result = favoriteService.getMyFavorites(1L);

        assertThat(result).extracting(FavoriteResponse::movieId).containsExactly(238L, 550L);
    }

    @Test
    void 즐겨찾기_안한_영화의_여부는_false() {
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(false);

        assertThat(favoriteService.getStatus(1L, 550L).isFavorite()).isFalse();
    }

    @Test
    void 없는_즐겨찾기_해제하면_404() {
        given(favoriteRepository.findByMember_MemberIdAndMovieId(1L, 550L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.delete(1L, 550L))
                .extracting("code").isEqualTo("FAVORITE404_1");
        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void 즐겨찾기_해제_성공() {
        Favorite favorite = favorite(member(1L), 1L, 550L);
        given(favoriteRepository.findByMember_MemberIdAndMovieId(1L, 550L)).willReturn(Optional.of(favorite));

        favoriteService.delete(1L, 550L);

        verify(favoriteRepository).delete(favorite);
    }
}
