package com.umc.movie.service;

import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** 저장 전에 규칙을 모두 검사해, 하나라도 걸리면 어느 테이블에도 저장하지 않는지 확인한다. */
@ExtendWith(MockitoExtension.class)
class MovieRecordServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock RatingRepository ratingRepository;
    @Mock FavoriteRepository favoriteRepository;
    @InjectMocks MovieRecordService movieRecordService;

    private Member member(long id) {
        Member member = Member.builder().email(id + "@umc.com").password("hash").nickname("user" + id).build();
        ReflectionTestUtils.setField(member, "memberId", id);
        return member;
    }

    @Test
    void 즐겨찾기_포함이면_평점과_즐겨찾기_모두_저장() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(1L)));
        given(ratingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(favoriteRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MovieRecordResponse response = movieRecordService.create(1L,
                new CreateMovieRecordRequest(238L, 5, "좋아요", true));

        assertThat(response.rating().movieId()).isEqualTo(238L);
        assertThat(response.favorite()).isNotNull();
        assertThat(response.favorite().movieId()).isEqualTo(238L);
    }

    @Test
    void 즐겨찾기_미포함이면_평점만_저장() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(1L)));
        given(ratingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MovieRecordResponse response = movieRecordService.create(1L,
                new CreateMovieRecordRequest(238L, 4, null, false));

        assertThat(response.favorite()).isNull();
        verify(favoriteRepository, never()).existsByMember_MemberIdAndMovieId(anyLong(), anyLong());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void 이미_평점이_있으면_409_아무것도_저장안함() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(1L)));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 238L)).willReturn(true);

        assertThatThrownBy(() -> movieRecordService.create(1L,
                new CreateMovieRecordRequest(238L, 5, null, true)))
                .extracting("code").isEqualTo("RATING409_1");
        verify(ratingRepository, never()).save(any());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void 이미_즐겨찾기했으면_409_평점도_저장안함() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(1L)));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 238L)).willReturn(false);
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 238L)).willReturn(true);

        assertThatThrownBy(() -> movieRecordService.create(1L,
                new CreateMovieRecordRequest(238L, 5, null, true)))
                .extracting("code").isEqualTo("FAVORITE409_1");
        verify(ratingRepository, never()).save(any());
        verify(favoriteRepository, never()).save(any());
    }
}
