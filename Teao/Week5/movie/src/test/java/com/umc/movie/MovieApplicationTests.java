package com.umc.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.umc.movie.entity.Rating;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.service.MemberService;
import com.umc.movie.service.RatingService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class MovieApplicationTests {

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final RatingRepository ratingRepository = mock(RatingRepository.class);

    @Test
    void nicknameAndEmailAvailability() {
        when(memberRepository.existsByNickname("수현")).thenReturn(true);
        when(memberRepository.existsByEmail("new@umc.com")).thenReturn(false);
        MemberService service = new MemberService(memberRepository);

        assertThat(service.checkNickname("수현").available()).isFalse();
        assertThat(service.checkEmail("new@umc.com").available()).isTrue();
    }

    @Test
    void memberRatings() {
        Rating first = mock(Rating.class);
        Rating second = mock(Rating.class);
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(1L))
                .thenReturn(List.of(first, second));
        RatingService service = new RatingService(ratingRepository, memberRepository);

        assertThat(service.getRatingsByMember(1L)).hasSize(2);
    }

    @Test
    void unknownMember() {
        when(memberRepository.existsById(999L)).thenReturn(false);
        RatingService service = new RatingService(ratingRepository, memberRepository);

        assertThatThrownBy(() -> service.getRatingsByMember(999L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
