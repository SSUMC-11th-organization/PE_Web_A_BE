package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** 서비스 검증(두 번째 문): 존재 여부, 중복, 소유자 확인 */
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock RatingRepository ratingRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks RatingService ratingService;

    private Member member(long id) {
        Member member = Member.builder().email(id + "@umc.com").password("hash").nickname("user" + id).build();
        ReflectionTestUtils.setField(member, "memberId", id);
        return member;
    }

    private Rating ratingOf(Member owner) {
        return Rating.builder().member(owner).movieId(550L).score(4).comment("good").build();
    }

    @Test
    void 없는_회원이_평점등록하면_404() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.create(99L, new CreateRatingRequest(550L, 5, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    void 같은영화_중복등록하면_409_저장안함() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(1L)));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, null)))
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RATING);
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void 없는_평점_수정하면_404() {
        given(ratingRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.update(1L, 999L, new UpdateRatingRequest(3, null)))
                .extracting("errorCode").isEqualTo(ErrorCode.RATING_NOT_FOUND);
    }

    @Test
    void 남의_평점_수정하면_403_값안바뀜() {
        Rating rating = ratingOf(member(1L));
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        assertThatThrownBy(() -> ratingService.update(2L, 10L, new UpdateRatingRequest(1, "hack")))
                .extracting("errorCode").isEqualTo(ErrorCode.RATING_FORBIDDEN);
        assertThat(rating.getScore()).isEqualTo(4);
    }

    @Test
    void 남의_평점_삭제하면_403_삭제안함() {
        given(ratingRepository.findById(10L)).willReturn(Optional.of(ratingOf(member(1L))));

        assertThatThrownBy(() -> ratingService.delete(2L, 10L))
                .extracting("errorCode").isEqualTo(ErrorCode.RATING_FORBIDDEN);
        verify(ratingRepository, never()).delete(any());
    }

    @Test
    void 본인_평점은_수정된다() {
        Rating rating = ratingOf(member(1L));
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        ratingService.update(1L, 10L, new UpdateRatingRequest(2, "다시 보니 별로"));

        assertThat(rating.getScore()).isEqualTo(2);
        assertThat(rating.getComment()).isEqualTo("다시 보니 별로");
    }
}
